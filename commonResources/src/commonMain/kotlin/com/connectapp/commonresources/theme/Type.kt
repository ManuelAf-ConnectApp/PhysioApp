package com.connectapp.commonresources.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseline = Typography()

val ConnectAppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = baseline.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = baseline.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.25).sp),
    headlineSmall = baseline.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = baseline.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = baseline.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = baseline.bodyLarge,
    bodyMedium = baseline.bodyMedium,
    bodySmall = baseline.bodySmall,
    labelLarge = baseline.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = baseline.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = baseline.labelSmall.copy(fontWeight = FontWeight.SemiBold),
)
