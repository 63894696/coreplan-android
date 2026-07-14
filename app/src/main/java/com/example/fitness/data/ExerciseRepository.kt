package com.example.fitness.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class ExerciseRepository(private val context: Context) {

    companion object {
        // 进程内单例缓存，保证多个 ExerciseRepository 实例（不同的 ExerciseViewModel）共享同一份翻译表
        @Volatile private var nameZhCache: Map<String, String>? = null
    }

    private var nameZh: Map<String, String> = emptyMap()

    private var _exercises: MutableList<Exercise> = mutableListOf()
    val exercises: List<Exercise> get() = _exercises

    private val gson = Gson()
    private var _isLoading = false
    val isLoading: Boolean get() = _isLoading

    init {
        // 在 init 里加载翻译表（gson 已经构造完）
        nameZh = nameZhCache ?: loadNameTranslations().also { nameZhCache = it }
    }

    suspend fun loadExercises() {
        if (_exercises.isNotEmpty()) return
        _isLoading = true
        try {
            _exercises = withContext(Dispatchers.IO) {
                loadExercisesFromAssets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading = false
        }
    }

    private fun loadExercisesFromAssets(): MutableList<Exercise> {
        val result = mutableListOf<Exercise>()
        try {
            context.assets.open("exercises.json").use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    // Read the entire file as a string and parse with Gson
                    // This is safe because we're on a background thread
                    val json = reader.readText()
                    val exercisesArray = JsonParser.parseString(json).asJsonArray

                    for (i in 0 until exercisesArray.size()) {
                        val obj = exercisesArray[i].asJsonObject

                        val id = obj["id"]?.asString ?: ""
                        val name = obj["name"]?.asString ?: "Unknown"
                        val category = obj["category"]?.asString ?: "Uncategorized"
                        val bodyPart = obj["body_part"]?.asString ?: "Unknown"
                        val equipment = obj["equipment"]?.asString ?: "Unknown"
                        val target = obj["target"]?.asString ?: "Unknown"
                        val mediaId = obj["media_id"]?.asString ?: ""
                        val muscleGroup = obj["muscle_group"]?.asString ?: "Unknown"
                        val createdAt = obj["created_at"]?.asString ?: ""

                        // Secondary muscles
                        val secondaryMuscles = obj["secondary_muscles"]
                            ?.asJsonArray?.map { it.asString }?.filter { it.isNotBlank() }
                            ?: emptyList()

                        // Instructions (multilingual dict)
                        val instructionsObj = obj["instructions"]?.asJsonObject ?: JsonObject()
                        val instructions = Instructions(
                            en = instructionsObj["en"]?.asString ?: "No instructions available",
                            es = instructionsObj["es"]?.asString ?: "No instructions available",
                            it = instructionsObj["it"]?.asString ?: "No instructions available",
                            tr = instructionsObj["tr"]?.asString ?: "No instructions available",
                            ru = instructionsObj["ru"]?.asString ?: "No instructions available",
                            zh = instructionsObj["zh"]?.asString ?: "No instructions available"
                        )

                        // Instruction steps (multilingual dict of arrays)
                        val stepsObj = obj["instruction_steps"]?.asJsonObject ?: JsonObject()
                        val stepsEn = stepsObj["en"]?.asJsonArray?.map { it.asString } ?: emptyList()
                        val stepsEs = stepsObj["es"]?.asJsonArray?.map { it.asString } ?: emptyList()
                        val stepsIt = stepsObj["it"]?.asJsonArray?.map { it.asString } ?: emptyList()
                        val stepsTr = stepsObj["tr"]?.asJsonArray?.map { it.asString } ?: emptyList()
                        val stepsRu = stepsObj["ru"]?.asJsonArray?.map { it.asString } ?: emptyList()
                        val stepsZh = stepsObj["zh"]?.asJsonArray?.map { it.asString } ?: emptyList()

                        val instructionSteps = InstructionSteps(
                            en = stepsEn.ifEmpty { listOf("Step 1", "Step 2", "Step 3") },
                            es = stepsEs.ifEmpty { listOf("Paso 1", "Paso 2", "Paso 3") },
                            it = stepsIt.ifEmpty { listOf("Passo 1", "Passo 2", "Passo 3") },
                            tr = stepsTr.ifEmpty { listOf("Adım 1", "Adım 2", "Adım 3") },
                            ru = stepsRu.ifEmpty { listOf("Шаг 1", "Шаг 2", "Шаг 3") },
                            zh = stepsZh.ifEmpty { listOf("步骤 1", "步骤 2", "步骤 3") }
                        )

                        val exercise = Exercise(
                            id = id,
                            name = name,
                            category = category,
                            bodyPart = bodyPart,
                            equipment = equipment,
                            instructions = instructions,
                            instructionSteps = instructionSteps,
                            muscleGroup = muscleGroup,
                            secondaryMuscles = secondaryMuscles,
                            target = target,
                            mediaId = mediaId,
                            createdAt = createdAt
                        )
                        result.add(exercise)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getAllExercises(): List<Exercise> = exercises

    fun getExercisesByCategory(category: String): List<Exercise> =
        exercises.filter { it.category.equals(category, ignoreCase = true) }

    fun getExercisesByEquipment(equipment: String): List<Exercise> =
        exercises.filter { it.equipment.equals(equipment, ignoreCase = true) }

    fun searchExercises(query: String): List<Exercise> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return exercises.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.bodyPart.contains(q, ignoreCase = true) ||
                it.target.contains(q, ignoreCase = true) ||
                it.equipment.contains(q, ignoreCase = true)
        }
    }

    fun getCategories(): Set<String> = exercises.mapTo(mutableSetOf()) { it.category.lowercase() }

    fun getEquipments(): Set<String> = exercises.mapTo(mutableSetOf()) { it.equipment }

    fun getPaginatedExercises(page: Int, pageSize: Int = 20): List<Exercise> {
        val fromIndex = page * pageSize
        if (fromIndex >= exercises.size) return emptyList()
        val toIndex = minOf(fromIndex + pageSize, exercises.size)
        return exercises.subList(fromIndex, toIndex)
    }

    fun getPaginatedByCategory(category: String, page: Int, pageSize: Int = 20): List<Exercise> {
        val filtered = getExercisesByCategory(category)
        val fromIndex = page * pageSize
        if (fromIndex >= filtered.size) return emptyList()
        val toIndex = minOf(fromIndex + pageSize, filtered.size)
        return filtered.subList(fromIndex, toIndex)
    }

    // ===== 收藏（持久化到 SharedPreferences）=====

    private val favoritePrefs by lazy {
        context.getSharedPreferences("fitness_favorites", Context.MODE_PRIVATE)
    }

    /** 当前收藏的 exerciseId set（只读 snapshot） */
    val favoriteIds: Set<String>
        get() = favoritePrefs.getStringSet("ids", emptySet())?.toSet() ?: emptySet()

    fun isFavorite(exerciseId: String): Boolean = favoriteIds.contains(exerciseId)

    /**
     * 切换收藏状态。返回新的状态（true=已收藏, false=取消）。
     * 改动立即写盘。
     */
    fun toggleFavorite(exerciseId: String): Boolean {
        val current = favoriteIds.toMutableSet()
        val newState = if (current.contains(exerciseId)) {
            current.remove(exerciseId)
            false
        } else {
            current.add(exerciseId)
            true
        }
        favoritePrefs.edit().putStringSet("ids", current).apply()
        return newState
    }

    fun setFavorite(exerciseId: String, favorite: Boolean) {
        val current = favoriteIds.toMutableSet()
        if (favorite) current.add(exerciseId) else current.remove(exerciseId)
        favoritePrefs.edit().putStringSet("ids", current).apply()
    }

    /**
     * 收藏的完整 Exercise 列表（按 id 顺序排）。UI 用来显示收藏页。
     */
    fun getFavorites(): List<Exercise> {
        val ids = favoriteIds
        return _exercises.filter { it.id in ids }
    }

    /**
     * 加载动作名中文翻译表（id -> 中文名）。文件不存在时返回空 map。
     */
    private fun loadNameTranslations(): Map<String, String> {
        return try {
            context.assets.open("exercise_names_zh.json").use { input ->
                InputStreamReader(input, Charsets.UTF_8).use { reader ->
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    gson.fromJson(reader, type) ?: emptyMap()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    /**
     * 返回动作应该显示给用户的名字：优先翻译表中的中文名，没有就回退英文原名。
     * 应用已经锁中文，所以默认显示中文。
     */
    fun displayNameOf(exercise: Exercise): String =
        nameZh[exercise.id]?.takeIf { it.isNotBlank() } ?: exercise.name
}
