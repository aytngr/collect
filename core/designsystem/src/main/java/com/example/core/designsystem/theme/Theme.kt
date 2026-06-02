package com.example.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val ink: Color,
    val sub: Color,
    val faint: Color,
    val line: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val danger: Color,
)

val LightAppColors = AppColors(
    bg         = Bg,
    surface    = Surface,
    surfaceAlt = SurfaceAlt,
    ink        = Ink,
    sub        = Sub,
    faint      = Faint,
    line       = Line,
    accent     = Accent,
    accentSoft = AccentSoft,
    onAccent   = OnAccent,
    danger     = Danger,
)

val DarkAppColors = AppColors(
    bg         = Bg,
    surface    = Surface,
    surfaceAlt = SurfaceAlt,
    ink        = Ink,
    sub        = Sub,
    faint      = Faint,
    line       = Line,
    accent     = Accent,
    accentSoft = AccentSoft,
    onAccent   = OnAccent,
    danger     = Danger,
)

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("AppColors not provided. Did you forget to wrap your UI in TaskFlowTheme?")
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val materialColorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
//            colorScheme = materialColorScheme,
            content     = content,
        )
    }
}