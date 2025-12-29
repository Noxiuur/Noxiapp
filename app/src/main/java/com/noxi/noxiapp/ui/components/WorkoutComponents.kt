package com.noxi.noxiapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.noxi.noxiapp.data.Exercise
import com.noxi.noxiapp.data.WorkoutDay
import com.noxi.noxiapp.ui.theme.LocalStrings
import androidx.compose.ui.res.painterResource

/**
 * Gün kartı komponenti
 */
@Composable
fun DayCard(
    day: WorkoutDay,
    isSelected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
    onClick: () -> Unit,
    onEditName: () -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface 
    val primaryColor = Color(0xFFDC143C) // Check if primary is available in theme or keep brand color? 
    // Usually brand colors should be in ColorScheme, but if not, keep it or use error/primary.
    // Assuming 0xFFDC143C is a specific brand color (Crimson). I'll keep it for now or use MaterialTheme.colorScheme.primary if it matches.

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seçim Yuvarlağı (Checkbox)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .clickable { onSelectionChange(!isSelected) }
                        .border(
                            width = 2.dp,
                            color = if (isSelected) primaryColor else Color.Gray,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryColor, androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Gün ismi ve hareket sayısı
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.dayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Text(
                        text = if (day.exerciseCount > 0) "${day.exerciseCount} ${strings.exerciseCount}" else strings.noExercisesYet,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Düzenle butonu
                IconButton(onClick = onEditName) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.editName,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Hareketler önizlemesi
            // Hareketlerin Listesi (Varsa)
            if (day.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFF2C2C2C), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                day.exercises.forEach { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseClick(exercise) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                fontSize = 15.sp,
                                color = contentColor
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Hedef
                                Text(
                                    text = "${exercise.sets}x${exercise.reps}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                exercise.weight?.let { 
                                    Text(
                                        text = " • ${it}${strings.kg}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                // Yapılan (Log)
                                if (exercise.actualWeight != null || exercise.actualReps != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "→ ${exercise.actualWeight ?: "-"}${strings.kg} ${exercise.actualReps ?: "-"} ${if (exercise.isCardio) strings.minutes else strings.reps}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }
                            // Not varsa göster
                            exercise.note?.let {
                                if (it.isNotBlank()) {
                                    Text(
                                        text = "📝 $it",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gün ismi düzenleme dialogu - İkon seçici ile
 */
@Composable
fun DayNameEditDialog(
    currentName: String,
    currentIconRes: Int?,
    onDismiss: () -> Unit,
    onSave: (String, Int?) -> Unit
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(currentName) }
    var selectedIconRes by remember { mutableStateOf(currentIconRes) }
    
    // Mevcut ikonlar
    val availableIcons = listOf(
        com.noxi.noxiapp.R.drawable.ic_chest to strings.chest,
        com.noxi.noxiapp.R.drawable.ic_back to strings.backBody,
        com.noxi.noxiapp.R.drawable.ic_shoulders to strings.shoulders,
        com.noxi.noxiapp.R.drawable.ic_abs to strings.abs,
        com.noxi.noxiapp.R.drawable.ic_legs to strings.legs,
        com.noxi.noxiapp.R.drawable.ic_arms to strings.arms,
        com.noxi.noxiapp.R.drawable.ic_cardio to strings.cardio
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = strings.editDayNameTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.day) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFFDC143C),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFDC143C),
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // İkon seçimi
                Text(
                    text = strings.selectIconOptional,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // İkonsuz seçeneği
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedIconRes == null) Color(0xFFDC143C).copy(alpha = 0.3f)
                                else Color(0xFF2C2C2C)
                            )
                            .clickable { selectedIconRes = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = strings.defaultIcon,
                            tint = if (selectedIconRes == null) Color(0xFFDC143C) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Mevcut ikonlar
                    availableIcons.forEach { (iconRes, label) ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIconRes == iconRes) Color(0xFFDC143C).copy(alpha = 0.3f)
                                    else Color(0xFF2C2C2C)
                                )
                                .clickable { selectedIconRes = iconRes },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(strings.cancel, color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, selectedIconRes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC143C)
                        )
                    ) {
                        Text(strings.save)
                    }
                }
            }
        }
    }
}

/**
 * Hareket kartı komponenti
 */
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Row(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = buildString {
                        append("${exercise.sets} ${strings.set} × ${exercise.reps} ${if (exercise.isCardio) strings.minutes else strings.reps}")
                        exercise.weight?.let { append(" • ${it}${strings.kg}") }
                    },
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.delete,
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}
@Composable
fun ExerciseLogDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    val strings = LocalStrings.current
    var actualWeight by remember { mutableStateOf(exercise.actualWeight ?: "") }
    var actualReps by remember { mutableStateOf(exercise.actualReps ?: "") }
    var note by remember { mutableStateOf(exercise.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    text = exercise.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${strings.goal}: ${exercise.sets}x${exercise.reps} • ${exercise.weight ?: "-"}${strings.kg}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ağırlık Girişi
                OutlinedTextField(
                    value = actualWeight,
                    onValueChange = { actualWeight = it },
                    label = { Text(strings.actualWeight) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFFDC143C),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFDC143C),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                // Tekrar Girişi
                OutlinedTextField(
                    value = actualReps,
                    onValueChange = { actualReps = it },
                    label = { Text(if (exercise.isCardio) strings.actualMinutes else strings.actualReps) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFFDC143C),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFDC143C),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                // Not Girişi
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(strings.addNote) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFFDC143C),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFDC143C),
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        exercise.copy(
                            actualWeight = actualWeight,
                            actualReps = actualReps,
                            note = note
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC143C)
                )
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = Color.Gray)
            }
        }
    )
}
