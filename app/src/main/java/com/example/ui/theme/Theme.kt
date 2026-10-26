package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OfficeColorScheme = lightColorScheme(
    primary = OfficeBluePrimary,
    onPrimary = Color.White,
    primaryContainer = OfficeBlueDark,
    onPrimaryContainer = Color.White,
    secondary = OfficeAccent,
    onSecondary = Color.White,
    background = WorkspaceBackground,
    onBackground = TextPrimary,
    surface = PaperWhite,
    onSurface = TextPrimary,
    surfaceVariant = RibbonBackground,
    onSurfaceVariant = TextSecondary,
    outline = RibbonBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OfficeColorScheme,
        typography = Typography,
        content = content
    )
}
