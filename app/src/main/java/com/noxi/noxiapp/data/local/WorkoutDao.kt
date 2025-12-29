package com.noxi.noxiapp.data.local

import androidx.room.*
import com.noxi.noxiapp.data.Habit
import com.noxi.noxiapp.data.WorkoutHistory
import com.noxi.noxiapp.data.WorkoutProgram
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // --- Habits ---
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
    
    @Update
    suspend fun updateHabit(habit: Habit)
    
    // Batch update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    // --- Workout History ---
    @Query("SELECT * FROM workout_history WHERE date = :date")
    fun getHistoryFlow(date: String): Flow<WorkoutHistory?>
    
    @Query("SELECT * FROM workout_history WHERE date = :date")
    suspend fun getHistory(date: String): WorkoutHistory?

    @Query("SELECT * FROM workout_history")
    fun getAllHistory(): Flow<List<WorkoutHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WorkoutHistory)

    // --- Workout Programs ---
    @Query("SELECT * FROM workout_programs")
    fun getAllPrograms(): Flow<List<WorkoutProgram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: WorkoutProgram)

    @Delete
    suspend fun deleteProgram(program: WorkoutProgram)
}
