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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
        MamaHenState.IDLE -> "mama_hen_idle"
        MamaHenState.HAPPY -> "mama_hen_happy"
        MamaHenState.SAD -> "mama_hen_sad"
        MamaHenState.TIMEOUT -> "mama_hen_timeout"
    }
    val resId = remember(drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mamaHenIdle")

    val idleBounce by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleBounce"
    )

    var enterOffset by remember { mutableFloatStateOf(200f) }
    val animatedEnterOffset by animateFloatAsState(
        targetValue = enterOffset,
        animationSpec = when (state) {
            MamaHenState.HAPPY -> spring(
                dampingRatio = 0.4f,
                stiffness = Spring.StiffnessMedium
            )
            MamaHenState.SAD -> tween(400)
            MamaHenState.TIMEOUT -> spring(
                dampingRatio = 0.2f,
                stiffness = Spring.StiffnessHigh
            )
            MamaHenState.IDLE -> spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "enterOffset"
    )

    LaunchedEffect(state) {
        enterOffset = if (state == MamaHenState.IDLE) 0f else 200f
        enterOffset = 0f
    }

    val yOffset = when (state) {
        MamaHenState.IDLE -> idleBounce
        else -> 0f
    }

    val xOffset = when (state) {
        MamaHenState.IDLE -> 0f
        else -> animatedEnterOffset
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Mama Hen ${state.name}",
            modifier = modifier
                .size(size.dp)
                .offset {
                    IntOffset(
                        x = xOffset.roundToInt(),
                        y = yOffset.roundToInt()
                    )
                }
        )
    }
}
