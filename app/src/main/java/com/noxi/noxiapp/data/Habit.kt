package com.noxi.noxiapp.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Alışkanlık veri sınıfı (Room Entity)
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val colorCode: Int, // Color value class causes KSP issues, storing as Int
    val iconName: String, // ImageVector yerine isim tutuyoruz
    val category: HabitCategory,
    val isCompleted: Boolean = false,
    val goalValue: Float, // Hedef değer (örn: 8 bardak su, 10000 adım)
    val currentValue: Float = 0f, // Mevcut değer
    val unit: String = "", // Birim (bardak, adım, kg, vb.)
    val incrementValue: Float = 1f, // Artış miktarı (tek tıklamada)

    // Kilo verme için özel alanlar
    val startWeight: Float? = null,
    val targetWeight: Float? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val dailyCalorieGoal: Int? = null,
    
    // Logs: Date (yyyy-MM-dd) -> Value (e.g. 2000 steps, 8 cups)
    val dateLogs: Map<String, Float> = emptyMap()
) {
    /**
     * Renk (Computed property)
     */
    val color: Color
        get() = Color(colorCode)

    /**
     * İkonu isme göre döndürür
     */
    val icon: ImageVector
        get() = HabitIcons.getIcon(iconName)

    /**
     * Tamamlanma yüzdesi (0-100)
     */
    val completionPercentage: Float
        get() = if (goalValue > 0) {
            ((currentValue / goalValue) * 100f).coerceIn(0f, 100f)
        } else 0f
}

/**
 * İkon eşleşmeleri
 */
object HabitIcons {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "WaterDrop" -> Icons.Default.WaterDrop
            "DirectionsWalk" -> Icons.Default.DirectionsWalk
            "MonitorWeight" -> Icons.Default.MonitorWeight
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "SelfImprovement" -> Icons.Default.SelfImprovement
            "Bedtime" -> Icons.Default.Bedtime
            "Book" -> Icons.Default.Book
            "Restaurant" -> Icons.Default.Restaurant
            "NoDrinks" -> Icons.Default.NoDrinks
            "SmokeFree" -> Icons.Default.SmokeFree
            else -> Icons.Default.Star // Varsayılan
        }
    }
}

/**
 * Alışkanlık kategorileri
 */
enum class HabitCategory(val displayName: String) {
    HEALTH("Sağlık"),
    BODY("Vücut"),
    CUSTOM("Özel")
}

/**
 * Hazır alışkanlık şablonları
 */
object PredefinedHabits {
    // Sağlık kategorisi renkleri
    val WaterBlue = Color(0xFF64B5F6) // Açık mavi
    
    // Vücut kategorisi renkleri
    val WeightYellow = Color(0xFFFFD54F) // Sarı
    val WalkPurple = Color(0xFFBA68C8) // Mor
}
