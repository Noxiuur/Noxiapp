package com.noxi.noxiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxi.noxiapp.ui.screens.HabitsAndGoalsScreen
import com.noxi.noxiapp.ui.screens.WorkoutScreen
import com.noxi.noxiapp.ui.screens.NutritionScreen
import com.noxi.noxiapp.ui.screens.ProfileScreen
import com.noxi.noxiapp.ui.screens.AuthScreen
import com.noxi.noxiapp.data.repository.AuthRepository
import com.noxi.noxiapp.ui.theme.NoxiappTheme
import com.noxi.noxiapp.ui.theme.LocalStrings
import com.noxi.noxiapp.ui.theme.trStrings
import com.noxi.noxiapp.ui.theme.enStrings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bildirim kanalını oluştur
        com.noxi.noxiapp.utils.NotificationHelper.createNotificationChannel(this)
        
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var currentLanguage by remember { mutableStateOf("TR") }
            
            val currentStrings = if (currentLanguage == "TR") trStrings else enStrings
            
            // Auth State
            val authRepository = remember { AuthRepository() }
            var currentUser by remember { mutableStateOf(authRepository.currentUser) }

            NoxiappTheme(darkTheme = isDarkTheme) {
                // Bildirim izni iste
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        // İzin sonucu
                    }
                    
                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                
                CompositionLocalProvider(
                    androidx.compose.material3.LocalRippleConfiguration provides null,
                    LocalStrings provides currentStrings
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        com.noxi.noxiapp.ui.screens.SplashScreen(
                            onAnimationFinished = { showSplash = false }
                        )
                    } else if (currentUser == null) {
                        AuthScreen(
                            onLoginSuccess = {
                                currentUser = authRepository.currentUser
                            }
                        )
                    } else {
                        MainScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDarkTheme = it },
                            currentLanguage = currentLanguage,
                            onLanguageChange = { currentLanguage = it },
                            userEmail = currentUser?.email ?: "",
                            onLogout = {
                                authRepository.logout()
                                currentUser = null
                            }
                        )
                    }
                }
            }
        }
    }

@Composable
fun MainScreen(
    isDarkTheme: Boolean = true,
    onThemeChange: (Boolean) -> Unit = {},
    currentLanguage: String = "TR",
    onLanguageChange: (String) -> Unit = {},
    userEmail: String = "",
    onLogout: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val strings = com.noxi.noxiapp.ui.theme.LocalStrings.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var hideBottomBar by remember { mutableStateOf(false) }
    
    // Database & Scope
    val db = remember { com.noxi.noxiapp.data.local.AppDatabase.getDatabase(context) }
    val dao = remember { db.workoutDao() }
    val scope = rememberCoroutineScope()
    val achievementRepository = remember { com.noxi.noxiapp.data.repository.AchievementRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Geçmiş Verileri (From DB)
    val historyList by dao.getAllHistory().collectAsState(initial = emptyList())
    val history = remember(historyList) { historyList.associateBy { it.date } }

    // Alışkanlıklar (From DB)
    val habits by dao.getAllHabits().collectAsState(initial = emptyList())
    val savedPrograms by dao.getAllPrograms().collectAsState(initial = emptyList())

    // Achievement Checks
    if (userEmail.isNotBlank()) {
         LaunchedEffect(habits, savedPrograms) {
            if (achievementRepository.checkTheyDontKnowMeSon(userEmail)) {
                snackbarHostState.showSnackbar("Başarım Açıldı: They dont know me son! \uD83C\uDFC6")
            }
            if (achievementRepository.checkATrain(userEmail)) {
                snackbarHostState.showSnackbar("Başarım Açıldı: A-Train! \uD83C\uDFC6")
            }
            if (achievementRepository.checkAquamen(userEmail)) {
                snackbarHostState.showSnackbar("Başarım Açıldı: Aquamen! \uD83C\uDFC6")
            }
        }
        
        LaunchedEffect(historyList) {
            if (achievementRepository.checkZyZ(userEmail)) {
                snackbarHostState.showSnackbar("Başarım Açıldı: ZyZ! \uD83C\uDFC6")
            }
        }
    }

    // --- Daily Reset Logic ---
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("noxiapp_prefs", android.content.Context.MODE_PRIVATE)
        val lastCheckDate = prefs.getString("last_check_date", "")
        val todayDate = java.time.LocalDate.now().toString()

        if (lastCheckDate != todayDate) {
            // New day detected! Reset all habits
            scope.launch {
                val currentHabits = dao.getAllHabits().firstOrNull() ?: emptyList()
                val resetHabits = currentHabits.map { habit ->
                    habit.copy(
                        currentValue = 0f,
                        isCompleted = false
                    )
                }
                if (resetHabits.isNotEmpty()) {
                     dao.insertHabits(resetHabits)
                }
                
                // Update last check date
                prefs.edit().putString("last_check_date", todayDate).apply()
            }
        }
    }

    // Antrenman Programı (Shared State)
    var selectedDayCount by remember { mutableIntStateOf(3) }
    var workoutDays by remember {
        mutableStateOf(
            (1..selectedDayCount).map { dayNum ->
                com.noxi.noxiapp.data.WorkoutDay(
                    dayNumber = dayNum,
                    dayName = "${dayNum}.Gün"
                )
            }
        )
    }

    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background, // Dinamik arka plan
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!hideBottomBar) {
                // Modern navigation bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(40.dp))
                            .then(if (!isDarkTheme) Modifier.border(2.dp, Color(0xFFB0B0B0), RoundedCornerShape(40.dp)) else Modifier),
                        color = MaterialTheme.colorScheme.surfaceContainer, // Uses theme surface container (dark or light)
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hedefler
                            NavigationItem(
                                icon = Icons.Default.List,
                                label = strings.navGoals,
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            
                            // Antrenman
                            NavigationItem(
                                icon = Icons.Default.FitnessCenter,
                                label = strings.navWorkout,
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            
                            // Takvim
                            NavigationItem(
                                icon = Icons.Default.DateRange,
                                label = strings.navCalendar,
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                            
                            // Profil
                            NavigationItem(
                                icon = Icons.Default.Person,
                                label = strings.navProfile,
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> {
                HabitsAndGoalsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    habits = habits,
                    onAddHabit = { habit ->
                        scope.launch { dao.insertHabit(habit) }
                    },
                    onUpdateHabit = { habit ->
                        scope.launch { dao.updateHabit(habit) }
                    },
                    onDeleteHabit = { habit ->
                        scope.launch { dao.deleteHabit(habit) }
                    },
                    onLogHabit = { logMessage ->
                        val today = java.time.LocalDate.now().toString()
                        scope.launch {
                            val currentHistory = dao.getHistory(today) 
                                ?: com.noxi.noxiapp.data.WorkoutHistory(date = today)
                            val updatedHistory = currentHistory.copy(habitLogs = currentHistory.habitLogs + logMessage)
                            dao.insertHistory(updatedHistory)
                        }
                    },
                    onHideBottomBar = { shouldHide ->
                        hideBottomBar = shouldHide
                    }
                )
            }
            1 -> {
                WorkoutScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (hideBottomBar) PaddingValues(0.dp) else innerPadding),
                    history = history,
                    onHistoryUpdate = { date, newHistory ->
                        // Persist workout history update
                        scope.launch {
                            dao.insertHistory(newHistory)
                        }
                    },
                    workoutDays = workoutDays,
                    onWorkoutDaysUpdate = { newDays ->
                        workoutDays = newDays
                    },
                    selectedDayCount = selectedDayCount,
                    onSelectedDayCountUpdate = { newCount ->
                        selectedDayCount = newCount
                    },
                    savedPrograms = savedPrograms,
                    onSaveProgram = { program ->
                        scope.launch { dao.insertProgram(program) }
                    },
                    onExerciseScreenChange = { inExerciseScreen ->
                        hideBottomBar = inExerciseScreen
                    }
                )
            }
            2 -> {
                com.noxi.noxiapp.ui.screens.CalendarScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    history = history,
                    workoutDays = workoutDays
                )
            }
            3 -> {
                ProfileScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange,
                    userEmail = userEmail,
                    onLogout = onLogout
                )
            }
        }
    }
}
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color(0xFFFF0000) else Color(0xFF888888),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color(0xFFFF0000) else Color(0xFF888888),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        
        // Seçili sekme için alt çizgi (görseldeki gibi)
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}