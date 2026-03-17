package app.krafted.chickquiz.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chickquiz.data.db.AppDatabase
import app.krafted.chickquiz.data.questions.QuestionRepository
import app.krafted.chickquiz.ui.components.TimerBar
import app.krafted.chickquiz.ui.theme.ChickYellow
import app.krafted.chickquiz.ui.theme.CoopCream
import app.krafted.chickquiz.ui.theme.CorrectGreen
import app.krafted.chickquiz.ui.theme.WrongRed
import app.krafted.chickquiz.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

private val optionLabels = listOf("A", "B", "C", "D")

@Composable
fun QuizScreen(
    category: String,
    onAnswerRevealed: () -> Unit,
    onSessionComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        val db = AppDatabase.getInstance(context)
        QuizViewModel(
            QuestionRepository(context, db.questionDao()),
            db.playerProgressDao(),
            db.scoreRecordDao()
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val timerAnimatable = remember { Animatable(1f) }

    val categoryAccent = categoryStyles[category]?.accentColor ?: ChickYellow

    LaunchedEffect(Unit) { viewModel.startSession(category) }

    LaunchedEffect(uiState.timerKey) {
        if (uiState.questions.isEmpty()) return@LaunchedEffect
        val timeLimit = viewModel.currentTimeLimit()
        timerAnimatable.snapTo(1f)
        val result = timerAnimatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = timeLimit * 1000, easing = LinearEasing)
        )
        if (result.endReason == AnimationEndReason.Finished) viewModel.onTimerExpired()
    }

    LaunchedEffect(uiState.selectedAnswer) {
        if (uiState.selectedAnswer != null) {
            timerAnimatable.stop()
            delay(1500)
            viewModel.onRevealAnswer()
        }
    }

    LaunchedEffect(uiState.isAnswerRevealed) {
        if (uiState.isAnswerRevealed) {
            if (uiState.currentIndex + 1 >= uiState.questions.size) viewModel.onNextQuestion()
            else onAnswerRevealed()
        }
    }

    LaunchedEffect(uiState.sessionComplete) {
        if (uiState.sessionComplete) onSessionComplete()
    }

    val bgResId = remember {
        context.resources.getIdentifier("bg_quiz", "drawable", context.packageName)
    }

    if (uiState.questions.isEmpty()) {
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
                            listOf(Color(0xF2000820), Color(0xCC000C2A), Color(0xF5000510))
                        )
                    )
            )
            Text(
                "Loading...",
                color = CoopCream.copy(0.45f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    val currentQuestion = uiState.questions[uiState.currentIndex]
    val pointsForQuestion = when (currentQuestion.difficulty) {
        "MEDIUM" -> 20; "HARD" -> 30; else -> 10
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(54.dp))

            // ── Header ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape, ambientColor = Color.White.copy(0.08f))
                        .clip(CircleShape)
                        .background(Color.White.copy(0.09f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CoopCream,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryAccent.copy(0.75f),
                        letterSpacing = 2.5.sp
                    )
                    AnimatedContent(
                        targetState = uiState.currentIndex + 1,
                        transitionSpec = {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        },
                        label = "qNum"
                    ) { n ->
                        Text(
                            text = "Question $n of ${uiState.questions.size}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Score chip with slide-up animation on change
                AnimatedContent(
                    targetState = uiState.score,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn(tween(200))) togetherWith
                            (fadeOut(tween(150)))
                    },
                    label = "score"
                ) { score ->
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = ChickYellow.copy(0.5f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ChickYellow.copy(0.22f), ChickYellow.copy(0.12f))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "$score pts",
                            color = ChickYellow,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Timer row ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimerBar(
                    progress = timerAnimatable.value,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.09f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (currentQuestion.difficulty) {
                            "HARD" -> "⚡"; "MEDIUM" -> "🔥"; else -> "✨"
                        },
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── Question card ────────────────────────────────
            AnimatedContent(
                targetState = uiState.currentIndex,
                transitionSpec = {
                    (scaleIn(initialScale = 0.96f, animationSpec = tween(280)) +
                     fadeIn(tween(280))) togetherWith
                    (fadeOut(tween(160)))
                },
                label = "questionCard"
            ) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = categoryAccent.copy(0.20f),
                            spotColor = categoryAccent.copy(0.12f)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3472)),
                    border = BorderStroke(1.5.dp, categoryAccent.copy(0.45f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        // Category-colored top strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            categoryAccent.copy(0.85f),
                                            categoryAccent.copy(0.40f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.questions.getOrNull(index)?.question ?: "",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 27.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Answer buttons ───────────────────────────────
            var showPoints by remember { mutableStateOf(false) }
            var earnedPoints by remember { mutableIntStateOf(0) }

            LaunchedEffect(uiState.selectedAnswer, uiState.currentIndex) {
                showPoints = false
                val selected = uiState.selectedAnswer ?: return@LaunchedEffect
                if (selected == currentQuestion.correctIndex) {
                    earnedPoints = pointsForQuestion
                    showPoints = true
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (row in 0..1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0..1) {
                                val idx = row * 2 + col
                                if (idx < currentQuestion.options.size) {
                                    AnswerButton(
                                        text = currentQuestion.options[idx],
                                        optionIndex = idx,
                                        questionIndex = uiState.currentIndex,
                                        selectedAnswer = uiState.selectedAnswer,
                                        correctIndex = currentQuestion.correctIndex,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.onAnswerSelected(idx) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showPoints) {
                    Box(modifier = Modifier.align(Alignment.TopCenter)) {
                        FloatingPoints(points = earnedPoints, onFinished = { showPoints = false })
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Progress segments ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 38.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(uiState.questions.size) { i ->
                    val isActive = i == uiState.currentIndex
                    val segScale by animateFloatAsState(
                        targetValue = if (isActive) 1f else 1f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        label = "seg_$i"
                    )
                    Box(
                        modifier = Modifier
                            .height(if (isActive) 5.dp else 4.dp)
                            .weight(if (isActive) 1.4f else 1f)
                            .scale(segScale)
                            .clip(CircleShape)
                            .background(
                                when {
                                    i < uiState.currentIndex  ->
                                        Brush.horizontalGradient(
                                            listOf(CorrectGreen.copy(0.9f), CorrectGreen.copy(0.6f))
                                        )
                                    i == uiState.currentIndex ->
                                        Brush.horizontalGradient(
                                            listOf(ChickYellow, ChickYellow.copy(0.7f))
                                        )
                                    else ->
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(0.12f),
                                                Color.White.copy(0.08f)
                                            )
                                        )
                                }
                            )
                    )
                }
            }
        }

    }
}

@Composable
private fun AnswerButton(
    text: String,
    optionIndex: Int,
    questionIndex: Int,
    selectedAnswer: Int?,
    correctIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = selectedAnswer == optionIndex
    val isCorrect = optionIndex == correctIndex
    val isRevealed = selectedAnswer != null
    val isWrongSelection = isSelected && !isCorrect

    // Staggered entrance per question
    var visible by remember(questionIndex) { mutableStateOf(false) }
    LaunchedEffect(questionIndex) {
        visible = false
        delay(optionIndex * 55L)
        visible = true
    }

    val scale = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isRevealed) {
        if (!isRevealed) { scale.snapTo(1f); shakeOffset.snapTo(0f); return@LaunchedEffect }
        if (isCorrect) {
            scale.animateTo(1.06f, spring(0.35f, Spring.StiffnessMedium))
            scale.animateTo(1f,    spring(0.5f,  Spring.StiffnessMedium))
        }
        if (isWrongSelection) {
            repeat(3) {
                shakeOffset.animateTo( 10f, spring(0.2f, Spring.StiffnessHigh))
                shakeOffset.animateTo(-10f, spring(0.2f, Spring.StiffnessHigh))
            }
            shakeOffset.animateTo(0f, spring(0.5f, Spring.StiffnessMedium))
        }
    }

    // Press feedback
    val btnInteraction = remember { MutableInteractionSource() }
    val isPressed by btnInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !isRevealed) 0.955f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
        label = "btnPress_$optionIndex"
    )

    val bgColor = when {
        isRevealed && isCorrect -> CorrectGreen.copy(0.88f)
        isWrongSelection        -> WrongRed.copy(0.88f)
        isRevealed              -> Color(0xFF1E2248).copy(0.70f)
        else                    -> Color(0xFF2E3472).copy(0.90f)
    }
    val borderColor = when {
        isRevealed && isCorrect -> CorrectGreen
        isWrongSelection        -> WrongRed
        else                    -> Color.White.copy(0.12f)
    }
    val labelBg = when {
        isRevealed && isCorrect -> Color.White.copy(0.28f)
        isWrongSelection        -> Color.White.copy(0.22f)
        isRevealed              -> Color.White.copy(0.05f)
        else                    -> ChickYellow.copy(0.16f)
    }
    val labelColor = when {
        isRevealed && isCorrect -> Color.White
        isWrongSelection        -> Color.White
        isRevealed              -> Color.White.copy(0.22f)
        else                    -> ChickYellow.copy(0.90f)
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
        ) + fadeIn(tween(180, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .offset { IntOffset(shakeOffset.value.dp.roundToPx(), 0) }
                .scale(scale.value * pressScale)
                .shadow(
                    elevation = if (isRevealed && isCorrect) 12.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = CorrectGreen,
                    spotColor = CorrectGreen.copy(0.5f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .background(
                    Brush.linearGradient(
                        listOf(borderColor.copy(0.20f), Color.Transparent)
                    )
                )
                .clickable(
                    enabled = selectedAnswer == null,
                    indication = null,
                    interactionSource = btnInteraction
                ) { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option label (A / B / C / D)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(labelBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optionLabels.getOrElse(optionIndex) { "" },
                        color = labelColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = text,
                    color = when {
                        isRevealed && (isCorrect || isWrongSelection) -> Color.White
                        isRevealed                                    -> Color.White.copy(0.28f)
                        else                                          -> CoopCream
                    },
                    fontSize = 13.sp,
                    fontWeight = if (isRevealed && (isCorrect || isWrongSelection))
                        FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    maxLines = 3,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp, top = 10.dp, bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingPoints(points: Int, onFinished: () -> Unit) {
    val alpha = remember { Animatable(1f) }
    val offsetY = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1.1f, tween(150, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f,   tween(100))
        alpha.animateTo(0f, tween(750))
        onFinished()
    }
    LaunchedEffect(Unit) {
        offsetY.animateTo(-90f, tween(900))
    }

    Text(
        text = "+$points",
        color = ChickYellow,
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .offset { IntOffset(0, offsetY.value.dp.roundToPx()) }
            .graphicsLayer { this.alpha = alpha.value; scaleX = scaleAnim.value; scaleY = scaleAnim.value }
    )
}
