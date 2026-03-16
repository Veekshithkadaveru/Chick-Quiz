package app.krafted.chickquiz.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotation: Float
)

private val confettiColors = listOf(
    Color(0xFFF5C518),
    Color(0xFFE53935),
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFFFF4081)
)

@Composable
fun ConfettiCanvas(
    isActive: Boolean,
    particleCount: Int = 60,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "confetti"
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            val random = Random(System.currentTimeMillis())
            particles = List(particleCount) {
                Particle(
                    x = random.nextFloat(),
                    y = random.nextFloat() * -0.5f,
                    velocityX = (random.nextFloat() - 0.5f) * 0.3f,
                    velocityY = 0.3f + random.nextFloat() * 0.5f,
                    color = confettiColors[random.nextInt(confettiColors.size)],
                    size = 6f + random.nextFloat() * 8f,
                    rotation = random.nextFloat() * 360f
                )
            }
            progress = 0f
            progress = 1f
        } else {
            progress = 0f
            particles = emptyList()
        }
    }

    if (particles.isNotEmpty()) {
        Canvas(modifier = modifier) {
            canvasWidth = size.width
            val canvasHeight = size.height

            particles.forEach { particle ->
                val currentX = (particle.x + particle.velocityX * animatedProgress) * canvasWidth
                val currentY = (particle.y + particle.velocityY * animatedProgress) * canvasHeight
                val alpha = (1f - animatedProgress).coerceIn(0f, 1f)

                if (currentY in -50f..canvasHeight + 50f) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(currentX, currentY),
                        size = Size(particle.size, particle.size * 1.5f)
                    )
                }
            }
        }
    }
}
