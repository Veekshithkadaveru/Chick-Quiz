package app.krafted.chickquiz.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.data.db.AppDatabase
import app.krafted.chickquiz.ui.components.ConfettiCanvas
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import app.krafted.chickquiz.ui.theme.CorrectGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ResultScreen(
    category: String,
    score: Int,
    correctCount: Int,
    isPersonalBest: Boolean,
    starsEarned: Int,
    recordId: Int = 0,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val prefs = remember { context.getSharedPreferences("chick_quiz", Context.MODE_PRIVATE) }

    val bgResId = remember {
        context.resources.getIdentifier("bg_result", "drawable", context.packageName)
    }

    val categoryAccent = categoryStyles[category]?.accentColor ?: ChickYellow
    val categoryEmoji = categoryStyles[category]?.emoji ?: ""

    var playerName by remember { mutableStateOf(prefs.getString("player_name", "") ?: "") }

    var animatedScore by remember { mutableIntStateOf(0) }
    LaunchedEffect(score) {
        val steps = 60
        val stepDelay = 1500 / steps
        for (i in 1..steps) {
            delay(stepDelay.toLong())
            animatedScore = (score * i / steps)
        }
        animatedScore = score
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                text = categoryEmoji,
                fontSize = 48.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Quiz Complete!",
                color = ChickYellow,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = category.replace("_", " "),
                color = categoryAccent.copy(0.75f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = categoryAccent.copy(0.20f)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3472)),
                border = BorderStroke(1.5.dp, categoryAccent.copy(0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$animatedScore",
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "points",
                        color = CoopCream.copy(0.6f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$correctCount/10 correct",
                        color = CorrectGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { starIndex ->
                    var starVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(800L + starIndex * 300L)
                        starVisible = true
                    }
                    AnimatedVisibility(
                        visible = starVisible,
                        enter = scaleIn(
                            initialScale = 0f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
                        ) + fadeIn()
                    ) {
                        Icon(
                            imageVector = if (starIndex < starsEarned) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (starIndex < starsEarned) "Star ${starIndex + 1} earned" else "Star ${starIndex + 1} not earned",
                            tint = if (starIndex < starsEarned) ChickYellow else Color.White.copy(0.3f),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isPersonalBest) {
                var bestVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(1800L)
                    bestVisible = true
                }
                AnimatedVisibility(
                    visible = bestVisible,
                    enter = scaleIn(
                        initialScale = 0.5f,
                        animationSpec = spring(dampingRatio = 0.5f)
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ChickYellow.copy(0.25f), ChickYellow.copy(0.10f))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "NEW PERSONAL BEST!",
                            color = ChickYellow,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Name entry
            OutlinedTextField(
                value = playerName,
                onValueChange = { newName ->
                    val capped = newName.take(30)
                    playerName = capped
                    prefs.edit().putString("player_name", capped.trim()).apply()
                    if (recordId > 0) {
                        scope.launch {
                            db.scoreRecordDao().updatePlayerName(recordId, capped.trim())
                        }
                    }
                },
                label = { Text("Your Name", color = CoopCream.copy(0.5f)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = categoryAccent.copy(0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = CoopCream,
                    cursorColor = ChickYellow,
                    focusedBorderColor = categoryAccent.copy(0.6f),
                    unfocusedBorderColor = CoopCream.copy(0.15f),
                    focusedContainerColor = Color.White.copy(0.05f),
                    unfocusedContainerColor = Color.White.copy(0.03f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChickYellow,
                    contentColor = Color(0xFF1A1A2E)
                )
            ) {
                Icon(Icons.Filled.Replay, contentDescription = "Play Again", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, CoopCream.copy(0.3f))
            ) {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = CoopCream,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Home", color = CoopCream, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(38.dp))
        }

        ConfettiCanvas(
            isActive = isPersonalBest,
            modifier = Modifier.fillMaxSize()
        )
    }
}
