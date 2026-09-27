package pl.meleko.trainspot.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.meleko.trainspot.core.onFailure
import pl.meleko.trainspot.core.pagination.Paginator
import pl.meleko.trainspot.domain.LikeRepository
import pl.meleko.trainspot.domain.SpotRepository
import pl.meleko.trainspot.presentation.util.toUiText

class FeedViewModel(
    private val spotRepository: SpotRepository,
    private val likeRepository: LikeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state = _state.asStateFlow()

    private val _events = Channel<FeedEvent>()
    val events = _events.receiveAsFlow()

    private val paginator = Paginator(
        initialKey = 0,
        onLoadUpdated = { isLoading ->
            _state.update { it.copy(isLoading = isLoading) }
        },
        onRequest = { page ->
            spotRepository.getSpots(page = page, limit = 20)
        },
        getNextKey = { currentKey, _ ->
            currentKey + 1
        },
        onError = { error ->
            _state.update { it.copy(isRefreshing = false) }
            _events.send(FeedEvent.Error(error.toUiText()))
        },
        onSuccess = { response, _ ->
            _state.update {
                it.copy(
                    spots = it.spots + response.items,
                    isRefreshing = false
                )
            }
        },
        endReached = { _, response ->
            response.page >= response.totalPages - 1
        }
    )

    init {
        loadSpots()
    }

    fun onAction(action: FeedAction) {
        when (action) {
            FeedAction.OnRefresh -> {
                _state.update { it.copy(isRefreshing = true, spots = emptyList()) }
                paginator.reset()
                loadSpots()
            }
            FeedAction.OnLoadMore -> {
                loadSpots()
            }
            is FeedAction.OnLikeClick -> {
                val spot = _state.value.spots.find { it.id.toString() == action.spotId } ?: return
                val wasLiked = spot.isLiked

                _state.update { state ->
                    state.copy(
                        spots = state.spots.map {
                            if (it.id.toString() == action.spotId) {
                                it.copy(
                                    isLiked = !wasLiked,
                                    likes = if (wasLiked) it.likes - 1 else it.likes + 1
                                )
                            } else it
                        }
                    )
                }

                viewModelScope.launch {
                    val result = if (wasLiked) {
                        likeRepository.unlikeSpot(action.spotId)
                    } else {
                        likeRepository.likeSpot(action.spotId)
                    }

                    result.onFailure { error ->
                        _state.update { state ->
                            state.copy(
                                spots = state.spots.map {
                                    if (it.id.toString() == action.spotId) {
                                        it.copy(
                                            isLiked = wasLiked,
                                            likes = if (wasLiked) it.likes + 1 else it.likes - 1
                                        )
                                    } else it
                                }
                            )
                        }
                        _events.send(FeedEvent.Error(error.toUiText()))
                    }
                }
            }
        }
    }

    private fun loadSpots() {
        viewModelScope.launch {
            paginator.loadNextItems()
        }
    }
}
