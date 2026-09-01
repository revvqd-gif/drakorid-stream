package com.drakorid.stream.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Brand-seeded fallback scheme. Used when dynamic color is unavailable
// (API <31) or when the user has disabled dynamic color.
private val BrandLightScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = OnBrandRed,
    primaryContainer = BrandRedLight,
    onPrimaryContainer = OnBrandRedLight,
    secondary = PremiumPurple,
    onSecondary = OnBrandRed,
    secondaryContainer = PremiumPurpleContainer,
    onSecondaryContainer = OnPremiumPurpleContainer,
    tertiary = GoldPremium,
    onTertiary = OnBrandRed,
    tertiaryContainer = GoldPremiumContainer,
    onTertiaryContainer = OnGoldPremiumContainer,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLowLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val BrandDarkScheme = darkColorScheme(
    primary = BrandRedLight,
    onPrimary = OnBrandRedLight,
    primaryContainer = BrandRedDark,
    onPrimaryContainer = BrandRedLight,
    secondary = PremiumPurple,
    onSecondary = OnBrandRedDark,
    secondaryContainer = PremiumPurpleDark,
    onSecondaryContainer = PremiumPurpleContainer,
    tertiary = GoldPremium,
    onTertiary = OnGoldPremiumContainer,
    tertiaryContainer = OnGoldPremiumContainer,
    onTertiaryContainer = GoldPremiumContainer,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

@Composable
fun DrakoridStreamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BrandDarkScheme
        else -> BrandLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DrakoridTypography,
        shapes = DrakoridShapes,
        content = content,
    )
}