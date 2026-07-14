package com.example.fitness.data

/**
 * 动作的健身规范（每组次数、组数、休息时间、卡路里消耗）。
 *
 * 设计：不开机就为 1324 个动作定制，而是按 category + target + equipment
 * 给一个粗略的默认推荐，落在合理的初学者区间（8-15 reps，3-4 组，60-90s 休息）。
 * 用户可以在方案里 override（更高级版留到以后做）。
 */
data class WorkoutSpec(
    val sets: Int,
    val reps: Int,         // 每组次数（区间下限；上界 = (reps * 1.2).toInt()）
    val restSeconds: Int,  // 组间休息秒数
    val caloriesPerMinute: Double  // 估算 kcal/min
)

/**
 * 推断默认规范。基于 category（部位）+ target（主要肌肉）+ equipment。
 * 规则：
 * - 大肌群（腿/背/胸）：3-4 组, 8-12 次, 90s 休息, 7-9 kcal/min
 * - 小肌群（臂/肩/腹）：3 组, 12-15 次, 60s 休息, 5-6 kcal/min
 * - 复合（深蹲/硬拉/卧推）：4 组, 6-10 次, 120s 休息, 9-10 kcal/min
 * - 有氧/跳跃：3 组, 15-20 次, 45s 休息, 8-12 kcal/min
 * - 拉伸：2 组, 30s 持续, 30s 休息, 2-3 kcal/min
 */
object WorkoutPresets {

    fun defaultSpec(exercise: Exercise): WorkoutSpec {
        val isCompound = isCompoundLift(exercise)
        val isCardio = exercise.category == "cardio"
        val isStretch = exercise.muscleGroup.contains("abdominals") == false &&
            exercise.secondaryMuscles.isEmpty() &&
            (exercise.target.contains("abdominals") == false) &&
            listOf("stretch", "拉伸").any { exercise.name.contains(it, ignoreCase = true) }

        return when {
            isStretch -> WorkoutSpec(sets = 2, reps = 1, restSeconds = 30, caloriesPerMinute = 2.5)
            isCardio -> WorkoutSpec(sets = 3, reps = 20, restSeconds = 45, caloriesPerMinute = 10.0)
            isCompound -> WorkoutSpec(sets = 4, reps = 8, restSeconds = 120, caloriesPerMinute = 9.5)
            isLargeMuscleGroup(exercise.target) -> WorkoutSpec(sets = 3, reps = 10, restSeconds = 90, caloriesPerMinute = 8.0)
            else -> WorkoutSpec(sets = 3, reps = 12, restSeconds = 60, caloriesPerMinute = 5.5)
        }
    }

    private fun isCompoundLift(exercise: Exercise): Boolean {
        val n = exercise.name.lowercase()
        return listOf(
            "squat", "deadlift", "bench press", "overhead press",
            "深蹲", "硬拉", "卧推", "推举"
        ).any { n.contains(it.lowercase()) }
    }

    private fun isLargeMuscleGroup(target: String): Boolean {
        val t = target.lowercase()
        return t in setOf(
            "quads", "hamstrings", "glutes", "chest", "pectorals", "back", "lats", "upper back"
        ) || t.contains("股四") || t.contains("腘绳") || t.contains("臀") || t.contains("胸")
    }
}
