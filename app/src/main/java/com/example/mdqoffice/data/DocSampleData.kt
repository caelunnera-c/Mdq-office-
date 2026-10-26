package com.example.mdqoffice.data

import com.example.mdqoffice.model.*

object DocSampleData {

    fun getBusinessProposal(): MdqDocument {
        val paragraphs = listOf(
            DocParagraph(
                runs = listOf(DocRun(text = "PROPOSAL PENGEMBANGAN TEKNOLOGI MDQ OFFICE", bold = true, fontSize = 20f, fontColor = 0xFF103F91)),
                style = ParagraphStyle.Title,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "Solusi Pengolah Dokumen Profesional Bergaya Desktop untuk Ekosistem Android", italic = true, fontSize = 12f, fontColor = 0xFF5F6368)),
                style = ParagraphStyle.Subtitle,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 16f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "1. RINGKASAN EKSEKUTIF", bold = true, fontSize = 14f, fontColor = 0xFF185ABD)),
                style = ParagraphStyle.Heading1,
                spaceAfterPt = 6f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "MDQ Office adalah platform pengolah kata profesional yang dirancang secara khusus untuk menghadirkan kembali "),
                    DocRun(text = "kemudahan dan kekuatan antarmuka desktop (Microsoft Word style)", bold = true),
                    DocRun(text = " langsung pada perangkat tablet dan smartphone Android. Seluruh alur kerja dirancang untuk berjalan "),
                    DocRun(text = "100% offline dan mandiri", italic = true),
                    DocRun(text = ", memberikan privasi mutlak serta kinerja tanpa jeda bagi para profesional, akademisi, dan instansi korporat.")
                ),
                style = ParagraphStyle.Normal,
                alignment = DocTextAlign.Justify,
                spaceAfterPt = 8f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "2. ARSITEKTUR & KEUNGGULAN UTAMA", bold = true, fontSize = 14f, fontColor = 0xFF185ABD)),
                style = ParagraphStyle.Heading1,
                spaceAfterPt = 6f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Antarmuka Ribbon Desktop: "),
                    DocRun(text = "Mempertahankan tata letak horizontal dengan tab File, Home, Insert, Layout, References, Mailings, Review, View, dan Help.")
                ),
                style = ParagraphStyle.ListBullet,
                isBullet = true,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Ruler Interaktif Presisi: "),
                    DocRun(text = "Memungkinkan pengaturan margin kertas, indentasi baris pertama (First-line Indent), dan Hanging Indent langsung secara visual.")
                ),
                style = ParagraphStyle.ListBullet,
                isBullet = true,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Dukungan Format Murni DOCX: "),
                    DocRun(text = "Mematuhi standar Open Packaging Conventions (OPC) OpenXML sehingga berkas dapat dibuka sempurna di PC.")
                ),
                style = ParagraphStyle.ListBullet,
                isBullet = true,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Dukungan Keyboard & Akselerasi PC: "),
                    DocRun(text = "Mendukung shortcut universal seperti Ctrl+S (Simpan), Ctrl+Z (Undo), Ctrl+B (Tebal), Ctrl+I (Miring), Ctrl+F (Cari), dan Ctrl+P (Cetak).")
                ),
                style = ParagraphStyle.ListBullet,
                isBullet = true,
                spaceAfterPt = 12f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "3. RENCANA KERJA & JADWAL IMPLEMENTASI", bold = true, fontSize = 14f, fontColor = 0xFF185ABD)),
                style = ParagraphStyle.Heading1,
                spaceAfterPt = 6f
            )
        )

        val table = DocTable(
            rows = listOf(
                DocTableRow(
                    isHeader = true,
                    cells = listOf(
                        DocTableCell(text = "No", bold = true),
                        DocTableCell(text = "Fase Pengembangan", bold = true),
                        DocTableCell(text = "Durasi", bold = true),
                        DocTableCell(text = "Status Kesiapan", bold = true)
                    )
                ),
                DocTableRow(
                    cells = listOf(
                        DocTableCell(text = "1"),
                        DocTableCell(text = "Pondasi Ribbon & Canvas Halaman"),
                        DocTableCell(text = "Minggu 1"),
                        DocTableCell(text = "Selesai (100%)", bold = true)
                    )
                ),
                DocTableRow(
                    cells = listOf(
                        DocTableCell(text = "2"),
                        DocTableCell(text = "Ruler Interaktif & Formatting Engine"),
                        DocTableCell(text = "Minggu 2"),
                        DocTableCell(text = "Selesai (100%)", bold = true)
                    )
                ),
                DocTableRow(
                    cells = listOf(
                        DocTableCell(text = "3"),
                        DocTableCell(text = "DOCX Serialization & PDF Export"),
                        DocTableCell(text = "Minggu 3"),
                        DocTableCell(text = "Stabil & Aktif", bold = true)
                    )
                ),
                DocTableRow(
                    cells = listOf(
                        DocTableCell(text = "4"),
                        DocTableCell(text = "Dukungan Mail Merge & Navigasi Outline"),
                        DocTableCell(text = "Minggu 4"),
                        DocTableCell(text = "Siap Digunakan", bold = true)
                    )
                )
            )
        )

        val closingParagraphs = listOf(
            DocParagraph(
                runs = listOf(DocRun(text = "4. KESIMPULAN & TANDA TANGAN RESMI", bold = true, fontSize = 14f, fontColor = 0xFF185ABD)),
                style = ParagraphStyle.Heading1,
                spaceBeforePt = 12f,
                spaceAfterPt = 6f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Melalui inovasi MDQ Office, batasan produktivitas mobile dapat dihapuskan sepenuhnya, memberikan kenyamanan PC Word di genggaman Anda.")
                ),
                style = ParagraphStyle.Normal,
                alignment = DocTextAlign.Justify,
                spaceAfterPt = 16f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Jakarta, 25 Oktober 2026\n", italic = true),
                    DocRun(text = "Pimpinan Proyek MDQ Office,\n\n\n\n", fontColor = 0xFF5F6368),
                    DocRun(text = "Masduqi Fadli, S.Kom.\n", bold = true, underline = true),
                    DocRun(text = "Direktur Pengembangan Teknologi", fontColor = 0xFF5F6368)
                ),
                style = ParagraphStyle.Normal,
                alignment = DocTextAlign.Right,
                spaceAfterPt = 8f
            )
        )

        val comments = listOf(
            DocComment(
                author = "Masduqi Fadli",
                timestamp = "25 Okt 2026, 09:30",
                content = "Periksa kembali tabel anggaran dan pastikan kompatibilitas DOCX sudah diverifikasi.",
                targetParagraphId = paragraphs[1].id
            )
        )

        return MdqDocument(
            title = "Proposal Bisnis MDQ",
            fileName = "Proposal Bisnis MDQ.docx",
            paragraphs = paragraphs + closingParagraphs,
            tables = listOf(table),
            comments = comments,
            isDirty = false
        )
    }

    fun getAcademicPaper(): MdqDocument {
        val paragraphs = listOf(
            DocParagraph(
                runs = listOf(DocRun(text = "ANALISIS EFISIENSI TATA LETAK DESKTOP PADA PENGOLAHAN DOKUMEN ANDROID", bold = true, fontSize = 16f, fontColor = 0xFF103F91)),
                style = ParagraphStyle.Title,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "Masduqi Fadli - Laboratorium Sistem Komputasi & Perangkat Lunak Terapan", italic = true, fontSize = 11f, fontColor = 0xFF5F6368)),
                style = ParagraphStyle.Subtitle,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 14f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "ABSTRAK", bold = true, fontSize = 12f, fontColor = 0xFF185ABD)),
                style = ParagraphStyle.Heading2,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Penelitian ini mengkaji perbandingan efisiensi pengetikan dan pemformatan dokumen kompleks antara antarmuka mobile minimalis dengan antarmuka desktop-style (Ribbon, Ruler, dan Multi-Page Canvas). Pengujian empiris pada tablet Android menunjukkan peningkatan kecepatan navigasi sebesar 42% ketika pengguna memiliki akses langsung ke kelompok perintah Ribbon dan ruler margin tanpa harus berpindah ke submenu tersembunyi.")
                ),
                style = ParagraphStyle.Quote,
                alignment = DocTextAlign.Justify,
                indentLeftCm = 1.0f,
                indentRightCm = 1.0f,
                spaceAfterPt = 12f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "BAB I PENDAHULUAN", bold = true, fontSize = 14f, fontColor = 0xFF185ABD)),
                style = ParagraphStyle.Heading1,
                spaceAfterPt = 6f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "1.1 Latar Belakang Masalah", bold = true, fontSize = 12f)
                ),
                style = ParagraphStyle.Heading2,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Perangkat tablet modern memiliki resolusi layar tinggi dan dukungan aksesoris seperti keyboard fisik serta mouse. Namun, sebagian besar aplikasi pengolah dokumen yang tersedia di pasar masih memaksakan paradigma antarmuka mobile yang sangat terbatas. Akibatnya, alur kerja profesional terhambat karena fungsi-fungsi penting seperti pengaturan tabel, margin kustom, dan penomoran paragraf tersembunyi jauh di balik menu drop-down.")
                ),
                style = ParagraphStyle.Normal,
                alignment = DocTextAlign.Justify,
                indentFirstLineCm = 1.0f,
                spaceAfterPt = 8f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "1.2 Rumusan Masalah", bold = true, fontSize = 12f)
                ),
                style = ParagraphStyle.Heading2,
                spaceAfterPt = 4f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Bagaimana merancang arsitektur Ribbon dan Document Workspace berbasis Jetpack Compose yang mampu beradaptasi pada orientasi lanskap maupun potret tanpa mengorbankan hierarki perintah desktop?")
                ),
                style = ParagraphStyle.Normal,
                alignment = DocTextAlign.Justify,
                indentFirstLineCm = 1.0f,
                spaceAfterPt = 8f
            )
        )

        return MdqDocument(
            title = "Laporan Riset & Skripsi",
            fileName = "Laporan Riset & Skripsi.docx",
            paragraphs = paragraphs,
            isDirty = false
        )
    }

    fun getOfficialLetter(): MdqDocument {
        val paragraphs = listOf(
            DocParagraph(
                runs = listOf(DocRun(text = "PT MDQ KARYA NUSANTARA", bold = true, fontSize = 16f, fontColor = 0xFF103F91)),
                style = ParagraphStyle.Title,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 2f
            ),
            DocParagraph(
                runs = listOf(DocRun(text = "Gedung Cyber Tower Lt. 12, Jl. Rasuna Said Kav. 8, Jakarta Selatan\nTelp: (021) 555-0199 | Email: sekretariat@mdqoffice.id", fontSize = 10f, fontColor = 0xFF5F6368)),
                style = ParagraphStyle.Subtitle,
                alignment = DocTextAlign.Center,
                spaceAfterPt = 16f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Nomor       : 042/MDQ-DIR/X/2026\n"),
                    DocRun(text = "Lampiran : 1 (Satu) Berkas Proposal\n"),
                    DocRun(text = "Perihal      : "),
                    DocRun(text = "Undangan Demonstrasi & Sosialisasi MDQ Office", bold = true)
                ),
                style = ParagraphStyle.Normal,
                spaceAfterPt = 12f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Kepada Yth.\n"),
                    DocRun(text = "«Nama_Penerima»\n", bold = true, fontColor = 0xFF185ABD),
                    DocRun(text = "«Instansi_Perusahaan»\n", fontColor = 0xFF185ABD),
                    DocRun(text = "di «Kota_Tujuan»")
                ),
                style = ParagraphStyle.Normal,
                spaceAfterPt = 12f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Dengan hormat,\n"),
                    DocRun(text = "Sehubungan dengan peluncuran versi terbaru "),
                    DocRun(text = "MDQ Office Desktop-Style for Android", bold = true),
                    DocRun(text = ", kami bermaksud mengundang Bapak/Ibu beserta tim teknologi informasi untuk menghadiri sesi pengenalan dan uji coba langsung sistem pengolah dokumen offline berkinerja tinggi.")
                ),
                style = ParagraphStyle.Normal,
                alignment = DocTextAlign.Justify,
                spaceAfterPt = 14f
            ),
            DocParagraph(
                runs = listOf(
                    DocRun(text = "Demikian undangan ini kami sampaikan. Atas perhatian dan kesediaan Bapak/Ibu, kami ucapkan terima kasih.\n\n\n"),
                    DocRun(text = "Hormat kami,\n\n\n\n"),
                    DocRun(text = "Masduqi Fadli\n", bold = true, underline = true),
                    DocRun(text = "Direktur Utama", fontColor = 0xFF5F6368)
                ),
                style = ParagraphStyle.Normal,
                spaceAfterPt = 8f
            )
        )

        return MdqDocument(
            title = "Surat Resmi & Mail Merge",
            fileName = "Surat Resmi & Mail Merge.docx",
            paragraphs = paragraphs,
            isDirty = false
        )
    }

    fun getBlankDocument(): MdqDocument {
        return MdqDocument(
            title = "Dokumen Baru",
            fileName = "Dokumen Baru.docx",
            paragraphs = listOf(
                DocParagraph(
                    runs = listOf(DocRun(text = "Ketik teks dokumen Anda di sini...")),
                    style = ParagraphStyle.Normal
                )
            ),
            isDirty = false
        )
    }
}
