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
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.onSuccess
import pl.meleko.trainspot.domain.CommentRepository
import pl.meleko.trainspot.presentation.util.toUiText

class CommentsViewModel(private val repository: CommentRepository) : ViewModel() {
    private val _state = MutableStateFlow(CommentsState())
    val state = _state.asStateFlow()
    private val _events = Channel<CommentsEvent>()
    val events = _events.receiveAsFlow()
    private var requestJob: Job? = null
    private var generation = 0

    fun onAction(action: CommentsAction) {
        when (action) {
            is CommentsAction.Open -> open(action.spotId)
            CommentsAction.Close -> close()
            is CommentsAction.DraftChanged -> _state.update { it.copy(draft = action.text.take(2000)) }
            CommentsAction.Send -> send()
            CommentsAction.LoadMore -> loadMore()
            is CommentsAction.Like -> toggleLike(action.commentId)
            is CommentsAction.Delete -> delete(action.commentId)
            CommentsAction.Retry -> loadPage(0, replace = true)
        }
    }

    private fun open(spotId: String) {
        if (_state.value.spotId == spotId) return
        generation++
        requestJob?.cancel()
        _state.value = CommentsState(spotId = spotId, isLoading = true)
        loadPage(0, replace = true)
    }

    private fun close() {
        generation++
        requestJob?.cancel()
        _state.value = CommentsState()
    }

    private fun loadPage(page: Int, replace: Boolean) {
        val spotId = _state.value.spotId ?: return
        val requestGeneration = generation
        if (page == 0) _state.update { it.copy(isLoading = true, error = null) }
        else _state.update { it.copy(isLoadingMore = true) }
        requestJob = viewModelScope.launch {
            repository.getComments(spotId, page, PAGE_SIZE)
                .onSuccess { response ->
                    if (generation != requestGeneration || _state.value.spotId != spotId) return@onSuccess
                    _state.update { state ->
                        val mergedItems = if (replace) response.items else (state.comments + response.items).distinctBy { it.id }
                        val pendingComments = state.comments.filter { it.id.toString() in state.pendingLikeIds }.associateBy { it.id }
                        state.copy(
                        comments = mergedItems.map { fresh ->
                            val pending = pendingComments[fresh.id]
                            if (pending != null) fresh.copy(isLiked = pending.isLiked, likesCount = pending.likesCount) else fresh
                        },
                        total = response.total,
                        page = response.page,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null
                    ) }
                }
                .onFailure { error ->
                    if (generation != requestGeneration) return@onFailure
                    _state.update { it.copy(isLoading = false, isLoadingMore = false, error = error.toUiText()) }
                    _events.send(CommentsEvent.Error(error.toUiText()))
                }
        }
    }

    private fun loadMore() {
        val state = _state.value
        if (state.isLoading || state.isLoadingMore || state.comments.size >= state.total || state.spotId == null) return
        loadPage(state.page + 1, replace = false)
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
                    loadPage(0, replace = true)
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
                if (_state.value.spotId == id) loadPage(0, replace = true)
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
                    loadPage(0, replace = true)
                }
                .onFailure { error -> _events.send(CommentsEvent.Error(error.toUiText())) }
            _state.update { it.copy(pendingDeleteIds = it.pendingDeleteIds - commentId) }
        }
    }

    companion object { const val PAGE_SIZE = 20 }
}
