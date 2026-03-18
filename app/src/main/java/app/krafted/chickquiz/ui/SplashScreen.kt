package app.krafted.chickquiz.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val context = LocalContext.current

    val bgResId = remember {
        context.resources.getIdentifier("bg_splash", "drawable", context.packageName)
    }
    val chickResId = remember {
        context.resources.getIdentifier("chick_breeds", "drawable", context.packageName)
    }

    // Animation state
    val eggScale = remember { Animatable(0f) }
    val eggAlpha = remember { Animatable(1f) }
    val wobbleAngle = remember { Animatable(0f) }
    var showCracks by remember { mutableStateOf(false) }
    var showChick by remember { mutableStateOf(false) }
    val chickScale = remember { Animatable(0f) }
    val chickAlpha = remember { Animatable(0f) }
    var showTitle by remember { mutableStateOf(false) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(30f) }
    var showSubtitle by remember { mutableStateOf(false) }
    val subtitleAlpha = remember { Animatable(0f) }

    // Gentle floating for the chick once revealed
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val chickFloat by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "chickFloat"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        // Phase 1: Egg appears
        eggScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 200f))
        delay(300)

        // Phase 2: Gentle wobble
        repeat(2) {
            wobbleAngle.animateTo(5f, tween(100))
            wobbleAngle.animateTo(-5f, tween(100))
        }
        wobbleAngle.animateTo(0f, tween(80))
        delay(200)

        // Phase 3: Cracks appear + stronger wobble
        showCracks = true
        repeat(3) { i ->
            val angle = 7f + i * 2f
            wobbleAngle.animateTo(angle, tween(70))
            wobbleAngle.animateTo(-angle, tween(70))
        }
        wobbleAngle.animateTo(0f, tween(50))
        delay(200)

        // Phase 4: Egg fades out, chick bursts in
        coroutineScope {
            launch { eggScale.animateTo(1.4f, tween(200, easing = FastOutSlowInEasing)) }
            launch { eggAlpha.animateTo(0f, tween(200)) }
        }

        showChick = true
        coroutineScope {
            launch { chickScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 200f)) }
            launch { chickAlpha.animateTo(1f, tween(250)) }
        }
        delay(300)

        // Phase 5: Title drops in
        showTitle = true
        coroutineScope {
            launch { titleAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
            launch { titleOffsetY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 150f)) }
        }
        delay(200)

        // Phase 6: Subtitle fades in
        showSubtitle = true
        subtitleAlpha.animateTo(1f, tween(400))

        delay(1200)
        onSplashComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
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
                            0.4f to Color(0xD9060E30),
                            0.7f to Color(0xE6030818),
                            1.0f to Color(0xFA020510)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.35f))

            // Egg / Chick area
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Egg — drawn entirely on Canvas for a proper oval shape
                if (eggAlpha.value > 0.01f) {
                    Canvas(
                        modifier = Modifier
                            .size(100.dp, 125.dp)
                            .graphicsLayer {
                                scaleX = eggScale.value
                                scaleY = eggScale.value
                                alpha = eggAlpha.value
                                rotationZ = wobbleAngle.value
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        // Egg body
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFF9C4),
                                    Color(0xFFFFE082),
                                    Color(0xFFF5C518)
                                ),
                                center = Offset(w * 0.5f, h * 0.45f),
                                radius = w * 0.6f
                            ),
                            size = size
                        )

                        // Highlight
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(0.4f),
                                    Color.Transparent
                                ),
                                center = Offset(w * 0.38f, h * 0.30f),
                                radius = w * 0.25f
                            ),
                            size = size
                        )

                        // Crack lines
                        if (showCracks) {
                            val crackColor = Color(0xFF8D6E63)
                            val sw = 2.dp.toPx()

                            drawPath(
                                Path().apply {
                                    moveTo(w * 0.52f, h * 0.15f)
                                    lineTo(w * 0.42f, h * 0.30f)
                                    lineTo(w * 0.54f, h * 0.42f)
                                    lineTo(w * 0.40f, h * 0.52f)
                                },
                                crackColor,
                                style = Stroke(sw, cap = StrokeCap.Round)
                            )
                            drawPath(
                                Path().apply {
                                    moveTo(w * 0.22f, h * 0.35f)
                                    lineTo(w * 0.34f, h * 0.48f)
                                    lineTo(w * 0.26f, h * 0.58f)
                                },
                                crackColor,
                                style = Stroke(sw, cap = StrokeCap.Round)
                            )
                            drawPath(
                                Path().apply {
                                    moveTo(w * 0.75f, h * 0.28f)
                                    lineTo(w * 0.64f, h * 0.42f)
                                },
                                crackColor,
                                style = Stroke(sw, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Burst particles
                if (eggAlpha.value < 0.5f && eggAlpha.value > 0.01f) {
                    SplashBurstParticles()
                }

                // Chick reveal
                if (showChick && chickResId != 0) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = chickFloat
                                scaleX = chickScale.value
                                scaleY = chickScale.value
                                alpha = chickAlpha.value
                            }
                            .size(120.dp)
                            .shadow(
                                elevation = (12 * glowPulse).dp,
                                shape = CircleShape,
                                ambientColor = ChickYellow,
                                spotColor = ChickYellow
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        ChickYellow.copy(glowPulse * 0.5f),
                                        ChickYellow.copy(glowPulse * 0.1f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = chickResId),
                            contentDescription = "Chick Quiz",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // Title
            if (showTitle) {
                Text(
                    text = "CHICK QUIZ",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ChickYellow,
                    letterSpacing = 3.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = titleAlpha.value
                            translationY = titleOffsetY.value
                        }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Subtitle
            if (showSubtitle) {
                Text(
                    text = "Chicken Expert",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CoopCream.copy(0.50f),
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .graphicsLayer { alpha = subtitleAlpha.value }
                )
            }

            Spacer(Modifier.weight(0.45f))

            // Bottom tagline
            if (showSubtitle) {
                Text(
                    text = "powered by krafted",
                    fontSize = 11.sp,
                    color = CoopCream.copy(0.18f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .graphicsLayer { alpha = subtitleAlpha.value }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SplashBurstParticles() {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.size(140.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val maxR = size.width * 0.42f

        for (i in 0 until 10) {
            val angle = (i * 36f) * (Math.PI / 180.0)
            val dist = maxR * progress.value
            val px = cx + (dist * kotlin.math.cos(angle)).toFloat()
            val py = cy + (dist * kotlin.math.sin(angle)).toFloat()
            val a = (1f - progress.value).coerceIn(0f, 1f)
            val r = 4.dp.toPx() * (1f - progress.value * 0.5f)

            drawCircle(
                color = if (i % 3 == 0) Color(0xFFF5C518)
                         else if (i % 3 == 1) Color(0xFFFFE082)
                         else Color(0xFFFFF9C4),
                radius = r,
                center = Offset(px, py),
                alpha = a
            )
        }
    }
}
