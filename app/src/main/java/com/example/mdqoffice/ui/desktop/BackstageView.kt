package com.example.mdqoffice.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.mdqoffice.model.MdqDocument
import com.example.mdqoffice.theme.OfficeBlueDark
import com.example.mdqoffice.theme.OfficeBluePrimary

@Composable
fun DesktopBackstageView(
    document: MdqDocument,
    onCloseBackstage: () -> Unit,
    onNewDocument: (Int) -> Unit,
    onOpenDocxPicker: () -> Unit,
    onLoadSample: (String) -> Unit,
    onSaveDocument: () -> Unit,
    onExportPdf: () -> Unit,
    onExportDocx: () -> Unit,
    onPrintDocument: () -> Unit,
    recentFiles: List<String>,
    modifier: Modifier = Modifier
) {
    var selectedMenu by remember { mutableStateOf("info") }

    Surface(
        color = Color(0xFFF3F5F8),
        modifier = modifier
            .fillMaxSize()
            .testTag("backstage_view")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Navigation Rail (Word Blue)
            Surface(
                color = OfficeBlueDark,
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                ) {
                    // Back button to document
                    IconButton(
                        onClick = onCloseBackstage,
                        modifier = Modifier
                            .padding(start = 8.dp, bottom = 12.dp)
                            .testTag("backstage_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Dokumen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    val menuItems = listOf(
                        "info" to ("Info" to Icons.Default.Info),
                        "new" to ("Baru (New)" to Icons.Default.NoteAdd),
                        "open" to ("Buka (Open)" to Icons.Default.FolderOpen),
                        "save" to ("Simpan (Save)" to Icons.Default.Save),
                        "export" to ("Ekspor PDF/DOCX" to Icons.Default.PictureAsPdf),
                        "print" to ("Cetak (Print)" to Icons.Default.Print)
                    )

                    menuItems.forEach { (key, pair) ->
                        val (label, icon) = pair
                        val isSelected = selectedMenu == key

                        Surface(
                            color = if (isSelected) Color(0xFF003D7A) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMenu = key
                                    if (key == "save") {
                                        onSaveDocument()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Close Backstage
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCloseBackstage() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tutup Backstage", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Right Detail Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(32.dp)
            ) {
                when (selectedMenu) {
                    "info" -> {
                        InfoPane(document = document, onSave = onSaveDocument)
                    }
                    "new" -> {
                        NewDocumentPane(onSelectTemplate = onNewDocument)
                    }
                    "open" -> {
                        OpenDocumentPane(
                            onBrowseDevice = onOpenDocxPicker,
                            onLoadSample = onLoadSample,
                            recentFiles = recentFiles
                        )
                    }
                    "save" -> {
                        SavePane(document = document, onSave = onSaveDocument, onExportDocx = onExportDocx)
                    }
                    "export" -> {
                        ExportPane(onExportPdf = onExportPdf, onExportDocx = onExportDocx)
                    }
                    "print" -> {
                        PrintPane(document = document, onPrint = onPrintDocument)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoPane(document: MdqDocument, onSave: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Informasi Dokumen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = document.fileName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OfficeBluePrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Properti Dokumen", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Jumlah Kata: ${document.wordCount}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Karakter: ${document.characterCount}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Ukuran Kertas: ${document.paperSize.displayName}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Orientasi: ${document.orientation.displayName}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Margin: ${document.margins.presetName} (${document.margins.leftCm}cm)", fontSize = 12.sp, color = Color.DarkGray)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Informasi Penulis", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Penulis: ${document.author}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Status: ${if (document.isDirty) "Perubahan Belum Disimpan" else "Tersimpan Lengkap"}", fontSize = 12.sp, color = if (document.isDirty) Color.Red else Color(0xFF2E7D32))
                        Text("Format Penyimpanan: DOCX (OpenXML)", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Sekarang")
                }
            }
        }
    }
}

@Composable
fun NewDocumentPane(onSelectTemplate: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Buat Dokumen Baru", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        Text("Pilih lembar kerja kosong atau template resmi MDQ Office:", fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val templates = listOf(
                Triple(3, "Dokumen Kosong", "Lembar putih standar A4 bersih"),
                Triple(0, "Proposal Bisnis MDQ", "Lengkap dengan tabel anggaran & tanda tangan"),
                Triple(1, "Laporan Riset & Skripsi", "Format akademik dengan abstrak & bab"),
                Triple(2, "Surat Resmi & Mail Merge", "Format surat instansi & placeholder dinamis")
            )

            templates.forEach { (typeId, title, desc) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp)
                        .clickable { onSelectTemplate(typeId) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color(0xFFF3F5F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = OfficeBluePrimary, modifier = Modifier.size(36.dp))
                        }
                        Column {
                            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202124), maxLines = 2)
                            Text(text = desc, fontSize = 10.sp, color = Color.Gray, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OpenDocumentPane(
    onBrowseDevice: () -> Unit,
    onLoadSample: (String) -> Unit,
    recentFiles: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Buka Dokumen (.docx)", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBrowseDevice,
            colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih Berkas DOCX dari Penyimpanan HP / Tablet")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Dokumen Bawaan & Terakhir Dibuka:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recentFiles) { fileName ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoadSample(fileName) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = OfficeBluePrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = fileName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = "Format Asli Microsoft Word (.docx)", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavePane(document: MdqDocument, onSave: () -> Unit, onExportDocx: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Simpan Dokumen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = document.fileName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan ke Penyimpanan Lokal Offline")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onExportDocx) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ekspor Salinan Berkas DOCX Baru")
                }
            }
        }
    }
}

@Composable
fun ExportPane(onExportPdf: () -> Unit, onExportDocx: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Ekspor Dokumen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        Text("Ekspor ke format PDF siap cetak atau format murni DOCX:", fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .width(280.dp)
                    .clickable { onExportPdf() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Buat Dokumen PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Menjaga tata letak, margin, dan pagination agar persis seperti di layar cetak.", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .width(280.dp)
                    .clickable { onExportDocx() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = OfficeBluePrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Simpan Berkas DOCX Murni", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Format OpenXML terstandar yang kompatibel 100% dibuka kembali di Microsoft Word PC.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PrintPane(document: MdqDocument, onPrint: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Cetak Dokumen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Pengaturan Cetak:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ukuran: ${document.paperSize.displayName}", fontSize = 12.sp)
                Text("Orientasi: ${document.orientation.displayName}", fontSize = 12.sp)
                Text("Cetak Seluruh Halaman", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim ke Printer / Cetak ke PDF")
                }
            }
        }
    }
}
