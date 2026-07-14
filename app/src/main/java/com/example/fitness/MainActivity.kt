package com.example.fitness

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fitness.i18n.LocaleManager
import com.example.fitness.ui.onboarding.showOnboarding
import com.example.fitness.ui.theme.FitnessTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // 关键：在 attachBaseContext 里 wrap，让 Compose + 资源都用正确的 locale
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 开发测试便利：启动时带 extra=skip_onboarding 可跳过引导直接进首页
        val skipOnboarding = intent?.getStringExtra("skip_onboarding") == "true"
        setContent {
            FitnessTheme {
                val onboardingDone = skipOnboarding || LocaleManager.isOnboardingDone(this)
                if (!onboardingDone) {
                    showOnboarding(
                        context = this,
                        onFinish = {
                            LocaleManager.setOnboardingDone(this)
                            // recreate 让语言选择立刻生效
                            recreate()
                        }
                    )
                } else {
                    FitnessApp()
                }
            }
        }
    }
}
