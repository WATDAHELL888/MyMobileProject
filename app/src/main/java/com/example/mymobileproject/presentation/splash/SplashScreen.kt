package com.example.mymobileproject.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymobileproject.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Logo scale animation
    var logoVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )

    // Text slide-up
    var textVisible by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "textAlpha"
    )
    val textOffset by animateDpAsState(
        targetValue = if (textVisible) 0.dp else 30.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "textOffset"
    )

    // Shimmer on logo
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "shimmer"
    )

    // Loading dots
    val dotCount = 3
    val dotAlphas = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 200)
            ),
            label = "dot$index"
        )
    }

    // Trigger animations sequentially
    LaunchedEffect(Unit) {
        logoVisible = true
        delay(400)
        textVisible = true
        delay(1800)  // Total splash duration ~2.2s
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B1120),
                        Color(0xFF0F172A),
                        Color(0xFF0B1120)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 1000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background glow circles
        Box(
            Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-200).dp)
                .clip(CircleShape)
                .background(Emerald500.copy(alpha = 0.05f))
        )
        Box(
            Modifier
                .size(250.dp)
                .offset(x = 120.dp, y = 250.dp)
                .clip(CircleShape)
                .background(Blue500.copy(alpha = 0.05f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Icon
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Emerald500, Emerald400, Blue500),
                            start = Offset(0f, 0f),
                            end = Offset(200f, 200f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Shimmer overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0f)
                                ),
                                start = Offset(shimmer * 200f, 0f),
                                end = Offset(shimmer * 200f + 100f, 200f)
                            )
                        )
                )
                Text("💰", fontSize = 48.sp)
            }

            Spacer(Modifier.height(24.dp))

            // App name
            Text(
                text = "Smart Finance",
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "AI-Powered Money Manager",
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textOffset),
                style = MaterialTheme.typography.bodyLarge,
                color = Emerald400.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(48.dp))

            // Loading dots
            Row(
                modifier = Modifier.alpha(textAlpha),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until dotCount) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlphas[i].value)
                            .clip(CircleShape)
                            .background(Emerald400)
                    )
                }
            }
        }
    }
}
