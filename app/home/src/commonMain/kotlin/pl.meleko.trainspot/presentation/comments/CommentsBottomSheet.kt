package pl.meleko.trainspot.presentation.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pl.meleko.trainspot.model.Comment
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import pl.meleko.trainspot.presentation.util.SnackbarController
import pl.meleko.trainspot.presentation.util.toRelativeTimeString
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.cancel
import trainspot.app.shared.generated.resources.comment_delete
import trainspot.app.shared.generated.resources.comment_delete_message
import trainspot.app.shared.generated.resources.comment_delete_title
import trainspot.app.shared.generated.resources.comment_like
import trainspot.app.shared.generated.resources.comment_placeholder
import trainspot.app.shared.generated.resources.comment_send
import trainspot.app.shared.generated.resources.comment_unknown_author
import trainspot.app.shared.generated.resources.comment_unlike
import trainspot.app.shared.generated.resources.comments_empty
import trainspot.app.shared.generated.resources.comments_load_more
import trainspot.app.shared.generated.resources.comments_loading
import trainspot.app.shared.generated.resources.comments_retry
import trainspot.app.shared.generated.resources.comments_title

@Composable
fun CommentsBottomSheet(
    spotId: String,
    currentUserId: String?,
    scaffoldPadding: PaddingValues,
    onCommentsCountChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<CommentsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnCommentsCountChanged by rememberUpdatedState(onCommentsCountChanged)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CommentsEvent.Error -> SnackbarController.onEvent(event.text.asText())
        }
    }

    LaunchedEffect(viewModel, spotId) {
        viewModel.onAction(CommentsAction.Open(spotId))
    }

    LaunchedEffect(state.spotId, state.total, state.isLoading, state.error) {
        if (state.spotId == spotId && !state.isLoading && state.error == null) {
            currentOnCommentsCountChanged(state.total)
        }
    }

    CommentsBottomSheetContent(
        state = state,
        currentUserId = currentUserId,
        scaffoldPadding = scaffoldPadding,
        onAction = viewModel::onAction,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheetContent(
    state: CommentsState,
    currentUserId: String?,
    scaffoldPadding: PaddingValues,
    onAction: (CommentsAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.comments) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (state.comments.isNotEmpty() && lastVisibleIndex == state.comments.lastIndex + 1) {
                    onAction(CommentsAction.LoadMore)
                }
            }
    }

    var deleteTarget by remember { mutableStateOf<String?>(null) }
    val firstAutomaticPartial = remember { mutableStateOf(true) }
    val confirmSheetValueChange = remember {
        { target: SheetValue ->
            if (target == SheetValue.PartiallyExpanded && firstAutomaticPartial.value) {
                firstAutomaticPartial.value = false
                false
            } else {
                true
            }
        }
    }
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded),
        confirmValueChange = confirmSheetValueChange
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.padding(top = scaffoldPadding.calculateTopPadding()),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight().imePadding().padding(horizontal = 20.dp)) {
            Text(
                text = "${stringResource(Res.string.comments_title)} (${state.total})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isLoading && state.comments.isEmpty()) {
                    item { Text(stringResource(Res.string.comments_loading), Modifier.padding(vertical = 24.dp)) }
                } else if (state.error != null && state.comments.isEmpty()) {
                    item {
                        TextButton(onClick = { onAction(CommentsAction.Retry) }) {
                            Text(stringResource(Res.string.comments_retry))
                        }
                    }
                } else if (state.comments.isEmpty() && state.error == null) {
                    item { Text(stringResource(Res.string.comments_empty), Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }

                items(state.comments, key = { it.id.toString() }) { comment ->
                    CommentRow(
                        comment = comment,
                        isOwner = comment.author.id.toString() == currentUserId,
                        isLikePending = comment.id.toString() in state.pendingLikeIds,
                        isDeletePending = comment.id.toString() in state.pendingDeleteIds,
                        onLike = { onAction(CommentsAction.Like(comment.id.toString())) },
                        onDelete = { deleteTarget = comment.id.toString() }
                    )
                }

                if (state.isLoadingMore) item { CircularProgressIndicator(Modifier.size(24.dp).padding(4.dp)) }

                if (state.comments.size < state.total && !state.isLoadingMore) {
                    item { TextButton(onClick = { onAction(CommentsAction.LoadMore) }) { Text(stringResource(Res.string.comments_load_more)) } }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = { onAction(CommentsAction.DraftChanged(it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(Res.string.comment_placeholder)) },
                    enabled = !state.isSending,
                    maxLines = 4
                )

                TextButton(
                    onClick = { onAction(CommentsAction.Send) },
                    enabled = state.draft.isNotBlank() && !state.isSending
                ) { Text(stringResource(Res.string.comment_send)) }
            }
        }
    }

    deleteTarget?.let { commentId ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(Res.string.comment_delete_title)) },
            text = { Text(stringResource(Res.string.comment_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(CommentsAction.Delete(commentId)); deleteTarget = null }) {
                    Text(stringResource(Res.string.comment_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    isOwner: Boolean,
    isLikePending: Boolean,
    isDeletePending: Boolean,
    onLike: () -> Unit,
    onDelete: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(comment.author.username ?: stringResource(Res.string.comment_unknown_author), style = MaterialTheme.typography.labelLarge)
                Text(comment.createdAt.toRelativeTimeString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(comment.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLike, enabled = !isLikePending) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(if (comment.isLiked) Res.string.comment_unlike else Res.string.comment_like),
                    tint = if (comment.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(comment.likesCount.toString(), style = MaterialTheme.typography.labelSmall)
            if (isOwner) IconButton(onClick = onDelete, enabled = !isDeletePending) {
                Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(Res.string.comment_delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
