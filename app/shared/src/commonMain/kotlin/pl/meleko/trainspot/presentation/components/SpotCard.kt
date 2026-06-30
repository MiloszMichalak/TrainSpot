package pl.meleko.trainspot.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import pl.meleko.trainspot.model.Spot

@Composable
fun SpotCard(
    spot: Spot,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorBgCard
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 12.dp, 16.dp, 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar approximation
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2A2D32), Color(0xFF3A3E45))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = spot.user.username.take(1).uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorAccent
                        )
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    Text(
                        text = spot.user.username,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorText1
                        )
                    )
                    Text(
                        text = "${spot.station ?: "Unknown"} · 14 min temu", // TODO: Format date
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = ColorText3
                        )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = ColorText3
                )
            }
            
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                AsyncImage(
                    model = spot.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Train Badge
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorBg.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = spot.trainRun.carrierCode + " " + (spot.trainRun.nationalNumber ?: ""),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorAccent
                        )
                    )
                    Text(
                        text = spot.model.model,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorText2,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
            
            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 10.dp, 16.dp, 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(ColorAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = spot.trainRun.stations.firstOrNull()?.stationId?.name ?: "Unknown",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ColorText2
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "→",
                        style = TextStyle(
                            color = ColorText3,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = spot.trainRun.stations.lastOrNull()?.stationId?.name ?: "Unknown",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ColorText2
                        )
                    )
                }
            }
            
            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onLikeClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (spot.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (spot.isLiked) ColorAccent else ColorText2
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = spot.likes.toString(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (spot.isLiked) ColorAccent else ColorText2
                        )
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ColorText2
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "12", // TODO: Implement comments
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ColorText2
                        )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "14:32", // TODO: Format time
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = ColorText3
                    )
                )
            }
        }
    }
}

// Re-using tokens from railspotter-ui.html
private val ColorBgCard = Color(0xFF141618)
private val ColorAccent = Color(0xFFE8C547)
private val ColorText1 = Color(0xFFF0EDE8)
private val ColorText2 = Color(0xFF9A9690)
private val ColorText3 = Color(0xFF5C5A56)
private val ColorBg = Color(0xFF0D0E0F)