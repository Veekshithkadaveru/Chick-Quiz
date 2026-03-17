package app.krafted.chickquiz.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.data.db.AppDatabase
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import app.krafted.chickquiz.ui.theme.GrassGreen
import app.krafted.chickquiz.viewmodel.Category
import app.krafted.chickquiz.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

internal data class CategoryStyle(val emoji: String, val accentColor: Color, val drawableName: String)

internal val categoryStyles = mapOf(
    "BREEDS"    to CategoryStyle("🐔", Color(0xFFF5C518), "chick_breeds"),
    "EGGS"      to CategoryStyle("🥚", Color(0xFFFFCC80), "chick_eggs"),
    "FEED_CARE" to CategoryStyle("🌾", Color(0xFF81C784), "chick_feed_care"),
    "HEALTH"    to CategoryStyle("💚", Color(0xFF4DB6AC), "chick_health"),
    "FUN_FACTS" to CategoryStyle("🎉", Color(0xFFCE93D8), "chick_fun_facts")
)

@Composable
fun HomeScreen(
    onCategoryClick: (String) -> Unit,
    onCollectionClick: () -> Unit,
    onLeaderboardClick: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val viewModel = remember { HomeViewModel(db.playerProgressDao()) }
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "home")

    val iconFloat by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "iconFloat"
    )
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f, targetValue = 900f,
        animationSpec = infiniteRepeatable(
            tween(2800, delayMillis = 1800, easing = LinearEasing), RepeatMode.Restart
        ),
        label = "shimmer"
    )
    val starShimmer by infiniteTransition.animateFloat(
        initialValue = 0.55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "starShimmer"
    )
    val dotGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "dotGlow"
    )

    val bgResId = remember {
        context.resources.getIdentifier("bg_home", "drawable", context.packageName)
    }

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
                            0.0f to Color(0xF2000820),
                            0.4f to Color(0xCC000C2A),
                            1.0f to Color(0xF5000510)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { translationY = iconFloat }
                        .size(56.dp)
                        .shadow(14.dp, CircleShape, ambientColor = ChickYellow, spotColor = ChickYellow)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(ChickYellow.copy(0.30f), ChickYellow.copy(0.08f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐣", fontSize = 30.sp)
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHICK QUIZ",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChickYellow,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Chicken Expert",
                        fontSize = 12.sp,
                        color = ChickYellow.copy(alpha = 0.50f),
                        letterSpacing = 1.4.sp
                    )
                }

            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, ChickYellow.copy(0.45f), Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORIES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CoopCream.copy(alpha = 0.38f),
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(0.10f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(Modifier.height(14.dp))

            Category.entries.forEachIndexed { index, cat ->
                val stars = uiState.starRatings[cat.name] ?: 0
                val style = categoryStyles[cat.name]!!

                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 80L)
                    visible = true
                }

                val cardInteraction = remember { MutableInteractionSource() }
                val isPressed by cardInteraction.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.965f else 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                    label = "press_${cat.name}"
                )

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                    ) + fadeIn(tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = style.accentColor.copy(0.25f),
                                    spotColor  = style.accentColor.copy(0.15f)
                                )
                                .clickable(
                                    interactionSource = cardInteraction,
                                    indication = null
                                ) { onCategoryClick(cat.name) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2E3472)
                            ),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = style.accentColor.copy(alpha = 0.55f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        style.accentColor.copy(0.95f),
                                                        style.accentColor.copy(0.35f)
                                                    )
                                                )
                                            )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp, vertical = 15.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val chickResId = remember(cat.name) {
                                            context.resources.getIdentifier(
                                                style.drawableName, "drawable", context.packageName
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(58.dp)
                                                .shadow(
                                                    elevation = 10.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    ambientColor = style.accentColor.copy(0.5f),
                                                    spotColor = style.accentColor.copy(0.3f)
                                                )
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(
                                                            style.accentColor.copy(0.55f),
                                                            style.accentColor.copy(0.18f)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (chickResId != 0) {
                                                Image(
                                                    painter = painterResource(id = chickResId),
                                                    contentDescription = cat.displayName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cat.displayName.uppercase(),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                letterSpacing = 0.8.sp,
                                                color = Color.White
                                            )
                                            Spacer(Modifier.height(7.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                repeat(3) { i ->
                                                    Icon(
                                                        imageVector = if (i < stars) Icons.Default.Star
                                                                      else Icons.Default.StarBorder,
                                                        contentDescription = null,
                                                        tint = when {
                                                            i < stars ->
                                                                style.accentColor.copy(starShimmer)
                                                            else ->
                                                                Color.White.copy(0.12f)
                                                        },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .shadow(
                                                    elevation = (6 * dotGlow).dp,
                                                    shape = CircleShape,
                                                    ambientColor = style.accentColor
                                                )
                                                .clip(CircleShape)
                                                .background(style.accentColor)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.055f),
                                                    Color.Transparent
                                                ),
                                                start = Offset(shimmerOffset - 180f, 0f),
                                                end   = Offset(shimmerOffset + 180f, 320f)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(34.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val collectionInteraction = remember { MutableInteractionSource() }
                val isCollectionPressed by collectionInteraction.collectIsPressedAsState()
                val collectionScale by animateFloatAsState(
                    targetValue = if (isCollectionPressed) 0.96f else 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                    label = "collectionScale"
                )

                val leaderInteraction = remember { MutableInteractionSource() }
                val isLeaderPressed by leaderInteraction.collectIsPressedAsState()
                val leaderScale by animateFloatAsState(
                    targetValue = if (isLeaderPressed) 0.96f else 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                    label = "leaderScale"
                )

                OutlinedButton(
                    onClick = onCollectionClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .graphicsLayer { scaleX = collectionScale; scaleY = collectionScale },
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = collectionInteraction,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CoopCream,
                        containerColor = Color.White.copy(0.05f)
                    ),
                    border = BorderStroke(1.dp, CoopCream.copy(0.22f))
                ) {
                    Icon(Icons.Default.Collections, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Collection", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { onLeaderboardClick("BREEDS") },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .graphicsLayer { scaleX = leaderScale; scaleY = leaderScale },
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = leaderInteraction,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GrassGreen,
                        containerColor = GrassGreen.copy(0.06f)
                    ),
                    border = BorderStroke(1.dp, GrassGreen.copy(0.38f))
                ) {
                    Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Leaderboard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(52.dp))
        }

    }
}
