package com.example.fitness.ui.exercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitness.data.Exercise
import com.example.fitness.data.ExerciseRepository
import com.example.fitness.data.PlanExercise
import com.example.fitness.data.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * 整个 app 共享一个 viewmodel 实例（用 companion INSTANCE），
 * 避免不同 backStackEntry 各自创建导致 nameZh 重复加载或数据不一致。
 */
class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExerciseRepository(application.applicationContext)
    private val planRepo = PlanRepository.get(application)

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadExercises()
    }

    fun loadExercises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.loadExercises()
                _exercises.value = repository.getAllExercises()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getByCategory(category: String): List<Exercise> = repository.getExercisesByCategory(category)

    fun getByEquipment(equipment: String): List<Exercise> = repository.getExercisesByEquipment(equipment)

    fun getCategories(): Set<String> = repository.getCategories()

    fun getEquipments(): Set<String> = repository.getEquipments()

    /** 把动作显示为中文名（已翻译），否则回退英文。 */
    fun displayNameOf(exercise: Exercise): String = repository.displayNameOf(exercise)

    fun displayNameOf(exerciseId: String): String {
        val ex = _exercises.value.find { it.id == exerciseId }
        return ex?.let { repository.displayNameOf(it) } ?: exerciseId
    }

    /**
     * 返回练习的翻译中文名（如果 id 在数据中） + 类目
     * 给搜索/选择 UI 用：可以显示 "空中自行车 - 腰部" 这种格式
     */
    fun searchForSelection(query: String, limit: Int = 20): List<Exercise> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return _exercises.value.filter {
            it.name.contains(q, ignoreCase = true) ||
                displayNameOf(it).contains(q, ignoreCase = true) ||
                it.bodyPart.contains(q, ignoreCase = true) ||
                it.target.contains(q, ignoreCase = true) ||
                it.equipment.contains(q, ignoreCase = true)
        }.take(limit)
    }

    // ===== 方案（替代单一收藏）=====

    val customPlans: StateFlow<List<com.example.fitness.data.WorkoutPlan>> =
        planRepo.plans.map { it.filter { p -> p.type == com.example.fitness.data.PlanType.CUSTOM } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun allCustomPlans(): List<com.example.fitness.data.WorkoutPlan> = planRepo.plans.value
        .filter { it.type == com.example.fitness.data.PlanType.CUSTOM }

    fun addExerciseToPlan(planId: String, planExercise: PlanExercise) {
        val plan = planRepo.getPlan(planId) ?: return
        val updated = plan.copy(exercises = plan.exercises + planExercise)
        planRepo.updatePlan(updated)
    }

    fun removeExerciseFromPlan(planId: String, index: Int) {
        val plan = planRepo.getPlan(planId) ?: return
        if (index !in plan.exercises.indices) return
        val updated = plan.copy(exercises = plan.exercises.toMutableList().also { it.removeAt(index) })
        planRepo.updatePlan(updated)
    }

    fun updatePlanExercise(planId: String, index: Int, sets: Int, reps: Int, restSeconds: Int) {
        val plan = planRepo.getPlan(planId) ?: return
        if (index !in plan.exercises.indices) return
        val newList = plan.exercises.toMutableList()
        newList[index] = newList[index].copy(sets = sets, reps = reps, restSeconds = restSeconds)
        planRepo.updatePlan(plan.copy(exercises = newList))
    }

    fun createPlan(name: String, description: String = ""): String {
        val id = java.util.UUID.randomUUID().toString()
        planRepo.addPlan(
            com.example.fitness.data.WorkoutPlan(
                id = id,
                name = name,
                description = description,
                type = com.example.fitness.data.PlanType.CUSTOM,
                exercises = emptyList()
            )
        )
        return id
    }

    fun deletePlan(planId: String) {
        planRepo.deletePlan(planId)
    }

    fun updatePlan(plan: com.example.fitness.data.WorkoutPlan) {
        planRepo.updatePlan(plan)
    }

    companion object {
        // 单例，保证翻译表只加载一次且对所有页面可见
        @Volatile private var INSTANCE: ExerciseViewModel? = null

        @Synchronized
        fun get(application: Application): ExerciseViewModel {
            return INSTANCE ?: ExerciseViewModel(application).also { INSTANCE = it }
        }
    }
}
