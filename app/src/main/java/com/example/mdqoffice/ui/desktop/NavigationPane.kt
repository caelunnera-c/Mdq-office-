package com.example.mdqoffice.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mdqoffice.model.DocParagraph
import com.example.mdqoffice.model.MdqDocument
import com.example.mdqoffice.model.ParagraphStyle
import com.example.mdqoffice.theme.OfficeBlueDark
import com.example.mdqoffice.theme.OfficeBluePrimary
import com.example.mdqoffice.theme.RibbonBorder

@Composable
fun DesktopNavigationPane(
    document: MdqDocument,
    activeTab: Int, // 0: Headings, 1: Pages, 2: Results
    onSelectTab: (Int) -> Unit,
    onJumpToParagraph: (Int) -> Unit,
    onClose: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF9FAFB),
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
            .border(width = 1.dp, color = RibbonBorder)
            .testTag("desktop_nav_pane")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: "Navigation" + Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFFEDEFEF))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Navigasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OfficeBlueDark
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp).testTag("close_nav_pane_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Navigasi",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tab selection (Headings, Pages, Results)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFFF3F5F8),
                contentColor = OfficeBluePrimary,
                modifier = Modifier.height(34.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { onSelectTab(0) },
                    text = { Text("Judul", fontSize = 11.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { onSelectTab(1) },
                    text = { Text("Halaman", fontSize = 11.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { onSelectTab(2) },
                    text = { Text("Cari", fontSize = 11.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Divider(color = RibbonBorder)

            // Content per Tab
            when (activeTab) {
                0 -> {
                    // Headings Outline
                    val headingList = remember(document.paragraphs) {
                        document.paragraphs.mapIndexedNotNull { index, p ->
                            if (p.style == ParagraphStyle.Title || p.style == ParagraphStyle.Heading1 ||
                                p.style == ParagraphStyle.Heading2 || p.style == ParagraphStyle.Heading3
                            ) {
                                Triple(index, p.style, p.fullText)
                            } else null
                        }
                    }

                    if (headingList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada heading.\nGunakan Style 'Heading 1' atau 'Heading 2' untuk membuat outline.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp)
                        ) {
                            itemsIndexed(headingList) { _, (origIndex, style, title) ->
                                val indent = when (style) {
                                    ParagraphStyle.Heading2 -> 14.dp
                                    ParagraphStyle.Heading3 -> 24.dp
                                    else -> 4.dp
                                }
                                Surface(
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJumpToParagraph(origIndex) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = indent),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(OfficeBluePrimary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title.ifBlank { "(Heading Tanpa Teks)" },
                                            fontSize = 11.sp,
                                            fontWeight = if (style == ParagraphStyle.Heading1 || style == ParagraphStyle.Title) FontWeight.SemiBold else FontWeight.Normal,
                                            color = Color(0xFF202124),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Pages preview thumbnails
                    val pageCount = 2 // realistic representation
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pageCount) { pIndex ->
                            Surface(
                                color = Color.White,
                                shadowElevation = 2.dp,
                                shape = RoundedCornerShape(2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clickable { onJumpToParagraph(pIndex * 3) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "Hal. ${pIndex + 1}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OfficeBluePrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Simulated miniature lines
                                    repeat(7) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (it == 6) 0.5f else 0.95f)
                                                .height(4.dp)
                                                .background(Color(0xFFE0E0E0))
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Search in Document
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Cari di dokumen...", fontSize = 11.sp) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val matches = remember(searchQuery, document.paragraphs) {
                            if (searchQuery.isBlank()) emptyList()
                            else {
                                document.paragraphs.mapIndexedNotNull { idx, p ->
                                    if (p.fullText.contains(searchQuery, ignoreCase = true)) {
                                        idx to p.fullText
                                    } else null
                                }
                            }
                        }

                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "Ditemukan ${matches.size} hasil:",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                itemsIndexed(matches) { _, (origIdx, snippet) ->
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(3.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onJumpToParagraph(origIdx) }
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = snippet.take(80) + if (snippet.length > 80) "..." else "",
                                            fontSize = 11.sp,
                                            color = Color(0xFF202124),
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
