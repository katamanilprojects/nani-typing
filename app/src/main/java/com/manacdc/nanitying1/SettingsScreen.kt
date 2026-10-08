package com.manacdc.nanityping1

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SettingsRow {
    SPEED,
    PITCH,
    LOCALE,
    RELOAD_STORAGE,
    BACK_TO_MENU
}

/**
 * Minimalist Settings Screen for TV keyboard navigation.
 * Allows adjusting voice speed, pitch, accent, and refreshing image storage.
 */
@Composable
fun SettingsScreen(
    selectedRow: SettingsRow,
    photoCount: Int,
    speed: Float,
    pitch: Float,
    localeCode: String,
    onCycleSpeed: () -> Unit,
    onCyclePitch: () -> Unit,
    onCycleLocale: () -> Unit,
    onReloadPhotos: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDD0))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 640.dp)
        ) {
            Text(
                text = "SETTINGS",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0B1B3D),
                letterSpacing = 1.sp
            )

            Text(
                text = "Voice, Pitch & Storage Preferences",
                fontSize = 15.sp,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Voice Speed Row
                val speedLabel = AppSettings.SPEED_OPTIONS.find { it.first == speed }?.second
                    ?: "${speed}x"
                SettingItemCard(
                    title = "Speech Speed",
                    value = speedLabel,
                    isSelected = selectedRow == SettingsRow.SPEED,
                    onClick = onCycleSpeed
                )

                // 2. Voice Pitch Row
                val pitchLabel = AppSettings.PITCH_OPTIONS.find { it.first == pitch }?.second
                    ?: "${pitch}x"
                SettingItemCard(
                    title = "Voice Pitch",
                    value = pitchLabel,
                    isSelected = selectedRow == SettingsRow.PITCH,
                    onClick = onCyclePitch
                )

                // 3. Voice Language / Accent Row
                val localeLabel = AppSettings.LOCALE_OPTIONS.find { it.first == localeCode }?.second
                    ?: localeCode
                SettingItemCard(
                    title = "Voice Accent",
                    value = localeLabel,
                    isSelected = selectedRow == SettingsRow.LOCALE,
                    onClick = onCycleLocale
                )

                // 4. Photos / USB Storage Row
                SettingItemCard(
                    title = "Real-World Photos",
                    value = "$photoCount photos indexed • Reload",
                    isSelected = selectedRow == SettingsRow.RELOAD_STORAGE,
                    onClick = onReloadPhotos
                )

                // 5. Back to Menu Row
                SettingItemCard(
                    title = "← Return to Main Menu",
                    value = "Press Enter or Esc",
                    isSelected = selectedRow == SettingsRow.BACK_TO_MENU,
                    isAction = true,
                    onClick = onBackToMenu
                )
            }

            Text(
                text = "[ ↑ ↓ to navigate • Enter or ← → to change • Esc to return ]",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 28.dp)
            )
        }
    }
}

@Composable
private fun SettingItemCard(
    title: String,
    value: String,
    isSelected: Boolean,
    isAction: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAction && isSelected) Color(0xFF0B1B3D) else Color.White,
        shadowElevation = if (isSelected) 6.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color(0xFFFF4500) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = if (isAction && isSelected) Color.White else Color(0xFF0B1B3D)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = if (isAction && isSelected) Color(0xFFFF4500) else if (isSelected) Color(0xFFFF4500) else Color(0xFF64748B)
            )
        }
    }
}
