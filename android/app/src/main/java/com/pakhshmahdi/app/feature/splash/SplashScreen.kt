package com.pakhshmahdi.app.feature.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.ui.components.BrandLogo
import com.pakhshmahdi.app.ui.theme.PMTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val c = PMTheme.colors

    LaunchedEffect(Unit) {
        delay(1050)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        c.background,
                        c.primaryDark.copy(alpha = if (c.background == c.primaryDark) 1f else .06f),
                        c.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopStart)
                .offset(x = (-110).dp, y = (-85).dp),
            shape = CircleShape,
            color = c.accentGold.copy(alpha = .06f)
        ) {}

        Surface(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 90.dp),
            shape = CircleShape,
            color = c.primary.copy(alpha = .06f)
        ) {}

        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                shadowElevation = 10.dp
            ) {
                BrandLogo(
                    Modifier
                        .width(238.dp)
                        .height(188.dp)
                        .padding(22.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "پخش مهدی",
                style = MaterialTheme.typography.displaySmall,
                color = c.primary,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(9.dp))
            Text(
                "عمده‌فروش لوازم خانه و آشپزخانه",
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "در صالح‌آباد",
                style = MaterialTheme.typography.bodyLarge,
                color = c.textSecondary
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            repeat(3) { index ->
                Surface(
                    modifier = Modifier
                        .width(if (index == 0) 30.dp else 8.dp)
                        .height(4.dp),
                    shape = CircleShape,
                    color = if (index == 0) c.accentGold else c.border
                ) {}
            }
        }
    }
}
