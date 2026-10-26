package com.example.mdqoffice.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdqoffice.model.*
import com.example.mdqoffice.theme.OfficeBlueDark
import com.example.mdqoffice.theme.OfficeBluePrimary

@Composable
fun FindAndReplaceDialog(
    searchQuery: String,
    replaceQuery: String,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onExecuteReplace: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Temukan dan Ganti (Find and Replace)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text("Cari kata:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("find_input_field")
                )
                OutlinedTextField(
                    value = replaceQuery,
                    onValueChange = onReplaceChange,
                    label = { Text("Ganti dengan:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("replace_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExecuteReplace()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary),
                modifier = Modifier.testTag("execute_replace_btn")
            ) {
                Text("Ganti Semua")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun WordCountDialog(
    document: MdqDocument,
    onDismiss: () -> Unit
) {
    val totalWords = document.wordCount
    val totalChars = document.characterCount
    val totalParagraphs = document.paragraphs.size
    val totalTables = document.tables.size

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = OfficeBluePrimary) },
        title = { Text("Statistik Dokumen (Word Count)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                StatRow("Jumlah Halaman", "2")
                StatRow("Kata", "$totalWords")
                StatRow("Karakter (dengan spasi)", "$totalChars")
                StatRow("Karakter (tanpa spasi)", "${(totalChars * 0.82).toInt()}")
                StatRow("Paragraf", "$totalParagraphs")
                StatRow("Tabel", "$totalTables")
                StatRow("Bahasa Pemeriksaan", "Indonesia")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF202124))
    }
}

@Composable
fun InsertTableDialog(
    onInsertTable: (rows: Int, cols: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var rows by remember { mutableStateOf("3") }
    var cols by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = OfficeBluePrimary) },
        title = { Text("Sisipkan Tabel", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = cols,
                    onValueChange = { cols = it },
                    label = { Text("Jumlah Kolom:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rows,
                    onValueChange = { rows = it },
                    label = { Text("Jumlah Baris:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rows.toIntOrNull() ?: 3
                    val c = cols.toIntOrNull() ?: 4
                    onInsertTable(r.coerceIn(1, 20), c.coerceIn(1, 10))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
            ) {
                Text("Sisipkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun PageSetupDialog(
    margins: DocMargins,
    paperSize: PaperSize,
    orientation: Orientation,
    onSaveSetup: (DocMargins, PaperSize, Orientation) -> Unit,
    onDismiss: () -> Unit
) {
    var topCm by remember { mutableStateOf(margins.topCm.toString()) }
    var bottomCm by remember { mutableStateOf(margins.bottomCm.toString()) }
    var leftCm by remember { mutableStateOf(margins.leftCm.toString()) }
    var rightCm by remember { mutableStateOf(margins.rightCm.toString()) }
    var selectedSize by remember { mutableStateOf(paperSize) }
    var selectedOrient by remember { mutableStateOf(orientation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan Halaman (Page Setup)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Margin Kertas (cm):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = topCm, onValueChange = { topCm = it }, label = { Text("Atas") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = bottomCm, onValueChange = { bottomCm = it }, label = { Text("Bawah") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = leftCm, onValueChange = { leftCm = it }, label = { Text("Kiri") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = rightCm, onValueChange = { rightCm = it }, label = { Text("Kanan") }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Ukuran Kertas:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaperSize.values().take(3).forEach { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size },
                            label = { Text(size.name) }
                        )
                    }
                }

                Text("Orientasi:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Orientation.values().forEach { orient ->
                        FilterChip(
                            selected = selectedOrient == orient,
                            onClick = { selectedOrient = orient },
                            label = { Text(orient.displayName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newMargins = DocMargins(
                        topCm = topCm.toFloatOrNull() ?: 2.54f,
                        bottomCm = bottomCm.toFloatOrNull() ?: 2.54f,
                        leftCm = leftCm.toFloatOrNull() ?: 2.54f,
                        rightCm = rightCm.toFloatOrNull() ?: 2.54f,
                        presetName = "Kustom"
                    )
                    onSaveSetup(newMargins, selectedSize, selectedOrient)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun KeyboardShortcutsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Keyboard, contentDescription = null, tint = OfficeBluePrimary) },
        title = { Text("Pintasan Keyboard PC (Shortcuts & Rumus Cepat)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                val shortcuts = listOf(
                    "Ctrl + A" to "Pilih Semua Teks (Select All)",
                    "Ctrl + C" to "Salin Teks / Objek (Copy)",
                    "Ctrl + X" to "Potong Teks / Objek (Cut)",
                    "Ctrl + V" to "Tempel Teks / Format (Paste)",
                    "Ctrl + S" to "Simpan Dokumen Cepat (Save)",
                    "Ctrl + Z" to "Batalkan Perubahan (Undo)",
                    "Ctrl + Y" to "Ulangi Perubahan (Redo)",
                    "Ctrl + B" to "Tebalkan Teks (Bold)",
                    "Ctrl + I" to "Miringkan Teks (Italic)",
                    "Ctrl + U" to "Garis Bawah Teks (Underline)",
                    "Ctrl + F" to "Cari Teks (Find)",
                    "Ctrl + H" to "Ganti Teks Otomatis (Replace)",
                    "Ctrl + E" to "Rata Tengah Paragraf (Align Center)",
                    "Ctrl + L" to "Rata Kiri Paragraf (Align Left)",
                    "Ctrl + R" to "Rata Kanan Paragraf (Align Right)",
                    "Ctrl + J" to "Rata Kanan Kiri (Justify)",
                    "Ctrl + P" to "Cetak atau Ekspor PDF (Print)",
                    "Ctrl + N" to "Buka Dokumen Baru (New)",
                    "Ctrl + O" to "Buka Berkas DOCX (Open)",
                    "Ctrl + ]" to "Perbesar Ukuran Font (Grow Font)",
                    "Ctrl + [" to "Perkecil Ukuran Font (Shrink Font)"
                )
                items(shortcuts.size) { idx ->
                    val (key, desc) = shortcuts[idx]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE8EEF5),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(key, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                        Text(desc, fontSize = 11.5.sp, color = Color(0xFF202124))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)) {
                Text("Mengerti")
            }
        }
    )
}

@Composable
fun ScreenModeSettingsDialog(
    currentMode: com.example.mdqoffice.data.ScreenMode,
    onSelectMode: (com.example.mdqoffice.data.ScreenMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pengaturan > Tampilan > Mode Layar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OfficeBlueDark)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Pilih mode tampilan layar penuh untuk tablet dan ponsel Android:",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                com.example.mdqoffice.data.ScreenMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE3EDFA) else Color(0xFFF9FAFB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OfficeBluePrimary else Color(0xFFE0E0E0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(mode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectMode(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(mode.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) OfficeBlueDark else Color(0xFF202124))
                                Text(mode.description, fontSize = 10.5.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)) {
                Text("Selesai")
            }
        }
    )
}

@Composable
fun HelpAboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = OfficeBluePrimary) },
        title = { Text("Tentang MDQ Office", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("MDQ Office Desktop-Style for Android", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OfficeBlueDark)
                Text("Versi 2.5.0 Professional Edition (Offline Standalone)", fontSize = 12.sp, color = Color.Gray)
                Divider()
                Text(
                    "MDQ Office mengimplementasikan alur kerja dan hierarki antarmuka Microsoft Word desktop versi komputer untuk tablet dan ponsel Android. Dilengkapi Ribbon horizontal penuh, ruler margin interaktif, navigasi outline, dan mesin DOCX berbasis standar OpenXML.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text("Pengembang: Masduqi Fadli", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun MailMergeDialog(
    onApplyMerge: (name: String, company: String, city: String) -> Unit,
    onDismiss: () -> Unit
) {
    var recipientName by remember { mutableStateOf("Budi Pratama, M.Kom.") }
    var company by remember { mutableStateOf("PT Solusi Digital Nusantara") }
    var city by remember { mutableStateOf("Surabaya") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wisaya Mail Merge MDQ Office", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Masukkan data penerima untuk menggantikan placeholder «Nama_Penerima», «Instansi_Perusahaan», dan «Kota_Tujuan»:", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Nama Penerima:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Instansi / Perusahaan:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Kota:") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyMerge(recipientName, company, city)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
            ) {
                Text("Terapkan ke Dokumen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AboutMdqOfficeDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFC5221F),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("MDQ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                Column {
                    Text("MDQ Office", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF103F91))
                    Text("Desktop Edition for Android • v2.4", fontSize = 10.sp, color = Color.Gray)
                }
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Surface(
                        color = Color(0xFFF3F5F8),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pengembang & Pembuat Aplikasi:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF202124))
                            Text("Masduqi Fadli & Tim MDQ Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC5221F))
                            Text("Hak Cipta © 2026. Hak cipta dilindungi undang-undang.", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
                item {
                    Text("Latar Belakang & Tujuan Pembuatan:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF103F91))
                    Text(
                        "MDQ Office dirancang untuk mengatasi kelemahan mendasar pengolah kata mobile konvensional yang terlalu minimalis. Banyak akademisi, mahasiswa, peneliti, dan profesional di Indonesia kesulitan memformat dokumen skripsi (margin 4-4-3-3 cm, ukuran kertas F4/Folio, penomoran Arab & Romawi) saat bekerja menggunakan tablet atau smartphone tanpa membawa laptop.",
                        fontSize = 11.sp,
                        color = Color(0xFF3C4043),
                        lineHeight = 16.sp
                    )
                }
                item {
                    Text("Fitur Unggulan Desktop:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF103F91))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("• 100% Offline & Privasi Terjaga: Tanpa koneksi internet atau server pihak ketiga.", fontSize = 10.5.sp)
                        Text("• Antarmuka Desktop Asli: Tata letak Ribbon horizontal Microsoft Word PC.", fontSize = 10.5.sp)
                        Text("• Format DOCX Nyata: Buka, edit, dan simpan berkas standar Microsoft Word.", fontSize = 10.5.sp)
                        Text("• Seleksi Kata & Sapu Format: Ubah gaya hanya pada kata yang diblok.", fontSize = 10.5.sp)
                        Text("• Multi-Lembar Kerja: Beralih antar-dokumen aktif dengan mudah.", fontSize = 10.5.sp)
                        Text("• Teks Arab (RTL) & Font Kustom: Dukungan font lokal dari penyimpanan.", fontSize = 10.5.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF103F91))
            ) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun UnsavedChangesConfirmDialog(
    docTitle: String,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100))
                Text("Pengingat Simpan Dokumen", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                "Ingin menyimpan perubahan pada \"$docTitle\" sebelum beralih atau menutup?",
                fontSize = 12.sp,
                color = Color(0xFF202124)
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC5221F))
                ) {
                    Text("Buang (Jangan Simpan)")
                }
                TextButton(onClick = onCancel) {
                    Text("Batal")
                }
            }
        }
    )
}

@Composable
fun DocumentSwitcherDialog(
    documents: List<MdqDocument>,
    activeIndex: Int,
    onSelectDoc: (Int) -> Unit,
    onCloseDoc: (Int) -> Unit,
    onNewDoc: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lembar Kerja Terbuka (${documents.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onNewDoc(); onDismiss() }) {
                    Text("+ Baru", fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(documents.size) { idx ->
                    val doc = documents[idx]
                    val isActive = idx == activeIndex
                    Surface(
                        color = if (isActive) Color(0xFFCCE8FF) else Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) OfficeBluePrimary else Color(0xFFD0D7DE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectDoc(idx)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = doc.title,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isActive) OfficeBlueDark else Color(0xFF202124)
                                    )
                                    if (doc.isDirty) {
                                        Surface(
                                            color = Color(0xFFFFF3E0),
                                            shape = RoundedCornerShape(2.dp)
                                        ) {
                                            Text("* Belum Disimpan", fontSize = 9.sp, color = Color(0xFFE65100), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = "${doc.paragraphs.size} paragraf • ${doc.wordCount} kata • ${doc.paperSize.name}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            if (documents.size > 1) {
                                IconButton(
                                    onClick = { onCloseDoc(idx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("✕", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun CustomPaperSizeDialog(
    initialWidthCm: Float,
    initialHeightCm: Float,
    onApply: (Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var widthStr by remember { mutableStateOf("$initialWidthCm") }
    var heightStr by remember { mutableStateOf("$initialHeightCm") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ukuran Kertas Kustom", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Atur lebar dan tinggi kertas dokumen dalam satuan centimeter (cm):", fontSize = 11.sp, color = Color.Gray)
                OutlinedTextField(
                    value = widthStr,
                    onValueChange = { widthStr = it },
                    label = { Text("Lebar (cm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text("Tinggi (cm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { widthStr = "21.5"; heightStr = "33.0" }) {
                        Text("F4 (21.5x33 cm)", fontSize = 10.sp)
                    }
                    OutlinedButton(onClick = { widthStr = "21.0"; heightStr = "29.7" }) {
                        Text("A4 (21x29.7 cm)", fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = widthStr.toFloatOrNull() ?: 21.0f
                    val h = heightStr.toFloatOrNull() ?: 29.7f
                    onApply(w, h)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary)
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun CustomFontImportDialog(
    customFonts: List<Pair<String, String>>,
    onImportRequested: () -> Unit,
    onSelectFont: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manajemen Font Kustom (.ttf / .otf)", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tambahkan file font dari penyimpanan perangkat Anda (misal font Arab, aksara, atau font khusus lainnya):", fontSize = 11.sp, color = Color.Gray)
                Button(
                    onClick = onImportRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = OfficeBluePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Pilih Berkas Font (.ttf / .otf)")
                }
                Divider()
                Text("Font Kustom yang Terpasang (${customFonts.size}):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                if (customFonts.isEmpty()) {
                    Text("Belum ada font kustom yang diimpor.", fontSize = 10.sp, color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 140.dp)) {
                        items(customFonts.size) { idx ->
                            val item = customFonts[idx]
                            Surface(
                                color = Color(0xFFF3F5F8),
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        onSelectFont(item.first)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.first, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("Gunakan", fontSize = 10.sp, color = OfficeBluePrimary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Selesai") }
        }
    )
}
