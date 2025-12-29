package com.noxi.noxiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noxi.noxiapp.ui.theme.LocalStrings

data class Achievement(
    val title: String,
    val description: String,
    val isEarned: Boolean,
    val icon: ImageVector,
    val iconColor: Color? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    userEmail: String,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.noxi.noxiapp.data.local.AppDatabase.getDatabase(context) }
    val dao = db.unlockedAchievementDao()
    
    val unlockedList by dao.getUnlockedAchievements(userEmail).collectAsState(initial = emptyList())
    val unlockedIds = unlockedList.map { it.achievementId }.toSet()

    val achievements = listOf(
        Achievement(
            title = "They dont know me son",
            description = "İlk hedefini ve ilk antrenman programını kaydet",
            isEarned = unlockedIds.contains("they_dont_know_me_son"),
            icon = Icons.Default.Person
        ),
        Achievement(
            title = "A-Train",
            description = "100bin adım at",
            isEarned = unlockedIds.contains("a_train"),
            icon = Icons.Default.DirectionsRun,
            iconColor = Color.Blue // Mavi giysili adam temsili
        ),
        Achievement(
            title = "Aquamen",
            description = "Bir hafta boyunca su iç",
            isEarned = unlockedIds.contains("aquamen"),
            icon = Icons.Default.Pool
        ),
        Achievement(
            title = "ZyZ",
            description = "Bir ay boyunca spor yap",
            isEarned = unlockedIds.contains("zyz"),
            icon = Icons.Default.FitnessCenter
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.achievements) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(achievements) { achievement ->
                AchievementItem(
                    achievement = achievement
                )
            }
        }
    }
}

@Composable
fun AchievementItem(
    achievement: Achievement
) {
    // Solukluk ve Renk Ayarı
    val isEarned = achievement.isEarned
    val containerColor = if (isEarned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentAlpha = if (isEarned) 1f else 0.5f
    
    // Icon color: custom if provided, otherwise Primary if earned, otherwise Gray
    val finalIconColor = if (isEarned) {
        achievement.iconColor ?: MaterialTheme.colorScheme.primary
    } else {
        Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isEarned) 1f else 0.7f),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = finalIconColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    // If earned, use the specific icon. If not, use the Help/QuestionMark icon.
                    imageVector = if (isEarned) achievement.icon else Icons.Default.Help,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = finalIconColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }

            // Kazanıldıysa Kırmızı Tik
            if (isEarned) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Earned",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
