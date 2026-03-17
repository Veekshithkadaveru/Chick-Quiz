package app.krafted.chickquiz.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.krafted.chickquiz.ui.theme.CorrectGreen
import app.krafted.chickquiz.ui.theme.WrongRed
import app.krafted.chickquiz.viewmodel.MamaHenState
import kotlin.math.roundToInt

@Composable
fun MamaHen(
    state: MamaHenState,
    modifier: Modifier = Modifier,
    size: Int = 120
) {
    val context = LocalContext.current
    val drawableName = when (state) {
        MamaHenState.IDLE    -> "mama_hen_idle"
        MamaHenState.HAPPY   -> "mama_hen_happy"
        MamaHenState.SAD     -> "mama_hen_sad"
        MamaHenState.TIMEOUT -> "mama_hen_timeout"
    }
    val resId = remember(drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }
    if (resId == 0) return

    val infiniteTransition = rememberInfiniteTransition(label = "mamaHenIdle")
    val idleBounce by infiniteTransition.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "idleBounce"
    )

    var targetX by remember { mutableFloatStateOf(size.toFloat() * 2) }
    val animatedX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = when (state) {
            MamaHenState.HAPPY   -> spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
            MamaHenState.SAD     -> tween(400)
            MamaHenState.TIMEOUT -> spring(dampingRatio = 0.2f, stiffness = Spring.StiffnessHigh)
            MamaHenState.IDLE    -> spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "enterX"
    )

    LaunchedEffect(state) { targetX = 0f }

    val borderColor = when (state) {
        MamaHenState.HAPPY   -> CorrectGreen.copy(alpha = 0.7f)
        MamaHenState.SAD     -> WrongRed.copy(alpha = 0.7f)
        MamaHenState.TIMEOUT -> Color(0xFFFF9800).copy(alpha = 0.7f)
        MamaHenState.IDLE    -> Color.White.copy(alpha = 0.15f)
    }

    val width = size
    val height = (size * 1.4f).toInt()

    Box(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .offset {
                IntOffset(
                    x = animatedX.roundToInt(),
                    y = if (state == MamaHenState.IDLE) idleBounce.roundToInt() else 0
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Mama Hen ${state.name}",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = androidx.compose.ui.Alignment.TopCenter
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                    )
                )
        )
    }
}
