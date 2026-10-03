package pl.meleko.trainspot.presentation.feed

sealed interface FeedAction {
    data object OnRefresh : FeedAction
    data object OnLoadMore : FeedAction
    data class OnLikeClick(val spotId: String) : FeedAction
    data class OnCommentsCountChanged(val spotId: String, val count: Int) : FeedAction
}
