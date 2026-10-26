package com.example.mdqoffice.model

import java.util.UUID

enum class PaperSize(val displayName: String, val widthCm: Float, val heightCm: Float) {
    A4("A4 (21.0 x 29.7 cm)", 21.0f, 29.7f),
    F4("F4 / Folio (21.5 x 33.0 cm)", 21.5f, 33.0f),
    Letter("Letter (21.59 x 27.94 cm)", 21.59f, 27.94f),
    Legal("Legal (21.59 x 35.56 cm)", 21.59f, 35.56f),
    A3("A3 (29.7 x 42.0 cm)", 29.7f, 42.0f),
    A5("A5 (14.8 x 21.0 cm)", 14.8f, 21.0f),
    B5("B5 (17.6 x 25.0 cm)", 17.6f, 25.0f),
    Executive("Executive (18.41 x 26.67 cm)", 18.41f, 26.67f),
    Tabloid("Tabloid (27.94 x 43.18 cm)", 27.94f, 43.18f),
    Custom("Kustom...", 21.0f, 29.7f)
}

enum class NumberingStyle(val displayName: String) {
    Decimal("1, 2, 3..."),
    DecimalParen("1), 2), 3)..."),
    UpperAlpha("A, B, C..."),
    LowerAlpha("a, b, c..."),
    LowerAlphaParen("a), b), c)..."),
    UpperRoman("I, II, III..."),
    LowerRoman("i, ii, iii..."),
    ArabicHijaiyah("أ, ب, ج, د..."),
    ArabicEastern("١, ٢, ٣...")
}

enum class BulletStyle(val symbol: String, val displayName: String) {
    Dot("•", "Bulat (•)"),
    Circle("○", "Lingkaran (○)"),
    Square("■", "Kotak (■)"),
    Checkmark("✓", "Centang (✓)"),
    Arrow("➢", "Panah (➢)"),
    Diamond("◆", "Belah Ketupat (◆)")
}

enum class CaseType(val displayName: String) {
    Sentence("Huruf Kalimat (Sentence case)"),
    Lower("huruf kecil semua (lowercase)"),
    Upper("HURUF BESAR SEMUA (UPPERCASE)"),
    CapitalizeEach("Huruf Besar Setiap Kata"),
    Toggle("tOGGLE cASE")
}

enum class Orientation(val displayName: String) {
    Portrait("Potret"),
    Landscape("Lanskap")
}

data class DocMargins(
    val topCm: Float = 2.54f,
    val bottomCm: Float = 2.54f,
    val leftCm: Float = 2.54f,
    val rightCm: Float = 2.54f,
    val presetName: String = "Normal"
) {
    companion object {
        val Normal = DocMargins(2.54f, 2.54f, 2.54f, 2.54f, "Normal")
        val Narrow = DocMargins(1.27f, 1.27f, 1.27f, 1.27f, "Sempit")
        val Moderate = DocMargins(2.54f, 2.54f, 1.91f, 1.91f, "Moderat")
        val Wide = DocMargins(2.54f, 2.54f, 5.08f, 5.08f, "Lebar")
        val Skripsi = DocMargins(4.0f, 3.0f, 4.0f, 3.0f, "Skripsi (4-4-3-3)")
        val Mirrored = DocMargins(2.54f, 2.54f, 3.18f, 2.54f, "Cermin")
        val Office2003 = DocMargins(2.54f, 2.54f, 3.18f, 3.18f, "Word 2003")
    }
}

enum class ParagraphStyle(val displayName: String, val defaultFontSize: Float, val defaultBold: Boolean) {
    Normal("Normal", 11f, false),
    NoSpacing("Tanpa Spasi", 11f, false),
    Heading1("Heading 1", 16f, true),
    Heading2("Heading 2", 13f, true),
    Heading3("Heading 3", 12f, true),
    Title("Judul", 24f, true),
    Subtitle("Subjudul", 14f, false),
    Quote("Kutipan", 11f, false),
    ListBullet("Daftar Poin", 11f, false),
    ListNumbered("Penomoran", 11f, false)
}

enum class DocTextAlign {
    Left, Center, Right, Justify
}

data class DocRun(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val subscript: Boolean = false,
    val superscript: Boolean = false,
    val fontColor: Long = 0xFF202124,
    val highlightColor: Long? = null,
    val fontSize: Float = 11f,
    val fontFamily: String = "Calibri"
)

data class DocParagraph(
    val id: String = UUID.randomUUID().toString(),
    val runs: List<DocRun> = listOf(DocRun(text = "")),
    val style: ParagraphStyle = ParagraphStyle.Normal,
    val alignment: DocTextAlign = DocTextAlign.Left,
    val indentLeftCm: Float = 0f,
    val indentFirstLineCm: Float = 0f,
    val indentRightCm: Float = 0f,
    val spaceBeforePt: Float = 0f,
    val spaceAfterPt: Float = 6f,
    val lineSpacingMultiplier: Float = 1.15f,
    val isBullet: Boolean = false,
    val bulletStyle: BulletStyle = BulletStyle.Dot,
    val isNumbered: Boolean = false,
    val numberIndex: Int = 1,
    val numberingStyle: NumberingStyle = NumberingStyle.Decimal,
    val isRtl: Boolean = false,
    val isPageBreakBefore: Boolean = false,
    val footnoteText: String? = null
) {
    val fullText: String
        get() = runs.joinToString(separator = "") { it.text }

    fun formattedNumber(): String {
        return when (numberingStyle) {
            NumberingStyle.Decimal -> "$numberIndex."
            NumberingStyle.DecimalParen -> "$numberIndex)"
            NumberingStyle.UpperAlpha -> "${('A'.code + (numberIndex - 1) % 26).toChar()}."
            NumberingStyle.LowerAlpha -> "${('a'.code + (numberIndex - 1) % 26).toChar()}."
            NumberingStyle.LowerAlphaParen -> "${('a'.code + (numberIndex - 1) % 26).toChar()})"
            NumberingStyle.UpperRoman -> toRoman(numberIndex) + "."
            NumberingStyle.LowerRoman -> toRoman(numberIndex).lowercase() + "."
            NumberingStyle.ArabicHijaiyah -> toHijaiyah(numberIndex) + "."
            NumberingStyle.ArabicEastern -> toEasternArabic(numberIndex) + "."
        }
    }

    private fun toRoman(num: Int): String {
        val vals = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val syms = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        var n = num
        val sb = StringBuilder()
        for (i in vals.indices) {
            while (n >= vals[i]) {
                n -= vals[i]
                sb.append(syms[i])
            }
        }
        return if (sb.isEmpty()) "I" else sb.toString()
    }

    private fun toHijaiyah(num: Int): String {
        val chars = arrayOf("أ", "ب", "ج", "د", "هـ", "و", "ز", "ح", "ط", "ي", "ك", "ل", "م", "ن", "س", "ع", "ف", "ص", "ق", "ر", "ش", "ت", "ث", "خ", "ذ", "ض", "ظ", "غ")
        val idx = (num - 1) % chars.size
        return chars[idx]
    }

    private fun toEasternArabic(num: Int): String {
        val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return num.toString().map { easternDigits[it - '0'] }.joinToString("")
    }
}

data class DocTableCell(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val bold: Boolean = false,
    val isHeader: Boolean = false,
    val backgroundColor: Long? = null
)

data class DocTableRow(
    val id: String = UUID.randomUUID().toString(),
    val cells: List<DocTableCell>,
    val isHeader: Boolean = false
)

data class DocTable(
    val id: String = UUID.randomUUID().toString(),
    val rows: List<DocTableRow>
)

enum class ShapeType(val displayName: String) {
    Rectangle("Persegi"),
    RoundedRectangle("Persegi Bulat"),
    Oval("Lingkaran / Elips"),
    Arrow("Panah Kanan"),
    Star("Bintang")
}

data class DocShape(
    val id: String = UUID.randomUUID().toString(),
    val shapeType: ShapeType,
    val label: String = "",
    val fillColor: Long = 0xFF185ABD
)

data class DocComment(
    val id: String = UUID.randomUUID().toString(),
    val author: String = "Masduqi Fadli",
    val timestamp: String,
    val content: String,
    val targetParagraphId: String,
    val resolved: Boolean = false
)

enum class ViewMode {
    PrintLayout, ReadMode, WebLayout
}

enum class RibbonTab {
    File, Home, Insert, Layout, References, Mailings, Review, View, Help
}

data class MdqDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Dokumen1",
    val fileName: String = "Dokumen1.docx",
    val paperSize: PaperSize = PaperSize.A4,
    val customPaperWidthCm: Float = 21.0f,
    val customPaperHeightCm: Float = 29.7f,
    val orientation: Orientation = Orientation.Portrait,
    val margins: DocMargins = DocMargins.Normal,
    val headerText: String = "",
    val footerText: String = "",
    val showPageNumbers: Boolean = true,
    val columnsCount: Int = 1,
    val paragraphs: List<DocParagraph> = emptyList(),
    val tables: List<DocTable> = emptyList(),
    val shapes: List<DocShape> = emptyList(),
    val comments: List<DocComment> = emptyList(),
    val trackChangesEnabled: Boolean = false,
    val author: String = "Masduqi Fadli",
    val isDirty: Boolean = false
) {
    val effectivePaperWidthCm: Float
        get() {
            val baseW = if (paperSize == PaperSize.Custom) customPaperWidthCm else paperSize.widthCm
            val baseH = if (paperSize == PaperSize.Custom) customPaperHeightCm else paperSize.heightCm
            return if (orientation == Orientation.Portrait) baseW else baseH
        }

    val effectivePaperHeightCm: Float
        get() {
            val baseW = if (paperSize == PaperSize.Custom) customPaperWidthCm else paperSize.widthCm
            val baseH = if (paperSize == PaperSize.Custom) customPaperHeightCm else paperSize.heightCm
            return if (orientation == Orientation.Portrait) baseH else baseW
        }

    val wordCount: Int
        get() {
            var count = 0
            for (p in paragraphs) {
                val words = p.fullText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                count += words.size
            }
            for (t in tables) {
                for (r in t.rows) {
                    for (c in r.cells) {
                        val words = c.text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                        count += words.size
                    }
                }
            }
            return count
        }

    val characterCount: Int
        get() = paragraphs.sumOf { it.fullText.length } + tables.sumOf { t -> t.rows.sumOf { r -> r.cells.sumOf { it.text.length } } }
}
