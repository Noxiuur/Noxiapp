package com.noxi.noxiapp.data.local

import androidx.room.TypeConverter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noxi.noxiapp.data.ExerciseLog
import com.noxi.noxiapp.data.WorkoutDay
import java.util.Date

class Converters {
    private val gson = Gson()



    // --- List<String> Converters (HabitLogs) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return if (value == null) emptyList() else gson.fromJson(value, listType)
    }

    // --- Map<String, ExerciseLog> Converters (WorkoutHistory logs) ---
    @TypeConverter
    fun fromExerciseLogMap(value: Map<String, ExerciseLog>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExerciseLogMap(value: String?): Map<String, ExerciseLog> {
        val mapType = object : TypeToken<Map<String, ExerciseLog>>() {}.type
        return if (value == null) emptyMap() else gson.fromJson(value, mapType)
    }

    // --- List<WorkoutDay> Converters (WorkoutProgram) ---
    @TypeConverter
    fun fromWorkoutDayList(value: List<WorkoutDay>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWorkoutDayList(value: String?): List<WorkoutDay> {
        val listType = object : TypeToken<List<WorkoutDay>>() {}.type
        return if (value == null) emptyList() else gson.fromJson(value, listType)
    }

    // --- HabitCategory Converter ---
    @TypeConverter
    fun toHabitCategory(value: String?): com.noxi.noxiapp.data.HabitCategory {
        return try {
            if (value != null) com.noxi.noxiapp.data.HabitCategory.valueOf(value) else com.noxi.noxiapp.data.HabitCategory.CUSTOM
        } catch (e: Exception) {
            com.noxi.noxiapp.data.HabitCategory.CUSTOM
        }
    }

    // --- Map<String, Float> Converters (Habit Date Logs) ---
    @TypeConverter
    fun fromFloatMap(value: Map<String, Float>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFloatMap(value: String?): Map<String, Float> {
        val mapType = object : TypeToken<Map<String, Float>>() {}.type
        return if (value == null) emptyMap() else gson.fromJson(value, mapType)
    }
}
