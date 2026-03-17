package app.krafted.chickquiz.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.data.db.AppDatabase
import app.krafted.chickquiz.data.db.ScoreRecord
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import app.krafted.chickquiz.viewmodel.Category
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val medalColors = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFFC0C0C0), // Silver
    Color(0xFFCD7F32)  // Bronze
)

@Composable
fun LeaderboardScreen(
    initialCategory: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    val categories = Category.entries
    val initialIndex = categories.indexOfFirst { it.name == initialCategory }.coerceAtLeast(0)
    var selectedTabIndex by remember { mutableIntStateOf(initialIndex) }
    val selectedCategory = categories[selectedTabIndex]
    val style = categoryStyles[selectedCategory.name]
    val accentColor = style?.accentColor ?: ChickYellow

    val scores by db.scoreRecordDao()
        .getTopScores(selectedCategory.name)
        .collectAsState(initial = emptyList())

    val bgResId = remember {
        context.resources.getIdentifier("bg_home", "drawable", context.packageName)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "leaderboard")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f, targetValue = 900f,
        animationSpec = infiniteRepeatable(
            tween(2800, delayMillis = 1800, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgResId != 0) {
            Image(
                painter = painterResource(id = bgResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xF0040C28),
                            0.35f to Color(0xD9060E30),
                            0.7f to Color(0xE6030818),
                            1.0f to Color(0xFA020510)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(54.dp))

            // Header — matches CollectionScreen style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = CoopCream)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LEADERBOARD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChickYellow,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Top Scores",
                        fontSize = 11.sp,
                        color = CoopCream.copy(alpha = 0.40f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(1.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, ChickYellow.copy(0.45f), Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(12.dp))

            // Category tabs — styled like HomeScreen section labels
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = CoopCream,
                edgePadding = 20.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = accentColor
                        )
                    }
                },
                divider = {}
            ) {
                categories.forEachIndexed { index, cat ->
                    val catStyle = categoryStyles[cat.name]
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = "${catStyle?.emoji ?: ""} ${cat.displayName}",
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == index)
                                    catStyle?.accentColor ?: CoopCream
                                else
                                    CoopCream.copy(0.5f)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (scores.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = CoopCream.copy(0.15f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "No scores yet",
                            color = CoopCream.copy(0.4f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Play ${selectedCategory.displayName} to\nclaim your spot!",
                            color = CoopCream.copy(0.22f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(scores, key = { _, record -> record.id }) { index, record ->
                        LeaderboardRow(
                            rank = index + 1,
                            record = record,
                            accentColor = accentColor,
                            index = index,
                            shimmerOffset = shimmerOffset
                        )
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    record: ScoreRecord,
    accentColor: Color,
    index: Int,
    shimmerOffset: Float
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }

    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val isMedal = rank <= 3
    val medalColor = if (isMedal) medalColors[rank - 1] else Color.White.copy(0.4f)
    val displayName = record.playerName.ifBlank { "Player" }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isMedal) 10.dp else 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isMedal) medalColor.copy(0.25f) else Color.Transparent,
                    spotColor = if (isMedal) medalColor.copy(0.15f) else Color.Transparent
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = if (isMedal) BorderStroke(1.5.dp, medalColor.copy(0.45f))
                     else BorderStroke(1.dp, Color.White.copy(0.06f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF252B6A),
                                    Color(0xFF1C2055)
                                )
                            )
                        )
                ) {
                    // Accent bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        medalColor.copy(if (isMedal) 0.95f else 0.20f),
                                        medalColor.copy(if (isMedal) 0.40f else 0.06f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(
                                    elevation = if (isMedal) 8.dp else 0.dp,
                                    shape = CircleShape,
                                    ambientColor = medalColor.copy(0.3f)
                                )
                                .clip(CircleShape)
                                .background(
                                    if (isMedal)
                                        Brush.radialGradient(
                                            listOf(
                                                medalColor.copy(0.55f),
                                                medalColor.copy(0.15f)
                                            )
                                        )
                                    else
                                        Brush.radialGradient(
                                            listOf(
                                                Color.White.copy(0.06f),
                                                Color.White.copy(0.02f)
                                            )
                                        )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMedal) {
                                Icon(
                                    Icons.Filled.EmojiEvents,
                                    contentDescription = "Rank $rank",
                                    tint = medalColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = "#$rank",
                                    color = CoopCream.copy(0.45f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = accentColor.copy(0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = displayName,
                                    color = if (isMedal) Color.White else CoopCream.copy(0.8f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${record.correctCount}/10",
                                    color = accentColor.copy(0.55f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    color = CoopCream.copy(0.22f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Score
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${record.score}",
                                color = if (isMedal) medalColor else Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "pts",
                                color = if (isMedal) medalColor.copy(0.6f) else CoopCream.copy(0.35f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Shimmer overlay for medal rows
                if (isMedal) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.04f),
                                        Color.Transparent
                                    ),
                                    start = Offset(shimmerOffset - 180f, 0f),
                                    end = Offset(shimmerOffset + 180f, 320f)
                                )
                            )
                    )
                }
            }
        }
    }
}
