package com.example.mdqoffice.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.mdqoffice.model.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object DocxEngine {

    /**
     * Reads a real .docx file and builds an MdqDocument model.
     */
    fun readDocx(inputStream: InputStream, originalFileName: String = "Dokumen.docx"): MdqDocument {
        var documentXmlString: String? = null
        try {
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val writer = StringWriter()
                        val reader = BufferedReader(InputStreamReader(zis, "UTF-8"))
                        var line: String? = reader.readLine()
                        while (line != null) {
                            writer.write(line)
                            line = reader.readLine()
                        }
                        documentXmlString = writer.toString()
                        break
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (documentXmlString.isNullOrBlank()) {
            return MdqDocument(
                title = originalFileName.removeSuffix(".docx"),
                fileName = originalFileName,
                paragraphs = listOf(DocParagraph(runs = listOf(DocRun(text = "Gagal memuat konten DOCX.")))),
                isDirty = false
            )
        }

        return parseDocumentXml(documentXmlString, originalFileName)
    }

    private fun parseDocumentXml(xmlContent: String, fileName: String): MdqDocument {
        val paragraphs = mutableListOf<DocParagraph>()
        val tables = mutableListOf<DocTable>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var inParagraph = false
            var inRun = false
            var inTable = false
            var inTableRow = false
            var inTableCell = false

            // Current state variables
            var currentRuns = mutableListOf<DocRun>()
            var currentRunText = StringBuilder()
            var currentRunBold = false
            var currentRunItalic = false
            var currentRunUnderline = false
            var currentRunStrike = false
            var currentRunColor = 0xFF202124
            var currentRunSize = 11f
            var currentRunFont = "Calibri"

            var currentParagraphStyle = ParagraphStyle.Normal
            var currentParagraphAlign = DocTextAlign.Left
            var currentIndentLeft = 0f
            var currentIndentFirstLine = 0f
            var currentLineSpacing = 1.15f
            var isPageBreak = false
            var isBullet = false
            var isNumbered = false

            // Table state
            var currentRows = mutableListOf<DocTableRow>()
            var currentCells = mutableListOf<DocTableCell>()
            var currentCellText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name ?: ""

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "tbl" -> {
                                inTable = true
                                currentRows = mutableListOf()
                            }
                            "tr" -> {
                                inTableRow = true
                                currentCells = mutableListOf()
                            }
                            "tc" -> {
                                inTableCell = true
                                currentCellText = StringBuilder()
                            }
                            "p" -> {
                                if (!inTableCell) {
                                    inParagraph = true
                                    currentRuns = mutableListOf()
                                    currentParagraphStyle = ParagraphStyle.Normal
                                    currentParagraphAlign = DocTextAlign.Left
                                    currentIndentLeft = 0f
                                    currentIndentFirstLine = 0f
                                    currentLineSpacing = 1.15f
                                    isPageBreak = false
                                    isBullet = false
                                    isNumbered = false
                                }
                            }
                            "pStyle" -> {
                                val styleVal = parser.getAttributeValue(null, "val") ?: ""
                                currentParagraphStyle = when {
                                    styleVal.contains("Heading1", true) -> ParagraphStyle.Heading1
                                    styleVal.contains("Heading2", true) -> ParagraphStyle.Heading2
                                    styleVal.contains("Heading3", true) -> ParagraphStyle.Heading3
                                    styleVal.contains("Title", true) -> ParagraphStyle.Title
                                    styleVal.contains("Subtitle", true) -> ParagraphStyle.Subtitle
                                    styleVal.contains("Quote", true) -> ParagraphStyle.Quote
                                    else -> ParagraphStyle.Normal
                                }
                            }
                            "jc" -> {
                                val alignVal = parser.getAttributeValue(null, "val") ?: ""
                                currentParagraphAlign = when (alignVal.lowercase()) {
                                    "center" -> DocTextAlign.Center
                                    "right" -> DocTextAlign.Right
                                    "both" -> DocTextAlign.Justify
                                    else -> DocTextAlign.Left
                                }
                            }
                            "ind" -> {
                                val leftVal = parser.getAttributeValue(null, "left")?.toFloatOrNull()
                                val firstLineVal = parser.getAttributeValue(null, "firstLine")?.toFloatOrNull()
                                if (leftVal != null) currentIndentLeft = leftVal / 567f
                                if (firstLineVal != null) currentIndentFirstLine = firstLineVal / 567f
                            }
                            "spacing" -> {
                                val lineVal = parser.getAttributeValue(null, "line")?.toFloatOrNull()
                                if (lineVal != null) currentLineSpacing = (lineVal / 240f).coerceIn(1.0f, 3.0f)
                            }
                            "numPr" -> {
                                isBullet = true
                            }
                            "br" -> {
                                val type = parser.getAttributeValue(null, "type")
                                if (type == "page") {
                                    isPageBreak = true
                                }
                            }
                            "r" -> {
                                inRun = true
                                currentRunText = StringBuilder()
                                currentRunBold = currentParagraphStyle.defaultBold
                                currentRunItalic = false
                                currentRunUnderline = false
                                currentRunStrike = false
                                currentRunColor = 0xFF202124
                                currentRunSize = currentParagraphStyle.defaultFontSize
                                currentRunFont = "Calibri"
                            }
                            "b" -> currentRunBold = true
                            "i" -> currentRunItalic = true
                            "u" -> currentRunUnderline = true
                            "strike" -> currentRunStrike = true
                            "color" -> {
                                val hex = parser.getAttributeValue(null, "val") ?: ""
                                if (hex.length == 6) {
                                    currentRunColor = (0xFF000000 or hex.toLong(16))
                                }
                            }
                            "sz" -> {
                                val szVal = parser.getAttributeValue(null, "val")?.toFloatOrNull()
                                if (szVal != null) currentRunSize = szVal / 2f
                            }
                            "rFonts" -> {
                                val ascii = parser.getAttributeValue(null, "ascii")
                                if (!ascii.isNullOrBlank()) currentRunFont = ascii
                            }
                            "t" -> {
                                val text = parser.nextText()
                                if (inTableCell) {
                                    if (currentCellText.isNotEmpty()) currentCellText.append(" ")
                                    currentCellText.append(text)
                                } else if (inRun) {
                                    currentRunText.append(text)
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "r" -> {
                                if (inRun) {
                                    currentRuns.add(
                                        DocRun(
                                            text = currentRunText.toString(),
                                            bold = currentRunBold,
                                            italic = currentRunItalic,
                                            underline = currentRunUnderline,
                                            strikethrough = currentRunStrike,
                                            fontColor = currentRunColor,
                                            fontSize = currentRunSize,
                                            fontFamily = currentRunFont
                                        )
                                    )
                                    inRun = false
                                }
                            }
                            "p" -> {
                                if (inParagraph && !inTableCell) {
                                    if (currentRuns.isEmpty()) {
                                        currentRuns.add(DocRun(text = ""))
                                    }
                                    paragraphs.add(
                                        DocParagraph(
                                            runs = currentRuns.toList(),
                                            style = currentParagraphStyle,
                                            alignment = currentParagraphAlign,
                                            indentLeftCm = currentIndentLeft,
                                            indentFirstLineCm = currentIndentFirstLine,
                                            lineSpacingMultiplier = currentLineSpacing,
                                            isPageBreakBefore = isPageBreak,
                                            isBullet = isBullet,
                                            isNumbered = isNumbered
                                        )
                                    )
                                    inParagraph = false
                                }
                            }
                            "tc" -> {
                                currentCells.add(DocTableCell(text = currentCellText.toString().trim()))
                                inTableCell = false
                            }
                            "tr" -> {
                                currentRows.add(DocTableRow(cells = currentCells.toList()))
                                inTableRow = false
                            }
                            "tbl" -> {
                                if (currentRows.isNotEmpty()) {
                                    tables.add(DocTable(rows = currentRows.toList()))
                                }
                                inTable = false
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (paragraphs.isEmpty()) {
            paragraphs.add(DocParagraph(runs = listOf(DocRun(text = "Dokumen kosong."))))
        }

        val baseName = fileName.removeSuffix(".docx")
        return MdqDocument(
            title = baseName,
            fileName = fileName,
            paragraphs = paragraphs,
            tables = tables,
            isDirty = false
        )
    }

    /**
     * Serializes an MdqDocument to a genuine .docx file (OpenXML Zip format).
     */
    fun writeDocx(document: MdqDocument, outputStream: OutputStream) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
            // 1. [Content_Types].xml
            writeZipFile(
                zos,
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""
            )

            // 2. _rels/.rels
            writeZipFile(
                zos,
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
            )

            // 3. word/_rels/document.xml.rels
            writeZipFile(
                zos,
                "word/_rels/document.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            )

            // 4. word/styles.xml
            writeZipFile(
                zos,
                "word/styles.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
        <w:sz w:val="22"/>
      </w:rPr>
    </w:rPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:rPr>
      <w:b/>
      <w:color w:val="185ABD"/>
      <w:sz w:val="32"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:rPr>
      <w:b/>
      <w:color w:val="185ABD"/>
      <w:sz w:val="26"/>
    </w:rPr>
  </w:style>
</w:styles>"""
            )

            // 5. word/document.xml
            val docXml = buildDocumentXml(document)
            writeZipFile(zos, "word/document.xml", docXml)
        }
    }

    private fun writeZipFile(zos: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(Charsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun buildDocumentXml(document: MdqDocument): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.append("<w:body>")

        for (p in document.paragraphs) {
            sb.append("<w:p>")
            sb.append("<w:pPr>")

            // Paragraph style
            val styleId = when (p.style) {
                ParagraphStyle.Heading1 -> "Heading1"
                ParagraphStyle.Heading2 -> "Heading2"
                ParagraphStyle.Heading3 -> "Heading3"
                ParagraphStyle.Title -> "Title"
                ParagraphStyle.Subtitle -> "Subtitle"
                ParagraphStyle.Quote -> "Quote"
                else -> "Normal"
            }
            sb.append("""<w:pStyle w:val="$styleId"/>""")

            // Alignment
            val jcVal = when (p.alignment) {
                DocTextAlign.Center -> "center"
                DocTextAlign.Right -> "right"
                DocTextAlign.Justify -> "both"
                DocTextAlign.Left -> "left"
            }
            sb.append("""<w:jc w:val="$jcVal"/>""")

            // Indents (twips = cm * 567)
            val leftTwips = (p.indentLeftCm * 567).toInt()
            val firstLineTwips = (p.indentFirstLineCm * 567).toInt()
            val rightTwips = (p.indentRightCm * 567).toInt()
            if (leftTwips != 0 || firstLineTwips != 0 || rightTwips != 0) {
                sb.append("""<w:ind w:left="$leftTwips" w:firstLine="$firstLineTwips" w:right="$rightTwips"/>""")
            }

            // Spacing
            val beforeTwips = (p.spaceBeforePt * 20).toInt()
            val afterTwips = (p.spaceAfterPt * 20).toInt()
            val lineTwips = (p.lineSpacingMultiplier * 240).toInt()
            sb.append("""<w:spacing w:before="$beforeTwips" w:after="$afterTwips" w:line="$lineTwips" w:lineRule="auto"/>""")

            sb.append("</w:pPr>")

            // Page break before
            if (p.isPageBreakBefore) {
                sb.append("""<w:r><w:br w:type="page"/></w:r>""")
            }

            // Runs
            for (r in p.runs) {
                sb.append("<w:r>")
                sb.append("<w:rPr>")

                if (r.bold) sb.append("<w:b/>")
                if (r.italic) sb.append("<w:i/>")
                if (r.underline) sb.append("""<w:u w:val="single"/>""")
                if (r.strikethrough) sb.append("<w:strike/>")

                val hexColor = String.format("%06X", (r.fontColor and 0x00FFFFFF))
                sb.append("""<w:color w:val="$hexColor"/>""")

                val halfPt = (r.fontSize * 2).toInt()
                sb.append("""<w:sz w:val="$halfPt"/>""")
                sb.append("""<w:rFonts w:ascii="${escapeXml(r.fontFamily)}" w:hAnsi="${escapeXml(r.fontFamily)}"/>""")

                sb.append("</w:rPr>")
                sb.append("""<w:t xml:space="preserve">${escapeXml(r.text)}</w:t>""")
                sb.append("</w:r>")
            }

            sb.append("</w:p>")
        }

        // Tables
        for (t in document.tables) {
            sb.append("<w:tbl>")
            sb.append("""<w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/></w:tblBorders></w:tblPr>""")
            for (row in t.rows) {
                sb.append("<w:tr>")
                for (cell in row.cells) {
                    sb.append("<w:tc>")
                    sb.append("<w:p>")
                    sb.append("<w:r>")
                    if (cell.bold) {
                        sb.append("<w:rPr><w:b/></w:rPr>")
                    }
                    sb.append("""<w:t xml:space="preserve">${escapeXml(cell.text)}</w:t>""")
                    sb.append("</w:r>")
                    sb.append("</w:p>")
                    sb.append("</w:tc>")
                }
                sb.append("</w:tr>")
            }
            sb.append("</w:tbl>")
        }

        // Page setup & margins in section properties
        val widthTwips = if (document.orientation == Orientation.Portrait) {
            (document.paperSize.widthCm * 567).toInt()
        } else {
            (document.paperSize.heightCm * 567).toInt()
        }
        val heightTwips = if (document.orientation == Orientation.Portrait) {
            (document.paperSize.heightCm * 567).toInt()
        } else {
            (document.paperSize.widthCm * 567).toInt()
        }
        val orient = if (document.orientation == Orientation.Portrait) "portrait" else "landscape"

        val topTwips = (document.margins.topCm * 567).toInt()
        val bottomTwips = (document.margins.bottomCm * 567).toInt()
        val leftTwips = (document.margins.leftCm * 567).toInt()
        val rightTwips = (document.margins.rightCm * 567).toInt()

        sb.append("<w:sectPr>")
        sb.append("""<w:pgSz w:w="$widthTwips" w:h="$heightTwips" w:orient="$orient"/>""")
        sb.append("""<w:pgMar w:top="$topTwips" w:bottom="$bottomTwips" w:left="$leftTwips" w:right="$rightTwips" w:header="720" w:footer="720"/>""")
        sb.append("</w:sectPr>")

        sb.append("</w:body>")
        sb.append("</w:document>")

        return sb.toString()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Exports the document to a standard PDF using Android's native PdfDocument.
     */
    fun exportToPdf(document: MdqDocument, outputStream: OutputStream) {
        val pdf = PdfDocument()

        // Page dimensions in points (72 points = 1 inch, 1 cm = 28.3465 pt)
        val pageWidthPt = if (document.orientation == Orientation.Portrait) {
            (document.paperSize.widthCm * 28.3465f).toInt()
        } else {
            (document.paperSize.heightCm * 28.3465f).toInt()
        }
        val pageHeightPt = if (document.orientation == Orientation.Portrait) {
            (document.paperSize.heightCm * 28.3465f).toInt()
        } else {
            (document.paperSize.widthCm * 28.3465f).toInt()
        }

        val marginLeftPt = document.margins.leftCm * 28.3465f
        val marginRightPt = document.margins.rightCm * 28.3465f
        val marginTopPt = document.margins.topCm * 28.3465f
        val marginBottomPt = document.margins.bottomCm * 28.3465f
        val printableWidth = pageWidthPt - marginLeftPt - marginRightPt

        val paint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.rgb(32, 33, 36)
        }

        val headerPaint = Paint().apply {
            isAntiAlias = true
            color = AndroidColor.rgb(100, 100, 100)
            textSize = 9f
        }

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNum).create()
        var page = pdf.startPage(pageInfo)
        var canvas = page.canvas

        var cursorY = marginTopPt + 20f

        fun drawHeaderAndFooter(c: Canvas, pNum: Int) {
            // Header
            if (document.headerText.isNotBlank()) {
                c.drawText(document.headerText, marginLeftPt, marginTopPt - 8f, headerPaint)
                c.drawLine(marginLeftPt, marginTopPt - 4f, pageWidthPt - marginRightPt, marginTopPt - 4f, headerPaint)
            }
            // Footer
            val footerStr = "Halaman $pNum"
            c.drawText(footerStr, pageWidthPt - marginRightPt - 50f, pageHeightPt - marginBottomPt + 15f, headerPaint)
            c.drawLine(marginLeftPt, pageHeightPt - marginBottomPt + 4f, pageWidthPt - marginRightPt, pageHeightPt - marginBottomPt + 4f, headerPaint)
        }

        drawHeaderAndFooter(canvas, pageNum)

        for (p in document.paragraphs) {
            val isHeading = p.style == ParagraphStyle.Heading1 || p.style == ParagraphStyle.Heading2 || p.style == ParagraphStyle.Title
            val fontSize = when (p.style) {
                ParagraphStyle.Title -> 22f
                ParagraphStyle.Heading1 -> 16f
                ParagraphStyle.Heading2 -> 13f
                ParagraphStyle.Heading3 -> 12f
                else -> 10.5f
            }

            paint.textSize = fontSize
            paint.typeface = if (isHeading) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            paint.color = if (isHeading) AndroidColor.rgb(24, 90, 189) else AndroidColor.rgb(32, 33, 36)

            val lineHeight = fontSize * p.lineSpacingMultiplier * 1.3f

            // Check if page break
            if (p.isPageBreakBefore || cursorY + lineHeight > (pageHeightPt - marginBottomPt)) {
                pdf.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNum).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas
                cursorY = marginTopPt + 20f
                drawHeaderAndFooter(canvas, pageNum)
            }

            val prefix = if (p.isBullet) "•  " else if (p.isNumbered) "${p.numberIndex}.  " else ""
            val fullText = prefix + p.fullText

            // Simple line wrapping
            val words = fullText.split(" ")
            var currentLine = ""
            for (w in words) {
                val testLine = if (currentLine.isEmpty()) w else "$currentLine $w"
                if (paint.measureText(testLine) <= printableWidth) {
                    currentLine = testLine
                } else {
                    if (cursorY + lineHeight > (pageHeightPt - marginBottomPt)) {
                        pdf.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNum).create()
                        page = pdf.startPage(pageInfo)
                        canvas = page.canvas
                        cursorY = marginTopPt + 20f
                        drawHeaderAndFooter(canvas, pageNum)
                    }
                    val x = when (p.alignment) {
                        DocTextAlign.Center -> marginLeftPt + (printableWidth - paint.measureText(currentLine)) / 2f
                        DocTextAlign.Right -> pageWidthPt - marginRightPt - paint.measureText(currentLine)
                        else -> marginLeftPt + (p.indentLeftCm * 28.3465f)
                    }
                    canvas.drawText(currentLine, x, cursorY, paint)
                    cursorY += lineHeight
                    currentLine = w
                }
            }

            if (currentLine.isNotEmpty()) {
                val x = when (p.alignment) {
                    DocTextAlign.Center -> marginLeftPt + (printableWidth - paint.measureText(currentLine)) / 2f
                    DocTextAlign.Right -> pageWidthPt - marginRightPt - paint.measureText(currentLine)
                    else -> marginLeftPt + (p.indentLeftCm * 28.3465f)
                }
                canvas.drawText(currentLine, x, cursorY, paint)
                cursorY += lineHeight + (p.spaceAfterPt * 0.75f)
            }
        }

        // Render tables
        for (tbl in document.tables) {
            val colCount = tbl.rows.firstOrNull()?.cells?.size ?: 1
            val colWidth = printableWidth / colCount
            val rowHeight = 22f

            for (row in tbl.rows) {
                if (cursorY + rowHeight > (pageHeightPt - marginBottomPt)) {
                    pdf.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNum).create()
                    page = pdf.startPage(pageInfo)
                    canvas = page.canvas
                    cursorY = marginTopPt + 20f
                    drawHeaderAndFooter(canvas, pageNum)
                }

                var colX = marginLeftPt
                for (cell in row.cells) {
                    // Draw cell border
                    val borderPaint = Paint().apply {
                        color = AndroidColor.LTGRAY
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawRect(colX, cursorY, colX + colWidth, cursorY + rowHeight, borderPaint)

                    // Draw text
                    paint.textSize = 9.5f
                    paint.typeface = if (cell.bold || row.isHeader) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    paint.color = AndroidColor.BLACK
                    canvas.drawText(cell.text, colX + 4f, cursorY + 15f, paint)

                    colX += colWidth
                }
                cursorY += rowHeight
            }
            cursorY += 12f
        }

        pdf.finishPage(page)
        pdf.writeTo(outputStream)
        pdf.close()
    }
}
