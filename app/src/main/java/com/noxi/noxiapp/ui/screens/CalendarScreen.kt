package com.noxi.noxiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import com.noxi.noxiapp.ui.theme.LocalStrings
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    history: Map<String, com.noxi.noxiapp.data.WorkoutHistory>,
    workoutDays: List<com.noxi.noxiapp.data.WorkoutDay>
) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val cardColor = MaterialTheme.colorScheme.surface // Or a slightly lighter variation if needed, but surface is fine logicwise for cards usually
    
    // Seçilen tarih (Varsayılan bugün)
    var selectedDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    // Görüntülenen ay (Varsayılan şimdiki ay)
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.now()) }
    
    // Expandable Card state
    var isDetailsExpanded by remember { mutableStateOf(false) }
    
    // Seçili tarihin string hali
    val selectedDateStr = selectedDate.toString()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = strings.calendarTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // --- Custom Calendar ---
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Ay Navigasyonu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Text("<", color = contentColor, fontSize = 20.sp)
                        }
                        
                        Text(
                            text = "${currentMonth.month.name} ${currentMonth.year}",
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        androidx.compose.material3.IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Text(">", color = contentColor, fontSize = 20.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Haftanın Günleri Başlıkları
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        strings.shortDays.forEach { 
                            Text(text = it, color = Color.Gray, fontSize = 12.sp) 
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Günler Grid
                    val firstDayOfMonth = currentMonth.atDay(1)
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) - 7 (Sun)
                    
                    // Toplam hücre sayısı (Boşluklar + Günler)
                    val totalCells = (firstDayOfWeek - 1) + daysInMonth
                    val rows = (totalCells + 6) / 7
                    
                    Column {
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                for (col in 0 until 7) {
                                    val index = row * 7 + col
                                    val dayOfMonth = index - (firstDayOfWeek - 1) + 1
                                    
                                    if (dayOfMonth in 1..daysInMonth) {
                                        val date = currentMonth.atDay(dayOfMonth)
                                        val dateStr = date.toString()
                                        val isSelected = date == selectedDate
                                        val hasWorkout = history[dateStr]?.completedDayId != null || history[dateStr]?.plannedDayId != null || (history[dateStr]?.logs?.isNotEmpty() == true)
                                        
                                        // Gün Hücresi
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(if (isSelected) Color(0xFFDC143C) else Color.Transparent)
                                                .clickable { 
                                                    selectedDate = date 
                                                    isDetailsExpanded = false // Yeni tarih seçince detayı kapat
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayOfMonth.toString(),
                                                    color = if (isSelected) Color.White else contentColor // Selected text always white on red
                                                )
                                                // Kırmızı/Mavi Nokta (Varsa)
                                                if (hasWorkout) {
                                                    val isPlanned = history[dateStr]?.plannedDayId != null
                                                    val dotColor = if (isPlanned) Color(0xFF2196F3) else Color(0xFFDC143C)
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(if (isSelected) Color.White else dotColor, androidx.compose.foundation.shape.CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Boş hücre
                                        Box(modifier = Modifier.size(40.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // --- End Custom Calendar ---
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Seçilen Tarihin Başlığı
            Text(
                text = "${strings.selectedDate}: $selectedDateStr",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Seçilen Günün Detayları
            val historyItem = history[selectedDateStr]
            
            if (historyItem == null || (historyItem.completedDayId == null && historyItem.plannedDayId == null && historyItem.logs.isEmpty() && historyItem.habitLogs.isEmpty())) {
                Text(
                    text = strings.noWorkoutRecorded,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    // Alışkanlık Logları
                    if (historyItem.habitLogs.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (strings.today == "Bugün") "Tamamlanan Alışkanlıklar" else "Completed Habits",
                                        color = contentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    historyItem.habitLogs.forEach { log ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.Green,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = log,
                                                color = contentColor,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Gün Tamamlanma/Planlanma Durumu (Genişletilebilir Kart)
                    if (historyItem.completedDayId != null || historyItem.plannedDayId != null) {
                        item {
                            val activeId = historyItem.completedDayId ?: historyItem.plannedDayId
                            val isPlanned = historyItem.plannedDayId != null
                            
                            val dayName = workoutDays.find { it.id == activeId }?.dayName ?: strings.navWorkout
                             
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { isDetailsExpanded = !isDetailsExpanded }, // Tıklayınca aç/kapa
                                colors = CardDefaults.cardColors(containerColor = surfaceColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isPlanned) androidx.compose.material.icons.Icons.Default.DateRange else androidx.compose.material.icons.Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isPlanned) Color(0xFF2196F3) else Color.Green
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isPlanned) "$dayName ${strings.planned}" else "$dayName ${strings.completed}", 
                                            color = contentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // Aşağı/Yukarı ok
                                        Text(
                                            text = if (isDetailsExpanded) "▲" else "▼",
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    // Detaylar (Açıksa göster)
                                    if (isDetailsExpanded) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = Color.Gray, thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        val day = workoutDays.find { it.id == activeId }
                                        if (day != null) {
                                            day.exercises.forEach { exercise ->
                                                val log = historyItem.logs[exercise.id]
                                                
                                                Text(
                                                    text = "• ${exercise.name}",
                                                    color = contentColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                
                                                // Log varsa detayları göster, yoksa varsayılan hedefi göster
                                                if (log != null) {
                                                     val stats = buildString {
                                                        if (!log.actualWeight.isNullOrEmpty()) append("${log.actualWeight}kg")
                                                        if (!log.actualWeight.isNullOrEmpty() && !log.actualReps.isNullOrEmpty()) append(" x ")
                                                        if (!log.actualReps.isNullOrEmpty()) append("${log.actualReps} ${strings.reps}")
                                                    }
                                                    
                                                    if (stats.isNotEmpty()) {
                                                         Text(
                                                            text = "   $stats",
                                                            color = Color.Gray,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                    
                                                    if (!log.note.isNullOrEmpty()) {
                                                        Text(
                                                            text = "   ${strings.note}: ${log.note}",
                                                            color = Color.LightGray,
                                                            fontSize = 12.sp,
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                        )
                                                    }
                                                } else {
                                                    // Log yoksa: "Girilmedi" veya Hedef gösterilebilir
                                                    // Kullanıcı sadece hareketleri görmek istiyor
                                                     val target = buildString {
                                                        if (exercise.weight != null && exercise.weight > 0f) append("${exercise.weight}kg")
                                                        if (exercise.weight != null && exercise.weight > 0f) append(" x ")
                                                        if (exercise.reps > 0) append("${exercise.reps} ${strings.reps}")
                                                        if (exercise.sets > 0) append(" (${exercise.sets} ${strings.set})")
                                                    }
                                                    if (target.isNotEmpty()) {
                                                        Text(
                                                            text = "   ${strings.goal}: $target",
                                                            color = Color.DarkGray,
                                                            fontSize = 12.sp
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "   ${strings.notEntered}",
                                                            color = Color.DarkGray,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        } else {
                                            // Gün verisi bulunamadı (bazen eski history için gün silinmiş olabilir)
                                            // O zaman sadece logs'dan göster (fallback)
                                            historyItem.logs.values.forEach { log ->
                                                Text(text = "• ${log.exerciseName}", color = contentColor)
                                                // ... (basit gösterim)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (historyItem.logs.isNotEmpty()) {
                        // Sadece log var, gün tamamlanmamış (Örn: Sadece 1-2 hareket girilmiş)
                        item {
                            Text(strings.incompleteExercises, color = Color.Gray)
                            historyItem.logs.values.toList().forEach { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = log.exerciseName, color = contentColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
