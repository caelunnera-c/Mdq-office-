package com.example.mdqoffice.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdqoffice.model.*
import com.example.mdqoffice.theme.*

@Composable
fun DesktopRibbonBar(
    activeTab: RibbonTab,
    onTabSelected: (RibbonTab) -> Unit,
    selectedParagraph: DocParagraph?,
    document: MdqDocument,
    // Top Quick Access Actions
    canUndo: Boolean,
    canRedo: Boolean,
    openDocsCount: Int = 1,
    onDocSwitcherClick: () -> Unit,
    onQuickSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNewDoc: () -> Unit,
    onPrint: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    // Home actions
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrike: () -> Unit,
    onToggleSubscript: () -> Unit,
    onToggleSuperscript: () -> Unit,
    onChangeCase: (CaseType) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onSetFontFamily: (String) -> Unit,
    onSetFontColor: (Long) -> Unit,
    onSetHighlightColor: (Long?) -> Unit,
    onClearFormatting: () -> Unit,
    onCopyFormat: () -> Unit,
    onPasteFormat: () -> Unit,
    onToggleFormatPainter: () -> Unit,
    hasCopiedFormat: Boolean,
    onSetAlignment: (DocTextAlign) -> Unit,
    onToggleTextDirection: () -> Unit,
    onSetStyle: (ParagraphStyle) -> Unit,
    onToggleBullet: () -> Unit,
    onSetBulletStyle: (BulletStyle) -> Unit,
    onToggleNumbering: () -> Unit,
    onSetNumberingStyle: (NumberingStyle) -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit,
    onFindClick: () -> Unit,
    onWordCountClick: () -> Unit,
    customFonts: List<Pair<String, String>> = emptyList(),
    onManageCustomFontsClick: () -> Unit,
    // Insert actions
    onInsertPageBreak: () -> Unit,
    onInsertTableClick: () -> Unit,
    onInsertShapeClick: (ShapeType) -> Unit,
    onInsertFootnoteClick: () -> Unit,
    onInsertTocClick: () -> Unit,
    // Layout actions
    onSetMarginsPreset: (DocMargins) -> Unit,
    onSetOrientation: (Orientation) -> Unit,
    onSetPaperSize: (PaperSize) -> Unit,
    onCustomPaperClick: () -> Unit,
    onPageSetupClick: () -> Unit,
    // Review actions
    onAddCommentClick: () -> Unit,
    onToggleTrackChanges: () -> Unit,
    // View actions
    onToggleRuler: () -> Unit,
    onToggleNavPane: () -> Unit,
    onToggleGridlines: () -> Unit,
    onSetViewMode: (ViewMode) -> Unit,
    onResetZoom: () -> Unit,
    showRuler: Boolean,
    showNavPane: Boolean,
    showGridlines: Boolean,
    viewMode: ViewMode,
    // Help actions
    onShortcutsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onMailMergeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OfficeBlueDark)
    ) {
        // Unified Top Tab Bar (Tabs on left, Search in middle-right, Quick Access on far right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(OfficeBlueDark)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Horizontal scrollable Tabs
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Bottom
            ) {
                // File Tab
                Surface(
                    color = if (activeTab == RibbonTab.File) Color(0xFF003D7A) else OfficeBlueDark,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    modifier = Modifier
                        .clickable { onTabSelected(RibbonTab.File) }
                        .testTag("ribbon_tab_file")
                ) {
                    Text(
                        text = "File",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                    )
                }

                val tabs = listOf(
                    RibbonTab.Home to "Home",
                    RibbonTab.Insert to "Insert",
                    RibbonTab.Layout to "Layout",
                    RibbonTab.References to "References",
                    RibbonTab.Mailings to "Mailings",
                    RibbonTab.Review to "Review",
                    RibbonTab.View to "View",
                    RibbonTab.Help to "Help"
                )

                tabs.forEach { (tab, label) ->
                    val isActive = activeTab == tab
                    Surface(
                        color = if (isActive) RibbonBackground else Color.Transparent,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                        modifier = Modifier
                            .clickable { onTabSelected(tab) }
                            .testTag("ribbon_tab_${tab.name.lowercase()}")
                    ) {
                        Text(
                            text = label,
                            color = if (isActive) OfficeBlueDark else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Search box (Cari perintah - moved to tab bar as requested)
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .widthIn(min = 140.dp, max = 220.dp)
                        .clickable { onSearchClick() }
                        .align(Alignment.CenterVertically)
                        .testTag("ribbon_search_command")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cari perintah...",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Access Toolbar on the far right (Save, Undo, Redo, New, Print, Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                // Document Switcher (placed to the left of Save)
                IconButton(
                    onClick = onDocSwitcherClick,
                    modifier = Modifier.size(30.dp).testTag("quick_doc_switcher_btn")
                ) {
                    BadgedBox(
                        badge = {
                            if (openDocsCount > 1) {
                                Badge(
                                    containerColor = Color(0xFFC5221F),
                                    contentColor = Color.White
                                ) {
                                    Text("$openDocsCount", fontSize = 8.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Lembar Kerja Terbuka",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Save
                IconButton(
                    onClick = onQuickSave,
                    modifier = Modifier.size(30.dp).testTag("quick_save_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Simpan",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Undo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(30.dp).testTag("quick_undo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(30.dp).testTag("quick_redo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // New
                IconButton(
                    onClick = onNewDoc,
                    modifier = Modifier.size(30.dp).testTag("quick_new_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Dokumen Baru",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Print
                IconButton(
                    onClick = onPrint,
                    modifier = Modifier.size(30.dp).testTag("quick_print_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Cetak",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Settings (Full-screen & Preferences)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(30.dp).testTag("quick_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Active Ribbon Content Panel
        Surface(
            color = RibbonBackground,
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .border(width = 0.8.dp, color = RibbonBorder)
                .testTag("ribbon_panel")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (activeTab) {
                    RibbonTab.Home -> {
                        HomeRibbonContent(
                            selectedParagraph = selectedParagraph,
                            onToggleBold = onToggleBold,
                            onToggleItalic = onToggleItalic,
                            onToggleUnderline = onToggleUnderline,
                            onToggleStrike = onToggleStrike,
                            onToggleSubscript = onToggleSubscript,
                            onToggleSuperscript = onToggleSuperscript,
                            onChangeCase = onChangeCase,
                            onSetFontSize = onSetFontSize,
                            onIncreaseFontSize = onIncreaseFontSize,
                            onDecreaseFontSize = onDecreaseFontSize,
                            onSetFontFamily = onSetFontFamily,
                            onSetFontColor = onSetFontColor,
                            onSetHighlightColor = onSetHighlightColor,
                            onClearFormatting = onClearFormatting,
                            onCopyFormat = onCopyFormat,
                            onPasteFormat = onPasteFormat,
                            onToggleFormatPainter = onToggleFormatPainter,
                            hasCopiedFormat = hasCopiedFormat,
                            onSetAlignment = onSetAlignment,
                            onToggleTextDirection = onToggleTextDirection,
                            onSetStyle = onSetStyle,
                            onToggleBullet = onToggleBullet,
                            onSetBulletStyle = onSetBulletStyle,
                            onToggleNumbering = onToggleNumbering,
                            onSetNumberingStyle = onSetNumberingStyle,
                            onIncreaseIndent = onIncreaseIndent,
                            onDecreaseIndent = onDecreaseIndent,
                            onFindClick = onFindClick,
                            onWordCountClick = onWordCountClick,
                            onAboutClick = onAboutClick,
                            customFonts = customFonts,
                            onManageCustomFontsClick = onManageCustomFontsClick
                        )
                    }
                    RibbonTab.Insert -> {
                        InsertRibbonContent(
                            onInsertPageBreak = onInsertPageBreak,
                            onInsertTableClick = onInsertTableClick,
                            onInsertShapeClick = onInsertShapeClick,
                            onInsertFootnoteClick = onInsertFootnoteClick,
                            onInsertTocClick = onInsertTocClick
                        )
                    }
                    RibbonTab.Layout -> {
                        LayoutRibbonContent(
                            document = document,
                            onSetMarginsPreset = onSetMarginsPreset,
                            onSetOrientation = onSetOrientation,
                            onSetPaperSize = onSetPaperSize,
                            onCustomPaperClick = onCustomPaperClick,
                            onPageSetupClick = onPageSetupClick,
                            onIncreaseIndent = onIncreaseIndent,
                            onDecreaseIndent = onDecreaseIndent
                        )
                    }
                    RibbonTab.References -> {
                        ReferencesRibbonContent(
                            onInsertTocClick = onInsertTocClick,
                            onInsertFootnoteClick = onInsertFootnoteClick
                        )
                    }
                    RibbonTab.Mailings -> {
                        MailingsRibbonContent(
                            onMailMergeClick = onMailMergeClick
                        )
                    }
                    RibbonTab.Review -> {
                        ReviewRibbonContent(
                            trackChanges = document.trackChangesEnabled,
                            onToggleTrackChanges = onToggleTrackChanges,
                            onAddCommentClick = onAddCommentClick,
                            onWordCountClick = onWordCountClick
                        )
                    }
                    RibbonTab.View -> {
                        ViewRibbonContent(
                            showRuler = showRuler,
                            showNavPane = showNavPane,
                            showGridlines = showGridlines,
                            viewMode = viewMode,
                            onToggleRuler = onToggleRuler,
                            onToggleNavPane = onToggleNavPane,
                            onToggleGridlines = onToggleGridlines,
                            onSetViewMode = onSetViewMode,
                            onResetZoom = onResetZoom
                        )
                    }
                    RibbonTab.Help -> {
                        HelpRibbonContent(
                            onShortcutsClick = onShortcutsClick,
                            onHelpClick = onHelpClick
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Command Group Container
// -------------------------------------------------------------
@Composable
fun RibbonGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }
            Text(
                text = title,
                fontSize = 8.5.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
        Divider(
            color = RibbonBorder,
            modifier = Modifier
                .fillMaxHeight(0.82f)
                .width(1.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

// -------------------------------------------------------------
// Small Ribbon Button
// -------------------------------------------------------------
@Composable
fun RibbonToolButton(
    icon: ImageVector? = null,
    text: String? = null,
    isActive: Boolean = false,
    iconTint: Color? = null,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        color = if (isActive) Color(0xFFCCE8FF) else Color.Transparent,
        shape = RoundedCornerShape(3.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, OfficeBluePrimary) else null,
        modifier = Modifier
            .size(width = if (text != null && icon != null) 64.dp else if (text != null) 36.dp else 26.dp, height = 26.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text ?: "",
                    tint = iconTint ?: if (isActive) OfficeBluePrimary else TextPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
            if (text != null) {
                if (icon != null) Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = text,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) OfficeBluePrimary else TextPrimary
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Large Ribbon Button
// -------------------------------------------------------------
@Composable
fun RibbonLargeButton(
    icon: ImageVector,
    label: String,
    iconTint: Color = OfficeBluePrimary,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight(0.9f)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                lineHeight = 10.sp
            )
        }
    }
}

// -------------------------------------------------------------
// HOME TAB CONTENT
// -------------------------------------------------------------
@Composable
fun HomeRibbonContent(
    selectedParagraph: DocParagraph?,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrike: () -> Unit,
    onToggleSubscript: () -> Unit,
    onToggleSuperscript: () -> Unit,
    onChangeCase: (CaseType) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onSetFontFamily: (String) -> Unit,
    onSetFontColor: (Long) -> Unit,
    onSetHighlightColor: (Long?) -> Unit,
    onClearFormatting: () -> Unit,
    onCopyFormat: () -> Unit,
    onPasteFormat: () -> Unit,
    onToggleFormatPainter: () -> Unit,
    hasCopiedFormat: Boolean,
    onSetAlignment: (DocTextAlign) -> Unit,
    onToggleTextDirection: () -> Unit,
    onSetStyle: (ParagraphStyle) -> Unit,
    onToggleBullet: () -> Unit,
    onSetBulletStyle: (BulletStyle) -> Unit,
    onToggleNumbering: () -> Unit,
    onSetNumberingStyle: (NumberingStyle) -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit,
    onFindClick: () -> Unit,
    onWordCountClick: () -> Unit,
    onAboutClick: () -> Unit,
    customFonts: List<Pair<String, String>> = emptyList(),
    onManageCustomFontsClick: () -> Unit
) {
    val firstRun = selectedParagraph?.runs?.firstOrNull()
    val isBold = firstRun?.bold == true
    val isItalic = firstRun?.italic == true
    val isUnderline = firstRun?.underline == true
    val isStrike = firstRun?.strikethrough == true
    val isSubscript = firstRun?.subscript == true
    val isSuperscript = firstRun?.superscript == true
    val fontSize = firstRun?.fontSize ?: 11f
    val fontFamily = firstRun?.fontFamily ?: "Calibri"
    val align = selectedParagraph?.alignment ?: DocTextAlign.Left
    val isBullet = selectedParagraph?.isBullet == true
    val isNumbered = selectedParagraph?.isNumbered == true
    val isRtl = selectedParagraph?.isRtl == true

    var showFontMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }
    var showColorMenu by remember { mutableStateOf(false) }
    var showHighlightMenu by remember { mutableStateOf(false) }
    var showCaseMenu by remember { mutableStateOf(false) }
    var showBulletMenu by remember { mutableStateOf(false) }
    var showNumberingMenu by remember { mutableStateOf(false) }

    // 1. Clipboard Group (Paste, Cut, Copy, and Yellow Format Painter / Sapu Format)
    RibbonGroup(title = "Clipboard") {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.ContentPaste,
                label = "Paste",
                onClick = onPasteFormat,
                testTag = "ribbon_paste"
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RibbonToolButton(icon = Icons.Default.ContentCut, text = "Cut", onClick = {}, testTag = "ribbon_cut")
                RibbonToolButton(icon = Icons.Default.ContentCopy, text = "Copy", onClick = {}, testTag = "ribbon_copy")
                // Format Painter with Yellow Brush Icon: toggleable on/off
                RibbonToolButton(
                    icon = Icons.Default.Brush,
                    text = "Sapu",
                    iconTint = Color(0xFFF9A825), // Yellow paintbrush
                    isActive = hasCopiedFormat,
                    onClick = onToggleFormatPainter,
                    testTag = "ribbon_format_painter"
                )
            }
        }
    }

    // 2. Font Group (Extensive 25+ fonts + Arabic + Custom, size, bold, italic, underline, colors, and red box features)
    RibbonGroup(title = "Font") {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Row 1: Font Family Dropdown, Size Dropdown, A+, A-, Clear
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Font Family Dropdown
                Box {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD0D8)),
                        modifier = Modifier
                            .height(24.dp)
                            .width(130.dp)
                            .clickable { showFontMenu = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fontFamily, fontSize = 10.sp, maxLines = 1)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                    DropdownMenu(expanded = showFontMenu, onDismissRequest = { showFontMenu = false }) {
                        val latinFonts = listOf(
                            "Calibri", "Aptos", "Arial", "Times New Roman", "Segoe UI",
                            "Georgia", "Garamond", "Cambria", "Verdana", "Tahoma",
                            "Trebuchet MS", "Century Gothic", "Comic Sans MS", "Courier New",
                            "Consolas", "Book Antiqua", "Palatino", "Impact", "Arial Black", "Rockwell"
                        )
                        val arabicFonts = listOf(
                            "Traditional Arabic", "Amiri", "Scheherazade", "Noto Naskh Arabic",
                            "Almarai", "Lateef", "Cairo", "KFGQPC Uthmanic Script", "Dubai"
                        )
                        DropdownMenuItem(
                            text = { Text("--- FONT LATIN ---", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray) },
                            onClick = {}
                        )
                        latinFonts.forEach { font ->
                            DropdownMenuItem(
                                text = { Text(font, fontSize = 12.sp) },
                                onClick = {
                                    onSetFontFamily(font)
                                    showFontMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("--- FONT ARABIC (خطوط عربية) ---", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF103F91)) },
                            onClick = {}
                        )
                        arabicFonts.forEach { font ->
                            DropdownMenuItem(
                                text = { Text(font, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    onSetFontFamily(font)
                                    showFontMenu = false
                                }
                            )
                        }
                        if (customFonts.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("--- FONT KUSTOM ANDA ---", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC5221F)) },
                                onClick = {}
                            )
                            customFonts.forEach { fontItem ->
                                DropdownMenuItem(
                                    text = { Text(fontItem.first, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        onSetFontFamily(fontItem.first)
                                        showFontMenu = false
                                    }
                                )
                            }
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Text("+ Kelola Font Kustom (.ttf/.otf)...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OfficeBluePrimary) },
                            onClick = {
                                onManageCustomFontsClick()
                                showFontMenu = false
                            }
                        )
                    }
                }

                // Font Size Dropdown
                Box {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD0D8)),
                        modifier = Modifier
                            .height(24.dp)
                            .width(44.dp)
                            .clickable { showSizeMenu = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${fontSize.toInt()}", fontSize = 10.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                    DropdownMenu(expanded = showSizeMenu, onDismissRequest = { showSizeMenu = false }) {
                        listOf(8f, 9f, 10f, 11f, 12f, 14f, 16f, 18f, 20f, 22f, 24f, 26f, 28f, 36f, 48f, 72f).forEach { sz ->
                            DropdownMenuItem(
                                text = { Text("${sz.toInt()}", fontSize = 12.sp) },
                                onClick = {
                                    onSetFontSize(sz)
                                    showSizeMenu = false
                                }
                            )
                        }
                    }
                }

                RibbonToolButton(icon = Icons.Default.FormatSize, text = "+", onClick = onIncreaseFontSize)
                RibbonToolButton(icon = Icons.Default.FormatSize, text = "-", onClick = onDecreaseFontSize)
                RibbonToolButton(icon = Icons.Default.FormatClear, onClick = onClearFormatting, testTag = "ribbon_clear_fmt")
            }

            // Row 2: Bold, Italic, Underline, Strike, Font Color, Highlight, AND RED BOX TOOLS (Subscript, Superscript, Case, Typography)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                RibbonToolButton(icon = Icons.Default.FormatBold, isActive = isBold, onClick = onToggleBold, testTag = "ribbon_bold")
                RibbonToolButton(icon = Icons.Default.FormatItalic, isActive = isItalic, onClick = onToggleItalic, testTag = "ribbon_italic")
                RibbonToolButton(icon = Icons.Default.FormatUnderlined, isActive = isUnderline, onClick = onToggleUnderline, testTag = "ribbon_underline")
                RibbonToolButton(icon = Icons.Default.FormatStrikethrough, isActive = isStrike, onClick = onToggleStrike, testTag = "ribbon_strike")

                // Text Color Dropdown
                Box {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { showColorMenu = true }
                            .testTag("ribbon_font_color")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.FormatColorText, contentDescription = "Font Color", modifier = Modifier.size(15.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(3.dp)
                                    .background(Color(firstRun?.fontColor ?: 0xFF202124))
                            )
                        }
                    }
                    DropdownMenu(expanded = showColorMenu, onDismissRequest = { showColorMenu = false }) {
                        val colors = listOf(
                            "Hitam Otomatis" to 0xFF202124L,
                            "Biru Word" to 0xFF185ABDL,
                            "Biru Gelap" to 0xFF103F91L,
                            "Merah" to 0xFFC00000L,
                            "Hijau Tua" to 0xFF2E7D32L,
                            "Oranye" to 0xFFE65100L,
                            "Ungu" to 0xFF6A1B9AL,
                            "Abu-Abu" to 0xFF5F6368L
                        )
                        colors.forEach { (name, hex) ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(hex))
                                    )
                                },
                                text = { Text(name, fontSize = 12.sp) },
                                onClick = {
                                    onSetFontColor(hex)
                                    showColorMenu = false
                                }
                            )
                        }
                    }
                }

                // Highlight Color Dropdown
                Box {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { showHighlightMenu = true }
                            .testTag("ribbon_highlight_color")
                    ) {
                        Icon(Icons.Default.BorderColor, contentDescription = "Highlight", tint = Color(0xFFF9A825), modifier = Modifier.size(15.dp))
                    }
                    DropdownMenu(expanded = showHighlightMenu, onDismissRequest = { showHighlightMenu = false }) {
                        val highlights = listOf(
                            "Kuning Terang" to 0xFFFFF176L,
                            "Hijau Terang" to 0xFFA5D6A7L,
                            "Sian Terang" to 0xFF80DEEAL,
                            "Merah Muda" to 0xFFF48FB1L,
                            "Tanpa Warna" to null
                        )
                        highlights.forEach { (name, hex) ->
                            DropdownMenuItem(
                                text = { Text(name, fontSize = 12.sp) },
                                onClick = {
                                    onSetHighlightColor(hex)
                                    showHighlightMenu = false
                                }
                            )
                        }
                    }
                }

                // --- RED BOX COMPLETED: Desktop Font formatting tools ---
                // Subscript X₂
                RibbonToolButton(
                    text = "X₂",
                    isActive = isSubscript,
                    onClick = onToggleSubscript,
                    testTag = "ribbon_subscript"
                )

                // Superscript X²
                RibbonToolButton(
                    text = "X²",
                    isActive = isSuperscript,
                    onClick = onToggleSuperscript,
                    testTag = "ribbon_superscript"
                )

                // Change Case Aa▾
                Box {
                    RibbonToolButton(
                        text = "Aa▾",
                        onClick = { showCaseMenu = true },
                        testTag = "ribbon_change_case"
                    )
                    DropdownMenu(expanded = showCaseMenu, onDismissRequest = { showCaseMenu = false }) {
                        CaseType.values().forEach { caseType ->
                            DropdownMenuItem(
                                text = { Text(caseType.displayName, fontSize = 12.sp) },
                                onClick = {
                                    onChangeCase(caseType)
                                    showCaseMenu = false
                                }
                            )
                        }
                    }
                }

                // Text Effects / Typography
                RibbonToolButton(
                    text = "A✧",
                    iconTint = OfficeBluePrimary,
                    onClick = { onChangeCase(CaseType.CapitalizeEach) },
                    testTag = "ribbon_typography_fx"
                )
            }
        }
    }

    // 3. Paragraph Group (Alignments, RTL/LTR Arabic, Bullets & Multi-Numbering)
    RibbonGroup(title = "Paragraph") {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Row 1: Bullets with dropdown, Numbering with dropdown, Indents
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                // Bullet Dropdown
                Box {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RibbonToolButton(icon = Icons.Default.FormatListBulleted, isActive = isBullet, onClick = onToggleBullet, testTag = "ribbon_bullet")
                        Box(
                            modifier = Modifier
                                .size(width = 12.dp, height = 26.dp)
                                .clickable { showBulletMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▾", fontSize = 10.sp, color = TextPrimary)
                        }
                    }
                    DropdownMenu(expanded = showBulletMenu, onDismissRequest = { showBulletMenu = false }) {
                        BulletStyle.values().forEach { bStyle ->
                            DropdownMenuItem(
                                text = { Text("${bStyle.symbol}  ${bStyle.displayName}", fontSize = 12.sp) },
                                onClick = {
                                    onSetBulletStyle(bStyle)
                                    showBulletMenu = false
                                }
                            )
                        }
                    }
                }

                // Numbering Dropdown
                Box {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RibbonToolButton(icon = Icons.Default.FormatListNumbered, isActive = isNumbered, onClick = onToggleNumbering, testTag = "ribbon_numbered")
                        Box(
                            modifier = Modifier
                                .size(width = 12.dp, height = 26.dp)
                                .clickable { showNumberingMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▾", fontSize = 10.sp, color = TextPrimary)
                        }
                    }
                    DropdownMenu(expanded = showNumberingMenu, onDismissRequest = { showNumberingMenu = false }) {
                        NumberingStyle.values().forEach { nStyle ->
                            DropdownMenuItem(
                                text = { Text(nStyle.displayName, fontSize = 12.sp) },
                                onClick = {
                                    onSetNumberingStyle(nStyle)
                                    showNumberingMenu = false
                                }
                            )
                        }
                    }
                }

                RibbonToolButton(icon = Icons.Default.FormatIndentDecrease, onClick = onDecreaseIndent, testTag = "ribbon_indent_dec")
                RibbonToolButton(icon = Icons.Default.FormatIndentIncrease, onClick = onIncreaseIndent, testTag = "ribbon_indent_inc")
            }

            // Row 2: Alignments + RTL / LTR
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                RibbonToolButton(icon = Icons.Default.FormatAlignLeft, isActive = align == DocTextAlign.Left, onClick = { onSetAlignment(DocTextAlign.Left) }, testTag = "ribbon_align_left")
                RibbonToolButton(icon = Icons.Default.FormatAlignCenter, isActive = align == DocTextAlign.Center, onClick = { onSetAlignment(DocTextAlign.Center) }, testTag = "ribbon_align_center")
                RibbonToolButton(icon = Icons.Default.FormatAlignRight, isActive = align == DocTextAlign.Right, onClick = { onSetAlignment(DocTextAlign.Right) }, testTag = "ribbon_align_right")
                RibbonToolButton(icon = Icons.Default.FormatAlignJustify, isActive = align == DocTextAlign.Justify, onClick = { onSetAlignment(DocTextAlign.Justify) }, testTag = "ribbon_align_justify")
                // RTL / LTR Direction
                RibbonToolButton(
                    text = if (isRtl) "←¶" else "¶→",
                    isActive = isRtl,
                    onClick = onToggleTextDirection,
                    testTag = "ribbon_rtl_toggle"
                )
            }
        }
    }

    // 4. Styles Gallery Group
    RibbonGroup(title = "Styles") {
        Row(
            modifier = Modifier.padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val currentStyle = selectedParagraph?.style ?: ParagraphStyle.Normal
            listOf(
                ParagraphStyle.Normal to "Normal",
                ParagraphStyle.Heading1 to "Heading 1",
                ParagraphStyle.Heading2 to "Heading 2",
                ParagraphStyle.Title to "Judul",
                ParagraphStyle.Subtitle to "Subjudul",
                ParagraphStyle.Quote to "Kutipan"
            ).forEach { (style, label) ->
                val isSelected = currentStyle == style
                Surface(
                    color = if (isSelected) Color(0xFFCCE8FF) else Color.White,
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OfficeBluePrimary else Color(0xFFCBD0D8)),
                    modifier = Modifier
                        .width(60.dp)
                        .height(56.dp)
                        .clickable { onSetStyle(style) }
                        .testTag("style_${style.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "AaBbCc",
                            fontSize = if (style == ParagraphStyle.Title) 13.sp else if (style == ParagraphStyle.Heading1) 12.sp else 10.sp,
                            fontWeight = if (style.defaultBold) FontWeight.Bold else FontWeight.Normal,
                            color = if (style == ParagraphStyle.Heading1 || style == ParagraphStyle.Title) OfficeBluePrimary else TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 8.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    // 5. Editing Group
    RibbonGroup(title = "Editing") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            RibbonToolButton(icon = Icons.Default.Search, text = "Cari", onClick = onFindClick, testTag = "ribbon_find")
            RibbonToolButton(icon = Icons.Default.FindReplace, text = "Ganti", onClick = onFindClick, testTag = "ribbon_replace")
            RibbonToolButton(icon = Icons.Default.Numbers, text = "Statistik", onClick = onWordCountClick, testTag = "ribbon_stats")
        }
    }

    // 6. Voice & Share Group
    RibbonGroup(title = "Voice & Share") {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.Mic,
                label = "Dikte",
                iconTint = Color(0xFFD32F2F),
                onClick = {}
            )
            RibbonLargeButton(
                icon = Icons.Default.AutoFixHigh,
                label = "Editor AI",
                iconTint = OfficeBluePrimary,
                onClick = onWordCountClick
            )
            RibbonLargeButton(
                icon = Icons.Default.Share,
                label = "Bagikan",
                iconTint = Color(0xFF2E7D32),
                onClick = {}
            )
        }
    }

    // 7. MDQ Office Elegant Brand Badge & Help/About (Pembuat Masduqi Fadli & Info Aplikasi)
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFFF3F5F8),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD0D8)),
            modifier = Modifier
                .clickable { onAboutClick() }
                .testTag("ribbon_about_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0xFFC5221F), // Elegant Crimson Red
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "MDQ",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp
                        )
                    }
                }
                Column {
                    Text(
                        text = "MDQ Office",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF103F91) // Deep Royal Navy
                    )
                    Text(
                        text = "Desktop v2.4",
                        fontSize = 8.sp,
                        color = Color(0xFF5F6368)
                    )
                }
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Tentang MDQ Office",
                    tint = Color(0xFFC5221F),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// INSERT TAB CONTENT
// -------------------------------------------------------------
@Composable
fun InsertRibbonContent(
    onInsertPageBreak: () -> Unit,
    onInsertTableClick: () -> Unit,
    onInsertShapeClick: (ShapeType) -> Unit,
    onInsertFootnoteClick: () -> Unit,
    onInsertTocClick: () -> Unit
) {
    var showShapesMenu by remember { mutableStateOf(false) }

    RibbonGroup(title = "Pages") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.InsertPageBreak, label = "Page Break", onClick = onInsertPageBreak, testTag = "insert_page_break")
        }
    }

    RibbonGroup(title = "Tables") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.TableChart, label = "Tabel", onClick = onInsertTableClick, testTag = "insert_table_btn")
        }
    }

    RibbonGroup(title = "Illustrations") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.Image, label = "Gambar", onClick = {})
            Box {
                RibbonLargeButton(icon = Icons.Default.Category, label = "Bentuk", onClick = { showShapesMenu = true }, testTag = "insert_shape_btn")
                DropdownMenu(expanded = showShapesMenu, onDismissRequest = { showShapesMenu = false }) {
                    ShapeType.values().forEach { shape ->
                        DropdownMenuItem(
                            text = { Text(shape.displayName, fontSize = 12.sp) },
                            onClick = {
                                onInsertShapeClick(shape)
                                showShapesMenu = false
                            }
                        )
                    }
                }
            }
        }
    }

    RibbonGroup(title = "Header & Footer") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            RibbonToolButton(icon = Icons.Default.VerticalAlignTop, text = "Header", onClick = {})
            RibbonToolButton(icon = Icons.Default.VerticalAlignBottom, text = "Footer", onClick = {})
            RibbonToolButton(icon = Icons.Default.FormatListNumbered, text = "Hal. X", onClick = {})
        }
    }

    RibbonGroup(title = "Symbols") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.Functions, label = "Persamaan", onClick = onInsertFootnoteClick)
            RibbonLargeButton(icon = Icons.Default.BookmarkBorder, label = "Daftar Isi", onClick = onInsertTocClick, testTag = "insert_toc_btn")
        }
    }
}

// -------------------------------------------------------------
// LAYOUT TAB CONTENT (Rich margin presets including Skripsi Indonesia)
// -------------------------------------------------------------
@Composable
fun LayoutRibbonContent(
    document: MdqDocument,
    onSetMarginsPreset: (DocMargins) -> Unit,
    onSetOrientation: (Orientation) -> Unit,
    onSetPaperSize: (PaperSize) -> Unit,
    onCustomPaperClick: () -> Unit = {},
    onPageSetupClick: () -> Unit,
    onIncreaseIndent: () -> Unit,
    onDecreaseIndent: () -> Unit
) {
    var showMarginsMenu by remember { mutableStateOf(false) }
    var showOrientMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    RibbonGroup(title = "Page Setup") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Margins Menu (Extended presets as requested)
            Box {
                RibbonLargeButton(
                    icon = Icons.Default.SpaceBar,
                    label = "Margin\n(${document.margins.presetName})",
                    onClick = { showMarginsMenu = true },
                    testTag = "layout_margins_btn"
                )
                DropdownMenu(expanded = showMarginsMenu, onDismissRequest = { showMarginsMenu = false }) {
                    DropdownMenuItem(text = { Text("Normal (Atas: 2.54, Bawah: 2.54, Kiri: 2.54, Kanan: 2.54 cm)") }, onClick = { onSetMarginsPreset(DocMargins.Normal); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Sempit / Narrow (1.27 cm di semua sisi)") }, onClick = { onSetMarginsPreset(DocMargins.Narrow); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Moderat (Atas/Bawah: 2.54, Kiri/Kanan: 1.91 cm)") }, onClick = { onSetMarginsPreset(DocMargins.Moderate); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Lebar / Wide (Atas/Bawah: 2.54, Kiri/Kanan: 5.08 cm)") }, onClick = { onSetMarginsPreset(DocMargins.Wide); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Format Skripsi / Makalah (Kiri: 4, Atas: 4, Kanan: 3, Bawah: 3 cm)", fontWeight = FontWeight.SemiBold) }, onClick = { onSetMarginsPreset(DocMargins.Skripsi); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Cermin / Mirrored (Dalam: 3.18, Luar: 2.54 cm)") }, onClick = { onSetMarginsPreset(DocMargins.Mirrored); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Word 2003 Klasik (Kiri/Kanan: 3.18 cm)") }, onClick = { onSetMarginsPreset(DocMargins.Office2003); showMarginsMenu = false })
                    DropdownMenuItem(text = { Text("Margin Kustom...", fontWeight = FontWeight.Bold, color = OfficeBluePrimary) }, onClick = { onPageSetupClick(); showMarginsMenu = false })
                }
            }

            // Orientation
            Box {
                RibbonLargeButton(
                    icon = Icons.Default.ScreenRotation,
                    label = "Orientasi\n(${document.orientation.displayName})",
                    onClick = { showOrientMenu = true },
                    testTag = "layout_orientation_btn"
                )
                DropdownMenu(expanded = showOrientMenu, onDismissRequest = { showOrientMenu = false }) {
                    DropdownMenuItem(text = { Text("Potret") }, onClick = { onSetOrientation(Orientation.Portrait); showOrientMenu = false })
                    DropdownMenuItem(text = { Text("Lanskap") }, onClick = { onSetOrientation(Orientation.Landscape); showOrientMenu = false })
                }
            }

            // Paper Size
            Box {
                RibbonLargeButton(
                    icon = Icons.Default.Description,
                    label = "Ukuran\n(${document.paperSize.name})",
                    onClick = { showSizeMenu = true },
                    testTag = "layout_size_btn"
                )
                DropdownMenu(expanded = showSizeMenu, onDismissRequest = { showSizeMenu = false }) {
                    PaperSize.values().forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size.displayName) },
                            onClick = { onSetPaperSize(size); showSizeMenu = false }
                        )
                    }
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Ukuran Kertas Kustom...", fontWeight = FontWeight.Bold, color = OfficeBluePrimary) },
                        onClick = { onCustomPaperClick(); showSizeMenu = false }
                    )
                }
            }

            RibbonLargeButton(icon = Icons.Default.ViewColumn, label = "Kolom\n(1 Kolom)", onClick = {})
        }
    }

    RibbonGroup(title = "Paragraph") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            RibbonToolButton(icon = Icons.Default.FormatIndentIncrease, text = "Indent +", onClick = onIncreaseIndent)
            RibbonToolButton(icon = Icons.Default.FormatIndentDecrease, text = "Indent -", onClick = onDecreaseIndent)
        }
    }
}

// -------------------------------------------------------------
// REFERENCES TAB CONTENT
// -------------------------------------------------------------
@Composable
fun ReferencesRibbonContent(
    onInsertTocClick: () -> Unit,
    onInsertFootnoteClick: () -> Unit
) {
    RibbonGroup(title = "Table of Contents") {
        RibbonLargeButton(
            icon = Icons.Default.MenuBook,
            label = "Daftar Isi",
            onClick = onInsertTocClick,
            testTag = "ref_toc_btn"
        )
    }

    RibbonGroup(title = "Footnotes") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.FormatQuote,
                label = "Catatan Kaki",
                onClick = onInsertFootnoteClick,
                testTag = "ref_footnote_btn"
            )
            RibbonLargeButton(
                icon = Icons.Default.NoteAlt,
                label = "Endnote",
                onClick = onInsertFootnoteClick
            )
        }
    }

    RibbonGroup(title = "Citations") {
        RibbonLargeButton(
            icon = Icons.Default.FormatColorText,
            label = "Kutipan (APA)",
            onClick = onInsertFootnoteClick
        )
    }
}

// -------------------------------------------------------------
// MAILINGS TAB CONTENT
// -------------------------------------------------------------
@Composable
fun MailingsRibbonContent(
    onMailMergeClick: () -> Unit
) {
    RibbonGroup(title = "Create") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.Email, label = "Amplop", onClick = {})
            RibbonLargeButton(icon = Icons.Default.Label, label = "Label", onClick = {})
        }
    }

    RibbonGroup(title = "Start Mail Merge") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.GroupAdd,
                label = "Mail Merge",
                onClick = onMailMergeClick,
                testTag = "mailings_merge_btn"
            )
        }
    }
}

// -------------------------------------------------------------
// REVIEW TAB CONTENT
// -------------------------------------------------------------
@Composable
fun ReviewRibbonContent(
    trackChanges: Boolean,
    onToggleTrackChanges: () -> Unit,
    onAddCommentClick: () -> Unit,
    onWordCountClick: () -> Unit
) {
    RibbonGroup(title = "Proofing") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.Spellcheck, label = "Ejaan", onClick = onWordCountClick)
            RibbonLargeButton(icon = Icons.Default.Calculate, label = "Hitung Kata", onClick = onWordCountClick)
        }
    }

    RibbonGroup(title = "Comments") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.AddComment,
                label = "Komentar Baru",
                onClick = onAddCommentClick,
                testTag = "review_add_comment"
            )
        }
    }

    RibbonGroup(title = "Tracking") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(
                icon = if (trackChanges) Icons.Default.CheckCircle else Icons.Default.ChangeCircle,
                label = if (trackChanges) "Lacak: ON" else "Lacak: OFF",
                onClick = onToggleTrackChanges,
                testTag = "review_track_changes"
            )
        }
    }
}

// -------------------------------------------------------------
// VIEW TAB CONTENT
// -------------------------------------------------------------
@Composable
fun ViewRibbonContent(
    showRuler: Boolean,
    showNavPane: Boolean,
    showGridlines: Boolean,
    viewMode: ViewMode,
    onToggleRuler: () -> Unit,
    onToggleNavPane: () -> Unit,
    onToggleGridlines: () -> Unit,
    onSetViewMode: (ViewMode) -> Unit,
    onResetZoom: () -> Unit
) {
    RibbonGroup(title = "Views") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.Article,
                label = "Print Layout",
                onClick = { onSetViewMode(ViewMode.PrintLayout) },
                testTag = "view_print_layout"
            )
            RibbonLargeButton(
                icon = Icons.Default.ChromeReaderMode,
                label = "Read Mode",
                onClick = { onSetViewMode(ViewMode.ReadMode) },
                testTag = "view_read_mode"
            )
            RibbonLargeButton(
                icon = Icons.Default.Web,
                label = "Web Layout",
                onClick = { onSetViewMode(ViewMode.WebLayout) },
                testTag = "view_web_layout"
            )
        }
    }

    RibbonGroup(title = "Show") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showRuler, onCheckedChange = { onToggleRuler() }, modifier = Modifier.size(24.dp).testTag("toggle_ruler_cb"))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ruler", fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showNavPane, onCheckedChange = { onToggleNavPane() }, modifier = Modifier.size(24.dp).testTag("toggle_nav_cb"))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Navigasi", fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showGridlines, onCheckedChange = { onToggleGridlines() }, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gridlines", fontSize = 10.sp)
            }
        }
    }

    RibbonGroup(title = "Zoom") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(icon = Icons.Default.ZoomIn, label = "100%", onClick = onResetZoom, testTag = "view_zoom_100")
        }
    }
}

// -------------------------------------------------------------
// HELP TAB CONTENT
// -------------------------------------------------------------
@Composable
fun HelpRibbonContent(
    onShortcutsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    RibbonGroup(title = "Help") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RibbonLargeButton(
                icon = Icons.Default.Help,
                label = "Panduan",
                onClick = onHelpClick,
                testTag = "help_guide_btn"
            )
            RibbonLargeButton(
                icon = Icons.Default.Keyboard,
                label = "Shortcuts PC",
                onClick = onShortcutsClick,
                testTag = "help_shortcuts_btn"
            )
            RibbonLargeButton(
                icon = Icons.Default.Info,
                label = "Tentang MDQ",
                onClick = onHelpClick,
                testTag = "help_about_btn"
            )
        }
    }
}
