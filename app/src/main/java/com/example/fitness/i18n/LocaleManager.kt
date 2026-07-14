package com.example.fitness.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/**
 * 锁死中文（zh-CN）。设计决定（2026-07-13）：
 * - 个人使用场景不需要多语言切换
 * - 动作名会单独做中文翻译表，所有动作内容也用中文
 * - Settings 页面不再暴露语言选项
 *
 * 保留这个类的存在是为了未来要重新打开多语言时不用改架构。
 */
object LocaleManager {

    private const val PREFS_NAME = "fitness_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    /** 锁死的语言 code */
    const val LOCKED_LANGUAGE_CODE: String = "zh"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedLanguageCode(context: Context): String = LOCKED_LANGUAGE_CODE

    /** 保留接口但直接忽略参数。 */
    fun saveLanguageCode(context: Context, code: String?) {
        // 故意 no-op：语言已锁死
    }

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    /**
     * attachBaseContext 里调用。强制把 Configuration 的 locale 设为中文。
     */
    fun wrap(context: Context): Context {
        val locale = Locale.SIMPLIFIED_CHINESE
        val config = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        }
        return context.createConfigurationContext(config)
    }

    /** 当前生效语言 code（用于取练习数据时决定语言字段） */
    fun currentLanguageCode(context: Context): String = LOCKED_LANGUAGE_CODE
}

/**
 * 兼容层。早期多语言方案遗留的 enum，保留以免编译错。
 * 现在没有任何调用方使用，直接返回锁定值。
 */
enum class AppLanguage(
    val code: String,
    val displayKey: String,
    val nativeName: String
) {
    CHINESE("zh", "lang_zh", "中文");

    companion object {
        fun fromCode(code: String?): AppLanguage = CHINESE
        fun defaultForSystemLocale(): AppLanguage = CHINESE
    }
}
