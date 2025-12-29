package com.noxi.noxiapp.data

/**
 * Hazır hareket kategorileri
 */
enum class ExerciseCategory(val displayName: String) {
    CHEST("Göğüs"),
    BACK("Sırt"),
    LEGS("Bacak"),
    SHOULDERS("Omuz"),
    ARMS("Kol"),
    ABS("Karın"),
    CARDIO("Kardiyo")
}

/**
 * Hazır hareketler
 */
object PredefinedExercises {
    // Göğüs hareketleri
    val benchPress = Exercise(name = "Bench Press", sets = 4, reps = 10)
    val inclineBenchPress = Exercise(name = "Incline Bench Press", sets = 3, reps = 12)
    val dumbbellFlyes = Exercise(name = "Dumbbell Flyes", sets = 3, reps = 12)
    val pushUps = Exercise(name = "Şınav", sets = 3, reps = 15)
    
    // Sırt hareketleri
    val pullUps = Exercise(name = "Barfiks", sets = 4, reps = 8)
    val bentOverRow = Exercise(name = "Bent Over Row", sets = 4, reps = 10)
    val latPulldown = Exercise(name = "Lat Pulldown", sets = 3, reps = 12)
    val deadlift = Exercise(name = "Deadlift", sets = 4, reps = 8)
    
    // Bacak hareketleri
    val squat = Exercise(name = "Squat", sets = 4, reps = 10)
    val legPress = Exercise(name = "Leg Press", sets = 4, reps = 12)
    val lunges = Exercise(name = "Lunges", sets = 3, reps = 12)
    val legCurl = Exercise(name = "Leg Curl", sets = 3, reps = 12)
    
    // Omuz hareketleri
    val shoulderPress = Exercise(name = "Shoulder Press", sets = 4, reps = 10)
    val lateralRaise = Exercise(name = "Lateral Raise", sets = 3, reps = 12)
    val frontRaise = Exercise(name = "Front Raise", sets = 3, reps = 12)
    val rearDeltFlyes = Exercise(name = "Rear Delt Flyes", sets = 3, reps = 12)
    
    // Kol hareketleri
    val bicepCurl = Exercise(name = "Bicep Curl", sets = 3, reps = 12)
    val hammerCurl = Exercise(name = "Hammer Curl", sets = 3, reps = 12)
    val tricepDips = Exercise(name = "Tricep Dips", sets = 3, reps = 12)
    val tricepExtension = Exercise(name = "Tricep Extension", sets = 3, reps = 12)
    
    // Karın hareketleri
    val crunches = Exercise(name = "Mekik", sets = 3, reps = 20)
    val plank = Exercise(name = "Plank", sets = 3, reps = 60)
    val legRaises = Exercise(name = "Leg Raises", sets = 3, reps = 15)
    val russianTwist = Exercise(name = "Russian Twist", sets = 3, reps = 20)
    
    // Kardiyo
    val running = Exercise(name = "Koşu", sets = 1, reps = 30, isCardio = true)
    val cycling = Exercise(name = "Bisiklet", sets = 1, reps = 30, isCardio = true)
    val jumpingRope = Exercise(name = "İp Atlama", sets = 3, reps = 100, isCardio = true)
    
    /**
     * Kategoriye göre hareketleri getir
     */
    fun getExercisesByCategory(category: ExerciseCategory): List<Exercise> {
        return when (category) {
            ExerciseCategory.CHEST -> listOf(benchPress, inclineBenchPress, dumbbellFlyes, pushUps)
            ExerciseCategory.BACK -> listOf(pullUps, bentOverRow, latPulldown, deadlift)
            ExerciseCategory.LEGS -> listOf(squat, legPress, lunges, legCurl)
            ExerciseCategory.SHOULDERS -> listOf(shoulderPress, lateralRaise, frontRaise, rearDeltFlyes)
            ExerciseCategory.ARMS -> listOf(bicepCurl, hammerCurl, tricepDips, tricepExtension)
            ExerciseCategory.ABS -> listOf(crunches, plank, legRaises, russianTwist)
            ExerciseCategory.CARDIO -> listOf(running, cycling, jumpingRope)
        }
    }
}
