package com.noxi.noxiapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.noxi.noxiapp.data.UnlockedAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockedAchievementDao {
    @Query("SELECT * FROM unlocked_achievements WHERE userEmail = :email")
    fun getUnlockedAchievements(email: String): Flow<List<UnlockedAchievement>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedAchievement(achievement: UnlockedAchievement)
    
    @Query("SELECT EXISTS(SELECT 1 FROM unlocked_achievements WHERE userEmail = :email AND achievementId = :achievementId)")
    suspend fun isAchievementUnlocked(email: String, achievementId: String): Boolean
}
