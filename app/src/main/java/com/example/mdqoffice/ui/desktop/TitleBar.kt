package com.example.mdqoffice.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdqoffice.model.MdqDocument
import com.example.mdqoffice.theme.OfficeBlueHeader

@Composable
fun DesktopTitleBar(
    document: MdqDocument,
    canUndo: Boolean,
    canRedo: Boolean,
    onQuickSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNewDoc: () -> Unit,
    onPrint: () -> Unit,
    onSearchClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OfficeBlueHeader,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .testTag("desktop_title_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Access Toolbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Save Icon
                IconButton(
                    onClick = onQuickSave,
                    modifier = Modifier.size(30.dp).testTag("quick_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Simpan Cepat",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Undo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(30.dp).testTag("quick_undo_button")
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
                    modifier = Modifier.size(30.dp).testTag("quick_redo_button")
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
                    modifier = Modifier.size(30.dp).testTag("quick_new_button")
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
                    modifier = Modifier.size(30.dp).testTag("quick_print_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Cetak Dokumen",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center: Document Title & App Name
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${document.fileName} ${if (document.isDirty) "• Belum Disimpan" else "— Tersimpan"}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = " - MDQ Office",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Search "Tell me what you want to do" / Search commands
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(26.dp)
                    .widthIn(min = 120.dp, max = 220.dp)
                    .clickable { onSearchClick() }
                    .testTag("command_search_box")
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
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
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

            Spacer(modifier = Modifier.width(8.dp))

            // User Profile badge (Masduqi Fadli)
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B78E4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MF",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Masduqi F.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Window Controls (Minimize, Restore, Close - desktop styling)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onHelpClick,
                    modifier = Modifier.size(26.dp).testTag("title_help_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Bantuan",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { /* decorative desktop minimize */ },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(1.5.dp)
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { /* decorative desktop maximize */ },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color.Transparent)
                            .padding(0.5.dp)
                    ) {
                        Surface(
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }
        }
    }
}
