package com.noxi.noxiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxi.noxiapp.ui.theme.LocalStrings

/**
 * Antrenman Sayfası
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    modifier: Modifier = Modifier,
    history: Map<String, com.noxi.noxiapp.data.WorkoutHistory>,
    onHistoryUpdate: (String, com.noxi.noxiapp.data.WorkoutHistory) -> Unit,
    workoutDays: List<com.noxi.noxiapp.data.WorkoutDay>,
    onWorkoutDaysUpdate: (List<com.noxi.noxiapp.data.WorkoutDay>) -> Unit,
    selectedDayCount: Int,
    onSelectedDayCountUpdate: (Int) -> Unit,
    savedPrograms: List<com.noxi.noxiapp.data.WorkoutProgram>,
    onSaveProgram: (com.noxi.noxiapp.data.WorkoutProgram) -> Unit,
    onExerciseScreenChange: (Boolean) -> Unit = {}
) {
    // Local State
    var expandedDayCount by remember { mutableStateOf(false) }
    var expandedProgram by remember { mutableStateOf(false) }
    
    // Program Kaydetme Dialogu
    var showSaveProgramDialog by remember { mutableStateOf(false) }
    var newProgramName by remember { mutableStateOf("") }
    
    // Gün ismi düzenleme
    var editingDay by remember { mutableStateOf<com.noxi.noxiapp.data.WorkoutDay?>(null) }
    
    // Antrenman ekleme sayfası
    var selectedDayForExercise by remember { mutableStateOf<com.noxi.noxiapp.data.WorkoutDay?>(null) }
    
    // Loglama durumu
    var loggingExercise by remember { mutableStateOf<com.noxi.noxiapp.data.Exercise?>(null) }
    var loggingDay by remember { mutableStateOf<com.noxi.noxiapp.data.WorkoutDay?>(null) }

    // Tarih Seçimi
    var currentDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) } // YYYY-MM-DD
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // --- TARİH DEĞİŞİNCE INPUTLARI TEMİZLEME LOGIC'İ ---
    LaunchedEffect(currentDate) {
        // Tarih değiştiğinde, eğer o güne ait loglar yoksa veya kullanıcı yeni bir sayfa açmak istiyorsa temizle.
        // Kullanıcı isteği: "yazılan not gün değişince gitsin"
        // Bu yüzden active weight/actual reps/note alanlarını sıfırlıyoruz.
        // Ancak bunu yaparken history'yi silmiyoruz, sadece UI'daki inputları temizliyoruz.
        
        val cleanedDays = workoutDays.map { day ->
            day.copy(exercises = day.exercises.map { ex ->
                ex.copy(actualWeight = null, actualReps = null, note = null)
            })
        }
        onWorkoutDaysUpdate(cleanedDays)
    }
    // ----------------------------------------------------
    


    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // ... (logic)
    
    // Log Dialog
    if (loggingExercise != null && loggingDay != null) {
        // ... (Log Dialog logic implementation remains same as before)
        com.noxi.noxiapp.ui.components.ExerciseLogDialog(
            exercise = loggingExercise!!,
            onDismiss = { 
                loggingExercise = null 
                loggingDay = null
            },
            onSave = { updatedExercise ->
                 // Logu history'ye kaydet
                 val currentHistory = history[currentDate] ?: com.noxi.noxiapp.data.WorkoutHistory(date = currentDate)
                 val newLog = com.noxi.noxiapp.data.ExerciseLog(
                     exerciseId = updatedExercise.id,
                     exerciseName = updatedExercise.name,
                     actualWeight = updatedExercise.actualWeight,
                     actualReps = updatedExercise.actualReps,
                     note = updatedExercise.note
                 )
                 val newLogs = currentHistory.logs + (updatedExercise.id to newLog)
                 
                 // Callback ile güncelle
                 onHistoryUpdate(currentDate, currentHistory.copy(logs = newLogs))
                 
                 // UI güncellemesi için workoutDays'i de güncelle (Görsel amaçlı)
                 val newDays = workoutDays.map { day ->
                     if (day.dayNumber == loggingDay!!.dayNumber) {
                         day.copy(exercises = day.exercises.map { 
                             if (it.id == loggingExercise!!.id) updatedExercise else it 
                         })
                     } else day
                 }
                 onWorkoutDaysUpdate(newDays)
                 

                 loggingExercise = null
                 loggingDay = null
            }
        )
    }
    
    // Bottom bar görünürlüğünü kontrol et
    LaunchedEffect(selectedDayForExercise) {
        onExerciseScreenChange(selectedDayForExercise != null)
    }
    
    // Gün sayısı değiştiğinde günleri güncelle (moved logic to callback usage below)
    
    if (showSaveProgramDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProgramDialog = false },
            title = { Text(strings.saveProgram) },
            text = { 
                OutlinedTextField(
                    value = newProgramName,
                    onValueChange = { newProgramName = it },
                    label = { Text(strings.programName) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProgramName.isNotBlank()) {
                            val newProgram = com.noxi.noxiapp.data.WorkoutProgram(
                                name = newProgramName,
                                totalDays = selectedDayCount,
                                days = workoutDays
                            )
                            onSaveProgram(newProgram)
                            showSaveProgramDialog = false
                            newProgramName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C))
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProgramDialog = false }) {
                    Text(strings.cancel, color = Color.Gray)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = contentColor,
            textContentColor = contentColor
        )
    }
    
    if (selectedDayForExercise == null) {
        // Ana ekran
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Başlık ve Tarih Seçici
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    
                    // Tarih Seçici
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            // Bir gün geri 
                            val date = java.time.LocalDate.parse(currentDate).minusDays(1)
                            currentDate = date.toString()
                        }) {
                            Text("<", color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        // Tarih Seçici Dialog
                        val showDatePicker = remember { mutableStateOf(false) }
                        if (showDatePicker.value) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker.value = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = java.time.Instant.ofEpochMilli(millis)
                                                .atZone(java.time.ZoneId.systemDefault())
                                                .toLocalDate()
                                            currentDate = date.toString()
                                        }
                                        showDatePicker.value = false
                                    }) {
                                        Text(strings.ok, color = Color(0xFFDC143C))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker.value = false }) {
                                        Text(strings.cancel, color = Color.Gray)
                                    }
                                },
                                colors = DatePickerDefaults.colors(
                                    containerColor = surfaceColor,
                                    titleContentColor = contentColor,
                                    headlineContentColor = contentColor,
                                    weekdayContentColor = contentColor,
                                    subheadContentColor = contentColor,
                                    yearContentColor = contentColor,
                                    currentYearContentColor = contentColor,
                                    selectedYearContentColor = Color.White,
                                    selectedYearContainerColor = Color(0xFFDC143C),
                                    dayContentColor = contentColor,
                                    disabledDayContentColor = Color.Gray,
                                    selectedDayContentColor = Color.White,
                                    selectedDayContainerColor = Color(0xFFDC143C),
                                    todayContentColor = Color(0xFFDC143C),
                                    todayDateBorderColor = Color(0xFFDC143C)
                                )
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        Text(
                            text = currentDate, // "2024-12-28" - format is date
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.clickable { showDatePicker.value = true }
                        )
                        
                        IconButton(onClick = { 
                            // Bir gün ileri
                            val date = java.time.LocalDate.parse(currentDate).plusDays(1)
                            currentDate = date.toString()
                        }) {
                            Text(">", color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.workoutProgram,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             // Program İşlemleri Row
                            Row(
                                modifier = Modifier
                                    .background(surfaceColor, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .clickable { expandedProgram = true } // Tüm kutuyu tıklanabilir yap
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                 Text(strings.programs, color = contentColor, fontSize = 14.sp)
                                 Text(" ▼", color = Color.Gray, fontSize = 12.sp)
                                 
                                DropdownMenu(
                                    expanded = expandedProgram,
                                    onDismissRequest = { expandedProgram = false },
                                    modifier = Modifier.background(surfaceColor)
                                ) {
                                    savedPrograms.forEach { program ->
                                        DropdownMenuItem(
                                            text = { Text(program.name, color = contentColor) }, 
                                            onClick = {
                                                onSelectedDayCountUpdate(program.totalDays)
                                                onWorkoutDaysUpdate(program.days)
                                                expandedProgram = false
                                            }
                                        )
                                    }
                                    
                                    if (savedPrograms.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(strings.noSavedPrograms, color = Color.Gray, fontSize = 12.sp) },
                                            onClick = {},
                                            enabled = false
                                        )
                                    }
                                }
                            }
                        
                            Spacer(modifier = Modifier.width(8.dp))
                        
                            // Gün sayısı seçici (Eski yerinde)
                            Box {
                                Surface(
                                    onClick = { expandedDayCount = true },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    color = surfaceColor
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$selectedDayCount ${strings.day}",
                                            fontSize = 14.sp,
                                            color = contentColor
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = expandedDayCount,
                                    onDismissRequest = { expandedDayCount = false },
                                    modifier = Modifier.background(surfaceColor)
                                ) {
                                    (3..7).forEach { dayCount ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "$dayCount ${strings.day}",
                                                    color = contentColor
                                                )
                                            },
                                            onClick = {
                                                onSelectedDayCountUpdate(dayCount)
                                                // Logic: Update workoutDays list size safely
                                                if (dayCount > workoutDays.size) {
                                                    val newDays = workoutDays + (workoutDays.size + 1..dayCount).map { dayNum ->
                                                        com.noxi.noxiapp.data.WorkoutDay(dayNumber = dayNum, dayName = "$dayNum.${strings.day}")
                                                    }
                                                    onWorkoutDaysUpdate(newDays)
                                                } else if (dayCount < workoutDays.size) {
                                                    onWorkoutDaysUpdate(workoutDays.take(dayCount))
                                                }
                                                expandedDayCount = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Günler listesi
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            items = workoutDays,
                            key = { it.id }
                        ) { day ->
                            // Bu gün için history kaydı var mı?
                            val historyKey = "${currentDate}_${day.id}"
                            val isCompleted = history[currentDate]?.completedDayId == day.id
                            val isPlanned = history[currentDate]?.plannedDayId == day.id
                            val isSelected = isCompleted || isPlanned
                            
                            com.noxi.noxiapp.ui.components.DayCard(
                                day = day,
                                isSelected = isSelected,
                                onSelectionChange = { selected ->
                                    if (selected) {
                                        // Tarih kontrolü: Gelecek mi?
                                        val today = java.time.LocalDate.now()
                                        val selectedDateParsed = java.time.LocalDate.parse(currentDate)
                                        
                                        val currentHistory = history[currentDate] ?: com.noxi.noxiapp.data.WorkoutHistory(date = currentDate)
                                        
                                        // O günkü egzersizlerin actual inputlarını history'ye snapshot olarak kaydet
                                        // Böylece kullanıcı tek tek save demese bile inputlar kaybolmaz
                                        val snapshotLogs = day.exercises.mapNotNull { ex ->
                                            if (ex.actualWeight != null || ex.actualReps != null || ex.note != null) {
                                                ex.id to com.noxi.noxiapp.data.ExerciseLog(
                                                    exerciseId = ex.id,
                                                    exerciseName = ex.name,
                                                    actualWeight = ex.actualWeight,
                                                    actualReps = ex.actualReps,
                                                    note = ex.note
                                                )
                                            } else null
                                        }.toMap()
                                        
                                        // Mevcut loglarla birleştir (Snapshot öncelikli olsun mu? Evet, UI'da ne varsa o.)
                                        // Ancak tamamen boşsa var olanı koruyalım mı? 
                                        // Kullanıcı UI'da sildiyse history'den de silinmeli mantıken.
                                        // Basitlik için: Eğer UI'da veri varsa update et.
                                        val mergedLogs = currentHistory.logs + snapshotLogs

                                        if (selectedDateParsed.isAfter(today)) {
                                            // Gelecek tarih -> PLANLANDI
                                            onHistoryUpdate(currentDate, currentHistory.copy(plannedDayId = day.id, completedDayId = null, logs = mergedLogs))
                                            
                                            // Bildirim Planla
                                            com.noxi.noxiapp.utils.NotificationHelper.scheduleWorkoutReminder(
                                                context = context,
                                                targetDate = selectedDateParsed,
                                                dayName = day.dayName
                                            )
                                        } else {
                                            // Bugün veya geçmiş -> TAMAMLANDI
                                            onHistoryUpdate(currentDate, currentHistory.copy(completedDayId = day.id, plannedDayId = null, logs = mergedLogs))
                                        }

                                    } else {
                                        // Seçimi kaldır
                                        val currentHistory = history[currentDate] ?: return@DayCard
                                        // Hem completed hem planned kontrol edilip silinmeli
                                        if (currentHistory.completedDayId == day.id || currentHistory.plannedDayId == day.id) {
                                            onHistoryUpdate(currentDate, currentHistory.copy(completedDayId = null, plannedDayId = null))

                                        }
                                    }
                                },
                                onClick = {
                                    selectedDayForExercise = day
                                },
                                onEditName = {
                                    editingDay = day
                                },
                                onExerciseClick = { exercise ->
                                    loggingExercise = exercise
                                    loggingDay = day
                                }
                            )
                        }
                    }
                    
                    // Programı Kaydet Butonu (Sol Alt)
                    // Menünün (BottomBar) hemen üstünde olması için padding ekliyoruz
                    FloatingActionButton(
                        onClick = { showSaveProgramDialog = true },
                        containerColor = Color(0xFFDC143C), // Artık Kırmızı! Gözüksün diye.
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 16.dp)
                    ) {
                         Icon(Icons.Default.Save, contentDescription = strings.saveProgram)
                    }


                }
            }
            
            // Gün ismi düzenleme dialogu
            editingDay?.let { day ->
                com.noxi.noxiapp.ui.components.DayNameEditDialog(
                    currentName = day.dayName,
                    currentIconRes = day.iconRes,
                    onDismiss = { editingDay = null },
                    onSave = { newName, newIconRes ->
                        val newDays = workoutDays.map {
                            if (it.dayNumber == day.dayNumber) it.copy(dayName = newName, iconRes = newIconRes) else it
                        }
                        onWorkoutDaysUpdate(newDays)
                        editingDay = null
                    }
                )
            }
        }
    } else {
        // Antrenman ekleme sayfası
        AddExerciseScreen(
            dayName = selectedDayForExercise!!.dayName,
            existingExercises = selectedDayForExercise!!.exercises,
            onBack = { selectedDayForExercise = null },
            onSaveExercises = { exercises ->
                val newDays = workoutDays.map {
                    if (it.dayNumber == selectedDayForExercise!!.dayNumber) {
                        it.copy(exercises = exercises)
                    } else it
                }
                onWorkoutDaysUpdate(newDays)
            }
        )
    }
}


/**
 * Beslenme Sayfası
 */
@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${strings.nutritionTitle}\n(${strings.comingSoon})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Profil Sayfası
 */

