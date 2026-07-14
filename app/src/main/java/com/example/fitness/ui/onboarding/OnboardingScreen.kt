package com.example.fitness.ui.onboarding

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitness.R
import com.example.fitness.i18n.LocaleManager

private const val EXERCISE_COUNT = 1324

/**
 * 首次启动欢迎页（单页）。看完成后直接进首页。
 * 简化：不做语言选择（语言锁死中文）、不做长问卷。
 */
@Composable
fun OnboardingScreen(
    context: Context,
    onFinish: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.onboarding_welcome_subtitle, EXERCISE_COUNT),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_get_started),
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * MainActivity 调用的入口。显示引导，完成时回调。
 */
@Composable
fun showOnboarding(
    context: Context,
    onFinish: () -> Unit
) {
    OnboardingScreen(context = context, onFinish = onFinish)
}