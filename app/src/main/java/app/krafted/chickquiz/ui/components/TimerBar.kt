package app.krafted.chickquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

private val TimerGreen = Color(0xFF4CAF50)
private val TimerYellow = Color(0xFFFFEB3B)
private val TimerRed = Color(0xFFE53935)

@Composable
fun TimerBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val color = when {
        progress > 0.5f -> lerp(TimerYellow, TimerGreen, (progress - 0.5f) * 2f)
        progress > 0.25f -> lerp(TimerRed, TimerYellow, (progress - 0.25f) * 4f)
        else -> TimerRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x33000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}
