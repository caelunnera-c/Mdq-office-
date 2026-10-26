package com.example.mdqoffice.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdqoffice.model.*
import com.example.mdqoffice.theme.*

class RichDocVisualTransformation(
    private val runs: List<DocRun>,
    private val zoomLevel: Float
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        var offset = 0
        for (run in runs) {
            val start = offset.coerceIn(0, text.length)
            val end = (offset + run.text.length).coerceIn(0, text.length)
            offset += run.text.length
            if (start < end) {
                val fontWeight = if (run.bold) FontWeight.Bold else FontWeight.Normal
                val fontStyle = if (run.italic) FontStyle.Italic else FontStyle.Normal
                val textDeco = when {
                    run.underline && run.strikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    run.underline -> TextDecoration.Underline
                    run.strikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }
                val baselineShift = when {
                    run.superscript -> BaselineShift.Superscript
                    run.subscript -> BaselineShift.Subscript
                    else -> BaselineShift.None
                }
                val fontFamily = when (run.fontFamily) {
                    "Times New Roman", "Georgia", "Garamond", "Cambria", "Book Antiqua", "Palatino", "Noto Serif" -> FontFamily.Serif
                    "Courier New", "Consolas" -> FontFamily.Monospace
                    else -> FontFamily.SansSerif
                }
                builder.addStyle(
                    SpanStyle(
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        textDecoration = textDeco,
                        color = Color(run.fontColor),
                        background = run.highlightColor?.let { Color(it) } ?: Color.Transparent,
                        fontSize = (run.fontSize * zoomLevel).sp,
                        fontFamily = fontFamily,
                        baselineShift = baselineShift
                    ),
                    start,
                    end
                )
            }
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

@Composable
fun DesktopDocumentCanvas(
    document: MdqDocument,
    selectedParagraphIndex: Int,
    onSelectParagraph: (Int) -> Unit,
    onUpdateParagraphText: (Int, String) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onAddNewParagraph: (Int) -> Unit,
    onDeleteParagraph: (Int) -> Unit,
    onUpdateTableCell: (Int, Int, Int, String) -> Unit,
    zoomLevel: Float,
    viewMode: ViewMode,
    showGridlines: Boolean,
    hasCopiedFormat: Boolean,
    onApplyFormatPainter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hScrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (viewMode == ViewMode.ReadMode) Color(0xFFF4ECD8) else WorkspaceBackground)
            .horizontalScroll(hScrollState)
            .testTag("document_workspace")
    ) {
        // Physical paper dimensions in logical dp: 1cm = ~28dp
        val paperWidthCm = document.effectivePaperWidthCm
        val basePaperWidthDp = (paperWidthCm * 28f).dp
        val paperWidth = (basePaperWidthDp.value * zoomLevel).dp

        val leftMarginDp = (document.margins.leftCm * 28f * zoomLevel).dp
        val rightMarginDp = (document.margins.rightCm * 28f * zoomLevel).dp
        val topMarginDp = (document.margins.topCm * 28f * zoomLevel).dp
        val bottomMarginDp = (document.margins.bottomCm * 28f * zoomLevel).dp

        // Split paragraphs into pages
        val pageList = remember(document.paragraphs) {
            val pages = mutableListOf<MutableList<IndexedValue<DocParagraph>>>()
            var currentPage = mutableListOf<IndexedValue<DocParagraph>>()
            var charAccumulator = 0

            document.paragraphs.forEachIndexed { idx, p ->
                val pLength = p.fullText.length + 50
                if (p.isPageBreakBefore || (charAccumulator > 1200 && currentPage.isNotEmpty())) {
                    pages.add(currentPage)
                    currentPage = mutableListOf()
                    charAccumulator = 0
                }
                currentPage.add(IndexedValue(idx, p))
                charAccumulator += pLength
            }
            if (currentPage.isNotEmpty() || pages.isEmpty()) {
                pages.add(currentPage)
            }
            pages
        }

        LazyColumn(
            modifier = Modifier.widthIn(min = paperWidth + 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
        ) {
            itemsIndexed(pageList) { pageIdx, pageParagraphs ->
                val pageNumber = pageIdx + 1
                val totalPages = pageList.size

                // Paper Sheet with exact desktop proportions
                Surface(
                    color = PaperWhite,
                    shadowElevation = if (viewMode == ViewMode.WebLayout) 0.dp else 5.dp,
                    border = if (viewMode == ViewMode.WebLayout) null else androidx.compose.foundation.BorderStroke(1.dp, PaperBorder),
                    modifier = Modifier
                        .width(if (viewMode == ViewMode.WebLayout) Modifier.fillMaxWidth().let { basePaperWidthDp } else paperWidth)
                        .testTag("paper_sheet_page_$pageNumber")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (viewMode == ViewMode.WebLayout) 16.dp else leftMarginDp,
                                end = if (viewMode == ViewMode.WebLayout) 16.dp else rightMarginDp,
                                top = if (viewMode == ViewMode.WebLayout) 16.dp else topMarginDp,
                                bottom = if (viewMode == ViewMode.WebLayout) 16.dp else bottomMarginDp
                            )
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Paragraphs on this page
                        pageParagraphs.forEach { (origIndex, paragraph) ->
                            val isSelected = selectedParagraphIndex == origIndex

                            ParagraphView(
                                paragraph = paragraph,
                                isSelected = isSelected,
                                zoomLevel = zoomLevel,
                                hasCopiedFormat = hasCopiedFormat,
                                onApplyFormatPainter = onApplyFormatPainter,
                                onSelect = { onSelectParagraph(origIndex) },
                                onTextChange = { onUpdateParagraphText(origIndex, it) },
                                onSelectionChange = onSelectionChange,
                                onEnterPressed = { onAddNewParagraph(origIndex) },
                                onDeletePressed = { onDeleteParagraph(origIndex) }
                            )

                            // Render footnote if present
                            if (!paragraph.footnoteText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "[1] ${paragraph.footnoteText}",
                                    fontSize = (8.5f * zoomLevel).sp,
                                    color = OfficeBluePrimary,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }

                        // Tables
                        if (pageIdx == 0 && document.tables.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            document.tables.forEachIndexed { tIdx, table ->
                                DocumentTableView(
                                    table = table,
                                    zoomLevel = zoomLevel,
                                    onCellChange = { rIdx, cIdx, text ->
                                        onUpdateTableCell(tIdx, rIdx, cIdx, text)
                                    }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // Shapes
                        if (pageIdx == 0 && document.shapes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                document.shapes.forEach { shape ->
                                    Surface(
                                        color = Color(shape.fillColor),
                                        shape = when (shape.shapeType) {
                                            ShapeType.RoundedRectangle -> RoundedCornerShape(8.dp)
                                            ShapeType.Oval -> RoundedCornerShape(20.dp)
                                            else -> RoundedCornerShape(2.dp)
                                        },
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(36.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = shape.label,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Footer
                        if (viewMode == ViewMode.PrintLayout) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Halaman $pageNumber dari $totalPages",
                                    fontSize = (8.5f * zoomLevel).sp,
                                    color = Color(0xFF9E9E9E),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Page Separator
                if (pageIdx < pageList.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 14.dp)
                            .width(paperWidth)
                            .height(2.dp)
                            .background(Color(0xFFCBD0D8))
                    )
                }
            }
        }
    }
}

@Composable
fun ParagraphView(
    paragraph: DocParagraph,
    isSelected: Boolean,
    zoomLevel: Float,
    hasCopiedFormat: Boolean,
    onApplyFormatPainter: () -> Unit,
    onSelect: () -> Unit,
    onTextChange: (String) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onEnterPressed: () -> Unit,
    onDeletePressed: () -> Unit
) {
    val firstRun = paragraph.runs.firstOrNull() ?: DocRun(text = "")
    val fontSizeSp = (firstRun.fontSize * zoomLevel).sp
    val fontFamily = when (firstRun.fontFamily) {
        "Times New Roman", "Georgia", "Garamond", "Cambria", "Book Antiqua", "Palatino", "Noto Serif" -> FontFamily.Serif
        "Courier New", "Consolas" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    val textAlign = when (paragraph.alignment) {
        DocTextAlign.Center -> TextAlign.Center
        DocTextAlign.Right -> TextAlign.Right
        DocTextAlign.Justify -> TextAlign.Justify
        DocTextAlign.Left -> TextAlign.Left
    }

    val fontColor = Color(firstRun.fontColor)
    val fontWeight = if (firstRun.bold) FontWeight.Bold else FontWeight.Normal
    val fontStyle = if (firstRun.italic) FontStyle.Italic else FontStyle.Normal

    val indentLeftDp = (paragraph.indentLeftCm * 28f * zoomLevel).dp
    val firstLineIndentDp = (paragraph.indentFirstLineCm * 28f * zoomLevel).dp
    val spaceBeforeDp = (paragraph.spaceBeforePt * zoomLevel * 0.75f).dp
    val spaceAfterDp = (paragraph.spaceAfterPt * zoomLevel * 0.75f).dp

    var tfValue by remember(paragraph.fullText) {
        mutableStateOf(TextFieldValue(text = paragraph.fullText, selection = TextRange.Zero))
    }

    val layoutDirection = if (paragraph.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = indentLeftDp + if (paragraph.indentFirstLineCm > 0) firstLineIndentDp else 0.dp,
                    top = spaceBeforeDp,
                    bottom = spaceAfterDp
                )
                .clickable { onSelect() }
                .testTag("paragraph_${paragraph.id.take(6)}")
        ) {
            // Floating format painter paste suggestion when format is copied and text is selected
            if (hasCopiedFormat && isSelected && tfValue.selection.length > 0) {
                Surface(
                    color = Color(0xFFFFF9C4),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF9A825)),
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .clickable { onApplyFormatPainter() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = null,
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Tempel Format ke Bagian yang Diblok",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (paragraph.isBullet) {
                    Text(
                        text = "${paragraph.bulletStyle.symbol} ",
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.Bold,
                        color = fontColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                } else if (paragraph.isNumbered) {
                    Text(
                        text = "${paragraph.formattedNumber()} ",
                        fontSize = fontSizeSp,
                        fontWeight = FontWeight.Bold,
                        color = fontColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                BasicTextField(
                    value = tfValue,
                    onValueChange = { newTf ->
                        val textChanged = newTf.text != tfValue.text
                        val selectionChanged = newTf.selection != tfValue.selection
                        tfValue = newTf
                        if (textChanged) {
                            onTextChange(newTf.text)
                        }
                        if (selectionChanged || textChanged) {
                            onSelectionChange(newTf.selection.start, newTf.selection.end)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) onSelect() },
                    visualTransformation = RichDocVisualTransformation(paragraph.runs, zoomLevel),
                    textStyle = TextStyle(
                        fontSize = fontSizeSp,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        fontFamily = fontFamily,
                        textAlign = textAlign,
                        textDirection = if (paragraph.isRtl) TextDirection.Rtl else TextDirection.Ltr,
                        color = fontColor,
                        lineHeight = (firstRun.fontSize * paragraph.lineSpacingMultiplier * 1.3f * zoomLevel).sp
                    ),
                    cursorBrush = SolidColor(OfficeBluePrimary),
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions(
                        onDone = { onEnterPressed() }
                    )
                )
            }
        }
    }
}

@Composable
fun DocumentTableView(
    table: DocTable,
    zoomLevel: Float,
    onCellChange: (Int, Int, String) -> Unit
) {
    Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB0B8C4)),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doc_table_${table.id.take(6)}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            table.rows.forEachIndexed { rIdx, row ->
                val isHeader = row.isHeader || rIdx == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isHeader) Color(0xFFE8EEF5) else Color.White)
                        .border(width = 0.5.dp, color = Color(0xFFD0D7DE)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.cells.forEachIndexed { cIdx, cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(width = 0.5.dp, color = Color(0xFFD0D7DE))
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            var cellVal by remember(cell.text) { mutableStateOf(cell.text) }
                            BasicTextField(
                                value = cellVal,
                                onValueChange = {
                                    cellVal = it
                                    onCellChange(rIdx, cIdx, it)
                                },
                                textStyle = TextStyle(
                                    fontSize = (10f * zoomLevel).sp,
                                    fontWeight = if (isHeader || cell.bold) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isHeader) OfficeBlueDark else TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
