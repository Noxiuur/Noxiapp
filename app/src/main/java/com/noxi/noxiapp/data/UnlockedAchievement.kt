package com.noxi.noxiapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_achievements")
data class UnlockedAchievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val achievementId: String,
    val userEmail: String,
    val unlockedDate: Long
)
