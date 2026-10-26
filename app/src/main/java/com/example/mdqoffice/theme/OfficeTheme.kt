package com.example.mdqoffice.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val OfficeBluePrimary = Color(0xFF185ABD)
val OfficeBlueDark = Color(0xFF103F91)
val OfficeBlueHeader = Color(0xFF0C2E63)
val OfficeBlueStatusBar = Color(0xFFE5E8EC)
val OfficeStatusBarGray = Color(0xFFE5E8EC)
val OfficeAccent = Color(0xFF2B78E4)

val RibbonBackground = Color(0xFFF3F5F8)
val RibbonBorder = Color(0xFFD6DAE1)
val WorkspaceBackground = Color(0xFFE5E8EC)
val RulerBackground = Color(0xFFF8F9FA)
val RulerBorder = Color(0xFFCBD0D8)

val PaperWhite = Color(0xFFFFFFFF)
val PaperBorder = Color(0xFFC7CBD1)

val TextPrimary = Color(0xFF202124)
val TextSecondary = Color(0xFF5F6368)
val TextDisabled = Color(0xFF9AA0A6)

val RibbonActiveTab = Color(0xFFFFFFFF)
val RibbonInactiveText = Color(0xFFFFFFFF)
val SelectionHighlight = Color(0xFFCCE8FF)

val OfficeTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

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
fun MdqOfficeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OfficeColorScheme,
        typography = OfficeTypography,
        content = content
    )
}
