package com.example.mdqoffice.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mdqoffice.data.AppPreferences
import com.example.mdqoffice.data.DocSampleData
import com.example.mdqoffice.engine.DocxEngine
import com.example.mdqoffice.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class OfficeViewModel : ViewModel() {

    val openDocuments = MutableStateFlow<List<MdqDocument>>(listOf(DocSampleData.getBusinessProposal()))
    val activeDocIndex = MutableStateFlow(0)

    private val _document = MutableStateFlow<MdqDocument>(DocSampleData.getBusinessProposal())
    val document: StateFlow<MdqDocument> = _document.asStateFlow()

    private val undoStack = ArrayDeque<MdqDocument>()
    private val redoStack = ArrayDeque<MdqDocument>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _activeTab = MutableStateFlow(RibbonTab.Home)
    val activeTab: StateFlow<RibbonTab> = _activeTab.asStateFlow()

    private val _selectedParagraphIndex = MutableStateFlow(0)
    val selectedParagraphIndex: StateFlow<Int> = _selectedParagraphIndex.asStateFlow()

    val selectionStart = MutableStateFlow(0)
    val selectionEnd = MutableStateFlow(0)

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.PrintLayout)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _showRuler = MutableStateFlow(true)
    val showRuler: StateFlow<Boolean> = _showRuler.asStateFlow()

    private val _showNavigationPane = MutableStateFlow(false)
    val showNavigationPane: StateFlow<Boolean> = _showNavigationPane.asStateFlow()

    private val _navPaneTab = MutableStateFlow(0) // 0: Headings, 1: Pages, 2: Search
    val navPaneTab: StateFlow<Int> = _navPaneTab.asStateFlow()

    private val _showGridlines = MutableStateFlow(false)
    val showGridlines: StateFlow<Boolean> = _showGridlines.asStateFlow()

    private val _showBackstage = MutableStateFlow(false)
    val showBackstage: StateFlow<Boolean> = _showBackstage.asStateFlow()

    // Dialog states
    val showFindDialog = MutableStateFlow(false)
    val showWordCountDialog = MutableStateFlow(false)
    val showInsertTableDialog = MutableStateFlow(false)
    val showPageSetupDialog = MutableStateFlow(false)
    val showShortcutsDialog = MutableStateFlow(false)
    val showHelpDialog = MutableStateFlow(false)
    val showMailMergeDialog = MutableStateFlow(false)
    val showSettingsDialog = MutableStateFlow(false)
    val showAboutDialog = MutableStateFlow(false)
    val showDocSwitcherDialog = MutableStateFlow(false)
    val showCustomPaperDialog = MutableStateFlow(false)
    val showCustomFontDialog = MutableStateFlow(false)
    val showUnsavedDialog = MutableStateFlow(false)
    val documentToPromptSave = MutableStateFlow<MdqDocument?>(null)
    var pendingAction: (() -> Unit)? = null

    val screenMode = MutableStateFlow(com.example.mdqoffice.data.ScreenMode.FULLSCREEN_IMMERSIVE)

    var copiedFormat: DocRun? = null
    val hasCopiedFormat = MutableStateFlow(false)

    val customFonts = MutableStateFlow<List<Pair<String, String>>>(emptyList())

    val searchQuery = MutableStateFlow("")
    val replaceQuery = MutableStateFlow("")

    val toastMessage = MutableStateFlow<String?>(null)

    val recentDocuments = MutableStateFlow<List<String>>(
        listOf("Proposal Bisnis MDQ.docx", "Laporan Riset & Skripsi.docx", "Surat Resmi & Mail Merge.docx")
    )

    private fun updateCurrentDoc(newDoc: MdqDocument) {
        _document.value = newDoc
        val docs = openDocuments.value.toMutableList()
        val idx = activeDocIndex.value
        if (idx in docs.indices) {
            docs[idx] = newDoc
            openDocuments.value = docs
        }
    }

    fun updateSelection(start: Int, end: Int) {
        selectionStart.value = minOf(start, end).coerceAtLeast(0)
        selectionEnd.value = maxOf(start, end).coerceAtLeast(0)
    }

    private fun saveUndoState() {
        if (undoStack.size > 40) {
            undoStack.removeFirst()
        }
        undoStack.addLast(_document.value)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(_document.value)
            val prev = undoStack.removeLast()
            _document.value = prev
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(_document.value)
            val next = redoStack.removeLast()
            _document.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    fun setActiveTab(tab: RibbonTab) {
        if (tab == RibbonTab.File) {
            _showBackstage.value = true
        } else {
            _showBackstage.value = false
            _activeTab.value = tab
        }
    }

    fun closeBackstage() {
        _showBackstage.value = false
    }

    fun selectParagraph(index: Int) {
        if (index in 0 until _document.value.paragraphs.size) {
            _selectedParagraphIndex.value = index
        }
    }

    fun updateParagraphText(index: Int, newText: String) {
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return

        val p = currentDoc.paragraphs[index]
        if (p.fullText == newText) return

        val firstRun = p.runs.firstOrNull() ?: DocRun(text = "")
        val updatedRun = firstRun.copy(text = newText)
        val updatedParagraph = p.copy(runs = listOf(updatedRun))

        val newParagraphs = currentDoc.paragraphs.toMutableList()
        newParagraphs[index] = updatedParagraph

        _document.value = currentDoc.copy(paragraphs = newParagraphs, isDirty = true)
    }

    fun addNewParagraph(afterIndex: Int) {
        saveUndoState()
        val currentDoc = _document.value
        val insertIndex = (afterIndex + 1).coerceIn(0, currentDoc.paragraphs.size)
        val newP = DocParagraph(runs = listOf(DocRun(text = "")))
        val newParagraphs = currentDoc.paragraphs.toMutableList()
        newParagraphs.add(insertIndex, newP)
        _document.value = currentDoc.copy(paragraphs = newParagraphs, isDirty = true)
        _selectedParagraphIndex.value = insertIndex
    }

    fun deleteParagraph(index: Int) {
        val currentDoc = _document.value
        if (currentDoc.paragraphs.size <= 1) return
        if (index !in 0 until currentDoc.paragraphs.size) return

        saveUndoState()
        val newParagraphs = currentDoc.paragraphs.toMutableList()
        newParagraphs.removeAt(index)
        _document.value = currentDoc.copy(paragraphs = newParagraphs, isDirty = true)
        _selectedParagraphIndex.value = (index - 1).coerceAtLeast(0)
    }

    private fun applyFormatToSelectionOrParagraph(transform: (DocRun) -> DocRun) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val selStart = selectionStart.value
        val selEnd = selectionEnd.value

        val updatedRuns = if (selStart < selEnd && selStart >= 0 && selEnd <= p.fullText.length) {
            splitRunsAtRange(p.runs, selStart, selEnd, transform)
        } else {
            p.runs.map { transform(it) }
        }

        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(runs = updatedRuns)
        updateCurrentDoc(currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true))
    }

    private fun splitRunsAtRange(
        runs: List<DocRun>,
        selStart: Int,
        selEnd: Int,
        transform: (DocRun) -> DocRun
    ): List<DocRun> {
        val result = mutableListOf<DocRun>()
        var charOffset = 0

        for (run in runs) {
            val runStart = charOffset
            val runEnd = charOffset + run.text.length
            charOffset = runEnd

            if (runEnd <= selStart || runStart >= selEnd) {
                result.add(run)
            } else {
                val localSelStart = (selStart - runStart).coerceIn(0, run.text.length)
                val localSelEnd = (selEnd - runStart).coerceIn(0, run.text.length)

                val beforeText = run.text.substring(0, localSelStart)
                val selectedText = run.text.substring(localSelStart, localSelEnd)
                val afterText = run.text.substring(localSelEnd)

                if (beforeText.isNotEmpty()) {
                    result.add(run.copy(id = UUID.randomUUID().toString(), text = beforeText))
                }
                if (selectedText.isNotEmpty()) {
                    result.add(transform(run.copy(id = UUID.randomUUID().toString(), text = selectedText)))
                }
                if (afterText.isNotEmpty()) {
                    result.add(run.copy(id = UUID.randomUUID().toString(), text = afterText))
                }
            }
        }
        return if (result.isEmpty()) listOf(DocRun(text = "")) else result
    }

    fun toggleBold() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        val p = currentDoc.paragraphs[index]
        val selStart = selectionStart.value
        val selEnd = selectionEnd.value

        val targetRuns = if (selStart < selEnd) {
            var offset = 0
            p.runs.filter { r ->
                val rStart = offset
                val rEnd = offset + r.text.length
                offset = rEnd
                rEnd > selStart && rStart < selEnd
            }
        } else p.runs

        val anyBold = targetRuns.any { it.bold }
        applyFormatToSelectionOrParagraph { it.copy(bold = !anyBold) }
    }

    fun toggleItalic() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        val p = currentDoc.paragraphs[index]
        val selStart = selectionStart.value
        val selEnd = selectionEnd.value

        val targetRuns = if (selStart < selEnd) {
            var offset = 0
            p.runs.filter { r ->
                val rStart = offset
                val rEnd = offset + r.text.length
                offset = rEnd
                rEnd > selStart && rStart < selEnd
            }
        } else p.runs

        val anyItalic = targetRuns.any { it.italic }
        applyFormatToSelectionOrParagraph { it.copy(italic = !anyItalic) }
    }

    fun toggleUnderline() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        val p = currentDoc.paragraphs[index]
        val selStart = selectionStart.value
        val selEnd = selectionEnd.value

        val targetRuns = if (selStart < selEnd) {
            var offset = 0
            p.runs.filter { r ->
                val rStart = offset
                val rEnd = offset + r.text.length
                offset = rEnd
                rEnd > selStart && rStart < selEnd
            }
        } else p.runs

        val anyUnderline = targetRuns.any { it.underline }
        applyFormatToSelectionOrParagraph { it.copy(underline = !anyUnderline) }
    }

    fun toggleStrikethrough() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        val p = currentDoc.paragraphs[index]
        val selStart = selectionStart.value
        val selEnd = selectionEnd.value

        val targetRuns = if (selStart < selEnd) {
            var offset = 0
            p.runs.filter { r ->
                val rStart = offset
                val rEnd = offset + r.text.length
                offset = rEnd
                rEnd > selStart && rStart < selEnd
            }
        } else p.runs

        val anyStrike = targetRuns.any { it.strikethrough }
        applyFormatToSelectionOrParagraph { it.copy(strikethrough = !anyStrike) }
    }

    fun toggleSubscript() {
        applyFormatToSelectionOrParagraph { run ->
            val newSub = !run.subscript
            run.copy(subscript = newSub, superscript = if (newSub) false else run.superscript)
        }
    }

    fun toggleSuperscript() {
        applyFormatToSelectionOrParagraph { run ->
            val newSuper = !run.superscript
            run.copy(superscript = newSuper, subscript = if (newSuper) false else run.subscript)
        }
    }

    fun changeCase(type: CaseType) {
        applyFormatToSelectionOrParagraph { run ->
            val newText = when (type) {
                CaseType.Lower -> run.text.lowercase()
                CaseType.Upper -> run.text.uppercase()
                CaseType.Sentence -> run.text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                CaseType.CapitalizeEach -> run.text.split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
                CaseType.Toggle -> run.text.map { if (it.isUpperCase()) it.lowercaseChar() else it.uppercaseChar() }.joinToString("")
            }
            run.copy(text = newText)
        }
        toastMessage.value = "Format huruf diubah: ${type.displayName}"
    }

    fun setFontSize(size: Float) {
        applyFormatToSelectionOrParagraph { it.copy(fontSize = size) }
    }

    fun setFontFamily(fontFamily: String) {
        applyFormatToSelectionOrParagraph { it.copy(fontFamily = fontFamily) }
    }

    fun setFontColor(colorHex: Long) {
        applyFormatToSelectionOrParagraph { it.copy(fontColor = colorHex) }
    }

    fun setHighlightColor(colorHex: Long?) {
        applyFormatToSelectionOrParagraph { it.copy(highlightColor = colorHex) }
    }

    fun clearFormatting() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val updatedRuns = p.runs.map {
            it.copy(
                bold = false,
                italic = false,
                underline = false,
                strikethrough = false,
                subscript = false,
                superscript = false,
                fontColor = 0xFF202124,
                highlightColor = null,
                fontSize = 11f,
                fontFamily = "Calibri"
            )
        }
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(
            runs = updatedRuns,
            style = ParagraphStyle.Normal,
            alignment = DocTextAlign.Left,
            indentLeftCm = 0f,
            indentFirstLineCm = 0f,
            indentRightCm = 0f,
            isBullet = false,
            isNumbered = false,
            isRtl = false
        )
        updateCurrentDoc(currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true))
        toastMessage.value = "Format dibersihkan"
    }

    fun toggleFormatPainter() {
        if (hasCopiedFormat.value) {
            hasCopiedFormat.value = false
            copiedFormat = null
            toastMessage.value = "Sapu Format dinonaktifkan"
        } else {
            val index = _selectedParagraphIndex.value
            val currentDoc = _document.value
            if (index in currentDoc.paragraphs.indices) {
                val p = currentDoc.paragraphs[index]
                copiedFormat = p.runs.firstOrNull()?.copy()
                hasCopiedFormat.value = copiedFormat != null
                if (hasCopiedFormat.value) {
                    toastMessage.value = "Format disalin. Blok teks tujuan lalu ketuk 'Tempel Format'"
                }
            }
        }
    }

    fun copyFormat() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index in currentDoc.paragraphs.indices) {
            copiedFormat = currentDoc.paragraphs[index].runs.firstOrNull()?.copy()
            hasCopiedFormat.value = copiedFormat != null
            toastMessage.value = "Format disalin. Blok teks tujuan lalu ketuk 'Tempel Format'"
        }
    }

    fun pasteFormat() {
        applyFormatPainter()
    }

    fun applyFormatPainter() {
        val format = copiedFormat ?: return
        applyFormatToSelectionOrParagraph { run ->
            run.copy(
                bold = format.bold,
                italic = format.italic,
                underline = format.underline,
                strikethrough = format.strikethrough,
                subscript = format.subscript,
                superscript = format.superscript,
                fontColor = format.fontColor,
                highlightColor = format.highlightColor,
                fontSize = format.fontSize,
                fontFamily = format.fontFamily
            )
        }
        hasCopiedFormat.value = false
        copiedFormat = null
        toastMessage.value = "Format berhasil diterapkan"
    }

    fun toggleTextDirection() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        saveUndoState()
        val p = currentDoc.paragraphs[index]
        val updated = p.copy(isRtl = !p.isRtl)
        val list = currentDoc.paragraphs.toMutableList()
        list[index] = updated
        updateCurrentDoc(currentDoc.copy(paragraphs = list, isDirty = true))
        toastMessage.value = if (updated.isRtl) "Arah teks: Kanan ke Kiri (RTL)" else "Arah teks: Kiri ke Kanan (LTR)"
    }

    fun setNumberingStyle(style: NumberingStyle) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        saveUndoState()
        val p = currentDoc.paragraphs[index]
        val updated = p.copy(isNumbered = true, isBullet = false, numberingStyle = style)
        val list = currentDoc.paragraphs.toMutableList()
        list[index] = updated
        updateCurrentDoc(currentDoc.copy(paragraphs = list, isDirty = true))
        toastMessage.value = "Penomoran diubah ke ${style.displayName}"
    }

    fun setBulletStyle(style: BulletStyle) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        saveUndoState()
        val p = currentDoc.paragraphs[index]
        val updated = p.copy(isBullet = true, isNumbered = false, bulletStyle = style)
        val list = currentDoc.paragraphs.toMutableList()
        list[index] = updated
        updateCurrentDoc(currentDoc.copy(paragraphs = list, isDirty = true))
        toastMessage.value = "Poin diubah ke ${style.displayName}"
    }

    fun setCustomPaperSize(widthCm: Float, heightCm: Float) {
        saveUndoState()
        val currentDoc = _document.value
        val updated = currentDoc.copy(
            paperSize = PaperSize.Custom,
            customPaperWidthCm = widthCm.coerceIn(5f, 100f),
            customPaperHeightCm = heightCm.coerceIn(5f, 100f),
            isDirty = true
        )
        updateCurrentDoc(updated)
        toastMessage.value = "Ukuran kertas kustom diterapkan: ${widthCm} x ${heightCm} cm"
    }

    fun loadCustomFonts(context: Context) {
        val prefs = AppPreferences(context)
        customFonts.value = prefs.getCustomFonts()
    }

    fun importCustomFont(context: Context, fontName: String, uri: android.net.Uri) {
        try {
            val fontsDir = File(context.filesDir, "custom_fonts")
            if (!fontsDir.exists()) fontsDir.mkdirs()
            val destFile = File(fontsDir, "$fontName.ttf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            val prefs = AppPreferences(context)
            prefs.addCustomFont(fontName, destFile.absolutePath)
            loadCustomFonts(context)
            setFontFamily(fontName)
            toastMessage.value = "Font '$fontName' berhasil ditambahkan!"
        } catch (e: Exception) {
            toastMessage.value = "Gagal mengimpor font: ${e.message}"
        }
    }

    fun requestSwitchDocument(targetIndex: Int) {
        if (targetIndex !in openDocuments.value.indices || targetIndex == activeDocIndex.value) return
        val current = _document.value
        if (current.isDirty) {
            documentToPromptSave.value = current
            pendingAction = {
                doSwitchDocument(targetIndex)
            }
            showUnsavedDialog.value = true
        } else {
            doSwitchDocument(targetIndex)
        }
    }

    private fun doSwitchDocument(index: Int) {
        activeDocIndex.value = index
        _document.value = openDocuments.value[index]
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _selectedParagraphIndex.value = 0
        showDocSwitcherDialog.value = false
        toastMessage.value = "Beralih ke: ${_document.value.title}"
    }

    fun requestCloseDocument(index: Int) {
        val docs = openDocuments.value
        if (docs.size <= 1) {
            toastMessage.value = "Minimal satu lembar kerja harus tetap terbuka"
            return
        }
        val targetDoc = docs.getOrNull(index) ?: return
        if (targetDoc.isDirty) {
            documentToPromptSave.value = targetDoc
            pendingAction = {
                doCloseDocument(index)
            }
            showUnsavedDialog.value = true
        } else {
            doCloseDocument(index)
        }
    }

    private fun doCloseDocument(index: Int) {
        val currentDocs = openDocuments.value.toMutableList()
        currentDocs.removeAt(index)
        openDocuments.value = currentDocs
        val newIndex = (activeDocIndex.value).coerceAtMost(currentDocs.size - 1)
        activeDocIndex.value = newIndex
        _document.value = currentDocs[newIndex]
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _selectedParagraphIndex.value = 0
        toastMessage.value = "Lembar kerja ditutup"
    }

    fun requestNewDocument(templateType: Int = 0) {
        val current = _document.value
        if (current.isDirty) {
            documentToPromptSave.value = current
            pendingAction = {
                doCreateNewDocument(templateType)
            }
            showUnsavedDialog.value = true
        } else {
            doCreateNewDocument(templateType)
        }
    }

    private fun doCreateNewDocument(templateType: Int) {
        saveUndoState()
        val newDoc = when (templateType) {
            1 -> DocSampleData.getAcademicPaper()
            2 -> DocSampleData.getOfficialLetter()
            3 -> DocSampleData.getBlankDocument()
            else -> DocSampleData.getBlankDocument()
        }
        val docs = openDocuments.value.toMutableList()
        docs.add(newDoc)
        openDocuments.value = docs
        activeDocIndex.value = docs.size - 1
        _document.value = newDoc
        _selectedParagraphIndex.value = 0
        _showBackstage.value = false
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        toastMessage.value = "Dokumen baru dibuka: ${newDoc.fileName}"
    }

    fun confirmSaveAndProceed(context: Context) {
        saveDocument(context)
        showUnsavedDialog.value = false
        documentToPromptSave.value = null
        val action = pendingAction
        pendingAction = null
        action?.invoke()
    }

    fun confirmDiscardAndProceed() {
        showUnsavedDialog.value = false
        documentToPromptSave.value = null
        val action = pendingAction
        pendingAction = null
        action?.invoke()
    }

    fun cancelUnsavedPrompt() {
        showUnsavedDialog.value = false
        documentToPromptSave.value = null
        pendingAction = null
    }

    fun increaseFontSize() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        val currentSize = currentDoc.paragraphs[index].runs.firstOrNull()?.fontSize ?: 11f
        setFontSize((currentSize + 2f).coerceAtMost(72f))
    }

    fun decreaseFontSize() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        val currentSize = currentDoc.paragraphs[index].runs.firstOrNull()?.fontSize ?: 11f
        setFontSize((currentSize - 2f).coerceAtLeast(6f))
    }

    fun setAlignment(align: DocTextAlign) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(alignment = align)
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun setStyle(style: ParagraphStyle) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val color = when (style) {
            ParagraphStyle.Title -> 0xFF103F91
            ParagraphStyle.Heading1, ParagraphStyle.Heading2, ParagraphStyle.Heading3 -> 0xFF185ABD
            ParagraphStyle.Subtitle -> 0xFF5F6368
            else -> 0xFF202124
        }
        val updatedRuns = p.runs.map {
            it.copy(
                fontSize = style.defaultFontSize,
                bold = style.defaultBold,
                fontColor = color
            )
        }
        val align = if (style == ParagraphStyle.Title || style == ParagraphStyle.Subtitle) DocTextAlign.Center else DocTextAlign.Left

        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(
            style = style,
            runs = updatedRuns,
            alignment = align,
            spaceAfterPt = if (style == ParagraphStyle.Title) 16f else if (style == ParagraphStyle.Heading1) 6f else 4f
        )
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun toggleBullet() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val newBullet = !p.isBullet
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(
            isBullet = newBullet,
            isNumbered = false,
            style = if (newBullet) ParagraphStyle.ListBullet else ParagraphStyle.Normal
        )
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun toggleNumbering() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val newNumbered = !p.isNumbered
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(
            isNumbered = newNumbered,
            isBullet = false,
            style = if (newNumbered) ParagraphStyle.ListNumbered else ParagraphStyle.Normal
        )
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun increaseIndent() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(indentLeftCm = p.indentLeftCm + 0.5f)
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun decreaseIndent() {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(indentLeftCm = (p.indentLeftCm - 0.5f).coerceAtLeast(0f))
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun setFirstLineIndent(cm: Float) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in 0 until currentDoc.paragraphs.size) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(indentFirstLineCm = cm.coerceIn(0f, 5f))
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
    }

    fun setLeftMargin(cm: Float) {
        saveUndoState()
        val currentDoc = _document.value
        val updatedMargins = currentDoc.margins.copy(leftCm = cm.coerceIn(0.5f, 6.0f), presetName = "Kustom")
        _document.value = currentDoc.copy(margins = updatedMargins, isDirty = true)
    }

    fun setRightMargin(cm: Float) {
        saveUndoState()
        val currentDoc = _document.value
        val updatedMargins = currentDoc.margins.copy(rightCm = cm.coerceIn(0.5f, 6.0f), presetName = "Kustom")
        _document.value = currentDoc.copy(margins = updatedMargins, isDirty = true)
    }

    fun setTopMargin(cm: Float) {
        saveUndoState()
        val currentDoc = _document.value
        val updatedMargins = currentDoc.margins.copy(topCm = cm.coerceIn(0.5f, 6.0f), presetName = "Kustom")
        _document.value = currentDoc.copy(margins = updatedMargins, isDirty = true)
    }

    fun setBottomMargin(cm: Float) {
        saveUndoState()
        val currentDoc = _document.value
        val updatedMargins = currentDoc.margins.copy(bottomCm = cm.coerceIn(0.5f, 6.0f), presetName = "Kustom")
        _document.value = currentDoc.copy(margins = updatedMargins, isDirty = true)
    }

    fun setMarginsPreset(preset: DocMargins) {
        saveUndoState()
        _document.value = _document.value.copy(margins = preset, isDirty = true)
    }

    fun setOrientation(orientation: Orientation) {
        saveUndoState()
        _document.value = _document.value.copy(orientation = orientation, isDirty = true)
    }

    fun setPaperSize(size: PaperSize) {
        saveUndoState()
        _document.value = _document.value.copy(paperSize = size, isDirty = true)
    }

    fun insertPageBreak() {
        saveUndoState()
        val currentDoc = _document.value
        val index = _selectedParagraphIndex.value
        val newP = DocParagraph(runs = listOf(DocRun(text = "")), isPageBreakBefore = true)
        val newParagraphs = currentDoc.paragraphs.toMutableList()
        val insertPos = (index + 1).coerceIn(0, newParagraphs.size)
        newParagraphs.add(insertPos, newP)
        _document.value = currentDoc.copy(paragraphs = newParagraphs, isDirty = true)
        _selectedParagraphIndex.value = insertPos
    }

    fun insertTable(rows: Int, cols: Int) {
        saveUndoState()
        val currentDoc = _document.value
        val tableRows = mutableListOf<DocTableRow>()
        for (r in 0 until rows) {
            val cells = mutableListOf<DocTableCell>()
            for (c in 0 until cols) {
                cells.add(DocTableCell(text = if (r == 0) "Kolom ${c + 1}" else "Data $r-${c + 1}", bold = r == 0, isHeader = r == 0))
            }
            tableRows.add(DocTableRow(cells = cells, isHeader = r == 0))
        }
        val newTable = DocTable(rows = tableRows)
        val updatedTables = currentDoc.tables + newTable
        _document.value = currentDoc.copy(tables = updatedTables, isDirty = true)
        toastMessage.value = "Tabel ${rows}x${cols} berhasil disisipkan"
    }

    fun updateTableCell(tableIndex: Int, rowIndex: Int, colIndex: Int, newText: String) {
        val currentDoc = _document.value
        if (tableIndex !in currentDoc.tables.indices) return
        val table = currentDoc.tables[tableIndex]
        if (rowIndex !in table.rows.indices) return
        val row = table.rows[rowIndex]
        if (colIndex !in row.cells.indices) return

        val newCells = row.cells.toMutableList()
        newCells[colIndex] = newCells[colIndex].copy(text = newText)

        val newRows = table.rows.toMutableList()
        newRows[rowIndex] = row.copy(cells = newCells)

        val newTables = currentDoc.tables.toMutableList()
        newTables[tableIndex] = table.copy(rows = newRows)

        _document.value = currentDoc.copy(tables = newTables, isDirty = true)
    }

    fun insertShape(type: ShapeType) {
        saveUndoState()
        val shape = DocShape(shapeType = type, label = type.displayName)
        _document.value = _document.value.copy(shapes = _document.value.shapes + shape, isDirty = true)
        toastMessage.value = "Bentuk ${type.displayName} disisipkan"
    }

    fun insertFootnote(text: String) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        if (index !in currentDoc.paragraphs.indices) return
        saveUndoState()

        val p = currentDoc.paragraphs[index]
        val updatedParagraphs = currentDoc.paragraphs.toMutableList()
        updatedParagraphs[index] = p.copy(footnoteText = text)
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
        toastMessage.value = "Catatan kaki ditambahkan"
    }

    fun insertTableOfContents() {
        saveUndoState()
        val currentDoc = _document.value
        val headings = currentDoc.paragraphs.filter {
            it.style == ParagraphStyle.Heading1 || it.style == ParagraphStyle.Heading2 || it.style == ParagraphStyle.Title
        }

        val tocParagraphs = mutableListOf<DocParagraph>()
        tocParagraphs.add(
            DocParagraph(
                runs = listOf(DocRun(text = "DAFTAR ISI", bold = true, fontSize = 14f, fontColor = 0xFF103F91)),
                style = ParagraphStyle.Heading1,
                spaceAfterPt = 8f
            )
        )

        var pageEstimate = 1
        for (h in headings) {
            val indent = if (h.style == ParagraphStyle.Heading2) 0.8f else 0.0f
            val dotCount = (45 - h.fullText.length).coerceAtLeast(3)
            val dots = ".".repeat(dotCount)
            val text = "${h.fullText} $dots Hal. $pageEstimate"
            tocParagraphs.add(
                DocParagraph(
                    runs = listOf(DocRun(text = text, fontSize = 11f, fontColor = 0xFF202124)),
                    style = ParagraphStyle.Normal,
                    indentLeftCm = indent,
                    spaceAfterPt = 3f
                )
            )
            pageEstimate++
        }

        val newParagraphs = tocParagraphs + currentDoc.paragraphs
        _document.value = currentDoc.copy(paragraphs = newParagraphs, isDirty = true)
        toastMessage.value = "Daftar Isi otomatis berhasil dibuat"
    }

    fun addComment(content: String) {
        val index = _selectedParagraphIndex.value
        val currentDoc = _document.value
        val targetId = if (index in currentDoc.paragraphs.indices) currentDoc.paragraphs[index].id else "doc"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val comment = DocComment(
            author = currentDoc.author,
            timestamp = sdf.format(Date()),
            content = content,
            targetParagraphId = targetId
        )
        _document.value = currentDoc.copy(comments = currentDoc.comments + comment, isDirty = true)
        toastMessage.value = "Komentar berhasil ditambahkan"
    }

    fun toggleTrackChanges() {
        val currentDoc = _document.value
        val newState = !currentDoc.trackChangesEnabled
        _document.value = currentDoc.copy(trackChangesEnabled = newState)
        toastMessage.value = if (newState) "Lacak Perubahan: Aktif" else "Lacak Perubahan: Nonaktif"
    }

    fun setZoomLevel(level: Float) {
        _zoomLevel.value = level.coerceIn(0.5f, 2.0f)
    }

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 0.1f).coerceIn(0.5f, 2.0f)
    }

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 0.1f).coerceIn(0.5f, 2.0f)
    }

    fun resetZoom() {
        _zoomLevel.value = 1.0f
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleRuler() {
        _showRuler.value = !_showRuler.value
    }

    fun toggleNavigationPane() {
        _showNavigationPane.value = !_showNavigationPane.value
    }

    fun setNavPaneTab(tab: Int) {
        _navPaneTab.value = tab
    }

    fun toggleGridlines() {
        _showGridlines.value = !_showGridlines.value
    }

    fun executeSearchAndReplace() {
        val search = searchQuery.value
        val replace = replaceQuery.value
        if (search.isBlank()) return

        saveUndoState()
        val currentDoc = _document.value
        var count = 0
        val updatedParagraphs = currentDoc.paragraphs.map { p ->
            val updatedRuns = p.runs.map { r ->
                if (r.text.contains(search, ignoreCase = true)) {
                    val replaced = r.text.replace(search, replace, ignoreCase = true)
                    count++
                    r.copy(text = replaced)
                } else {
                    r
                }
            }
            p.copy(runs = updatedRuns)
        }

        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
        toastMessage.value = "Berhasil mengganti $count kecocokan"
    }

    fun applyMailMerge(recipientName: String, company: String, city: String) {
        saveUndoState()
        val currentDoc = _document.value
        val updatedParagraphs = currentDoc.paragraphs.map { p ->
            val updatedRuns = p.runs.map { r ->
                var text = r.text
                text = text.replace("«Nama_Penerima»", recipientName)
                text = text.replace("«Instansi_Perusahaan»", company)
                text = text.replace("«Kota_Tujuan»", city)
                r.copy(text = text)
            }
            p.copy(runs = updatedRuns)
        }
        _document.value = currentDoc.copy(paragraphs = updatedParagraphs, isDirty = true)
        toastMessage.value = "Mail Merge berhasil diterapkan untuk $recipientName"
    }

    fun createNewDocument(templateType: Int = 0) {
        saveUndoState()
        val newDoc = when (templateType) {
            1 -> DocSampleData.getAcademicPaper()
            2 -> DocSampleData.getOfficialLetter()
            3 -> DocSampleData.getBlankDocument()
            else -> DocSampleData.getBusinessProposal()
        }
        _document.value = newDoc
        _selectedParagraphIndex.value = 0
        _showBackstage.value = false
        toastMessage.value = "Dokumen baru dibuka: ${newDoc.fileName}"
    }

    fun loadSample(sampleName: String) {
        saveUndoState()
        val doc = when (sampleName) {
            "Laporan Riset & Skripsi.docx" -> DocSampleData.getAcademicPaper()
            "Surat Resmi & Mail Merge.docx" -> DocSampleData.getOfficialLetter()
            else -> DocSampleData.getBusinessProposal()
        }
        val docs = openDocuments.value.toMutableList()
        docs.add(doc)
        openDocuments.value = docs
        activeDocIndex.value = docs.size - 1
        _document.value = doc
        _selectedParagraphIndex.value = 0
        _showBackstage.value = false
        toastMessage.value = "Memuat $sampleName"
    }

    fun saveDocument(context: Context) {
        try {
            val doc = _document.value
            val file = File(context.filesDir, doc.fileName)
            FileOutputStream(file).use { fos ->
                DocxEngine.writeDocx(doc, fos)
            }
            val savedDoc = doc.copy(isDirty = false)
            updateCurrentDoc(savedDoc)
            toastMessage.value = "Tersimpan: ${file.name} (${file.length()} bytes DOCX)"
        } catch (e: Exception) {
            e.printStackTrace()
            toastMessage.value = "Gagal menyimpan: ${e.message}"
        }
    }

    fun exportDocxToStream(outputStream: OutputStream) {
        try {
            DocxEngine.writeDocx(_document.value, outputStream)
            val savedDoc = _document.value.copy(isDirty = false)
            updateCurrentDoc(savedDoc)
            toastMessage.value = "Dokumen DOCX berhasil diekspor"
        } catch (e: Exception) {
            toastMessage.value = "Gagal ekspor DOCX: ${e.message}"
        }
    }

    fun exportPdfToStream(outputStream: OutputStream) {
        try {
            DocxEngine.exportToPdf(_document.value, outputStream)
            toastMessage.value = "Dokumen PDF berhasil diekspor"
        } catch (e: Exception) {
            toastMessage.value = "Gagal ekspor PDF: ${e.message}"
        }
    }

    fun openDocxFromStream(inputStream: InputStream, fileName: String) {
        try {
            saveUndoState()
            val doc = DocxEngine.readDocx(inputStream, fileName)
            val docs = openDocuments.value.toMutableList()
            docs.add(doc)
            openDocuments.value = docs
            activeDocIndex.value = docs.size - 1
            _document.value = doc
            _selectedParagraphIndex.value = 0
            _showBackstage.value = false
            toastMessage.value = "Berhasil membuka DOCX: $fileName"
        } catch (e: Exception) {
            toastMessage.value = "Gagal membaca DOCX: ${e.message}"
        }
    }
}
