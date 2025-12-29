package com.noxi.noxiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxi.noxiapp.data.Habit
import com.noxi.noxiapp.data.HabitCategory
import com.noxi.noxiapp.data.PredefinedHabits
import com.noxi.noxiapp.ui.components.HabitCard
import com.noxi.noxiapp.ui.components.MultiColorCircularProgress
import com.noxi.noxiapp.ui.components.PredefinedHabitCard
import com.noxi.noxiapp.ui.theme.LocalStrings

/**
 * Alışkanlıklar ve Hedefler Ana Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsAndGoalsScreen(
    modifier: Modifier = Modifier,
    habits: List<Habit>,
    onAddHabit: (Habit) -> Unit,
    onUpdateHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onLogHabit: (String) -> Unit,
    onHideBottomBar: (Boolean) -> Unit = {}
) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Alışkanlıklar listesi state (Hoisted -> Removed internal state)
    // var habits by remember { mutableStateOf(listOf<Habit>()) }
    
    // Tam ekran ekleme sayfası
    var showAddHabitPage by remember { mutableStateOf(false) }
    
    // Özel oluştur için tam ekran sayfa
    var showCustomHabitPage by remember { mutableStateOf(false) }
    
    // Seçilen hazır alışkanlık için ayarlar
    var selectedPredefinedHabit by remember { mutableStateOf<PredefinedHabitType?>(null) }
    
    // Alt menüyü gizle/göster
    LaunchedEffect(showAddHabitPage, showCustomHabitPage, selectedPredefinedHabit) {
        val shouldHide = showAddHabitPage || showCustomHabitPage || selectedPredefinedHabit != null
        onHideBottomBar(shouldHide)
    }
    
    if (!showAddHabitPage && !showCustomHabitPage && selectedPredefinedHabit == null) {
        // Ana ekran
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Başlık
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tarih Formatlama
                    val locale = if (strings.today == "Bugün") java.util.Locale("tr", "TR") else java.util.Locale.ENGLISH
                    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM, EEEE", locale)
                    val formattedDate = java.time.LocalDate.now().format(dateFormatter)

                    Text(
                        text = formattedDate,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    
                    // + Butonu - Tam ekran sayfa açar
                    IconButton(
                        onClick = { showAddHabitPage = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.addHabit,
                            tint = contentColor
                        )
                    }
                }
                
                // Merkezi dairesel ilerleme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MultiColorCircularProgress(
                        habits = habits
                    )
                }
                
                // Alışkanlıklar listesi
                if (habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noHabitsYet,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(habits) { habit ->
                            HabitCard(
                                habit = habit,
                                onToggleComplete = {
                                    val todayDate = java.time.LocalDate.now().toString()
                                    val newHabit = if (habit.goalValue > 1f) {
                                        val newValue = (habit.currentValue + habit.incrementValue).coerceAtMost(habit.goalValue)
                                        val newLogs = habit.dateLogs.toMutableMap().apply { put(todayDate, newValue) }
                                        habit.copy(
                                            currentValue = newValue,
                                            isCompleted = newValue >= habit.goalValue,
                                            dateLogs = newLogs
                                        )
                                    } else {
                                        val isCompleted = !habit.isCompleted
                                        val newValue = if (isCompleted) habit.goalValue else 0f
                                        val newLogs = habit.dateLogs.toMutableMap().apply { put(todayDate, newValue) }
                                        habit.copy(
                                            isCompleted = isCompleted,
                                            currentValue = newValue,
                                            dateLogs = newLogs
                                        )
                                    }
                                    
                                    onUpdateHabit(newHabit)
                                    
                                    // Log history if completed right now
                                    if (newHabit.isCompleted && !habit.isCompleted) {
                                        val logMsg = if (habit.goalValue > 1f) {
                                            "${habit.goalValue.toInt()} ${habit.unit} ${habit.name}"
                                        } else {
                                            habit.name
                                        }
                                        onLogHabit("$logMsg ${strings.completed}") // "8 bardak su Tamamlandı"
                                    }
                                },
                                onUpdateValue = { newValue ->
                                    val todayDate = java.time.LocalDate.now().toString()
                                    val newLogs = habit.dateLogs.toMutableMap().apply { put(todayDate, newValue) }
                                    val newHabit = habit.copy(currentValue = newValue, dateLogs = newLogs)
                                    onUpdateHabit(newHabit)
                                },
                                onDelete = {
                                    onDeleteHabit(habit)
                                }
                            )
                        }
                    }
                }
            }
        }
    } else if (showAddHabitPage) {
        // Tam ekran ekleme sayfası
        AddHabitSelectionPage(
            onBack = { showAddHabitPage = false },
            onSelectCustom = {
                showAddHabitPage = false
                showCustomHabitPage = true
            },
            onSelectPredefined = { habitType ->
                showAddHabitPage = false
                selectedPredefinedHabit = habitType
            }
        )
    } else if (showCustomHabitPage) {
        // Özel alışkanlık oluşturma sayfası
        CustomHabitCreationPage(
            onBack = { showCustomHabitPage = false },
            onCreateHabit = { habit ->
                onAddHabit(habit)
                showCustomHabitPage = false
            }
        )
    } else if (selectedPredefinedHabit != null) {
        // Hazır alışkanlık ayarları sayfası
        PredefinedHabitSettingsPage(
            habitType = selectedPredefinedHabit!!,
            onBack = { 
                selectedPredefinedHabit = null
                showAddHabitPage = true
            },
            onCreateHabit = { habit ->
                onAddHabit(habit)
                selectedPredefinedHabit = null
            }
        )
    }
}

/**
 * Alışkanlık seçim sayfası - Tam ekran (görseldeki gibi)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSelectionPage(
    onBack: () -> Unit,
    onSelectCustom: () -> Unit,
    onSelectPredefined: (PredefinedHabitType) -> Unit
) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Üst bar - Geri ok
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geri ok butonu
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = strings.back,
                        tint = contentColor
                    )
                }
            }
            
            // Başlık
            Text(
                text = strings.addHabit,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Özel Oluştur
                item {
                    ModernHabitCard(
                        icon = "➕",
                        iconColor = Color(0xFF6366F1),
                        text = strings.createCustomHabit,
                        onClick = onSelectCustom
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // --- SAĞLIK KATEGORİSİ ---
                item {
                    Text(
                        text = "Sağlık", // TODO: Move to strings if needed
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor
                    ) {
                        Column {
                            ModernHabitCard(
                                icon = "💧",
                                iconColor = Color(0xFF3B82F6),
                                text = strings.drinkWater,
                                onClick = { onSelectPredefined(PredefinedHabitType.DRINK_WATER) },
                                showDivider = true
                            )
                            ModernHabitCard(
                                icon = "💊",
                                iconColor = Color(0xFF10B981),
                                text = strings.takeVitamin,
                                onClick = { onSelectPredefined(PredefinedHabitType.TAKE_VITAMIN) },
                                showDivider = false
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- VÜCUT KATEGORİSİ ---
                item {
                    Text(
                        text = "Vücut", // TODO: Move to strings if needed
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor
                    ) {
                        Column {
                            ModernHabitCard(
                                icon = "⚖️",
                                iconColor = Color(0xFFFBBF24),
                                text = strings.loseWeight,
                                onClick = { onSelectPredefined(PredefinedHabitType.LOSE_WEIGHT) },
                                showDivider = true
                            )
                            ModernHabitCard(
                                icon = "🚶",
                                iconColor = Color(0xFF8B5CF6),
                                text = strings.walk,
                                onClick = { onSelectPredefined(PredefinedHabitType.WALK) },
                                showDivider = false
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- GÜNLÜK KATEGORİSİ ---
                item {
                    Text(
                        text = strings.daily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor
                    ) {
                        Column {
                            ModernHabitCard(
                                icon = "📚",
                                iconColor = Color(0xFF92400E),
                                text = strings.readBook,
                                onClick = { onSelectPredefined(PredefinedHabitType.READ_BOOK) },
                                showDivider = true
                            )
                            ModernHabitCard(
                                icon = "🧘",
                                iconColor = Color(0xFF059669),
                                text = strings.meditate,
                                onClick = { onSelectPredefined(PredefinedHabitType.MEDITATE) },
                                showDivider = true
                            )
                            ModernHabitCard(
                                icon = "💪",
                                iconColor = Color(0xFFEA580C),
                                text = strings.exercise,
                                onClick = { onSelectPredefined(PredefinedHabitType.EXERCISE) },
                                showDivider = false
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ModernHabitCard(
    icon: String,
    iconColor: Color,
    text: String,
    onClick: () -> Unit,
    showDivider: Boolean = false
) {
    // Colors
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Metin
            Text(
                text = text,
                fontSize = 16.sp,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (showDivider) {
            Divider(
                color = Color(0xFF2C2C2C), // Divider seems generic enough or could be onSurfaceVariant
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Özel alışkanlık oluşturma sayfası
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomHabitCreationPage(
    onBack: () -> Unit,
    onCreateHabit: (Habit) -> Unit
) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.createCustomHabit, color = contentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = strings.back,
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            CustomHabitForm(
                onCreateHabit = onCreateHabit
            )
        }
    }
}

/**
 * Eski AddHabitPage - ARTIK KULLANILMIYOR
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitPage(
    onBack: () -> Unit,
    onSelectPredefinedHabit: (PredefinedHabitType) -> Unit,
    onCreateCustomHabit: (Habit) -> Unit
) {
    var showCustomHabitForm by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alışkanlık Ekle", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ÖZEL OLUŞTUR - EN ÜSTTE
            item {
                Text(
                    text = "Özel Oluştur",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            item {
                // Özel Oluştur kartı - tıklanınca açılır
                PredefinedHabitCard(
                    name = "Özel Alışkanlık Oluştur",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF64B5F6),
                    onClick = { showCustomHabitForm = !showCustomHabitForm }
                )
            }
            
            // Özel alışkanlık formu - açıldığında göster
            if (showCustomHabitForm) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomHabitForm(
                        onCreateHabit = onCreateCustomHabit
                    )
                }
            }
            
            // HAZIR SEÇ KATEGORİSİ
            item {
                Text(
                    text = "Hazır Seç",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                )
            }
            
            // Hazır alışkanlıklar - kategori başlıkları YOK
            item {
                PredefinedHabitCard(
                    name = "Su İç",
                    icon = Icons.Default.WaterDrop,
                    color = PredefinedHabits.WaterBlue,
                    onClick = { onSelectPredefinedHabit(PredefinedHabitType.DRINK_WATER) }
                )
            }
            
            item {
                PredefinedHabitCard(
                    name = "Kilo Ver",
                    icon = Icons.Default.Scale,
                    color = PredefinedHabits.WeightYellow,
                    onClick = { onSelectPredefinedHabit(PredefinedHabitType.LOSE_WEIGHT) }
                )
            }
            
            item {
                PredefinedHabitCard(
                    name = "Yürüyüş Yap",
                    icon = Icons.Default.DirectionsWalk,
                    color = PredefinedHabits.WalkPurple,
                    onClick = { onSelectPredefinedHabit(PredefinedHabitType.WALK) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Özel alışkanlık formu
 */
@Composable
fun CustomHabitForm(
    onCreateHabit: (Habit) -> Unit
) {
    // Strings
    val strings = LocalStrings.current
    
    var habitName by remember { mutableStateOf("") }
    var goalValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var incrementValue by remember { mutableStateOf("1") }
    var selectedColor by remember { mutableStateOf(Color(0xFF64B5F6)) }
    
    val availableColors = listOf(
        Color(0xFF64B5F6), // Mavi
        Color(0xFFFFD54F), // Sarı
        Color(0xFFBA68C8), // Mor
        Color(0xFF81C784), // Yeşil
        Color(0xFFFF8A65), // Turuncu
        Color(0xFFE57373)  // Kırmızı
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text(strings.habitName) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = selectedColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = goalValue,
                    onValueChange = { goalValue = it },
                    label = { Text(strings.goal) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = selectedColor,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(strings.unit) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = selectedColor,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Increment Value
            OutlinedTextField(
                value = incrementValue,
                onValueChange = { incrementValue = it },
                label = { Text(strings.incrementAmount) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = selectedColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Renk seçimi
            Text(
                text = strings.selectColor,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                availableColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (color == selectedColor) {
                                    Modifier.padding(4.dp)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { selectedColor = color },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = strings.selected,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // İkon seçimi
            Text(
                text = strings.selectIcon,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val availableIcons = listOf(
                "Star" to Icons.Default.Star,
                "Favorite" to Icons.Default.Favorite,
                "FitnessCenter" to Icons.Default.FitnessCenter,
                "Book" to Icons.Default.MenuBook, // Using generic 'Book' mapping to MenuBook
                "MusicNote" to Icons.Default.MusicNote,
                "Brush" to Icons.Default.Brush
            )
            
            var selectedIconName by remember { mutableStateOf("Star") }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                availableIcons.forEach { (name, icon) ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (name == selectedIconName) selectedColor.copy(alpha = 0.3f)
                                else Color(0xFF2C2C2C)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { selectedIconName = name },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = strings.icon,
                                tint = if (name == selectedIconName) selectedColor else Color.Gray
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (habitName.isNotBlank() && goalValue.isNotBlank()) {
                        val habit = Habit(
                            name = habitName,
                            colorCode = selectedColor.toArgb(),
                            iconName = selectedIconName,
                            category = HabitCategory.CUSTOM,
                            goalValue = goalValue.toFloatOrNull() ?: 1f,
                            unit = unit,
                            incrementValue = incrementValue.toFloatOrNull() ?: 1f
                        )
                        onCreateHabit(habit)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor
                ),
                enabled = habitName.isNotBlank() && goalValue.isNotBlank()
            ) {
                Text(strings.create, fontSize = 16.sp)
            }
        }
    }
}

/**
 * Hazır alışkanlık ayarları sayfası
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredefinedHabitSettingsPage(
    habitType: PredefinedHabitType,
    onBack: () -> Unit,
    onCreateHabit: (Habit) -> Unit
) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (habitType) {
                            PredefinedHabitType.DRINK_WATER -> strings.drinkWaterSettings
                            PredefinedHabitType.LOSE_WEIGHT -> strings.loseWeightSettings
                            PredefinedHabitType.WALK -> strings.walkSettings
                            PredefinedHabitType.READ_BOOK -> "${strings.readBook} ${strings.settings}"
                            PredefinedHabitType.TAKE_VITAMIN -> "${strings.takeVitamin} ${strings.settings}"
                            PredefinedHabitType.MEDITATE -> "${strings.meditate} ${strings.settings}"
                            PredefinedHabitType.EXERCISE -> "${strings.exercise} ${strings.settings}"
                        },
                        color = contentColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = strings.back,
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (habitType) {
                PredefinedHabitType.DRINK_WATER -> {
                    WaterHabitSettings(onCreateHabit)
                }
                PredefinedHabitType.LOSE_WEIGHT -> {
                    WeightLossHabitSettings(onCreateHabit)
                }
                PredefinedHabitType.WALK -> {
                    WalkHabitSettings(onCreateHabit)
                }
                PredefinedHabitType.READ_BOOK -> {
                    ReadBookHabitSettings(onCreateHabit)
                }
                PredefinedHabitType.TAKE_VITAMIN -> {
                    TakeVitaminHabitSettings(onCreateHabit)
                }
                PredefinedHabitType.MEDITATE -> {
                    MeditateHabitSettings(onCreateHabit)
                }
                PredefinedHabitType.EXERCISE -> {
                    ExerciseHabitSettings(onCreateHabit)
                }
            }
        }
    }
}

@Composable
fun WaterHabitSettings(onCreateHabit: (Habit) -> Unit) {
    // Strings
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    var glassCount by remember { mutableStateOf("8") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = strings.drinkWaterSettings,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = glassCount,
            onValueChange = { glassCount = it },
            label = { Text(strings.dailyGoalGlass) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = PredefinedHabits.WaterBlue,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = PredefinedHabits.WaterBlue,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                val habit = Habit(
                    name = strings.drinkWater,
                    colorCode = PredefinedHabits.WaterBlue.toArgb(),
                    iconName = "WaterDrop",
                    category = HabitCategory.HEALTH,
                    goalValue = glassCount.toFloatOrNull() ?: 8f,
                    unit = strings.glass
                )
                onCreateHabit(habit)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PredefinedHabits.WaterBlue
            )
        ) {
            Text(strings.create, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WeightLossHabitSettings(onCreateHabit: (Habit) -> Unit) {
    // Strings
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    var startWeight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var dailyCalories by remember { mutableStateOf("2000") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = strings.loseWeightSettings,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = startWeight,
            onValueChange = { startWeight = it },
            label = { Text(strings.startWeightKg) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = PredefinedHabits.WeightYellow,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = PredefinedHabits.WeightYellow,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = targetWeight,
            onValueChange = { targetWeight = it },
            label = { Text(strings.targetWeightKg) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = PredefinedHabits.WeightYellow,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = PredefinedHabits.WeightYellow,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = dailyCalories,
            onValueChange = { dailyCalories = it },
            label = { Text(strings.dailyCalorieGoal) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = PredefinedHabits.WeightYellow,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = PredefinedHabits.WeightYellow,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                val start = startWeight.toFloatOrNull() ?: 0f
                val target = targetWeight.toFloatOrNull() ?: 0f
                val habit = Habit(
                    name = strings.loseWeight,
                    colorCode = PredefinedHabits.WeightYellow.toArgb(),
                    iconName = "MonitorWeight",
                    category = HabitCategory.BODY,
                    goalValue = start - target, // Hedef: kaybedilecek kilo
                    currentValue = 0f,
                    unit = strings.kg,
                    startWeight = start,
                    targetWeight = target,
                    dailyCalorieGoal = dailyCalories.toIntOrNull() ?: 2000
                )
                onCreateHabit(habit)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PredefinedHabits.WeightYellow
            )
        ) {
            Text(strings.create, fontSize = 16.sp, color = Color.Black)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WalkHabitSettings(onCreateHabit: (Habit) -> Unit) {
    // Strings
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    var stepGoal by remember { mutableStateOf("10000") }
    var incrementStep by remember { mutableStateOf("1000") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = strings.walkSettings,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = stepGoal,
            onValueChange = { stepGoal = it },
            label = { Text(strings.dailyStepGoal) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = PredefinedHabits.WalkPurple,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = PredefinedHabits.WalkPurple,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = incrementStep,
            onValueChange = { incrementStep = it },
            label = { Text(strings.incrementAmount) }, // "Arttırma Miktarı" (New string needed or hardcode "Arttırma Miktarı")
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = PredefinedHabits.WalkPurple,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = PredefinedHabits.WalkPurple,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                val habit = Habit(
                    name = strings.walk,
                    colorCode = PredefinedHabits.WalkPurple.toArgb(),
                    iconName = "DirectionsWalk",
                    category = HabitCategory.BODY,
                    goalValue = stepGoal.toFloatOrNull() ?: 10000f,
                    unit = strings.steps,
                    incrementValue = incrementStep.toFloatOrNull() ?: 1000f
                )
                onCreateHabit(habit)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PredefinedHabits.WalkPurple
            )
        ) {
            Text(strings.create, fontSize = 16.sp)
        }
    }
}

/**
 * Özel alışkanlık oluşturma
 */
@Composable
fun CustomHabitSheet(onCreateHabit: (Habit) -> Unit) {
    var habitName by remember { mutableStateOf("") }
    var goalValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var incrementValue by remember { mutableStateOf("1") }
    var selectedColor by remember { mutableStateOf(Color(0xFF64B5F6)) }
    
    val availableColors = listOf(
        Color(0xFF64B5F6), Color(0xFFFFD54F), Color(0xFFBA68C8),
        Color(0xFF81C784), Color(0xFFFF8A65), Color(0xFFE57373)
    )
    
     val availableIcons = listOf(
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Book" to Icons.Default.MenuBook,
        "MusicNote" to Icons.Default.MusicNote,
        "Brush" to Icons.Default.Brush
    )
    var selectedIconName by remember { mutableStateOf("Star") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Özel Alışkanlık Oluştur",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            label = { Text("Alışkanlık Adı") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = selectedColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = selectedColor,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = goalValue,
                onValueChange = { goalValue = it },
                label = { Text("Hedef") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = selectedColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Birim") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = selectedColor,
                    unfocusedLabelColor = Color.Gray
                )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Increment Value
        OutlinedTextField(
            value = incrementValue,
            onValueChange = { incrementValue = it },
            label = { Text("Arttırma Miktarı") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = selectedColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = selectedColor,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Renk seçimi
        Text(
            text = "Renk Seç",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (color == selectedColor) {
                                Modifier.padding(4.dp)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { selectedColor = color },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (color == selectedColor) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçili",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // İkon seçimi
        Text(
            text = "İkon Seç",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            availableIcons.forEach { (name, icon) ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (name == selectedIconName) selectedColor.copy(alpha = 0.3f)
                            else Color(0xFF2C2C2C)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { selectedIconName = name },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "İkon",
                            tint = if (name == selectedIconName) selectedColor else Color.Gray
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (habitName.isNotBlank() && goalValue.isNotBlank()) {
                    val habit = Habit(
                        name = habitName,
                        colorCode = selectedColor.toArgb(),
                        iconName = selectedIconName, // FIX
                        category = HabitCategory.CUSTOM,
                        goalValue = goalValue.toFloatOrNull() ?: 1f,
                        unit = unit,
                        incrementValue = incrementValue.toFloatOrNull() ?: 1f
                    )
                    onCreateHabit(habit)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedColor
            ),
            enabled = habitName.isNotBlank() && goalValue.isNotBlank()
        ) {
            Text("Oluştur", fontSize = 16.sp)
        }
        
    }
}

// ===== YENİ ALIŞKANLIK AYAR SAYFALARI =====

@Composable
fun ReadBookHabitSettings(onCreateHabit: (Habit) -> Unit) {
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    var goalValue by remember { mutableStateOf("30") }
    var usePages by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${strings.readBook} ${strings.settings}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = strings.pageOrMinute,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { usePages = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (usePages) Color(0xFF92400E) else Color.Gray
                )
            ) {
                Text(strings.pages)
            }
            Button(
                onClick = { usePages = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!usePages) Color(0xFF92400E) else Color.Gray
                )
            ) {
                Text(strings.minutes)
            }
        }
        
        OutlinedTextField(
            value = goalValue,
            onValueChange = { goalValue = it },
            label = { Text("${strings.goal} (${if (usePages) strings.pages else strings.minutes})") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = Color(0xFF92400E),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF92400E),
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (goalValue.isNotBlank()) {
                    val habit = Habit(
                        name = strings.readBook,
                        colorCode = Color(0xFF92400E).toArgb(),
                        iconName = "Book",
                        category = HabitCategory.HEALTH,
                        goalValue = goalValue.toFloatOrNull() ?: 30f,
                        unit = if (usePages) strings.pages else strings.minutes,
                        incrementValue = if (usePages) 10f else 5f
                    )
                    onCreateHabit(habit)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF92400E)
            ),
            enabled = goalValue.isNotBlank()
        ) {
            Text(strings.create, fontSize = 16.sp)
        }
    }
}

@Composable
fun TakeVitaminHabitSettings(onCreateHabit: (Habit) -> Unit) {
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    var pillCount by remember { mutableStateOf("2") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${strings.takeVitamin} ${strings.settings}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = pillCount,
            onValueChange = { pillCount = it },
            label = { Text("${strings.goal} (${strings.pills})") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF10B981),
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (pillCount.isNotBlank()) {
                    val habit = Habit(
                        name = strings.takeVitamin,
                        colorCode = Color(0xFF10B981).toArgb(),
                        iconName = "Star",
                        category = HabitCategory.HEALTH,
                        goalValue = pillCount.toFloatOrNull() ?: 2f,
                        unit = strings.pills,
                        incrementValue = 1f
                    )
                    onCreateHabit(habit)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            ),
            enabled = pillCount.isNotBlank()
        ) {
            Text(strings.create, fontSize = 16.sp)
        }
    }
}

@Composable
fun MeditateHabitSettings(onCreateHabit: (Habit) -> Unit) {
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    var minutes by remember { mutableStateOf("10") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${strings.meditate} ${strings.settings}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it },
            label = { Text("${strings.goal} (${strings.minutes})") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = Color(0xFF059669),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF059669),
                unfocusedLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (minutes.isNotBlank()) {
                    val habit = Habit(
                        name = strings.meditate,
                        colorCode = Color(0xFF059669).toArgb(),
                        iconName = "SelfImprovement",
                        category = HabitCategory.HEALTH,
                        goalValue = minutes.toFloatOrNull() ?: 10f,
                        unit = strings.minutes,
                        incrementValue = 5f
                    )
                    onCreateHabit(habit)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF059669)
            ),
            enabled = minutes.isNotBlank()
        ) {
            Text(strings.create, fontSize = 16.sp)
        }
    }
}

@Composable
fun ExerciseHabitSettings(onCreateHabit: (Habit) -> Unit) {
    val strings = LocalStrings.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${strings.exercise} ${strings.settings}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Bu alışkanlık basit bir tamamlandı/tamamlanmadı işaretidir.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = {
                val habit = Habit(
                    name = strings.exercise,
                    colorCode = Color(0xFFEA580C).toArgb(),
                    iconName = "FitnessCenter",
                    category = HabitCategory.BODY,
                    goalValue = 1f,
                    unit = "",
                    incrementValue = 1f
                )
                onCreateHabit(habit)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEA580C)
            )
        ) {
            Text(strings.create, fontSize = 16.sp)
        }
    }
}

/**
 * Hazır alışkanlık tipleri
 */
enum class PredefinedHabitType {
    DRINK_WATER,
    LOSE_WEIGHT,
    WALK,
    READ_BOOK,      // Kitap Oku
    TAKE_VITAMIN,   // Vitamin Al
    MEDITATE,       // Meditasyon Yap
    EXERCISE        // Egzersiz Yap
}
