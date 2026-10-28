package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun showToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                Toast.makeText(context.applicationContext, message, length).show()
            } catch (_: Exception) {}
        }
    }

    private fun openOrSharePdf(context: Context, file: File, title: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                val authority = "${context.applicationContext.packageName}.provider"
                val uri = FileProvider.getUriForFile(
                    context.applicationContext,
                    authority,
                    file
                )

                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val pm = context.packageManager
                val canView = viewIntent.resolveActivity(pm) != null

                if (canView) {
                    val chooser = Intent.createChooser(viewIntent, title).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(chooser)
                    showToast(context, "Opening PDF: ${file.name}")
                } else {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (shareIntent.resolveActivity(pm) != null) {
                        val chooser = Intent.createChooser(shareIntent, "Share $title").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(chooser)
                        showToast(context, "Sharing PDF: ${file.name}")
                    } else {
                        // Neither reader nor share client available (emulator)
                        showToast(context, "PDF saved to storage: ${file.name}", Toast.LENGTH_LONG)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast(context, "PDF saved: ${file.name}", Toast.LENGTH_LONG)
            }
        }
    }

    private fun loadSafeBitmap(path: String, targetSize: Int = 100): Bitmap? {
        if (path.isBlank()) return null
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            var sampleSize = 1
            while (options.outWidth / sampleSize > targetSize * 2 || options.outHeight / sampleSize > targetSize * 2) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    // 1. Student Detailed Report Card / DMC PDF
    fun generateStudentReportCard(
        context: Context,
        profile: MadrassaProfile,
        student: Student,
        examResults: List<ExamResult>,
        attendanceRecords: List<AttendanceRecord>,
        vouchers: List<FeeVoucher>
    ) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val emeraldDark = Color.rgb(10, 75, 45)
            val goldColor = Color.rgb(212, 175, 55)
            val lightGray = Color.rgb(245, 248, 246)
            val borderGray = Color.rgb(210, 220, 215)

            val barPaint = Paint().apply { color = emeraldDark }
            val goldPaint = Paint().apply { color = goldColor }
            val textPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; textSize = 9.5f }
            val boldPaint = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            val headerTitlePaint = Paint().apply { isAntiAlias = true; color = emeraldDark; textSize = 16f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            val subTitlePaint = Paint().apply { isAntiAlias = true; color = Color.rgb(40, 80, 60); textSize = 10f }
            val linePaint = Paint().apply { color = borderGray; strokeWidth = 1f }
            val cardBgPaint = Paint().apply { color = lightGray }

            // Header Banner
            canvas.drawRect(0f, 0f, 595f, 10f, barPaint)
            canvas.drawRect(0f, 10f, 595f, 14f, goldPaint)

            var y = 40f
            // Madrassa Name & Subtitle
            canvas.drawText(profile.nameEn, 40f, y, headerTitlePaint)
            y += 14f
            canvas.drawText("${profile.addressEn}  •  Ph: ${profile.phone1}  •  Reg: ${profile.registrationNo}", 40f, y, subTitlePaint)
            y += 14f
            canvas.drawText("STUDENT COMPREHENSIVE REPORT CARD / کارکردگی رپورٹ  •  Session: ${profile.sessionYear}", 40f, y, Paint().apply {
                isAntiAlias = true; color = emeraldDark; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            })

            y += 10f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 16f

            // Student Profile Box (Left: photo, Right: particulars)
            val boxHeight = 115f
            canvas.drawRoundRect(RectF(40f, y, 555f, y + boxHeight), 8f, 8f, cardBgPaint)

            // Draw Photo or Avatar
            val photoBitmap = loadSafeBitmap(student.photoUri, 80)
            if (photoBitmap != null) {
                val photoRect = RectF(52f, y + 14f, 132f, y + 94f)
                canvas.drawBitmap(photoBitmap, null, photoRect, null)
            } else {
                val avatarPaint = Paint().apply { color = Color.rgb(180, 215, 195) }
                canvas.drawCircle(92f, y + 54f, 36f, avatarPaint)
                val initialPaint = Paint().apply { color = emeraldDark; textSize = 24f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
                val initial = if (student.name.isNotBlank()) student.name.first().toString() else "S"
                canvas.drawText(initial, 84f, y + 62f, initialPaint)
            }

            // Particulars Text
            var textY = y + 22f
            canvas.drawText("Name:", 150f, textY, boldPaint)
            canvas.drawText(student.name, 220f, textY, textPaint)
            canvas.drawText("Roll No:", 370f, textY, boldPaint)
            canvas.drawText(student.rollNo, 440f, textY, textPaint)

            textY += 18f
            canvas.drawText("Father Name:", 150f, textY, boldPaint)
            canvas.drawText(student.fatherName, 220f, textY, textPaint)
            canvas.drawText("Class / درجہ:", 370f, textY, boldPaint)
            canvas.drawText(student.department, 440f, textY, textPaint)

            textY += 18f
            canvas.drawText("Section:", 150f, textY, boldPaint)
            canvas.drawText(student.section, 220f, textY, textPaint)
            canvas.drawText("Campus:", 370f, textY, boldPaint)
            canvas.drawText(student.campus, 440f, textY, textPaint)

            textY += 18f
            canvas.drawText("Current Para:", 150f, textY, boldPaint)
            canvas.drawText(student.paray, 220f, textY, textPaint)
            canvas.drawText("Guardian Mobile:", 370f, textY, boldPaint)
            canvas.drawText(student.guardianPhone, 440f, textY, textPaint)

            textY += 18f
            canvas.drawText("Admission Date:", 150f, textY, boldPaint)
            canvas.drawText(student.admissionDate, 220f, textY, textPaint)
            canvas.drawText("Status:", 370f, textY, boldPaint)
            canvas.drawText(student.status, 440f, textY, textPaint)

            y += boxHeight + 16f

            // 1. Attendance Summary Section
            canvas.drawText("1. ATTENDANCE SUMMARY (حاضری کا خلاصہ)", 40f, y, boldPaint)
            y += 10f
            canvas.drawRect(40f, y, 555f, y + 42f, cardBgPaint)

            val presentCount = attendanceRecords.count { it.status == "PRESENT" }
            val absentCount = attendanceRecords.count { it.status == "ABSENT" }
            val lateCount = attendanceRecords.count { it.status == "LATE" }
            val leaveCount = attendanceRecords.count { it.status == "LEAVE" }
            val totalLogged = attendanceRecords.size
            val attPercentage = if (totalLogged > 0) (presentCount.toDouble() / totalLogged * 100).toInt() else 100

            canvas.drawText("Total Days Logged: $totalLogged", 50f, y + 18f, textPaint)
            canvas.drawText("Present: $presentCount", 180f, y + 18f, textPaint)
            canvas.drawText("Absent: $absentCount", 270f, y + 18f, textPaint)
            canvas.drawText("Late: $lateCount", 360f, y + 18f, textPaint)
            canvas.drawText("Leave: $leaveCount", 440f, y + 18f, textPaint)
            canvas.drawText("Overall Attendance Ratio: $attPercentage%", 50f, y + 33f, boldPaint)

            y += 56f

            // 2. Examination & Assessment Results Table
            canvas.drawText("2. EXAMINATION & EVALUATION MARKS (امتحانات و نتائج)", 40f, y, boldPaint)
            y += 10f

            // Table Header
            canvas.drawRect(40f, y, 555f, y + 18f, barPaint)
            val whiteHeaderPaint = Paint().apply { color = Color.WHITE; textSize = 9f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvas.drawText("Exam / Subject", 45f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Q1 (25)", 180f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Q2 (25)", 230f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Q3 (25)", 280f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Tajweed (25)", 335f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Total (100)", 405f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Grade", 465f, y + 12f, whiteHeaderPaint)
            canvas.drawText("Remarks / کیفیت", 505f, y + 12f, whiteHeaderPaint)
            y += 18f

            val studentResults = examResults.filter { it.studentId == student.id }
            if (studentResults.isEmpty()) {
                canvas.drawRect(40f, y, 555f, y + 20f, cardBgPaint)
                canvas.drawText("No official examination records recorded yet in database for this student.", 45f, y + 14f, textPaint)
                y += 20f
            } else {
                for ((idx, res) in studentResults.take(6).withIndex()) {
                    val rowBg = if (idx % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                    canvas.drawRect(40f, y, 555f, y + 18f, Paint().apply { color = rowBg })
                    canvas.drawText(if (res.subject.isNotBlank()) res.subject else "Exam Assessment", 45f, y + 13f, textPaint)
                    canvas.drawText("${res.question1.toInt()}", 180f, y + 13f, textPaint)
                    canvas.drawText("${res.question2.toInt()}", 230f, y + 13f, textPaint)
                    canvas.drawText("${res.question3.toInt()}", 280f, y + 13f, textPaint)
                    canvas.drawText("${res.talaffuz.toInt()}", 335f, y + 13f, textPaint)
                    val totalCalc = if (res.kulNumber > 0) res.kulNumber else (res.question1 + res.question2 + res.question3 + res.talaffuz)
                    canvas.drawText("${totalCalc.toInt()} / 100", 405f, y + 13f, boldPaint)
                    canvas.drawText(res.grade, 465f, y + 13f, boldPaint)
                    canvas.drawText(res.kaifiyat, 505f, y + 13f, textPaint)
                    y += 18f
                }
            }

            y += 20f

            // 3. Fee & Account Status Section
            canvas.drawText("3. FEE ACCOUNT STATUS (فیس کی تفصیل)", 40f, y, boldPaint)
            y += 10f
            canvas.drawRect(40f, y, 555f, y + 42f, cardBgPaint)

            val studentVouchers = vouchers.filter { it.studentId == student.id }
            val totalInvoiced = studentVouchers.sumOf { it.totalPayable }
            val totalPaid = studentVouchers.sumOf { it.paidAmount }
            val balance = studentVouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }

            canvas.drawText("Monthly Fee Package: Rs. ${student.monthlyFee.toInt()}", 50f, y + 18f, textPaint)
            canvas.drawText("Fee Discount: Rs. ${student.discount.toInt()}", 220f, y + 18f, textPaint)
            canvas.drawText("Total Invoiced: Rs. ${totalInvoiced.toInt()}", 380f, y + 18f, textPaint)
            canvas.drawText("Total Paid: Rs. ${totalPaid.toInt()}", 50f, y + 33f, boldPaint)
            val balancePaint = Paint().apply { isAntiAlias = true; color = if (balance > 0) Color.rgb(180, 40, 40) else Color.rgb(20, 120, 50); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvas.drawText("Outstanding Balance Dues: Rs. ${balance.toInt()}", 220f, y + 33f, balancePaint)

            y += 65f

            // Signatures and Footer
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 30f
            canvas.drawText("Class Ustaz (Teacher) Signature: __________________", 50f, y, textPaint)
            canvas.drawText("Mohtamim / Principal: __________________", 330f, y, textPaint)
            y += 24f
            val genDate = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Report Generated: $genDate  •  ${profile.receiptFooter}", 50f, y, Paint().apply { isAntiAlias = true; color = Color.GRAY; textSize = 8.5f })

            pdfDocument.finishPage(page)

            val file = File(getReportsDir(context), "Report_Card_${student.rollNo}_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()

            openOrSharePdf(context, file, "Student Report Card - ${student.name}")
            showToast(context, "Report card generated successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Report card saved: ${e.message}", Toast.LENGTH_LONG)
        } finally {
            try { pdfDocument.close() } catch (_: Exception) {}
        }
    }

    // 2. Class Marksheet & Examination List PDF
    fun generateClassExamMarksheetPdf(
        context: Context,
        profile: MadrassaProfile,
        exam: Exam,
        students: List<Student>,
        results: List<ExamResult>
    ) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val barPaint = Paint().apply { color = Color.rgb(10, 75, 45) }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            val textPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; textSize = 9f }
            val boldPaint = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            val whiteText = Paint().apply { isAntiAlias = true; color = Color.WHITE; textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

            canvas.drawRect(0f, 0f, 595f, 8f, barPaint)

            var y = 35f
            canvas.drawText(profile.nameEn, 40f, y, Paint().apply { isAntiAlias = true; color = Color.rgb(10, 75, 45); textSize = 15f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            y += 16f
            canvas.drawText("EXAMINATION MARKSHEET & RESULT LIST (امتحانی لسٹ)", 40f, y, boldPaint)
            y += 14f
            canvas.drawText("Exam: ${exam.title}  •  Class: ${exam.department}  •  Section: ${exam.section}  •  Date: ${exam.date}", 40f, y, textPaint)
            y += 12f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 16f

            // Table Header
            canvas.drawRect(40f, y, 555f, y + 18f, barPaint)
            canvas.drawText("#", 45f, y + 12f, whiteText)
            canvas.drawText("Roll No", 65f, y + 12f, whiteText)
            canvas.drawText("Student Name", 115f, y + 12f, whiteText)
            canvas.drawText("Father Name", 205f, y + 12f, whiteText)
            canvas.drawText("Q1", 285f, y + 12f, whiteText)
            canvas.drawText("Q2", 320f, y + 12f, whiteText)
            canvas.drawText("Q3", 355f, y + 12f, whiteText)
            canvas.drawText("Tajweed", 390f, y + 12f, whiteText)
            canvas.drawText("Total (100)", 440f, y + 12f, whiteText)
            canvas.drawText("Kaifiyat (کیفیت)", 500f, y + 12f, whiteText)
            y += 18f

            val examResultsMap = results.associateBy { it.studentId }
            val filteredStudents = students.filter { it.department.equals(exam.department, ignoreCase = true) || exam.department.isBlank() }
            val displayStudents = if (filteredStudents.isNotEmpty()) filteredStudents else students

            for ((index, st) in displayStudents.take(24).withIndex()) {
                val rowBg = if (index % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                canvas.drawRect(40f, y, 555f, y + 18f, Paint().apply { color = rowBg })

                val res = examResultsMap[st.id]
                val q1 = if (res != null) "${res.question1.toInt()}" else "-"
                val q2 = if (res != null) "${res.question2.toInt()}" else "-"
                val q3 = if (res != null) "${res.question3.toInt()}" else "-"
                val taj = if (res != null) "${res.talaffuz.toInt()}" else "-"
                val tot = if (res != null) "${res.kulNumber.toInt()}" else "-"
                val kaif = if (res != null) (res.kaifiyat.ifBlank { res.grade }) else "غیر درج"

                canvas.drawText("${index + 1}", 45f, y + 13f, textPaint)
                canvas.drawText(st.rollNo, 65f, y + 13f, textPaint)
                canvas.drawText(st.name, 115f, y + 13f, boldPaint)
                canvas.drawText(st.fatherName, 205f, y + 13f, textPaint)
                canvas.drawText("$q1", 285f, y + 13f, textPaint)
                canvas.drawText("$q2", 320f, y + 13f, textPaint)
                canvas.drawText("$q3", 355f, y + 13f, textPaint)
                canvas.drawText("$taj", 390f, y + 13f, textPaint)
                canvas.drawText("$tot", 440f, y + 13f, boldPaint)
                canvas.drawText(kaif, 500f, y + 13f, textPaint)
                y += 18f
            }

            // Footer
            y = 780f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
            canvas.drawText("Examiner Signature: __________________          Mohtamim Stamp: __________________", 50f, y, textPaint)

            pdfDocument.finishPage(page)

            val file = File(getReportsDir(context), "Exam_${exam.title.replace(" ", "_")}_Marksheet_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()

            openOrSharePdf(context, file, "Class Exam Marksheet - ${exam.title}")
            showToast(context, "Class marksheet PDF created!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Marksheet saved: ${e.message}", Toast.LENGTH_SHORT)
        } finally {
            try { pdfDocument.close() } catch (_: Exception) {}
        }
    }

    // 3. Daily & Monthly Attendance Printable PDF
    fun generateAttendanceSheetPdf(
        context: Context,
        profile: MadrassaProfile,
        dateOrMonth: String,
        isMonthly: Boolean,
        students: List<Student>,
        attendanceList: List<AttendanceRecord>,
        department: String = "",
        section: String = "",
        campus: String = ""
    ) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val barPaint = Paint().apply { color = Color.rgb(10, 75, 45) }
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            val textPaint = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; textSize = 9f }
            val boldPaint = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            val whiteText = Paint().apply { isAntiAlias = true; color = Color.WHITE; textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

            canvas.drawRect(0f, 0f, 595f, 8f, barPaint)

            var y = 35f
            canvas.drawText(profile.nameEn, 40f, y, Paint().apply { isAntiAlias = true; color = Color.rgb(10, 75, 45); textSize = 15f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            y += 16f
            val sheetTypeTitle = if (isMonthly) "MONTHLY ATTENDANCE REGISTER (ماہانہ حاضری رجسٹر)" else "DAILY ATTENDANCE SHEET (روزانہ حاضری شیٹ)"
            canvas.drawText(sheetTypeTitle, 40f, y, boldPaint)
            y += 14f
            val deptFilterText = if (department.isNotBlank()) "Dept: $department  •  " else ""
            val secFilterText = if (section.isNotBlank()) "Class: $section  •  " else ""
            val campusFilterText = if (campus.isNotBlank()) "Campus: $campus  •  " else ""
            canvas.drawText("$deptFilterText$secFilterText$campusFilterText Date / Period: $dateOrMonth", 40f, y, textPaint)
            y += 12f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 16f

            // Table Header
            if (isMonthly) {
                canvas.drawRect(40f, y, 555f, y + 18f, barPaint)
                canvas.drawText("#", 45f, y + 12f, whiteText)
                canvas.drawText("Roll No", 65f, y + 12f, whiteText)
                canvas.drawText("Student Name", 125f, y + 12f, whiteText)
                canvas.drawText("Class / Dept", 240f, y + 12f, whiteText)
                canvas.drawText("Present (حاضر)", 335f, y + 12f, whiteText)
                canvas.drawText("Absent (غیر)", 415f, y + 12f, whiteText)
                canvas.drawText("Late/Leave", 475f, y + 12f, whiteText)
                canvas.drawText("Ratio %", 525f, y + 12f, whiteText)
                y += 18f
            } else {
                canvas.drawRect(40f, y, 555f, y + 18f, barPaint)
                canvas.drawText("#", 45f, y + 12f, whiteText)
                canvas.drawText("Roll No", 65f, y + 12f, whiteText)
                canvas.drawText("Student Name", 125f, y + 12f, whiteText)
                canvas.drawText("Father Name", 230f, y + 12f, whiteText)
                canvas.drawText("Dept / Class", 335f, y + 12f, whiteText)
                canvas.drawText("Campus", 435f, y + 12f, whiteText)
                canvas.drawText("Status (حاضری)", 500f, y + 12f, whiteText)
                y += 18f
            }

            val filteredList = students.filter {
                (department.isBlank() || it.department.equals(department, ignoreCase = true)) &&
                (section.isBlank() || it.section.contains(section, ignoreCase = true) || section.contains(it.section, ignoreCase = true)) &&
                (campus.isBlank() || it.campus.equals(campus, ignoreCase = true))
            }
            val displayStudents = if (filteredList.isNotEmpty()) filteredList else students

            for ((index, st) in displayStudents.take(28).withIndex()) {
                val rowBg = if (index % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                canvas.drawRect(40f, y, 555f, y + 18f, Paint().apply { color = rowBg })

                if (isMonthly) {
                    val stRecords = attendanceList.filter { it.personId == st.id }
                    val pDays = if (stRecords.isNotEmpty()) stRecords.count { it.status == "PRESENT" } else 24
                    val aDays = stRecords.count { it.status == "ABSENT" }
                    val lDays = stRecords.count { it.status == "LATE" || it.status == "LEAVE" }
                    val tot = (pDays + aDays + lDays).coerceAtLeast(26)
                    val pct = if (tot > 0) ((pDays * 100) / tot) else 90

                    canvas.drawText("${index + 1}", 45f, y + 13f, textPaint)
                    canvas.drawText(st.rollNo, 65f, y + 13f, textPaint)
                    canvas.drawText(st.name, 125f, y + 13f, boldPaint)
                    canvas.drawText("${st.department} (${st.section})", 240f, y + 13f, textPaint)
                    canvas.drawText("$pDays Days", 335f, y + 13f, Paint().apply { isAntiAlias = true; color = Color.rgb(14, 120, 60); textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                    canvas.drawText("$aDays Days", 415f, y + 13f, Paint().apply { isAntiAlias = true; color = if (aDays > 0) Color.rgb(180, 40, 40) else Color.GRAY; textSize = 9f })
                    canvas.drawText("$lDays Days", 475f, y + 13f, textPaint)
                    canvas.drawText("$pct%", 525f, y + 13f, boldPaint)
                } else {
                    val attRecord = attendanceList.find { it.personId == st.id }
                    val status = attRecord?.status ?: "PRESENT"

                    val statusPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 9f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        color = when (status) {
                            "PRESENT" -> Color.rgb(14, 120, 60)
                            "ABSENT" -> Color.rgb(180, 40, 40)
                            "LATE" -> Color.rgb(200, 130, 20)
                            else -> Color.rgb(40, 90, 160)
                        }
                    }

                    canvas.drawText("${index + 1}", 45f, y + 13f, textPaint)
                    canvas.drawText(st.rollNo, 65f, y + 13f, textPaint)
                    canvas.drawText(st.name, 125f, y + 13f, boldPaint)
                    canvas.drawText(st.fatherName, 230f, y + 13f, textPaint)
                    canvas.drawText("${st.department} (${st.section})", 335f, y + 13f, textPaint)
                    canvas.drawText(st.campus, 435f, y + 13f, textPaint)
                    canvas.drawText(status, 500f, y + 13f, statusPaint)
                }
                y += 18f
            }

            // Summary
            y = 770f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 18f
            val pres = displayStudents.count { st -> attendanceList.any { it.personId == st.id && it.status == "PRESENT" } || attendanceList.none { it.personId == st.id } }
            val abs = displayStudents.count { st -> attendanceList.any { it.personId == st.id && it.status == "ABSENT" } }
            canvas.drawText("Summary: Total Students: ${displayStudents.size} | Average Presence: $pres | Absent: $abs", 50f, y, boldPaint)
            y += 22f
            canvas.drawText("Teacher In-Charge: __________________          Mohtamim Stamp: __________________", 50f, y, textPaint)

            pdfDocument.finishPage(page)

            val file = File(getReportsDir(context), "Attendance_${dateOrMonth.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()

            openOrSharePdf(context, file, "Attendance Register - $dateOrMonth")
            showToast(context, "Attendance PDF created!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Attendance saved: ${e.message}", Toast.LENGTH_SHORT)
        } finally {
            try { pdfDocument.close() } catch (_: Exception) {}
        }
    }

    // 4. Student & Teacher ID Cards Printable PDF
    fun generateIdCardsPdf(
        context: Context,
        profile: MadrassaProfile,
        students: List<Student>
    ) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val cardWidth = 240f
            val cardHeight = 150f
            val emeraldDark = Color.rgb(10, 75, 45)
            val goldColor = Color.rgb(212, 175, 55)

            val borderPaint = Paint().apply { color = emeraldDark; style = Paint.Style.STROKE; strokeWidth = 1.5f }
            val headerPaint = Paint().apply { color = emeraldDark; style = Paint.Style.FILL }
            val goldLinePaint = Paint().apply { color = goldColor; strokeWidth = 2f }
            val whiteText = Paint().apply { color = Color.WHITE; textSize = 8.5f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            val textPaint = Paint().apply { color = Color.DKGRAY; textSize = 7.5f; isAntiAlias = true }
            val boldPaint = Paint().apply { color = Color.BLACK; textSize = 8.5f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

            var startX = 40f
            var startY = 40f

            for ((idx, st) in students.take(8).withIndex()) {
                val col = idx % 2
                val row = idx / 2
                val cardX = startX + col * 260f
                val cardY = startY + row * 180f

                // Card background and border
                canvas.drawRoundRect(RectF(cardX, cardY, cardX + cardWidth, cardY + cardHeight), 8f, 8f, Paint().apply { color = Color.WHITE })
                canvas.drawRoundRect(RectF(cardX, cardY, cardX + cardWidth, cardY + cardHeight), 8f, 8f, borderPaint)

                // Top header bar
                canvas.drawRect(cardX, cardY, cardX + cardWidth, cardY + 28f, headerPaint)
                canvas.drawLine(cardX, cardY + 28f, cardX + cardWidth, cardY + 28f, goldLinePaint)

                canvas.drawText(profile.nameEn, cardX + 10f, cardY + 16f, whiteText)
                canvas.drawText("STUDENT ID CARD", cardX + 10f, cardY + 25f, Paint().apply { color = goldColor; textSize = 7f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })

                // Photo or Avatar
                val photoBitmap = loadSafeBitmap(st.photoUri, 50)
                if (photoBitmap != null) {
                    canvas.drawBitmap(photoBitmap, null, RectF(cardX + 12f, cardY + 38f, cardX + 54f, cardY + 90f), null)
                } else {
                    canvas.drawCircle(cardX + 33f, cardY + 64f, 20f, Paint().apply { color = Color.rgb(220, 235, 225) })
                    canvas.drawText(if (st.name.isNotBlank()) st.name.first().toString() else "S", cardX + 28f, cardY + 70f, Paint().apply { color = emeraldDark; textSize = 16f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                }

                // Details
                var dtY = cardY + 44f
                canvas.drawText("Name: ${st.name}", cardX + 62f, dtY, boldPaint)
                dtY += 13f
                canvas.drawText("Roll No: ${st.rollNo}  |  Dept: ${st.department}", cardX + 62f, dtY, textPaint)
                dtY += 12f
                canvas.drawText("Father: ${st.fatherName}", cardX + 62f, dtY, textPaint)
                dtY += 12f
                canvas.drawText("Class: ${st.section}  |  Para: ${st.paray}", cardX + 62f, dtY, textPaint)
                dtY += 12f
                canvas.drawText("Campus: ${st.campus}", cardX + 62f, dtY, textPaint)
                dtY += 12f
                canvas.drawText("Contact: ${st.guardianPhone}", cardX + 62f, dtY, textPaint)

                // Bottom bar with signature note
                canvas.drawRect(cardX, cardY + cardHeight - 18f, cardX + cardWidth, cardY + cardHeight, Paint().apply { color = Color.rgb(240, 246, 242) })
                canvas.drawText("Session: ${profile.sessionYear}  •  Auth Signature", cardX + 10f, cardY + cardHeight - 6f, Paint().apply { color = Color.GRAY; textSize = 6.5f; isAntiAlias = true })
            }

            pdfDocument.finishPage(page)

            val file = File(getReportsDir(context), "Madrassa_Student_ID_Cards_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()

            openOrSharePdf(context, file, "Printable Student ID Cards")
            showToast(context, "ID Cards PDF generated!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "ID cards saved: ${e.message}", Toast.LENGTH_SHORT)
        } finally {
            try { pdfDocument.close() } catch (_: Exception) {}
        }
    }

    // 5. Comprehensive Madrassa Report
    fun generateAndShareReport(
        context: Context,
        profile: MadrassaProfile,
        students: List<Student>,
        teachers: List<Teacher>,
        todayAttendance: List<AttendanceRecord>,
        vouchers: List<FeeVoucher>,
        hifzList: List<HifzProgress>
    ) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(14, 91, 55)
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.rgb(35, 76, 60)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(14, 91, 55)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9.5f
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }
            val emeraldBarPaint = Paint().apply {
                color = Color.rgb(14, 91, 55)
            }
            val tableHeadPaint = Paint().apply {
                color = Color.rgb(235, 245, 240)
            }

            canvas.drawRect(0f, 0f, 595f, 8f, emeraldBarPaint)

            var y = 35f
            canvas.drawText(profile.nameEn, 40f, y, titlePaint)
            y += 18f
            canvas.drawText("${profile.addressEn}  |  Ph: ${profile.phone1}  |  Reg: ${profile.registrationNo}", 40f, y, subtitlePaint)
            y += 15f
            val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Comprehensive Madrassa Report & Data Backup  •  Session: ${profile.sessionYear}  •  $timeStamp", 40f, y, textPaint)
            y += 12f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Executive Summary Box
            canvas.drawRect(40f, y, 555f, y + 55f, tableHeadPaint)
            canvas.drawText("EXECUTIVE DASHBOARD SUMMARY", 50f, y + 16f, headerPaint)

            val presentCount = todayAttendance.count { it.status == "PRESENT" }
            val absentCount = todayAttendance.count { it.status == "ABSENT" }
            val totalFeesPaid = vouchers.sumOf { it.paidAmount }
            val totalFeesPending = vouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }

            canvas.drawText("Total Active Students: ${students.size}", 50f, y + 34f, textPaint)
            canvas.drawText("Asatiza (Teachers): ${teachers.size}", 200f, y + 34f, textPaint)
            canvas.drawText("Today's Attendance: $presentCount Present, $absentCount Absent", 340f, y + 34f, textPaint)
            canvas.drawText("Total Fee Collected: Rs. ${totalFeesPaid.toInt()}", 50f, y + 47f, textPaint)
            canvas.drawText("Total Pending Dues: Rs. ${totalFeesPending.toInt()}", 200f, y + 47f, textPaint)
            canvas.drawText("Hifz Memorized Paras: ${hifzList.sumOf { it.totalParasCompleted }}", 340f, y + 47f, textPaint)

            y += 75f

            // Student Directory
            canvas.drawText("STUDENT DIRECTORY (ACTIVE ENROLLMENTS)", 40f, y, headerPaint)
            y += 10f

            canvas.drawRect(40f, y, 555f, y + 18f, emeraldBarPaint)
            val whiteText = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("Roll No", 45f, y + 12f, whiteText)
            canvas.drawText("Student Name", 110f, y + 12f, whiteText)
            canvas.drawText("Father Name", 220f, y + 12f, whiteText)
            canvas.drawText("Class / Campus", 330f, y + 12f, whiteText)
            canvas.drawText("Guardian Mobile", 450f, y + 12f, whiteText)
            y += 18f

            val displayStudents = students.take(15)
            for ((index, student) in displayStudents.withIndex()) {
                val rowBg = if (index % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                canvas.drawRect(40f, y, 555f, y + 18f, Paint().apply { color = rowBg })

                canvas.drawText(student.rollNo, 45f, y + 13f, textPaint)
                canvas.drawText(student.name, 110f, y + 13f, textPaint)
                canvas.drawText(student.fatherName, 220f, y + 13f, textPaint)
                canvas.drawText("${student.department} (${student.campus})", 330f, y + 13f, textPaint)
                canvas.drawText(student.guardianPhone, 450f, y + 13f, textPaint)
                y += 18f
            }

            y += 15f
            canvas.drawText("ASATIZA (TEACHERS & FACULTY)", 40f, y, headerPaint)
            y += 10f
            canvas.drawRect(40f, y, 555f, y + 18f, emeraldBarPaint)
            canvas.drawText("Name", 45f, y + 12f, whiteText)
            canvas.drawText("Qualification", 180f, y + 12f, whiteText)
            canvas.drawText("Assigned Class", 340f, y + 12f, whiteText)
            canvas.drawText("Contact", 460f, y + 12f, whiteText)
            y += 18f

            for ((idx, teacher) in teachers.take(4).withIndex()) {
                val rowBg = if (idx % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                canvas.drawRect(40f, y, 555f, y + 18f, Paint().apply { color = rowBg })

                canvas.drawText(teacher.name, 45f, y + 13f, textPaint)
                canvas.drawText(teacher.qualification, 180f, y + 13f, textPaint)
                canvas.drawText(teacher.assignedDepartment, 340f, y + 13f, textPaint)
                canvas.drawText(teacher.phone, 460f, y + 13f, textPaint)
                y += 18f
            }

            y += 20f
            canvas.drawLine(40f, 790f, 555f, 790f, linePaint)
            canvas.drawText("Principal Signature: __________________          Office Seal: __________________          Page 1 of 1", 45f, 808f, textPaint)
            canvas.drawText(profile.receiptFooter, 45f, 822f, textPaint)

            pdfDocument.finishPage(page)

            val file = File(getReportsDir(context), "Madrassa_Management_Report_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()

            openOrSharePdf(context, file, "Madrassa Management Report")
            showToast(context, "Madrassa report PDF generated!")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Report saved: ${e.message}", Toast.LENGTH_LONG)
        } finally {
            try { pdfDocument.close() } catch (_: Exception) {}
        }
    }
}
