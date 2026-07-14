package com.example.fitness.data

import com.google.gson.annotations.SerializedName

data class Exercise(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("body_part") val bodyPart: String,
    @SerializedName("equipment") val equipment: String,
    @SerializedName("instructions") val instructions: Instructions,
    @SerializedName("instruction_steps") val instructionSteps: InstructionSteps,
    @SerializedName("muscle_group") val muscleGroup: String,
    @SerializedName("secondary_muscles") val secondaryMuscles: List<String>,
    @SerializedName("target") val target: String,
    @SerializedName("media_id") val mediaId: String,
    @SerializedName("created_at") val createdAt: String
)

data class Instructions(
    @SerializedName("en") val en: String,
    @SerializedName("es") val es: String,
    @SerializedName("it") val it: String,
    @SerializedName("tr") val tr: String,
    @SerializedName("ru") val ru: String,
    @SerializedName("zh") val zh: String
)

data class InstructionSteps(
    @SerializedName("en") val en: List<String>,
    @SerializedName("es") val es: List<String>,
    @SerializedName("it") val it: List<String>,
    @SerializedName("tr") val tr: List<String>,
    @SerializedName("ru") val ru: List<String>,
    @SerializedName("zh") val zh: List<String>
)
