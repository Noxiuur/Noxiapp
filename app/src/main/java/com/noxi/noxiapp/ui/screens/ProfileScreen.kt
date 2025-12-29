package com.noxi.noxiapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.*
import com.noxi.noxiapp.ui.theme.LocalStrings
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onThemeChange: (Boolean) -> Unit = {},
    currentLanguage: String = "TR",
    onLanguageChange: (String) -> Unit = {},
    userEmail: String = "",
    onLogout: () -> Unit = {}
) {
    // Strings
    val context = androidx.compose.ui.platform.LocalContext.current
    val strings = LocalStrings.current
    val dao = remember { com.noxi.noxiapp.data.local.AppDatabase.getDatabase(context).userProfileDao() }
    
    // State
    var currentScreen by remember { mutableStateOf("MAIN") }
    var userProfile by remember { mutableStateOf<com.noxi.noxiapp.data.UserProfile?>(null) }
    
    // Load Profile Data
    LaunchedEffect(userEmail) {
        if (userEmail.isNotBlank()) {
            dao.getUserProfile(userEmail).collect { profile ->
                userProfile = profile
            }
        }
    }

    // Sub-screens
    if (currentScreen == "INFO") {
        ProfileInfoScreen(
            userEmail = userEmail,
            onBack = { currentScreen = "MAIN" }
        )
        return
    } else if (currentScreen == "ACHIEVEMENTS") {
        AchievementsScreen(
            userEmail = userEmail,
            onBack = { currentScreen = "MAIN" }
        )
        return
    }

    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // --- Üst Bölüm (Avatar ve İsim) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, contentColor, CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                val currentProfile = userProfile
                if (currentProfile?.photoUri != null) {
                    AsyncImage(
                        model = currentProfile.photoUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // İsim & Email
            Column {
                val currentProfile = userProfile
                Text(
                    text = currentProfile?.name?.ifBlank { null } ?: userEmail.takeIf { it.isNotBlank() } ?: strings.profileName,
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!currentProfile?.phoneNumber.isNullOrBlank()) {
                    Text(
                        text = currentProfile!!.phoneNumber,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Divider(color = contentColor, thickness = 2.dp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Menü Listesi ---
        
        // Profil Bilgileri
        ProfileMenuItem(
            text = strings.profileInfo, 
            textColor = contentColor,
            onClick = { currentScreen = "INFO" }
        )
        
        // Başarımlar
        ProfileMenuItem(
            text = strings.achievements, 
            textColor = contentColor,
            onClick = { currentScreen = "ACHIEVEMENTS" }
        )
        
        // Temalar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.themes,
                color = contentColor,
                fontSize = 16.sp
            )
            
            // Switch for Dark/Light Mode
            androidx.compose.material3.Switch(
                checked = isDarkTheme,
                onCheckedChange = onThemeChange,
                thumbContent = {
                    if (isDarkTheme) {
                        Text("🌙", fontSize = 10.sp)
                    } else {
                        Text("☀️", fontSize = 10.sp)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.DarkGray, // Koyu taraf
                    uncheckedThumbColor = Color.Yellow,
                    uncheckedTrackColor = Color.LightGray // Açık taraf
                )
            )
        }
        
        // Dil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.language,
                color = contentColor,
                fontSize = 16.sp
            )
            
            // Language Toggle Button
            androidx.compose.material3.Button(
                onClick = { 
                    val newLang = if (currentLanguage == "TR") "ENG" else "TR"
                    onLanguageChange(newLang)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFE0E0E0),
                    contentColor = contentColor
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(text = currentLanguage)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Çıkış Yap Butonu
        androidx.compose.material3.Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC143C),
                contentColor = Color.White
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(
                text = strings.logout,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    text: String,
    textColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp
        )
    }
}

