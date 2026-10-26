package com.example.mdqoffice.ui.desktop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdqoffice.model.ViewMode
import com.example.mdqoffice.theme.OfficeBlueStatusBar

@Composable
fun DesktopStatusBar(
    currentPage: Int,
    totalPages: Int,
    wordCount: Int,
    characterCount: Int,
    language: String = "Indonesia",
    viewMode: ViewMode,
    zoomLevel: Float,
    onZoomChange: (Float) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onSetViewMode: (ViewMode) -> Unit,
    onWordCountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OfficeBlueStatusBar,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFD6DAE1)),
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .testTag("desktop_status_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Page number, Word count, Language, Spellcheck
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Page indicator
                Text(
                    text = "Halaman $currentPage dari $totalPages",
                    color = Color(0xFF202124),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { /* jump to page */ }
                        .testTag("status_page_indicator")
                )

                // Word count
                Text(
                    text = "$wordCount kata",
                    color = Color(0xFF202124),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable { onWordCountClick() }
                        .testTag("status_word_count")
                )

                // Language
                Text(
                    text = language,
                    color = Color(0xFF5F6368),
                    fontSize = 10.sp
                )

                // Spell check icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Pemeriksaan Ejaan Bebas Masalah",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Ejaan OK",
                        color = Color(0xFF3C4043),
                        fontSize = 9.sp
                    )
                }
            }

            // Right Group: View Modes & Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // View Mode Icons
                IconButton(
                    onClick = { onSetViewMode(ViewMode.ReadMode) },
                    modifier = Modifier.size(20.dp).testTag("status_read_mode")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChromeReaderMode,
                        contentDescription = "Read Mode",
                        tint = if (viewMode == ViewMode.ReadMode) Color(0xFF185ABD) else Color(0xFF5F6368),
                        modifier = Modifier.size(13.dp)
                    )
                }
                IconButton(
                    onClick = { onSetViewMode(ViewMode.PrintLayout) },
                    modifier = Modifier.size(20.dp).testTag("status_print_layout")
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "Print Layout",
                        tint = if (viewMode == ViewMode.PrintLayout) Color(0xFF185ABD) else Color(0xFF5F6368),
                        modifier = Modifier.size(13.dp)
                    )
                }
                IconButton(
                    onClick = { onSetViewMode(ViewMode.WebLayout) },
                    modifier = Modifier.size(20.dp).testTag("status_web_layout")
                ) {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = "Web Layout",
                        tint = if (viewMode == ViewMode.WebLayout) Color(0xFF185ABD) else Color(0xFF5F6368),
                        modifier = Modifier.size(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Zoom Out Button
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(18.dp).testTag("status_zoom_out")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color(0xFF3C4043),
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Zoom Slider
                Slider(
                    value = zoomLevel,
                    onValueChange = onZoomChange,
                    valueRange = 0.5f..2.0f,
                    modifier = Modifier
                        .width(70.dp)
                        .height(18.dp)
                        .testTag("status_zoom_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF185ABD),
                        activeTrackColor = Color(0xFF185ABD),
                        inactiveTrackColor = Color(0xFFBDC1C6)
                    )
                )

                // Zoom In Button
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(18.dp).testTag("status_zoom_in")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color(0xFF3C4043),
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Zoom Percentage Text
                Text(
                    text = "${(zoomLevel * 100).toInt()}%",
                    color = Color(0xFF202124),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}
