package com.qodein.qode.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.qodein.qode.ui.theme.backgroundDark
import com.qodein.qode.ui.theme.backgroundDarkHighContrast
import com.qodein.qode.ui.theme.backgroundDarkMediumContrast
import com.qodein.qode.ui.theme.backgroundLight
import com.qodein.qode.ui.theme.backgroundLightHighContrast
import com.qodein.qode.ui.theme.backgroundLightMediumContrast
import com.qodein.qode.ui.theme.errorContainerDark
import com.qodein.qode.ui.theme.errorContainerDarkHighContrast
import com.qodein.qode.ui.theme.errorContainerDarkMediumContrast
import com.qodein.qode.ui.theme.errorContainerLight
import com.qodein.qode.ui.theme.errorContainerLightHighContrast
import com.qodein.qode.ui.theme.errorContainerLightMediumContrast
import com.qodein.qode.ui.theme.errorDark
import com.qodein.qode.ui.theme.errorDarkHighContrast
import com.qodein.qode.ui.theme.errorDarkMediumContrast
import com.qodein.qode.ui.theme.errorLight
import com.qodein.qode.ui.theme.errorLightHighContrast
import com.qodein.qode.ui.theme.errorLightMediumContrast
import com.qodein.qode.ui.theme.inverseOnSurfaceDark
import com.qodein.qode.ui.theme.inverseOnSurfaceDarkHighContrast
import com.qodein.qode.ui.theme.inverseOnSurfaceDarkMediumContrast
import com.qodein.qode.ui.theme.inverseOnSurfaceLight
import com.qodein.qode.ui.theme.inverseOnSurfaceLightHighContrast
import com.qodein.qode.ui.theme.inverseOnSurfaceLightMediumContrast
import com.qodein.qode.ui.theme.inversePrimaryDark
import com.qodein.qode.ui.theme.inversePrimaryDarkHighContrast
import com.qodein.qode.ui.theme.inversePrimaryDarkMediumContrast
import com.qodein.qode.ui.theme.inversePrimaryLight
import com.qodein.qode.ui.theme.inversePrimaryLightHighContrast
import com.qodein.qode.ui.theme.inversePrimaryLightMediumContrast
import com.qodein.qode.ui.theme.inverseSurfaceDark
import com.qodein.qode.ui.theme.inverseSurfaceDarkHighContrast
import com.qodein.qode.ui.theme.inverseSurfaceDarkMediumContrast
import com.qodein.qode.ui.theme.inverseSurfaceLight
import com.qodein.qode.ui.theme.inverseSurfaceLightHighContrast
import com.qodein.qode.ui.theme.inverseSurfaceLightMediumContrast
import com.qodein.qode.ui.theme.onBackgroundDark
import com.qodein.qode.ui.theme.onBackgroundDarkHighContrast
import com.qodein.qode.ui.theme.onBackgroundDarkMediumContrast
import com.qodein.qode.ui.theme.onBackgroundLight
import com.qodein.qode.ui.theme.onBackgroundLightHighContrast
import com.qodein.qode.ui.theme.onBackgroundLightMediumContrast
import com.qodein.qode.ui.theme.onErrorContainerDark
import com.qodein.qode.ui.theme.onErrorContainerDarkHighContrast
import com.qodein.qode.ui.theme.onErrorContainerDarkMediumContrast
import com.qodein.qode.ui.theme.onErrorContainerLight
import com.qodein.qode.ui.theme.onErrorContainerLightHighContrast
import com.qodein.qode.ui.theme.onErrorContainerLightMediumContrast
import com.qodein.qode.ui.theme.onErrorDark
import com.qodein.qode.ui.theme.onErrorDarkHighContrast
import com.qodein.qode.ui.theme.onErrorDarkMediumContrast
import com.qodein.qode.ui.theme.onErrorLight
import com.qodein.qode.ui.theme.onErrorLightHighContrast
import com.qodein.qode.ui.theme.onErrorLightMediumContrast
import com.qodein.qode.ui.theme.onPrimaryContainerDark
import com.qodein.qode.ui.theme.onPrimaryContainerDarkHighContrast
import com.qodein.qode.ui.theme.onPrimaryContainerDarkMediumContrast
import com.qodein.qode.ui.theme.onPrimaryContainerLight
import com.qodein.qode.ui.theme.onPrimaryContainerLightHighContrast
import com.qodein.qode.ui.theme.onPrimaryContainerLightMediumContrast
import com.qodein.qode.ui.theme.onPrimaryDark
import com.qodein.qode.ui.theme.onPrimaryDarkHighContrast
import com.qodein.qode.ui.theme.onPrimaryDarkMediumContrast
import com.qodein.qode.ui.theme.onPrimaryLight
import com.qodein.qode.ui.theme.onPrimaryLightHighContrast
import com.qodein.qode.ui.theme.onPrimaryLightMediumContrast
import com.qodein.qode.ui.theme.onSecondaryContainerDark
import com.qodein.qode.ui.theme.onSecondaryContainerDarkHighContrast
import com.qodein.qode.ui.theme.onSecondaryContainerDarkMediumContrast
import com.qodein.qode.ui.theme.onSecondaryContainerLight
import com.qodein.qode.ui.theme.onSecondaryContainerLightHighContrast
import com.qodein.qode.ui.theme.onSecondaryContainerLightMediumContrast
import com.qodein.qode.ui.theme.onSecondaryDark
import com.qodein.qode.ui.theme.onSecondaryDarkHighContrast
import com.qodein.qode.ui.theme.onSecondaryDarkMediumContrast
import com.qodein.qode.ui.theme.onSecondaryLight
import com.qodein.qode.ui.theme.onSecondaryLightHighContrast
import com.qodein.qode.ui.theme.onSecondaryLightMediumContrast
import com.qodein.qode.ui.theme.onSurfaceDark
import com.qodein.qode.ui.theme.onSurfaceDarkHighContrast
import com.qodein.qode.ui.theme.onSurfaceDarkMediumContrast
import com.qodein.qode.ui.theme.onSurfaceLight
import com.qodein.qode.ui.theme.onSurfaceLightHighContrast
import com.qodein.qode.ui.theme.onSurfaceLightMediumContrast
import com.qodein.qode.ui.theme.onSurfaceVariantDark
import com.qodein.qode.ui.theme.onSurfaceVariantDarkHighContrast
import com.qodein.qode.ui.theme.onSurfaceVariantDarkMediumContrast
import com.qodein.qode.ui.theme.onSurfaceVariantLight
import com.qodein.qode.ui.theme.onSurfaceVariantLightHighContrast
import com.qodein.qode.ui.theme.onSurfaceVariantLightMediumContrast
import com.qodein.qode.ui.theme.onTertiaryContainerDark
import com.qodein.qode.ui.theme.onTertiaryContainerDarkHighContrast
import com.qodein.qode.ui.theme.onTertiaryContainerDarkMediumContrast
import com.qodein.qode.ui.theme.onTertiaryContainerLight
import com.qodein.qode.ui.theme.onTertiaryContainerLightHighContrast
import com.qodein.qode.ui.theme.onTertiaryContainerLightMediumContrast
import com.qodein.qode.ui.theme.onTertiaryDark
import com.qodein.qode.ui.theme.onTertiaryDarkHighContrast
import com.qodein.qode.ui.theme.onTertiaryDarkMediumContrast
import com.qodein.qode.ui.theme.onTertiaryLight
import com.qodein.qode.ui.theme.onTertiaryLightHighContrast
import com.qodein.qode.ui.theme.onTertiaryLightMediumContrast
import com.qodein.qode.ui.theme.outlineDark
import com.qodein.qode.ui.theme.outlineDarkHighContrast
import com.qodein.qode.ui.theme.outlineDarkMediumContrast
import com.qodein.qode.ui.theme.outlineLight
import com.qodein.qode.ui.theme.outlineLightHighContrast
import com.qodein.qode.ui.theme.outlineLightMediumContrast
import com.qodein.qode.ui.theme.outlineVariantDark
import com.qodein.qode.ui.theme.outlineVariantDarkHighContrast
import com.qodein.qode.ui.theme.outlineVariantDarkMediumContrast
import com.qodein.qode.ui.theme.outlineVariantLight
import com.qodein.qode.ui.theme.outlineVariantLightHighContrast
import com.qodein.qode.ui.theme.outlineVariantLightMediumContrast
import com.qodein.qode.ui.theme.primaryContainerDark
import com.qodein.qode.ui.theme.primaryContainerDarkHighContrast
import com.qodein.qode.ui.theme.primaryContainerDarkMediumContrast
import com.qodein.qode.ui.theme.primaryContainerLight
import com.qodein.qode.ui.theme.primaryContainerLightHighContrast
import com.qodein.qode.ui.theme.primaryContainerLightMediumContrast
import com.qodein.qode.ui.theme.primaryDark
import com.qodein.qode.ui.theme.primaryDarkHighContrast
import com.qodein.qode.ui.theme.primaryDarkMediumContrast
import com.qodein.qode.ui.theme.primaryLight
import com.qodein.qode.ui.theme.primaryLightHighContrast
import com.qodein.qode.ui.theme.primaryLightMediumContrast
import com.qodein.qode.ui.theme.scrimDark
import com.qodein.qode.ui.theme.scrimDarkHighContrast
import com.qodein.qode.ui.theme.scrimDarkMediumContrast
import com.qodein.qode.ui.theme.scrimLight
import com.qodein.qode.ui.theme.scrimLightHighContrast
import com.qodein.qode.ui.theme.scrimLightMediumContrast
import com.qodein.qode.ui.theme.secondaryContainerDark
import com.qodein.qode.ui.theme.secondaryContainerDarkHighContrast
import com.qodein.qode.ui.theme.secondaryContainerDarkMediumContrast
import com.qodein.qode.ui.theme.secondaryContainerLight
import com.qodein.qode.ui.theme.secondaryContainerLightHighContrast
import com.qodein.qode.ui.theme.secondaryContainerLightMediumContrast
import com.qodein.qode.ui.theme.secondaryDark
import com.qodein.qode.ui.theme.secondaryDarkHighContrast
import com.qodein.qode.ui.theme.secondaryDarkMediumContrast
import com.qodein.qode.ui.theme.secondaryLight
import com.qodein.qode.ui.theme.secondaryLightHighContrast
import com.qodein.qode.ui.theme.secondaryLightMediumContrast
import com.qodein.qode.ui.theme.surfaceBrightDark
import com.qodein.qode.ui.theme.surfaceBrightDarkHighContrast
import com.qodein.qode.ui.theme.surfaceBrightDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceBrightLight
import com.qodein.qode.ui.theme.surfaceBrightLightHighContrast
import com.qodein.qode.ui.theme.surfaceBrightLightMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerDark
import com.qodein.qode.ui.theme.surfaceContainerDarkHighContrast
import com.qodein.qode.ui.theme.surfaceContainerDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerHighDark
import com.qodein.qode.ui.theme.surfaceContainerHighDarkHighContrast
import com.qodein.qode.ui.theme.surfaceContainerHighDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerHighLight
import com.qodein.qode.ui.theme.surfaceContainerHighLightHighContrast
import com.qodein.qode.ui.theme.surfaceContainerHighLightMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerHighestDark
import com.qodein.qode.ui.theme.surfaceContainerHighestDarkHighContrast
import com.qodein.qode.ui.theme.surfaceContainerHighestDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerHighestLight
import com.qodein.qode.ui.theme.surfaceContainerHighestLightHighContrast
import com.qodein.qode.ui.theme.surfaceContainerHighestLightMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerLight
import com.qodein.qode.ui.theme.surfaceContainerLightHighContrast
import com.qodein.qode.ui.theme.surfaceContainerLightMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerLowDark
import com.qodein.qode.ui.theme.surfaceContainerLowDarkHighContrast
import com.qodein.qode.ui.theme.surfaceContainerLowDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerLowLight
import com.qodein.qode.ui.theme.surfaceContainerLowLightHighContrast
import com.qodein.qode.ui.theme.surfaceContainerLowLightMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerLowestDark
import com.qodein.qode.ui.theme.surfaceContainerLowestDarkHighContrast
import com.qodein.qode.ui.theme.surfaceContainerLowestDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceContainerLowestLight
import com.qodein.qode.ui.theme.surfaceContainerLowestLightHighContrast
import com.qodein.qode.ui.theme.surfaceContainerLowestLightMediumContrast
import com.qodein.qode.ui.theme.surfaceDark
import com.qodein.qode.ui.theme.surfaceDarkHighContrast
import com.qodein.qode.ui.theme.surfaceDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceDimDark
import com.qodein.qode.ui.theme.surfaceDimDarkHighContrast
import com.qodein.qode.ui.theme.surfaceDimDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceDimLight
import com.qodein.qode.ui.theme.surfaceDimLightHighContrast
import com.qodein.qode.ui.theme.surfaceDimLightMediumContrast
import com.qodein.qode.ui.theme.surfaceLight
import com.qodein.qode.ui.theme.surfaceLightHighContrast
import com.qodein.qode.ui.theme.surfaceLightMediumContrast
import com.qodein.qode.ui.theme.surfaceVariantDark
import com.qodein.qode.ui.theme.surfaceVariantDarkHighContrast
import com.qodein.qode.ui.theme.surfaceVariantDarkMediumContrast
import com.qodein.qode.ui.theme.surfaceVariantLight
import com.qodein.qode.ui.theme.surfaceVariantLightHighContrast
import com.qodein.qode.ui.theme.surfaceVariantLightMediumContrast
import com.qodein.qode.ui.theme.tertiaryContainerDark
import com.qodein.qode.ui.theme.tertiaryContainerDarkHighContrast
import com.qodein.qode.ui.theme.tertiaryContainerDarkMediumContrast
import com.qodein.qode.ui.theme.tertiaryContainerLight
import com.qodein.qode.ui.theme.tertiaryContainerLightHighContrast
import com.qodein.qode.ui.theme.tertiaryContainerLightMediumContrast
import com.qodein.qode.ui.theme.tertiaryDark
import com.qodein.qode.ui.theme.tertiaryDarkHighContrast
import com.qodein.qode.ui.theme.tertiaryDarkMediumContrast
import com.qodein.qode.ui.theme.tertiaryLight
import com.qodein.qode.ui.theme.tertiaryLightHighContrast
import com.qodein.qode.ui.theme.tertiaryLightMediumContrast

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun QodeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      
      darkTheme -> darkScheme
      else -> lightScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = QodeTypography,
    content = content
  )
}

