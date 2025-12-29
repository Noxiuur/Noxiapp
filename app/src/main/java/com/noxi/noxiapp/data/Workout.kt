package com.noxi.noxiapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tek bir egzersiz hareketi
 */
data class Exercise(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Float? = null, // Ağırlık (kg) - opsiyonel
    val isCardio: Boolean = false, // Kardiyo mu? (Dakika mi Tekrar mı)
    val actualWeight: String? = null,
    val actualReps: String? = null,
    val note: String? = null
)

/**
 * Bir günlük antrenman programı
 */
data class WorkoutDay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dayNumber: Int, // 1, 2, 3, vb.
    val dayName: String, // "1.Gün", "Göğüs Günü", vb.
    val iconRes: Int? = null, // Drawable resource ID (opsiyonel)
    val exercises: List<Exercise> = emptyList()
) {
    /**
     * Bu günde toplam kaç hareket var
     */
    val exerciseCount: Int
        get() = exercises.size
}

/**
 * Tek bir egzersiz logu (geçmiş kaydı)
 */
data class ExerciseLog(
    val exerciseId: String,
    val exerciseName: String = "", // Loglama anındaki ismi (Snapshot)
    val actualWeight: String? = null,
    val actualReps: String? = null,
    val note: String? = null
)

/**
 * Antrenman geçmişi (belirli bir tarih için)
 */
@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey val date: String, // YYYY-AA-GG formatında
    val completedDayId: String? = null, // O gün tamamlanan günün ID'si
    val plannedDayId: String? = null, // O gün PLANLANAN günün ID'si
    val logs: Map<String, ExerciseLog> = emptyMap(), // key: exerciseId
    val habitLogs: List<String> = emptyList() // Tamamlanan alışkanlıklar (örn: "8 bardak Su içildi")
)

/**
 * Tüm antrenman programı
 */
@Entity(tableName = "workout_programs")
data class WorkoutProgram(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String, // Program ismi
    val totalDays: Int, // 3-7 arası
    val days: List<WorkoutDay> = emptyList()
)
