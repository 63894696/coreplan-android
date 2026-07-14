package com.example.fitness.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 健身方案的持久化（SharedPreferences + JSON）。
 * 同时管理饮水量 + 饮水提醒开关。
 */
class PlanRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("fitness_plans", Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    // 先用默认方案初始化 flow，再做持久化（避免 init order 死锁）
    private val _plans = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val plans: StateFlow<List<WorkoutPlan>> = _plans.asStateFlow()

    private val _waterCount = MutableStateFlow(prefs.getInt(KEY_WATER_COUNT, 0))
    val waterCount: StateFlow<Int> = _waterCount.asStateFlow()

    private val _waterReminderOn = MutableStateFlow(prefs.getBoolean(KEY_WATER_REMINDER, false))
    val waterReminderOn: StateFlow<Boolean> = _waterReminderOn.asStateFlow()

    // 新版提醒字段：
    // - waterReminderStartDelayMinutes: 多少分钟后开始第一次提醒
    // - waterReminderIntervalMinutes: 每次间隔多少分钟
    // - waterReminderCount: 总共提醒几次（用户设定）
    // - waterReminderRemaining: 剩余几次（运行中递减）
    // - waterReminderStartEpochMillis: 第一次闹钟的 RTC 时间
    private val _waterReminderStartDelayMinutes = MutableStateFlow(prefs.getInt(KEY_WATER_START_DELAY, 30))
    val waterReminderStartDelayMinutes: StateFlow<Int> = _waterReminderStartDelayMinutes.asStateFlow()

    private val _waterReminderIntervalMinutes = MutableStateFlow(prefs.getInt(KEY_WATER_INTERVAL, 60))
    val waterReminderIntervalMinutes: StateFlow<Int> = _waterReminderIntervalMinutes.asStateFlow()

    private val _waterReminderCount = MutableStateFlow(prefs.getInt(KEY_WATER_COUNT_REMINDERS, 0))
    val waterReminderCount: StateFlow<Int> = _waterReminderCount.asStateFlow()

    private val _waterReminderRemaining = MutableStateFlow(prefs.getInt(KEY_WATER_REMAINING, 0))
    val waterReminderRemaining: StateFlow<Int> = _waterReminderRemaining.asStateFlow()

    private val _waterReminderStartEpochMillis = MutableStateFlow(prefs.getLong(KEY_WATER_START_AT, 0L))
    val waterReminderStartEpochMillis: StateFlow<Long> = _waterReminderStartEpochMillis.asStateFlow()

    /** Receiver 用：把分钟换算成 ms */
    val waterReminderIntervalMillis: Long get() = _waterReminderIntervalMinutes.value * 60_000L

    fun setWaterReminderStartDelay(minutes: Int) {
        _waterReminderStartDelayMinutes.value = minutes
        prefs.edit().putInt(KEY_WATER_START_DELAY, minutes).apply()
    }

    fun setWaterReminderCount(count: Int) {
        _waterReminderCount.value = count
        prefs.edit().putInt(KEY_WATER_COUNT_REMINDERS, count).apply()
    }

    fun setWaterReminderRemaining(count: Int) {
        _waterReminderRemaining.value = count
        prefs.edit().putInt(KEY_WATER_REMAINING, count).apply()
    }

    fun setWaterReminderStartEpochMillis(epochMillis: Long) {
        _waterReminderStartEpochMillis.value = epochMillis
        prefs.edit().putLong(KEY_WATER_START_AT, epochMillis).apply()
    }

    fun setWaterReminderActive(on: Boolean) {
        prefs.edit().putBoolean(KEY_WATER_REMINDER, on).apply()
        _waterReminderOn.value = on
    }

    /**
     * 触发一个新提醒周期：第一次在 startDelay 分钟后响，每次间隔 intervalMinutes 分钟，共 count 次。
     * 起点时间通过 AlarmManager 设置。
     */
    fun armReminder(startDelayMinutes: Int, intervalMinutes: Int, count: Int) {
        setWaterReminderStartDelay(startDelayMinutes)
        _waterReminderIntervalMinutes.value = intervalMinutes
        prefs.edit().putInt(KEY_WATER_INTERVAL, intervalMinutes).apply()
        setWaterReminderCount(count)
        setWaterReminderRemaining(count)
        val firstTrigger = System.currentTimeMillis() + startDelayMinutes * 60_000L
        setWaterReminderStartEpochMillis(firstTrigger)
        setWaterReminderActive(true)
    }

    /**
     * 关闭提醒（取消闹钟，重置状态）
     */
    fun disarmReminder() {
        setWaterReminderActive(false)
        setWaterReminderRemaining(0)
    }

    init {
        _plans.value = loadPlans()
    }

    fun addWater() {
        val newCount = _waterCount.value + 1
        prefs.edit().putInt(KEY_WATER_COUNT, newCount).apply()
        _waterCount.value = newCount
    }

    fun resetWater() {
        prefs.edit().putInt(KEY_WATER_COUNT, 0).apply()
        _waterCount.value = 0
    }

    /**
     * 用户每按一次"喝一口"加的量（ml）。
     * 250ml = 一杯标准玻璃杯。
     */
    val waterCupMl: Int = 250

    fun addPlan(plan: WorkoutPlan) {
        val current = _plans.value.toMutableList()
        current.add(plan)
        savePlans(current)
    }

    fun deletePlan(planId: String) {
        val current = _plans.value.filterNot { it.id == planId }
        savePlans(current)
    }

    fun updatePlan(plan: WorkoutPlan) {
        val current = _plans.value.map { if (it.id == plan.id) plan else it }
        savePlans(current)
    }

    fun getPlan(planId: String): WorkoutPlan? = _plans.value.find { it.id == planId }

    /**
     * 每日推荐饮水量（ml）。
     * 来源：US National Academies of Sciences, Engineering, and Medicine 2004 推荐
     *   成年男 3.7L / 成年女 2.7L（包括食物水，~20% 来自食物，故纯饮水约 2.0-2.5L）。
     * 取保守值 2500ml 作为通用目标；用户可调整。
     */
    val dailyWaterGoalMl: Int = 2500

    /** 当前实际饮水量（ml） */
    val currentWaterMl: Int get() = _waterCount.value * waterCupMl

    private fun savePlans(plans: List<WorkoutPlan>) {
        prefs.edit().putString(KEY_PLANS, gson.toJson(plans)).apply()
        _plans.value = plans
    }

    private fun loadPlans(): List<WorkoutPlan> {
        val json = prefs.getString(KEY_PLANS, null)
        if (json == null) {
            // 首次启动：写入默认方案以便后续持久化能正常工作
            val defaults = defaultPlans()
            savePlans(defaults)
            prefs.edit().commit()
            return defaults
        }
        return try {
            val type = object : TypeToken<List<WorkoutPlan>>() {}.type
            gson.fromJson(json, type) ?: defaultPlans()
        } catch (e: Exception) {
            e.printStackTrace()
            defaultPlans()
        }
    }

    /**
     * 内置三个方案（首次启动时返回）。从 exercises.json 里挑出代表性动作。
     */
    private fun defaultPlans(): List<WorkoutPlan> = listOf(
        WorkoutPlan(
            id = "builtin_light",
            name = "轻度活动日",
            description = "适合日常恢复或刚入门，每个动作轻量",
            type = PlanType.BUILT_IN_LIGHT,
            exercises = listOf(
                PlanExercise("0001", sets = 2, reps = 8, restSeconds = 45),
                PlanExercise("0002", sets = 2, reps = 10, restSeconds = 45),
                PlanExercise("1512", sets = 2, reps = 8, restSeconds = 45),
                PlanExercise("1368", sets = 2, reps = 12, restSeconds = 30)
            )
        ),
        WorkoutPlan(
            id = "builtin_moderate",
            name = "中度健身日",
            description = "标准训练，兼顾肌力和心肺",
            type = PlanType.BUILT_IN_MODERATE,
            exercises = listOf(
                PlanExercise("0001", sets = 3, reps = 12, restSeconds = 60),
                PlanExercise("0006", sets = 3, reps = 20, restSeconds = 45),
                PlanExercise("0007", sets = 3, reps = 12, restSeconds = 60),
                PlanExercise("0009", sets = 3, reps = 10, restSeconds = 60),
                PlanExercise("0003", sets = 3, reps = 20, restSeconds = 30)
            )
        ),
        WorkoutPlan(
            id = "builtin_intense",
            name = "强度减肥日",
            description = "高强度、偏有氧、消耗大",
            type = PlanType.BUILT_IN_INTENSE,
            exercises = listOf(
                PlanExercise("0003", sets = 4, reps = 30, restSeconds = 30),
                PlanExercise("0001", sets = 4, reps = 15, restSeconds = 45),
                PlanExercise("0007", sets = 4, reps = 12, restSeconds = 45),
                PlanExercise("0002", sets = 3, reps = 20, restSeconds = 30),
                PlanExercise("1512", sets = 2, reps = 8, restSeconds = 30)
            )
        )
    )

    companion object {
        @Volatile private var INSTANCE: PlanRepository? = null
        fun get(context: Context): PlanRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: PlanRepository(context.applicationContext).also { INSTANCE = it }
        }
        private const val KEY_PLANS = "plans_json"
        private const val KEY_WATER_COUNT = "water_count"
        private const val KEY_WATER_REMINDER = "water_reminder_on"
        private const val KEY_WATER_INTERVAL = "water_reminder_interval_min"
        private const val KEY_WATER_START_DELAY = "water_reminder_start_delay_min"
        private const val KEY_WATER_COUNT_REMINDERS = "water_reminder_count"
        private const val KEY_WATER_REMAINING = "water_reminder_remaining"
        private const val KEY_WATER_START_AT = "water_reminder_start_epoch_ms"
    }
}
