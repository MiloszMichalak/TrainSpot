package pl.meleko.trainspot.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.DataError
import pl.meleko.trainspot.core.Result
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.core.pagination.Paginator
import pl.meleko.trainspot.domain.CommentRepository
import pl.meleko.trainspot.presentation.util.toUiText

class CommentsViewModel(private val repository: CommentRepository) : ViewModel() {
    private val _state = MutableStateFlow(CommentsState())
    val state = _state.asStateFlow()
    private val _events = Channel<CommentsEvent>()
    val events = _events.receiveAsFlow()

    private var requestJob: Job? = null
    private var replacingComments = true

    private val paginator = Paginator(
        initialKey = 0,
        onLoadUpdated = { loading ->
            _state.update { if (replacingComments) it.copy(isLoading = loading) else it.copy(isLoadingMore = loading) }
        },
        onRequest = { page ->
            val spotId = _state.value.spotId
            if (spotId == null) Result.Error(DataError.Network.UNKNOWN)
            else repository.getComments(spotId, page, PAGE_SIZE)
        },
        getNextKey = { currentKey, _ -> currentKey + 1 },
        onError = { error ->
            replacingComments = false
            _state.update { it.copy(isLoading = false, isLoadingMore = false, error = error.toUiText()) }
            _events.send(CommentsEvent.Error(error.toUiText()))
        },
        onSuccess = { response, _ ->
            if (response.page == 0) replacingComments = false
            _state.update { state ->
                val pendingComments = state.comments.filter { it.id.toString() in state.pendingLikeIds }.associateBy { it.id }

                val loadedComments = response.items.map { fresh ->
                    val pending = pendingComments[fresh.id]
                    if (pending != null) fresh.copy(isLiked = pending.isLiked, likesCount = pending.likesCount) else fresh
                }

                state.copy(
                    comments = if (response.page == 0) loadedComments else state.comments + loadedComments,
                    total = response.total,
                    page = response.page,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null
                )
            }
        },
        endReached = { _, response -> response.page >= response.totalPages - 1 }
    )

    fun onAction(action: CommentsAction) {
        when (action) {
            is CommentsAction.Open -> open(action.spotId)
            CommentsAction.Close -> close()
            is CommentsAction.DraftChanged -> _state.update { it.copy(draft = action.text.take(2000)) }
            CommentsAction.Send -> send()
            CommentsAction.LoadMore -> loadMore()
            is CommentsAction.Like -> toggleLike(action.commentId)
            is CommentsAction.Delete -> delete(action.commentId)
            CommentsAction.Retry -> refreshComments()
        }
    }

    private fun open(spotId: String) {
        if (_state.value.spotId == spotId) return
        requestJob?.cancel()
        paginator.reset()
        replacingComments = true
        _state.value = CommentsState(spotId = spotId, isLoading = true)
        loadNextPage()
    }

    private fun close() {
        requestJob?.cancel()
        paginator.reset()
        replacingComments = true
        _state.value = CommentsState()
    }

    private fun loadMore() {
        val state = _state.value
        if (state.isLoading || state.isLoadingMore || state.spotId == null) return
        loadNextPage()
    }

    private fun loadNextPage() {
        if (_state.value.spotId == null) return
        requestJob = viewModelScope.launch { paginator.loadNextItems() }
    }

    private fun refreshComments() {
        if (_state.value.spotId == null) return
        requestJob?.cancel()
        paginator.reset()
        replacingComments = true
        _state.update { it.copy(error = null) }
        loadNextPage()
    }

    private fun send() {
        val state = _state.value
        val spotId = state.spotId ?: return
        val text = state.draft.trim()
        if (text.isEmpty() || state.isSending) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            repository.addComment(spotId, text)
                .onSuccess {
                    if (_state.value.spotId != spotId) return@onSuccess
                    _state.update { it.copy(isSending = false, draft = "") }
                    refreshComments()
                }
                .onFailure { error ->
                    if (_state.value.spotId != spotId) return@onFailure
                    _state.update { it.copy(isSending = false) }
                    _events.send(CommentsEvent.Error(error.toUiText()))
                }
        }
    }

    private fun toggleLike(commentId: String) {
        val id = _state.value.spotId ?: return
        if (commentId in _state.value.pendingLikeIds) return
        val comment = _state.value.comments.find { it.id.toString() == commentId } ?: return
        val wasLiked = comment.isLiked
        _state.update { state -> state.copy(
            comments = state.comments.map { if (it.id.toString() == commentId) it.copy(
                isLiked = !wasLiked,
                likesCount = (it.likesCount + if (wasLiked) -1 else 1).coerceAtLeast(0)
            ) else it },
            pendingLikeIds = state.pendingLikeIds + commentId
        ) }
        viewModelScope.launch {
            val result = if (wasLiked) repository.unlikeComment(commentId) else repository.likeComment(commentId)
            result.onFailure { error ->
                _state.update { state -> state.copy(comments = state.comments.map { if (it.id.toString() == commentId) it.copy(
                    isLiked = wasLiked,
                    likesCount = (it.likesCount + if (wasLiked) 1 else -1).coerceAtLeast(0)
                ) else it }) }
                _events.send(CommentsEvent.Error(error.toUiText()))
                    if (_state.value.spotId == id) refreshComments()
            }
            _state.update { it.copy(pendingLikeIds = it.pendingLikeIds - commentId) }
        }
    }

    private fun delete(commentId: String) {
        val spotId = _state.value.spotId ?: return
        if (commentId in _state.value.pendingDeleteIds) return
        _state.update { it.copy(pendingDeleteIds = it.pendingDeleteIds + commentId) }
        viewModelScope.launch {
            repository.deleteComment(spotId, commentId)
                .onSuccess {
                    if (_state.value.spotId != spotId) return@onSuccess
                    _state.update { it.copy(total = (it.total - 1).coerceAtLeast(0)) }
                    refreshComments()
                }
                .onFailure { error -> _events.send(CommentsEvent.Error(error.toUiText())) }
            _state.update { it.copy(pendingDeleteIds = it.pendingDeleteIds - commentId) }
        }
    }

    companion object { const val PAGE_SIZE = 20 }
}
