package app.krafted.chickquiz.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import app.krafted.chickquiz.ui.theme.CorrectGreen
import app.krafted.chickquiz.ui.theme.WrongRed
import kotlinx.coroutines.delay

@Composable
fun AnswerRevealScreen(
    funFact: String,
    correctAnswer: String,
    wasCorrect: Boolean,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val bgResId = remember {
        context.resources.getIdentifier("bg_quiz", "drawable", context.packageName)
    }

    // Guard against double-navigation (tap + auto-advance race)
    var hasNavigated by remember { mutableStateOf(false) }
    val safeOnNext = remember(onNext) {
        {
            if (!hasNavigated) {
                hasNavigated = true
                onNext()
            }
        }
    }

    var displayedLength by remember { mutableIntStateOf(0) }
    LaunchedEffect(funFact) {
        displayedLength = 0
        for (i in funFact.indices) {
            delay(30)
            displayedLength = i + 1
        }
    }
    val displayedText = funFact.take(displayedLength)

    val autoAdvance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        autoAdvance.animateTo(1f, tween(4000, easing = LinearEasing))
        safeOnNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { safeOnNext() }
    ) {
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            val indicatorColor = if (wasCorrect) CorrectGreen else WrongRed
            val indicatorIcon = if (wasCorrect) Icons.Filled.Check else Icons.Filled.Close
            val indicatorText = if (wasCorrect) "Correct!" else "Not quite..."

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = indicatorIcon,
                        contentDescription = if (wasCorrect) "Correct" else "Incorrect",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = indicatorText,
                    color = indicatorColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3472)),
                border = BorderStroke(1.5.dp, ChickYellow.copy(0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Did you know?",
                        color = ChickYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = displayedText,
                        color = CoopCream,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.08f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Answer: $correctAnswer",
                            color = if (wasCorrect) CorrectGreen else ChickYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { autoAdvance.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ChickYellow,
                trackColor = Color.White.copy(0.12f)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Tap anywhere to continue",
                color = CoopCream.copy(0.45f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(38.dp))
        }
    }
}
