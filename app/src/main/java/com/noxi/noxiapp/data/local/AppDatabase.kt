package com.noxi.noxiapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.noxi.noxiapp.data.Habit
import com.noxi.noxiapp.data.WorkoutHistory
import com.noxi.noxiapp.data.WorkoutProgram
import com.noxi.noxiapp.data.UserProfile
import com.noxi.noxiapp.data.UnlockedAchievement
import com.noxi.noxiapp.data.local.UserProfileDao
import com.noxi.noxiapp.data.local.UnlockedAchievementDao

@Database(
    entities = [Habit::class, WorkoutHistory::class, WorkoutProgram::class, UserProfile::class, UnlockedAchievement::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun unlockedAchievementDao(): UnlockedAchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noxi_database"
                )
                .fallbackToDestructiveMigration() // Geliştirme aşamasında şema değişirse veriyi sil
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
