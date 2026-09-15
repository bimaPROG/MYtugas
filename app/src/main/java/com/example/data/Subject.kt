package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class Subject(
    val code: String,
    val name: String,
    val color: Color,
    val iconName: String = "menu_book"
)

object SchoolSubjects {
    val ALL_SUBJECTS = listOf(
        Subject(code = "B.ING", name = "B. Inggris", color = SubjectColorEnglish),
        Subject(code = "KIK", name = "KIK (Kewirausahaan)", color = SubjectColorKIK),
        Subject(code = "PEWEB", name = "Peweb (Pemrograman Web)", color = SubjectColorPeweb),
        Subject(code = "PPB", name = "PPB (Perangkat Bergerak)", color = SubjectColorPPB),
        Subject(code = "PABP", name = "PABP (Pendidikan Agama)", color = SubjectColorPABP),
        Subject(code = "B.INDO", name = "B. Indo (Bahasa Indonesia)", color = SubjectColorIndo),
        Subject(code = "B.JPN", name = "B. Jepang", color = SubjectColorJepang),
        Subject(code = "MTK", name = "MTK (Matematika)", color = SubjectColorMTK),
        Subject(code = "B.SND", name = "B. Sunda", color = SubjectColorSunda),
        Subject(code = "PBGTM", name = "PBGTM", color = SubjectColorPBGTM),
        Subject(code = "BASDAT", name = "Basdat (Basis Data)", color = SubjectColorBasdat),
        Subject(code = "SJ", name = "SJ (Sejarah)", color = SubjectColorSJ),
        Subject(code = "PJOK", name = "PJOK (Olahraga)", color = SubjectColorPJOK),
        Subject(code = "KKA", name = "KKA (Koding & AI)", color = SubjectColorKKA),
        Subject(code = "PKN", name = "PKN (Pancasila & Kewarganegaraan)", color = SubjectColorPKN)
    )

    fun findByCode(code: String): Subject {
        return ALL_SUBJECTS.find { it.code.equals(code, ignoreCase = true) }
            ?: ALL_SUBJECTS.find { it.name.contains(code, ignoreCase = true) }
            ?: Subject(code = code, name = code, color = SubjectColorDefault)
    }
}
