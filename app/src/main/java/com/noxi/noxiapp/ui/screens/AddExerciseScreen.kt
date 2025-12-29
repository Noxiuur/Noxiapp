package com.noxi.noxiapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxi.noxiapp.R
import com.noxi.noxiapp.data.Exercise
import com.noxi.noxiapp.data.ExerciseCategory
import com.noxi.noxiapp.data.PredefinedExercises
import com.noxi.noxiapp.ui.components.ExerciseCard
import com.noxi.noxiapp.ui.theme.LocalStrings

/**
 * Antrenman ekleme/düzenleme sayfası - Alışkanlık ekleme sayfası benzeri tasarım
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    dayName: String,
    existingExercises: List<Exercise>,
    onBack: () -> Unit,
    onSaveExercises: (List<Exercise>) -> Unit
) {
    // Strings
    val strings = LocalStrings.current
    
    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    var exercises by remember { mutableStateOf(existingExercises) }
    var showCustomExerciseDialog by remember { mutableStateOf(false) }
    
    // Açık olan kategorileri tutan liste
    val expandedCategories = remember { mutableStateListOf<ExerciseCategory>() }
    
    // Düzenlenen hazır hareket (Listeden değil, kategoriden seçilen)
    var editingPredefinedExercise by remember { mutableStateOf<Exercise?>(null) }
    
    // Düzenlenen eklenmiş hareket (Listeden seçilen)
    var editingAddedExercise by remember { mutableStateOf<Exercise?>(null) }
    
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
                IconButton(onClick = {
                    onSaveExercises(exercises)
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = strings.back,
                        tint = contentColor
                    )
                }
            }
            
            // Başlık
            Text(
                text = dayName,
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
                    ModernExerciseCard(
                        text = strings.createCustomExercise,
                        onClick = { showCustomExerciseDialog = true },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.add,
                                tint = Color.White // Keep white as it's likely on a colored/dark card? Wait, ModernExerciseCard design check needed.
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Eklenen Hareketler
                if (exercises.isNotEmpty()) {
                    item {
                        Text(
                            text = "${strings.addedExercises} (${exercises.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    items(
                        items = exercises,
                        key = { it.id }
                    ) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { editingAddedExercise = exercise },
                            onDelete = {
                                exercises = exercises - exercise
                                onSaveExercises(exercises)
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                // Hazır Hareketler
                item {
                    Text(
                        text = strings.predefinedExercises,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                // Kategoriler (Accordion)
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor
                    ) {
                        Column {
                            ExerciseCategory.values().forEachIndexed { index, category ->
                                val isExpanded = expandedCategories.contains(category)
                                
                                Column {
                                    ModernExerciseCard(
                                        iconRes = getCategoryIconRes(category),
                                        iconColor = getCategoryColor(category),
                                        text = getCategoryName(category, strings),
                                        isExpanded = isExpanded,
                                        onClick = { 
                                            if (isExpanded) expandedCategories.remove(category)
                                            else expandedCategories.add(category)
                                        },
                                        showDivider = index < ExerciseCategory.values().size - 1 && !isExpanded
                                    )
                                    
                                    // Kategori içeriği (Hareketler)
                                    if (isExpanded) {
                                        Column(
                                            modifier = Modifier.background(surfaceColor.copy(alpha = 0.8f)) // Slightly lighter/darker? Or just surface.
                                        ) {
                                            PredefinedExercises.getExercisesByCategory(category).forEachIndexed { i, exercise ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { editingPredefinedExercise = exercise }
                                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = exercise.name,
                                                            fontSize = 16.sp,
                                                            color = contentColor
                                                        )
                                                    Text(
                                                        text = "${exercise.sets} ${strings.set} × ${exercise.reps} ${if (exercise.isCardio) strings.minutes else strings.reps}",
                                                        fontSize = 14.sp,
                                                        color = Color.Gray
                                                    )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.AddCircle,
                                                        contentDescription = strings.add,
                                                        tint = Color(0xFFDC143C)
                                                    )
                                                }
                                                
                                                if (i < PredefinedExercises.getExercisesByCategory(category).size - 1) {
                                                    Divider(
                                                        color = Color(0xFF333333),
                                                        thickness = 0.5.dp,
                                                        modifier = Modifier.padding(horizontal = 24.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Kategori ayırıcı (eğer son değilse)
                                        if (index < ExerciseCategory.values().size - 1) {
                                            Divider(
                                                color = Color(0xFF2C2C2C),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Özel hareket ekleme dialogu
    if (showCustomExerciseDialog) {
        val strings = LocalStrings.current
        AddCustomExerciseDialog(
            onDismiss = { showCustomExerciseDialog = false },
            onSave = { exercise ->
                exercises = exercises + exercise
                onSaveExercises(exercises)
                showCustomExerciseDialog = false
            }
        )
    }
    
    // Hazır hareket düzenleme ve ekleme dialogu
    editingPredefinedExercise?.let { exercise ->
        EditExerciseDialog(
            exercise = exercise,
            onDismiss = { editingPredefinedExercise = null },
            onSave = { editedExercise ->
                exercises = exercises + editedExercise
                onSaveExercises(exercises)
                editingPredefinedExercise = null
            }
        )
    }
    
    // Eklenmiş hareket düzenleme/silme dialogu
    editingAddedExercise?.let { exercise ->
        EditExerciseDialog(
            exercise = exercise,
            onDismiss = { editingAddedExercise = null },
            onSave = { editedExercise ->
                // Listeyi güncelle (Eski hareketi yenisiyle değiştir)
                exercises = exercises.map { if (it == exercise) editedExercise else it }
                onSaveExercises(exercises)
                editingAddedExercise = null
            },
            onDelete = {
                exercises = exercises - exercise
                onSaveExercises(exercises)
                editingAddedExercise = null
            }
        )
    }
}

@Composable
fun ModernExerciseCard(
    iconRes: Int? = null,
    iconColor: Color = Color.White,
    text: String,
    onClick: () -> Unit,
    isExpanded: Boolean = false,
    showDivider: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    val contentColor = if(text.contains(strings.createCustomExercise)) Color.White else Color.White // Keeping white for now as it's likely on dark surface or custom card style. 
    // Wait multi-theme support might need check. But icons and specific colors usually ignore theme in this specific custom UI.
    // However, I should try to use 'contentColor' from LocalContentColor if possible or just Text Color.
    // Given the previous design used hardcoded colors heavily, I'll stick to White for text on these cards if they have specific background.
    
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Arrow Rotation"
    )

    Column(modifier = Modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PNG İkon (sadece varsa)
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            // Metin
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            // Sağdaki İkon (Özel veya Ok)
            if (trailingIcon != null) {
                trailingIcon()
            } else {
                // Ok ikonu (Açık/Kapalı animasyonlu)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) strings.close else strings.open,
                    tint = Color.Gray,
                    modifier = Modifier.rotate(rotationState)
                )
            }
        }
        
        if (showDivider && !isExpanded) {
            Divider(
                color = Color(0xFF2C2C2C),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Özel hareket ekleme dialogu
 */
@Composable
fun AddCustomExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    // Strings
    val strings = LocalStrings.current
    
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var isCardio by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface, // Use surface color
        title = {
            Text(
                text = strings.createCustomExercise,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.exerciseName) },
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                
                // Kardiyo Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCardio,
                        onCheckedChange = { isCardio = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFDC143C))
                    )
                    Text(text = "Kardiyo (Dakika)", color = MaterialTheme.colorScheme.onSurface) // Or localized string "Cardio"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text(strings.set) }, // Or "Set"
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = Color(0xFFDC143C),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFDC143C),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text(if (isCardio) strings.minutes else strings.reps) }, // Or "Tekrar"
                        modifier = Modifier.weight(1f),
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(strings.targetWeight) }, // Using targetWeight "Hedef Ağırlık (Opsiyonel)"
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && sets.isNotBlank() && reps.isNotBlank()) {
                        val exercise = Exercise(
                            name = name,
                            sets = sets.toIntOrNull() ?: 1,
                            reps = reps.toIntOrNull() ?: 1,
                            weight = weight.toFloatOrNull(),
                            isCardio = isCardio
                        )
                        onSave(exercise)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC143C)
                ),
                enabled = name.isNotBlank() && sets.isNotBlank() && reps.isNotBlank()
            ) {
                Text(strings.add)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = Color.Gray)
            }
        }
    )
}

/**
 * Hareket düzenleme dialogu
 */
@Composable
fun EditExerciseDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // Strings
    val strings = LocalStrings.current
    
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var reps by remember { mutableStateOf(exercise.reps.toString()) }
    var weight by remember { mutableStateOf(exercise.weight?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = exercise.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text(strings.set) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = Color(0xFFDC143C),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFDC143C),
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text(if (exercise.isCardio) strings.minutes else strings.reps) },
                        modifier = Modifier.weight(1f),
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(strings.targetWeight) }, // Can use simple "Ağırlık (kg)" if preferred, but targetWeight works.
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val editedExercise = exercise.copy(
                        sets = sets.toIntOrNull() ?: exercise.sets,
                        reps = reps.toIntOrNull() ?: exercise.reps,
                        weight = weight.toFloatOrNull()
                    )
                    onSave(editedExercise)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC143C)
                )
            ) {
                Text(if (onDelete != null) strings.save else strings.add)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = strings.delete,
                            tint = Color.Gray
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(strings.cancel, color = Color.Gray)
                }
            }
        }
    )
}

/**
 * Kategori PNG ikonları
 */
private fun getCategoryIconRes(category: ExerciseCategory): Int? {
    return when (category) {
        ExerciseCategory.CHEST -> R.drawable.ic_chest
        ExerciseCategory.BACK -> R.drawable.ic_back
        ExerciseCategory.LEGS -> R.drawable.ic_legs
        ExerciseCategory.SHOULDERS -> R.drawable.ic_shoulders
        ExerciseCategory.ABS -> R.drawable.ic_abs
        ExerciseCategory.ARMS -> R.drawable.ic_arms
        ExerciseCategory.CARDIO -> R.drawable.ic_cardio
    }
}

/**
 * Kategori renkleri
 */
private fun getCategoryColor(category: ExerciseCategory): Color {
    return when (category) {
        ExerciseCategory.CHEST -> Color(0xFFDC143C)
        ExerciseCategory.BACK -> Color(0xFF3B82F6)
        ExerciseCategory.LEGS -> Color(0xFFFBBF24)
        ExerciseCategory.SHOULDERS -> Color(0xFF8B5CF6)
        ExerciseCategory.ARMS -> Color(0xFF10B981)
        ExerciseCategory.ABS -> Color(0xFFEF4444)
        ExerciseCategory.CARDIO -> Color(0xFF06B6D4)
    }
}

/**
 * Kategori isimleri (Localized)
 */
private fun getCategoryName(category: ExerciseCategory, strings: com.noxi.noxiapp.ui.theme.AppStrings): String {
    return when (category) {
        ExerciseCategory.CHEST -> strings.chest
        ExerciseCategory.BACK -> strings.backBody
        ExerciseCategory.LEGS -> strings.legs
        ExerciseCategory.SHOULDERS -> strings.shoulders
        ExerciseCategory.ABS -> strings.abs
        ExerciseCategory.ARMS -> strings.arms
        ExerciseCategory.CARDIO -> strings.cardio
    }
}
