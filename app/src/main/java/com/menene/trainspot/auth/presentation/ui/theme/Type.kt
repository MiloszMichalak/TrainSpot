package com.menene.trainspot.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal
    )
)
