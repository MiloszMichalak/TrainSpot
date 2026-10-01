package pl.meleko.trainspot.presentation.comments

import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.presentation.util.UiText

data class CommentsState(
    val spotId: String? = null,
    val comments: List<Comment> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val draft: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val pendingLikeIds: Set<String> = emptySet(),
    val pendingDeleteIds: Set<String> = emptySet(),
    val error: UiText? = null
)
