package com.example.fitness.data

import java.util.UUID

/**
 * 方案中的一个具体动作，包含该动作的组数/次数/休息时间。
 */
data class PlanExercise(
    val exerciseId: String,
    val sets: Int = 3,
    val reps: Int = 10,
    val restSeconds: Int = 60,
    val notes: String = ""
)

/**
 * 一个健身方案：内置或用户自建。
 *
 * type 决定是默认推荐（首次启动预置三个）还是用户自建。
 */
data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val type: PlanType,
    val exercises: List<PlanExercise>
)

enum class PlanType {
    /** 内置：轻度日 */
    BUILT_IN_LIGHT,
    /** 内置：中度日 */
    BUILT_IN_MODERATE,
    /** 内置：强度减肥日 */
    BUILT_IN_INTENSE,
    /** 用户自建 */
    CUSTOM
}
