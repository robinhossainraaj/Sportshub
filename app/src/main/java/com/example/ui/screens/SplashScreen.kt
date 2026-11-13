package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BgMain
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
        delay(2200) // Keep visible for ~2.5s total
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            // Drawn Z Logo matching Prototype Canvas
            Canvas(
                modifier = Modifier
                    .size(130.dp)
                    .testTag("splash_logo")
            ) {
                // outer triangular border representation
                val trianglePath = Path().apply {
                    moveTo(12f * density, 6f * density)
                    lineTo(118f * density, 52.5f * density)
                    lineTo(12f * density, 99f * density)
                    close()
                }
                
                drawPath(
                    path = trianglePath,
                    color = Color.White,
                    style = Stroke(
                        width = 8f * density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // draw internal Z lines
                // line 1: top bar of Z (38,34) to (74,34)
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(38f * density, 34f * density),
                    end = androidx.compose.ui.geometry.Offset(74f * density, 34f * density),
                    strokeWidth = 6f * density,
                    cap = StrokeCap.Round
                )
                
                // line 2: slash representing center of Z
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(74f * density, 34f * density),
                    end = androidx.compose.ui.geometry.Offset(38f * density, 70f * density),
                    strokeWidth = 6f * density,
                    cap = StrokeCap.Round
                )
                
                // line 3: bottom bar of Z
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(38f * density, 70f * density),
                    end = androidx.compose.ui.geometry.Offset(74f * density, 70f * density),
                    strokeWidth = 6f * density,
                    cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sports24",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.testTag("splash_title")
            )
        }
    }
}
