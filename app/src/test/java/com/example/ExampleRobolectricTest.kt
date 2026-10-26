package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mdqoffice.data.AppPreferences
import com.example.mdqoffice.data.ScreenMode
import com.example.mdqoffice.engine.DocxEngine
import com.example.mdqoffice.model.DocMargins
import com.example.mdqoffice.model.DocParagraph
import com.example.mdqoffice.model.DocRun
import com.example.mdqoffice.model.MdqDocument
import com.example.mdqoffice.viewmodel.OfficeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MDQ Office", appName)
    }

    @Test
    fun `default screen mode is full screen immersive`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)
        assertEquals(ScreenMode.FULLSCREEN_IMMERSIVE, prefs.screenMode)
    }

    @Test
    fun `margins preset has skripsi 4-4-3-3`() {
        val skripsi = DocMargins.Skripsi
        assertEquals(4.0f, skripsi.topCm)
        assertEquals(4.0f, skripsi.leftCm)
        assertEquals(3.0f, skripsi.bottomCm)
        assertEquals(3.0f, skripsi.rightCm)
    }

    @Test
    fun `docx engine serializes and parses document accurately`() {
        val originalDoc = MdqDocument(
            title = "Test Doc",
            fileName = "Test.docx",
            paragraphs = listOf(
                DocParagraph(runs = listOf(DocRun(text = "Halo Dunia MDQ Office", bold = true, fontSize = 14f)))
            )
        )

        val bos = ByteArrayOutputStream()
        DocxEngine.writeDocx(originalDoc, bos)
        val docxBytes = bos.toByteArray()
        assertTrue(docxBytes.isNotEmpty())

        val bis = ByteArrayInputStream(docxBytes)
        val parsedDoc = DocxEngine.readDocx(bis, "Test.docx")
        assertNotNull(parsedDoc)
        assertTrue(parsedDoc.paragraphs.any { it.fullText.contains("Halo Dunia MDQ Office") })
    }

    @Test
    fun `viewmodel format painter copies and applies format correctly`() {
        val vm = OfficeViewModel()
        vm.createNewDocument(3)
        vm.setFontFamily("Times New Roman")
        vm.setFontSize(18f)
        vm.toggleBold()

        vm.copyFormat()
        assertTrue(vm.hasCopiedFormat.value)

        // Select next paragraph and paste format
        vm.addNewParagraph(0)
        vm.pasteFormat()

        val p2 = vm.document.value.paragraphs[1]
        val run2 = p2.runs.first()
        assertEquals("Times New Roman", run2.fontFamily)
        assertEquals(18f, run2.fontSize)
        assertTrue(run2.bold)
    }

    @Test
    fun `selection-based formatting applies bold only to selected substring`() {
        val vm = OfficeViewModel()
        vm.createNewDocument(3)
        vm.updateParagraphText(0, "Halo Dunia MDQ")
        
        // Block "Dunia" (index 5 to 10)
        vm.updateSelection(5, 10)
        vm.toggleBold()

        val runs = vm.document.value.paragraphs[0].runs
        assertTrue(runs.size >= 3)
        // First run: "Halo " (not bold)
        assertEquals("Halo ", runs[0].text)
        assertEquals(false, runs[0].bold)
        // Second run: "Dunia" (bold!)
        assertEquals("Dunia", runs[1].text)
        assertEquals(true, runs[1].bold)
        // Third run: " MDQ" (not bold)
        assertEquals(" MDQ", runs[2].text)
        assertEquals(false, runs[2].bold)
    }

    @Test
    fun `f4 paper size and custom paper size calculate effective dimensions correctly`() {
        val vm = OfficeViewModel()
        vm.createNewDocument(3)
        
        // F4 / Folio: 21.5 x 33.0 cm
        vm.setPaperSize(com.example.mdqoffice.model.PaperSize.F4)
        assertEquals(21.5f, vm.document.value.effectivePaperWidthCm)
        assertEquals(33.0f, vm.document.value.effectivePaperHeightCm)

        // Custom paper size: 18.0 x 25.0 cm
        vm.setCustomPaperSize(18.0f, 25.0f)
        assertEquals(com.example.mdqoffice.model.PaperSize.Custom, vm.document.value.paperSize)
        assertEquals(18.0f, vm.document.value.effectivePaperWidthCm)
        assertEquals(25.0f, vm.document.value.effectivePaperHeightCm)
    }

    @Test
    fun `rtl text direction and arabic numbering styles work properly`() {
        val vm = OfficeViewModel()
        vm.createNewDocument(3)
        
        // Toggle RTL for Arabic typing
        vm.toggleTextDirection()
        assertTrue(vm.document.value.paragraphs[0].isRtl)

        // Set Hijaiyah numbering
        vm.setNumberingStyle(com.example.mdqoffice.model.NumberingStyle.ArabicHijaiyah)
        assertEquals(com.example.mdqoffice.model.NumberingStyle.ArabicHijaiyah, vm.document.value.paragraphs[0].numberingStyle)
    }

    @Test
    fun `format painter toggle can be deactivated without sticking`() {
        val vm = OfficeViewModel()
        vm.createNewDocument(3)
        vm.copyFormat()
        assertTrue(vm.hasCopiedFormat.value)

        // User can tap format painter again to deactivate
        vm.toggleFormatPainter()
        assertEquals(false, vm.hasCopiedFormat.value)
    }

    @Test
    fun `unsaved changes prompt triggers before switching dirty document`() {
        val vm = OfficeViewModel()
        val context = ApplicationProvider.getApplicationContext<Context>()
        vm.createNewDocument(3)
        
        // Modify document to make it dirty
        vm.updateParagraphText(0, "Perubahan penting")
        assertTrue(vm.document.value.isDirty)

        // Add a second document to allow switching
        val docs = vm.openDocuments.value.toMutableList()
        docs.add(com.example.mdqoffice.model.MdqDocument(title = "Doc 2", fileName = "Doc2.docx"))
        vm.openDocuments.value = docs

        // Request switch to doc index 1
        vm.requestSwitchDocument(1)
        assertTrue(vm.showUnsavedDialog.value)
        assertEquals(vm.document.value.title, vm.documentToPromptSave.value?.title)

        // Cancel prompt
        vm.cancelUnsavedPrompt()
        assertEquals(false, vm.showUnsavedDialog.value)
    }
}
