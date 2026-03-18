package app.krafted.chickquiz.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.data.db.AppDatabase
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import app.krafted.chickquiz.viewmodel.ChickInfo
import app.krafted.chickquiz.viewmodel.CollectionViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ChickStyle(val accentColor: Color, val label: String)

private val chickStyles = mapOf(
    1 to ChickStyle(Color(0xFFF5C518), "BREEDS"),
    2 to ChickStyle(Color(0xFFFFCC80), "EGGS"),
    3 to ChickStyle(Color(0xFF81C784), "FEED & CARE"),
    4 to ChickStyle(Color(0xFF4DB6AC), "HEALTH"),
    5 to ChickStyle(Color(0xFFCE93D8), "FUN FACTS"),
    6 to ChickStyle(Color(0xFFFFD700), "MASTER")
)

private val greyscaleFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToSaturation(0f) }
)

@Composable
fun CollectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val prefs = remember { context.getSharedPreferences("collection", Context.MODE_PRIVATE) }
    val viewModel = remember { CollectionViewModel(db.playerProgressDao(), prefs) }
    val uiState by viewModel.uiState.collectAsState()

    var hatchedIds by remember { mutableStateOf(emptySet<Int>()) }

    val bgResId = remember {
        context.resources.getIdentifier("bg_collection", "drawable", context.packageName)
    }

    val progressAnim by animateFloatAsState(
        targetValue = if (uiState.totalCount > 0)
            uiState.unlockedCount.toFloat() / uiState.totalCount else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "progress"
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(54.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "COLLECTION",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChickYellow,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Your Chick Family",
                        fontSize = 11.sp,
                        color = CoopCream.copy(alpha = 0.40f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(0.06f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxWidth(progressAnim)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ChickYellow, ChickYellow.copy(0.65f))
                                )
                            )
                    )
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "${uiState.unlockedCount} / ${uiState.totalCount}",
                    color = ChickYellow.copy(0.70f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            val rows = uiState.chicks.chunked(2)
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEachIndexed { colIndex, chick ->
                        val flatIndex = rowIndex * 2 + colIndex
                        val isNewlyUnlocked =
                            chick.id in uiState.newlyUnlockedIds && chick.id !in hatchedIds
                        ChickCard(
                            chick = chick,
                            index = flatIndex,
                            style = chickStyles[chick.id] ?: ChickStyle(ChickYellow, ""),
                            isNewlyUnlocked = isNewlyUnlocked,
                            onHatched = {
                                hatchedIds = hatchedIds + chick.id
                                viewModel.acknowledgeUnlock(chick.id)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChickCard(
    chick: ChickInfo,
    index: Int,
    style: ChickStyle,
    isNewlyUnlocked: Boolean,
    onHatched: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accent = style.accentColor
    val resId = remember(chick.drawableName) {
        context.resources.getIdentifier(chick.drawableName, "drawable", context.packageName)
    }

    var showHatch by remember { mutableStateOf(isNewlyUnlocked) }
    val chickRevealScale = remember { Animatable(if (isNewlyUnlocked) 0f else 1f) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    val inf = rememberInfiniteTransition(label = "c_$index")

    val bounce by inf.animateFloat(
        initialValue = 0f, targetValue = -5f,
        animationSpec = infiniteRepeatable(
            tween(1000 + index * 150, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "b_$index"
    )

    val wobble by inf.animateFloat(
        initialValue = -1.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(2400 + index * 200), RepeatMode.Reverse
        ), label = "w_$index"
    )

    val glowPulse by inf.animateFloat(
        initialValue = 0.25f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "g_$index"
    )

    val lockPulse by inf.animateFloat(
        initialValue = 0.45f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            tween(1600), RepeatMode.Reverse
        ), label = "l_$index"
    )

    val shimmerOffset by inf.animateFloat(
        initialValue = -400f, targetValue = 800f,
        animationSpec = infiniteRepeatable(
            tween(3000, delayMillis = 2000, easing = LinearEasing), RepeatMode.Restart
        ), label = "s_$index"
    )

    Box(modifier = modifier.aspectRatio(0.82f)) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxSize(),
            enter = scaleIn(
                initialScale = 0.65f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(tween(250))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = if (chick.isUnlocked) 12.dp else 4.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = accent.copy(if (chick.isUnlocked) 0.30f else 0.06f),
                        spotColor = accent.copy(if (chick.isUnlocked) 0.20f else 0.04f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF252B6A),
                                    Color(0xFF1C2055),
                                    Color(0xFF141840)
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        accent.copy(if (chick.isUnlocked) 0.9f else 0.20f),
                                        accent.copy(if (chick.isUnlocked) 0.5f else 0.08f)
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp, bottom = 52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showHatch) {
                            EggHatchAnimation(
                                accentColor = accent,
                                chickResId = resId,
                                chickName = chick.name,
                                chickRevealScale = chickRevealScale,
                                onComplete = {
                                    showHatch = false
                                    onHatched()
                                }
                            )
                        } else if (chick.isUnlocked) {
                            UnlockedChickImage(
                                resId = resId,
                                accent = accent,
                                glowPulse = glowPulse,
                                bounce = bounce,
                                chickName = chick.name
                            )
                        } else {
                            LockedChickImage(
                                resId = resId,
                                wobble = wobble,
                                lockPulse = lockPulse,
                                chickName = chick.name
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF0C0F30))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (chick.isUnlocked || showHatch) chick.name else "???",
                            color = if (chick.isUnlocked) Color.White else Color.White.copy(0.35f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        if (chick.isUnlocked) {
                            Text(
                                text = style.label,
                                color = accent.copy(0.70f),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp,
                                letterSpacing = 2.sp
                            )
                        } else {
                            Text(
                                text = chick.unlockDescription,
                                color = CoopCream.copy(0.28f),
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp,
                                maxLines = 2,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    if (chick.isUnlocked && !showHatch) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.045f),
                                            Color.Transparent
                                        ),
                                        start = Offset(shimmerOffset, 0f),
                                        end = Offset(shimmerOffset + 220f, 280f)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockedChickImage(
    resId: Int,
    accent: Color,
    glowPulse: Float,
    bounce: Float,
    chickName: String
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = bounce }
                .size(74.dp)
                .shadow(
                    elevation = (8 * glowPulse).dp,
                    shape = CircleShape,
                    ambientColor = accent,
                    spotColor = accent
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(glowPulse * 0.6f),
                            accent.copy(glowPulse * 0.15f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, accent.copy(0.50f), CircleShape)
                    .background(Color(0xFF1A1E4D).copy(0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = chickName,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("\uD83D\uDC23", fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
private fun LockedChickImage(
    resId: Int,
    wobble: Float,
    lockPulse: Float,
    chickName: String
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .graphicsLayer { rotationZ = wobble }
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.04f))
                .border(1.dp, Color.White.copy(0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "$chickName (locked)",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .graphicsLayer { alpha = 0.22f },
                    colorFilter = greyscaleFilter,
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    "\uD83E\uDD5A", fontSize = 28.sp,
                    modifier = Modifier.graphicsLayer { alpha = 0.3f }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0x55080C24))
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(0.5f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1E2258), Color(0xFF12153A))
                        )
                    )
                    .border(1.dp, Color.White.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(lockPulse),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun EggHatchAnimation(
    accentColor: Color,
    chickResId: Int,
    chickName: String,
    chickRevealScale: Animatable<Float, *>,
    onComplete: () -> Unit
) {
    val eggScale = remember { Animatable(0.6f) }
    val eggAlpha = remember { Animatable(1f) }
    val wobbleAngle = remember { Animatable(0f) }
    var showCracks by remember { mutableStateOf(false) }
    var showChick by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        eggScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f))
        delay(150)

        repeat(3) { i ->
            val angle = 6f + i * 3f
            wobbleAngle.animateTo(angle, tween(70))
            wobbleAngle.animateTo(-angle, tween(70))
        }
        wobbleAngle.animateTo(0f, tween(50))

        showCracks = true
        delay(250)

        repeat(4) { i ->
            val angle = 10f + i * 3f
            wobbleAngle.animateTo(angle, tween(45))
            wobbleAngle.animateTo(-angle, tween(45))
        }
        wobbleAngle.animateTo(0f, tween(30))

        coroutineScope {
            launch { eggScale.animateTo(1.6f, tween(220, easing = FastOutSlowInEasing)) }
            launch { eggAlpha.animateTo(0f, tween(220)) }
        }

        showChick = true
        chickRevealScale.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 180f))
        delay(300)
        onComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (eggAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        scaleX = eggScale.value
                        scaleY = eggScale.value * 1.2f
                        alpha = eggAlpha.value
                        rotationZ = wobbleAngle.value
                    }
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFF9C4),
                                Color(0xFFFFE082),
                                accentColor.copy(0.5f)
                            )
                        )
                    )
            ) {
                if (showCracks) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cc = Color(0xFF5D4037)
                        val sw = 2.dp.toPx()

                        drawPath(
                            Path().apply {
                                moveTo(w * 0.55f, 0f)
                                lineTo(w * 0.42f, h * 0.22f)
                                lineTo(w * 0.56f, h * 0.38f)
                                lineTo(w * 0.40f, h * 0.55f)
                            }, cc, style = Stroke(sw, cap = StrokeCap.Round)
                        )
                        drawPath(
                            Path().apply {
                                moveTo(w * 0.18f, h * 0.35f)
                                lineTo(w * 0.32f, h * 0.48f)
                                lineTo(w * 0.25f, h * 0.65f)
                            }, cc, style = Stroke(sw, cap = StrokeCap.Round)
                        )
                        drawPath(
                            Path().apply {
                                moveTo(w * 0.75f, h * 0.25f)
                                lineTo(w * 0.65f, h * 0.42f)
                            }, cc, style = Stroke(sw, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        if (showChick && chickResId != 0) {
            Image(
                painter = painterResource(id = chickResId),
                contentDescription = chickName,
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer {
                        scaleX = chickRevealScale.value
                        scaleY = chickRevealScale.value
                    }
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        if (showChick && chickResId == 0) {
            Text(
                "\uD83D\uDC23", fontSize = 32.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = chickRevealScale.value
                    scaleY = chickRevealScale.value
                }
            )
        }

        if (eggAlpha.value < 0.5f && eggAlpha.value > 0.01f) {
            BurstParticles(accentColor)
        }
    }
}

@Composable
private fun BurstParticles(accentColor: Color) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.size(90.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val maxR = size.width * 0.45f

        for (i in 0 until 8) {
            val angle = (i * 45f) * (Math.PI / 180.0)
            val dist = maxR * progress.value
            val px = cx + (dist * kotlin.math.cos(angle)).toFloat()
            val py = cy + (dist * kotlin.math.sin(angle)).toFloat()
            val a = (1f - progress.value).coerceIn(0f, 1f)
            val r = 3.5.dp.toPx() * (1f - progress.value * 0.6f)

            drawCircle(
                color = if (i % 2 == 0) accentColor else Color(0xFFFFE082),
                radius = r,
                center = Offset(px, py),
                alpha = a
            )
        }
    }
}
