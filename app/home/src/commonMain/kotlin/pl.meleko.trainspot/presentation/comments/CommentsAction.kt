package pl.meleko.trainspot.presentation.comments

sealed interface CommentsAction {
    data class Open(val spotId: String) : CommentsAction
    data object Close : CommentsAction
    data class DraftChanged(val text: String) : CommentsAction
    data object Send : CommentsAction
    data object LoadMore : CommentsAction
    data class Like(val commentId: String) : CommentsAction
    data class Delete(val commentId: String) : CommentsAction
    data object Retry : CommentsAction
}
