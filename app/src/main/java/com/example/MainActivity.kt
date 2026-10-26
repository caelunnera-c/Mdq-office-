package com.example

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mdqoffice.data.AppPreferences
import com.example.mdqoffice.data.ScreenMode
import com.example.mdqoffice.model.DocTextAlign
import com.example.mdqoffice.model.RibbonTab
import com.example.mdqoffice.model.ViewMode
import com.example.mdqoffice.theme.MdqOfficeTheme
import com.example.mdqoffice.ui.desktop.*
import com.example.mdqoffice.ui.dialogs.*
import com.example.mdqoffice.viewmodel.OfficeViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val viewModel: OfficeViewModel by viewModels()
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appPreferences = AppPreferences(this)
        val savedScreenMode = appPreferences.screenMode
        viewModel.screenMode.value = savedScreenMode

        // Ensure decor fits false so system bars overlay on top without resizing layout
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyScreenMode(savedScreenMode)

        setContent {
            MdqOfficeTheme {
                MdqOfficeApp(
                    viewModel = viewModel,
                    onUpdateScreenMode = { mode ->
                        appPreferences.screenMode = mode
                        viewModel.screenMode.value = mode
                        applyScreenMode(mode)
                    }
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyScreenMode(viewModel.screenMode.value)
        }
    }

    private fun applyScreenMode(mode: ScreenMode) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        when (mode) {
            ScreenMode.FULLSCREEN_IMMERSIVE -> {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            ScreenMode.FULLSCREEN_GESTURE -> {
                insetsController.hide(WindowInsetsCompat.Type.statusBars())
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
            }
            ScreenMode.MODE_NORMAL -> {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

@Composable
fun MdqOfficeApp(
    viewModel: OfficeViewModel,
    onUpdateScreenMode: (ScreenMode) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val document by viewModel.document.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedParagraphIndex by viewModel.selectedParagraphIndex.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val showRuler by viewModel.showRuler.collectAsState()
    val showNavPane by viewModel.showNavigationPane.collectAsState()
    val navPaneTab by viewModel.navPaneTab.collectAsState()
    val showGridlines by viewModel.showGridlines.collectAsState()
    val showBackstage by viewModel.showBackstage.collectAsState()
    val recentFiles by viewModel.recentDocuments.collectAsState()
    val hasCopiedFormat by viewModel.hasCopiedFormat.collectAsState()
    val currentScreenMode by viewModel.screenMode.collectAsState()

    val showFindDialog by viewModel.showFindDialog.collectAsState()
    val showWordCountDialog by viewModel.showWordCountDialog.collectAsState()
    val showInsertTableDialog by viewModel.showInsertTableDialog.collectAsState()
    val showPageSetupDialog by viewModel.showPageSetupDialog.collectAsState()
    val showShortcutsDialog by viewModel.showShortcutsDialog.collectAsState()
    val showHelpDialog by viewModel.showHelpDialog.collectAsState()
    val showMailMergeDialog by viewModel.showMailMergeDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()

    val openDocuments by viewModel.openDocuments.collectAsState()
    val activeDocIndex by viewModel.activeDocIndex.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()
    val showDocSwitcherDialog by viewModel.showDocSwitcherDialog.collectAsState()
    val showCustomPaperDialog by viewModel.showCustomPaperDialog.collectAsState()
    val showCustomFontDialog by viewModel.showCustomFontDialog.collectAsState()
    val showUnsavedDialog by viewModel.showUnsavedDialog.collectAsState()
    val documentToPromptSave by viewModel.documentToPromptSave.collectAsState()
    val customFonts by viewModel.customFonts.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val replaceQuery by viewModel.replaceQuery.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.loadCustomFonts(context)
    }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.toastMessage.value = null
        }
    }

    // Launchers for system file picker
    val importFontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fontName = it.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "FontKustom"
            viewModel.importCustomFont(context, fontName, it)
        }
    }

    val openDocxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = it.lastPathSegment ?: "Dokumen.docx"
                if (inputStream != null) {
                    viewModel.openDocxFromStream(inputStream, fileName)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val saveDocxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    viewModel.exportDocxToStream(os)
                }
                Toast.makeText(context, "DOCX berhasil disimpan!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengekspor: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    viewModel.exportPdfToStream(os)
                }
                Toast.makeText(context, "PDF berhasil disimpan!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handlePrint() {
        try {
            val cacheFile = File(context.cacheDir, "MDQ_Print_${System.currentTimeMillis()}.pdf")
            FileOutputStream(cacheFile).use { fos ->
                viewModel.exportPdfToStream(fos)
            }
            Toast.makeText(context, "Dokumen PDF siap dicetak (${cacheFile.name})", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Pencetakan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val selectedParagraph = if (selectedParagraphIndex in document.paragraphs.indices) {
        document.paragraphs[selectedParagraphIndex]
    } else null

    // Main desktop layout: No statusBarsPadding or navigationBarsPadding so transient system bars do NOT shift or resize MDQ Office
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.isCtrlPressed || keyEvent.isMetaPressed)) {
                    when (keyEvent.key) {
                        Key.A -> {
                            Toast.makeText(context, "Semua teks dipilih", Toast.LENGTH_SHORT).show()
                            true
                        }
                        Key.C -> {
                            viewModel.copyFormat()
                            true
                        }
                        Key.V -> {
                            if (hasCopiedFormat) viewModel.pasteFormat()
                            true
                        }
                        Key.S -> {
                            viewModel.saveDocument(context)
                            true
                        }
                        Key.Z -> {
                            viewModel.undo()
                            true
                        }
                        Key.Y -> {
                            viewModel.redo()
                            true
                        }
                        Key.B -> {
                            viewModel.toggleBold()
                            true
                        }
                        Key.I -> {
                            viewModel.toggleItalic()
                            true
                        }
                        Key.U -> {
                            viewModel.toggleUnderline()
                            true
                        }
                        Key.F -> {
                            viewModel.showFindDialog.value = true
                            true
                        }
                        Key.H -> {
                            viewModel.showFindDialog.value = true
                            true
                        }
                        Key.N -> {
                            viewModel.createNewDocument(3)
                            true
                        }
                        Key.O -> {
                            openDocxLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"))
                            true
                        }
                        Key.P -> {
                            handlePrint()
                            true
                        }
                        Key.E -> {
                            viewModel.setAlignment(DocTextAlign.Center)
                            true
                        }
                        Key.L -> {
                            viewModel.setAlignment(DocTextAlign.Left)
                            true
                        }
                        Key.R -> {
                            viewModel.setAlignment(DocTextAlign.Right)
                            true
                        }
                        Key.J -> {
                            viewModel.setAlignment(DocTextAlign.Justify)
                            true
                        }
                        Key.RightBracket -> {
                            viewModel.increaseFontSize()
                            true
                        }
                        Key.LeftBracket -> {
                            viewModel.decreaseFontSize()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .testTag("mdq_office_root")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Topmost Desktop Bar: Unified Ribbon Bar with Tab row, Search, and Quick Access
                DesktopRibbonBar(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) },
                    selectedParagraph = selectedParagraph,
                    document = document,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    openDocsCount = openDocuments.size,
                    onDocSwitcherClick = { viewModel.showDocSwitcherDialog.value = true },
                    onQuickSave = { viewModel.saveDocument(context) },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onNewDoc = { viewModel.createNewDocument(0) },
                    onPrint = { handlePrint() },
                    onSearchClick = { viewModel.showFindDialog.value = true },
                    onSettingsClick = { viewModel.showSettingsDialog.value = true },
                    onAboutClick = { viewModel.showAboutDialog.value = true },
                    onToggleBold = { viewModel.toggleBold() },
                    onToggleItalic = { viewModel.toggleItalic() },
                    onToggleUnderline = { viewModel.toggleUnderline() },
                    onToggleStrike = { viewModel.toggleStrikethrough() },
                    onToggleSubscript = { viewModel.toggleSubscript() },
                    onToggleSuperscript = { viewModel.toggleSuperscript() },
                    onChangeCase = { viewModel.changeCase(it) },
                    onSetFontSize = { viewModel.setFontSize(it) },
                    onIncreaseFontSize = { viewModel.increaseFontSize() },
                    onDecreaseFontSize = { viewModel.decreaseFontSize() },
                    onSetFontFamily = { viewModel.setFontFamily(it) },
                    onSetFontColor = { viewModel.setFontColor(it) },
                    onSetHighlightColor = { viewModel.setHighlightColor(it) },
                    onClearFormatting = { viewModel.clearFormatting() },
                    onCopyFormat = { viewModel.copyFormat() },
                    onPasteFormat = { viewModel.pasteFormat() },
                    onToggleFormatPainter = { viewModel.toggleFormatPainter() },
                    hasCopiedFormat = hasCopiedFormat,
                    onSetAlignment = { viewModel.setAlignment(it) },
                    onToggleTextDirection = { viewModel.toggleTextDirection() },
                    onSetStyle = { viewModel.setStyle(it) },
                    onToggleBullet = { viewModel.toggleBullet() },
                    onSetBulletStyle = { viewModel.setBulletStyle(it) },
                    onToggleNumbering = { viewModel.toggleNumbering() },
                    onSetNumberingStyle = { viewModel.setNumberingStyle(it) },
                    onIncreaseIndent = { viewModel.increaseIndent() },
                    onDecreaseIndent = { viewModel.decreaseIndent() },
                    onFindClick = { viewModel.showFindDialog.value = true },
                    onWordCountClick = { viewModel.showWordCountDialog.value = true },
                    customFonts = customFonts,
                    onManageCustomFontsClick = { viewModel.showCustomFontDialog.value = true },
                    onInsertPageBreak = { viewModel.insertPageBreak() },
                    onInsertTableClick = { viewModel.showInsertTableDialog.value = true },
                    onInsertShapeClick = { viewModel.insertShape(it) },
                    onInsertFootnoteClick = { viewModel.insertFootnote("Catatan kaki resmi MDQ Office") },
                    onInsertTocClick = { viewModel.insertTableOfContents() },
                    onSetMarginsPreset = { viewModel.setMarginsPreset(it) },
                    onSetOrientation = { viewModel.setOrientation(it) },
                    onSetPaperSize = { viewModel.setPaperSize(it) },
                    onCustomPaperClick = { viewModel.showCustomPaperDialog.value = true },
                    onPageSetupClick = { viewModel.showPageSetupDialog.value = true },
                    onAddCommentClick = { viewModel.addComment("Catatan revisi paragraf ini.") },
                    onToggleTrackChanges = { viewModel.toggleTrackChanges() },
                    onToggleRuler = { viewModel.toggleRuler() },
                    onToggleNavPane = { viewModel.toggleNavigationPane() },
                    onToggleGridlines = { viewModel.toggleGridlines() },
                    onSetViewMode = { viewModel.setViewMode(it) },
                    onResetZoom = { viewModel.resetZoom() },
                    showRuler = showRuler,
                    showNavPane = showNavPane,
                    showGridlines = showGridlines,
                    viewMode = viewMode,
                    onShortcutsClick = { viewModel.showShortcutsDialog.value = true },
                    onHelpClick = { viewModel.showHelpDialog.value = true },
                    onMailMergeClick = { viewModel.showMailMergeDialog.value = true }
                )

                // 2. Horizontal Ruler (Compact height with numbers)
                if (showRuler && viewMode == ViewMode.PrintLayout) {
                    DesktopHorizontalRuler(
                        document = document,
                        selectedParagraph = selectedParagraph,
                        onSetLeftMargin = { viewModel.setLeftMargin(it) },
                        onSetRightMargin = { viewModel.setRightMargin(it) },
                        onSetFirstLineIndent = { viewModel.setFirstLineIndent(it) }
                    )
                }

                // 3. Middle Workspace (Navigation Pane + Vertical Ruler + Document Canvas)
                Row(modifier = Modifier.weight(1f)) {
                    // Navigation Pane (collapsible)
                    if (showNavPane) {
                        DesktopNavigationPane(
                            document = document,
                            activeTab = navPaneTab,
                            onSelectTab = { viewModel.setNavPaneTab(it) },
                            onJumpToParagraph = { viewModel.selectParagraph(it) },
                            onClose = { viewModel.toggleNavigationPane() },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it }
                        )
                    }

                    // Vertical Ruler
                    if (showRuler && viewMode == ViewMode.PrintLayout) {
                        DesktopVerticalRuler(
                            document = document,
                            onSetTopMargin = { viewModel.setTopMargin(it) }
                        )
                    }

                    // Document Page Canvas
                    DesktopDocumentCanvas(
                        document = document,
                        selectedParagraphIndex = selectedParagraphIndex,
                        onSelectParagraph = { viewModel.selectParagraph(it) },
                        onUpdateParagraphText = { idx, text -> viewModel.updateParagraphText(idx, text) },
                        onSelectionChange = { start, end -> viewModel.updateSelection(start, end) },
                        onAddNewParagraph = { viewModel.addNewParagraph(it) },
                        onDeleteParagraph = { viewModel.deleteParagraph(it) },
                        onUpdateTableCell = { tIdx, rIdx, cIdx, text -> viewModel.updateTableCell(tIdx, rIdx, cIdx, text) },
                        zoomLevel = zoomLevel,
                        viewMode = viewMode,
                        showGridlines = showGridlines,
                        hasCopiedFormat = hasCopiedFormat,
                        onApplyFormatPainter = { viewModel.applyFormatPainter() },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 4. Desktop Status Bar
                DesktopStatusBar(
                    currentPage = 1,
                    totalPages = 2,
                    wordCount = document.wordCount,
                    characterCount = document.characterCount,
                    viewMode = viewMode,
                    zoomLevel = zoomLevel,
                    onZoomChange = { viewModel.setZoomLevel(it) },
                    onZoomIn = { viewModel.zoomIn() },
                    onZoomOut = { viewModel.zoomOut() },
                    onSetViewMode = { viewModel.setViewMode(it) },
                    onWordCountClick = { viewModel.showWordCountDialog.value = true }
                )
            }

            // Backstage View Overlay (When "File" tab is clicked)
            if (showBackstage) {
                DesktopBackstageView(
                    document = document,
                    onCloseBackstage = { viewModel.closeBackstage() },
                    onNewDocument = { viewModel.createNewDocument(it) },
                    onOpenDocxPicker = { openDocxLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword")) },
                    onLoadSample = { viewModel.loadSample(it) },
                    onSaveDocument = { viewModel.saveDocument(context) },
                    onExportPdf = { exportPdfLauncher.launch("${document.title}.pdf") },
                    onExportDocx = { saveDocxLauncher.launch(document.fileName) },
                    onPrintDocument = { handlePrint() },
                    recentFiles = recentFiles
                )
            }
        }
    }

    // Modal Dialogs
    if (showFindDialog) {
        FindAndReplaceDialog(
            searchQuery = searchQuery,
            replaceQuery = replaceQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            onReplaceChange = { viewModel.replaceQuery.value = it },
            onExecuteReplace = { viewModel.executeSearchAndReplace() },
            onDismiss = { viewModel.showFindDialog.value = false }
        )
    }

    if (showWordCountDialog) {
        WordCountDialog(
            document = document,
            onDismiss = { viewModel.showWordCountDialog.value = false }
        )
    }

    if (showInsertTableDialog) {
        InsertTableDialog(
            onInsertTable = { r, c -> viewModel.insertTable(r, c) },
            onDismiss = { viewModel.showInsertTableDialog.value = false }
        )
    }

    if (showPageSetupDialog) {
        PageSetupDialog(
            margins = document.margins,
            paperSize = document.paperSize,
            orientation = document.orientation,
            onSaveSetup = { m, s, o ->
                viewModel.setMarginsPreset(m)
                viewModel.setPaperSize(s)
                viewModel.setOrientation(o)
            },
            onDismiss = { viewModel.showPageSetupDialog.value = false }
        )
    }

    if (showShortcutsDialog) {
        KeyboardShortcutsDialog(
            onDismiss = { viewModel.showShortcutsDialog.value = false }
        )
    }

    if (showHelpDialog) {
        HelpAboutDialog(
            onDismiss = { viewModel.showHelpDialog.value = false }
        )
    }

    if (showMailMergeDialog) {
        MailMergeDialog(
            onApplyMerge = { name, company, city ->
                viewModel.applyMailMerge(name, company, city)
            },
            onDismiss = { viewModel.showMailMergeDialog.value = false }
        )
    }

    if (showSettingsDialog) {
        ScreenModeSettingsDialog(
            currentMode = currentScreenMode,
            onSelectMode = { mode ->
                onUpdateScreenMode(mode)
            },
            onDismiss = { viewModel.showSettingsDialog.value = false }
        )
    }

    if (showAboutDialog) {
        AboutMdqOfficeDialog(
            onDismiss = { viewModel.showAboutDialog.value = false }
        )
    }

    if (showUnsavedDialog) {
        UnsavedChangesConfirmDialog(
            docTitle = documentToPromptSave?.title ?: document.title,
            onSave = { viewModel.confirmSaveAndProceed(context) },
            onDiscard = { viewModel.confirmDiscardAndProceed() },
            onCancel = { viewModel.cancelUnsavedPrompt() }
        )
    }

    if (showDocSwitcherDialog) {
        DocumentSwitcherDialog(
            documents = openDocuments,
            activeIndex = activeDocIndex,
            onSelectDoc = { viewModel.requestSwitchDocument(it) },
            onCloseDoc = { viewModel.requestCloseDocument(it) },
            onNewDoc = { viewModel.requestNewDocument(0) },
            onDismiss = { viewModel.showDocSwitcherDialog.value = false }
        )
    }

    if (showCustomPaperDialog) {
        CustomPaperSizeDialog(
            initialWidthCm = document.customPaperWidthCm ?: 21.0f,
            initialHeightCm = document.customPaperHeightCm ?: 29.7f,
            onApply = { w, h -> viewModel.setCustomPaperSize(w, h) },
            onDismiss = { viewModel.showCustomPaperDialog.value = false }
        )
    }

    if (showCustomFontDialog) {
        CustomFontImportDialog(
            customFonts = customFonts,
            onImportRequested = {
                importFontLauncher.launch(arrayOf("font/*", "application/x-font-ttf", "application/x-font-opentype", "application/octet-stream"))
            },
            onSelectFont = { fontName ->
                viewModel.setFontFamily(fontName)
            },
            onDismiss = { viewModel.showCustomFontDialog.value = false }
        )
    }
}
