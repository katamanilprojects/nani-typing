package com.manacdc.nanityping1

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MenuCard {
    LEVEL_1,
    SETTINGS
}

/**
 * Minimalist TV-first Main Menu.
 * Features large tactile cards navigable by physical keyboard arrow keys or remote D-pad.
 */
@Composable
fun MainMenuScreen(
    selectedCard: MenuCard,
    onSelectCard: (MenuCard) -> Unit,
    onLaunchCard: (MenuCard) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDD0)) // Warm cream canvas
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val isNarrow = maxWidth < 700.dp
        val scrollState = rememberScrollState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            // App Title
            Text(
                text = "NANI TYPING",
                fontSize = if (isNarrow) 40.sp else 52.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0B1B3D),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Minimalist Phonics & Engineering Typer",
                fontSize = if (isNarrow) 15.sp else 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 6.dp, bottom = if (isNarrow) 24.dp else 44.dp)
            )

            // Cards Row / Column
            if (isNarrow) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuCardItem(
                        badge = "LEVEL 1",
                        title = "Free Typing",
                        description = "Tactile Phonics, Words\n& Kinetic Physics",
                        prompt = "Press Enter or Start Typing →",
                        isSelected = selectedCard == MenuCard.LEVEL_1,
                        onClick = {
                            onSelectCard(MenuCard.LEVEL_1)
                            onLaunchCard(MenuCard.LEVEL_1)
                        }
                    )

                    MenuCardItem(
                        badge = "PREFERENCES",
                        title = "Settings",
                        description = "Voice speed, pitch\n& photo storage",
                        prompt = "Press Enter to Configure →",
                        isSelected = selectedCard == MenuCard.SETTINGS,
                        onClick = {
                            onSelectCard(MenuCard.SETTINGS)
                            onLaunchCard(MenuCard.SETTINGS)
                        }
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuCardItem(
                        badge = "LEVEL 1",
                        title = "Free Typing",
                        description = "Tactile Phonics, Words\n& Kinetic Physics",
                        prompt = "Press Enter or Start Typing →",
                        isSelected = selectedCard == MenuCard.LEVEL_1,
                        onClick = {
                            onSelectCard(MenuCard.LEVEL_1)
                            onLaunchCard(MenuCard.LEVEL_1)
                        }
                    )

                    MenuCardItem(
                        badge = "PREFERENCES",
                        title = "Settings",
                        description = "Voice speed, pitch\n& photo storage",
                        prompt = "Press Enter to Configure →",
                        isSelected = selectedCard == MenuCard.SETTINGS,
                        onClick = {
                            onSelectCard(MenuCard.SETTINGS)
                            onLaunchCard(MenuCard.SETTINGS)
                        }
                    )
                }
            }

            // Keyboard navigation cue
            Text(
                text = "[ ← → Arrow Keys to select • Enter to start ]",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = if (isNarrow) 24.dp else 40.dp)
            )
        }
    }
}

@Composable
private fun MenuCardItem(
    badge: String,
    title: String,
    description: String,
    prompt: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "CardScale"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = if (isSelected) 12.dp else 2.dp,
        modifier = Modifier
            .width(320.dp)
            .height(230.dp)
            .scale(scale)
            .border(
                width = if (isSelected) 3.5.dp else 1.dp,
                color = if (isSelected) Color(0xFFFF4500) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top Badge
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (isSelected) Color(0xFFFF4500).copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = badge,
                        color = if (isSelected) Color(0xFFFF4500) else Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                // Title
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF0B1B3D),
                    modifier = Modifier.padding(top = 10.dp)
                )

                // Description
                Text(
                    text = description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Bottom Prompt
            Text(
                text = prompt,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = if (isSelected) Color(0xFFFF4500) else Color(0xFF94A3B8)
            )
        }
    }
}
