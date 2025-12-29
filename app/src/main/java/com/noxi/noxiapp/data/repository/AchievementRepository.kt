package com.noxi.noxiapp.data.repository

import android.content.Context
import com.noxi.noxiapp.data.UnlockedAchievement
import com.noxi.noxiapp.data.local.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import com.noxi.noxiapp.data.Habit

class AchievementRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val achievementDao = db.unlockedAchievementDao()
    private val workoutDao = db.workoutDao()

    suspend fun unlock(email: String, achievementId: String): Boolean {
        if (achievementDao.isAchievementUnlocked(email, achievementId)) return false
        
        achievementDao.insertUnlockedAchievement(
            UnlockedAchievement(
                achievementId = achievementId,
                userEmail = email,
                unlockedDate = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun checkTheyDontKnowMeSon(email: String): Boolean {
        if (achievementDao.isAchievementUnlocked(email, "they_dont_know_me_son")) return false

        val habits = workoutDao.getAllHabits().firstOrNull() ?: emptyList()
        val programs = workoutDao.getAllPrograms().firstOrNull() ?: emptyList()

        if (habits.isNotEmpty() && programs.isNotEmpty()) {
            return unlock(email, "they_dont_know_me_son")
        }
        return false
    }

    suspend fun checkATrain(email: String): Boolean {
        if (achievementDao.isAchievementUnlocked(email, "a_train")) return false

        // Logic: Sum of all "Yürüyüş Yap" (Walk) habit logs >= 100,000 steps
        // Note: We need to iterate all habits and their logs.
        val habits = workoutDao.getAllHabits().firstOrNull() ?: emptyList()
        var totalSteps = 0

        habits.filter { it.name == "Yürüyüş Yap" || it.name == "Walk" }.forEach { habit ->
             habit.dateLogs.values.forEach { 
                 totalSteps += it.toInt()
             }
        }

        if (totalSteps >= 100000) {
            return unlock(email, "a_train")
        }
        return false
    }

    suspend fun checkAquamen(email: String): Boolean {
        if (achievementDao.isAchievementUnlocked(email, "aquamen")) return false

        // Logic: 7 consecutive days of drinking water (or just 7 days in last 7 days)
        
        val habits = workoutDao.getAllHabits().firstOrNull() ?: emptyList()
        val waterHabits = habits.filter { it.name == "Su İç" || it.name == "Drink Water" }
        
        if (waterHabits.isEmpty()) return false

        // Simple check: iterate last 7 days
        for (i in 0..6) {
            val dateToCheck = java.time.LocalDate.now().minusDays(i.toLong())
             val dateStr = dateToCheck.toString()
             
             val hasEntry = waterHabits.any { it.dateLogs.containsKey(dateStr) && it.dateLogs[dateStr]!! > 0 }
             if (!hasEntry) return false
        }

        return unlock(email, "aquamen")
    }

    suspend fun checkZyZ(email: String): Boolean {
        if (achievementDao.isAchievementUnlocked(email, "zyz")) return false

        // Logic: Workout for a month. Difference between first and last workout >= 30 days.
        val history = workoutDao.getAllHistory().firstOrNull() ?: emptyList()
        if (history.isEmpty()) return false

        val sortedHistory = history.sortedBy { it.date } // date is String usually, need parsing if it is date string. Wait, WorkoutHistory date is String.
        // If date is "yyyy-MM-dd", comparing strings works for sorting if format is correct (ISO).
        // But subtraction (diff) requires parsing.
        
        val firstWorkoutDateStr = sortedHistory.first().date
        val lastWorkoutDateStr = sortedHistory.last().date
        
        // Parse dates
        try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val firstDate = java.time.LocalDate.parse(firstWorkoutDateStr, formatter)
            val lastDate = java.time.LocalDate.parse(lastWorkoutDateStr, formatter)
            
            val diff = java.time.temporal.ChronoUnit.DAYS.between(firstDate, lastDate)

            if (diff >= 30) {
                return unlock(email, "zyz")
            }
        } catch (e: Exception) {
            // Handle parsing error or ignore
        }
        
        return false
    }
}
