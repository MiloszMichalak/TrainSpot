package pl.meleko.trainspot.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pl.meleko.trainspot.presentation.comments.CommentsBottomSheet
import pl.meleko.trainspot.presentation.components.SpotCard
import pl.meleko.trainspot.presentation.util.ComponentViewModelScope
import pl.meleko.trainspot.presentation.util.ObserveAsEvents
import pl.meleko.trainspot.presentation.util.SnackbarController
import trainspot.app.shared.generated.resources.Res
import trainspot.app.shared.generated.resources.profile_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedRoot(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToLocationMap: (String, Double, Double) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: FeedViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is FeedEvent.Error -> {
                SnackbarController.onEvent(event.error.asText())
            }
        }
    }

    FeedScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToDetails = onNavigateToDetails,
        onNavigateToLocationMap = onNavigateToLocationMap,
        onNavigateToProfile = onNavigateToProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedState,
    onAction: (FeedAction) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToLocationMap: (String, Double, Double) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val commentsSaveableStateHolder = rememberSaveableStateHolder()
    var commentsSpotId by rememberSaveable { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            FeedHeader(state.currentUser?.avatarUrl, onNavigateToProfile)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Color(0xFFE8C547),
                contentColor = Color(0xFF0D0E0F),
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Add spot")
            }
        },
        containerColor = Color(0xFF0D0E0F)
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(FeedAction.OnRefresh) },
                modifier = Modifier.padding(padding)
            ) {
                val listState = rememberLazyListState()

                LaunchedEffect(state.spots) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .distinctUntilChanged()
                        .collect { lastVisibleIndex ->
                            if (state.spots.isNotEmpty() && lastVisibleIndex == state.spots.lastIndex + 1) {
                                onAction(FeedAction.OnLoadMore)
                            }
                        }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.spots, key = { it.id.toString() }) { spot ->
                        SpotCard(
                            spot = spot,
                            onLikeClick = { onAction(FeedAction.OnLikeClick(spot.id.toString())) },
                            onLocationClick = onNavigateToLocationMap,
                            onCommentClick = { commentsSpotId = spot.id.toString() },
                            isLikePending = spot.id.toString() in state.pendingLikeSpotIds,
                            modifier = Modifier.clickable { onNavigateToDetails(spot.id.toString()) }
                        )
                    }

                    if (state.spots.isNotEmpty() || (state.isLoading && !state.isRefreshing)) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoading && !state.isRefreshing) {
                                    CircularProgressIndicator(color = Color(0xFFE8C547))
                                }
                            }
                        }
                    }
                }
            }
            commentsSpotId?.let { spotId ->
                ComponentViewModelScope(
                    key = spotId,
                    saveableStateHolder = commentsSaveableStateHolder
                ) {
                    CommentsBottomSheet(
                        spotId = spotId,
                        currentUserId = state.currentUser?.id?.toString(),
                        scaffoldPadding = padding,
                        onCommentsCountChanged = { total ->
                            onAction(FeedAction.OnCommentsCountChanged(spotId, total))
                        },
                        onDismiss = { commentsSpotId = null }
                    )
                }
            }
        }
    }
}

@Composable
fun FeedHeader(avatarUrl: String?, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0E0F))
            .padding(20.dp, 12.dp, 20.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color(0xFFF0EDE8))) {
                append("Train")
            }
            withStyle(style = SpanStyle(color = Color(0xFFE8C547))) {
                append("Spot")
            }
        }
        Text(
            text = annotatedString,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2124))
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF9A9690)
                )
            }
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2124))
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = stringResource(Res.string.profile_title),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = stringResource(Res.string.profile_title), tint = Color(0xFFE8C547))
                }
            }
        }
    }
}
