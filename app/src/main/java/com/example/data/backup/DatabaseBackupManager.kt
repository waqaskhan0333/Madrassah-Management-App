package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSummary(
    val studentCount: Int,
    val teacherCount: Int,
    val attendanceCount: Int,
    val hifzCount: Int,
    val nazraCount: Int = 0,
    val qaidaCount: Int = 0,
    val tajweedCount: Int = 0,
    val courseCount: Int = 0,
    val examCount: Int = 0,
    val examResultCount: Int = 0,
    val voucherCount: Int = 0,
    val paymentCount: Int = 0,
    val photoCount: Int = 0,
    val exportedDate: String = "",
    val madrassaName: String = ""
)

class DatabaseBackupManager(
    private val db: AppDatabase
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // 1. Export Complete Database to JSON (including Photos as Base64, Full Student Bio Data, Sabaq Progress, Exam Marks & Results)
    suspend fun exportToJson(context: Context? = null): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("format", "MADRASSA_MANAGER_BACKUP")
        root.put("version", 3)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("exportedDate", dateFormat.format(Date()))
        root.put("databaseType", "Room_SQLite_Offline")

        var photosEncoded = 0

        // Profile
        val profile = db.madrassaProfileDao().getProfileDirect()
        if (profile != null) {
            val profObj = JSONObject()
            profObj.put("id", profile.id)
            profObj.put("nameUr", profile.nameUr)
            profObj.put("nameEn", profile.nameEn)
            profObj.put("mottoUr", profile.mottoUr)
            profObj.put("mottoEn", profile.mottoEn)
            profObj.put("addressUr", profile.addressUr)
            profObj.put("addressEn", profile.addressEn)
            profObj.put("phone1", profile.phone1)
            profObj.put("phone2", profile.phone2)
            profObj.put("email", profile.email)
            profObj.put("website", profile.website)
            profObj.put("registrationNo", profile.registrationNo)
            profObj.put("sessionYear", profile.sessionYear)
            profObj.put("principalName", profile.principalName)
            profObj.put("receiptFooter", profile.receiptFooter)
            profObj.put("logoUri", profile.logoUri)
            if (profile.logoUri.isNotBlank()) {
                val b64 = ImageStorageHelper.fileToBase64(profile.logoUri)
                if (b64 != null) {
                    profObj.put("logoBase64", b64)
                    photosEncoded++
                }
            }
            root.put("profile", profObj)
        }

        // Students (Full Bio Data & Photos)
        val students = db.studentDao().getAllStudentsDirect()
        val studentsArr = JSONArray()
        students.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("rollNo", s.rollNo)
            obj.put("name", s.name)
            obj.put("fatherName", s.fatherName)
            obj.put("guardianName", s.guardianName)
            obj.put("guardianPhone", s.guardianPhone)
            obj.put("phone2", s.phone2)
            obj.put("dob", s.dob)
            obj.put("gender", s.gender)
            obj.put("cnicBForm", s.cnicBForm)
            obj.put("admissionDate", s.admissionDate)
            obj.put("address", s.address)
            obj.put("department", s.department)
            obj.put("section", s.section)
            obj.put("status", s.status)
            obj.put("monthlyFee", s.monthlyFee)
            obj.put("discount", s.discount)
            obj.put("photoUri", s.photoUri)
            obj.put("notes", s.notes)
            obj.put("isDeleted", s.isDeleted)
            obj.put("deletedAt", s.deletedAt ?: JSONObject.NULL)
            obj.put("createdAt", s.createdAt)

            if (s.photoUri.isNotBlank()) {
                val b64 = ImageStorageHelper.fileToBase64(s.photoUri)
                if (b64 != null) {
                    obj.put("photoBase64", b64)
                    photosEncoded++
                }
            }
            studentsArr.put(obj)
        }
        root.put("students", studentsArr)

        // Teachers (Full Bio Data & Photos)
        val teachers = db.teacherDao().getAllTeachersDirect()
        val teachersArr = JSONArray()
        teachers.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("name", t.name)
            obj.put("fatherName", t.fatherName)
            obj.put("phone", t.phone)
            obj.put("cnic", t.cnic)
            obj.put("qualification", t.qualification)
            obj.put("joinDate", t.joinDate)
            obj.put("monthlySalary", t.monthlySalary)
            obj.put("assignedDepartment", t.assignedDepartment)
            obj.put("assignedSections", t.assignedSections)
            obj.put("photoUri", t.photoUri)
            obj.put("role", t.role)
            obj.put("status", t.status)
            obj.put("isDeleted", t.isDeleted)
            obj.put("deletedAt", t.deletedAt ?: JSONObject.NULL)

            if (t.photoUri.isNotBlank()) {
                val b64 = ImageStorageHelper.fileToBase64(t.photoUri)
                if (b64 != null) {
                    obj.put("photoBase64", b64)
                    photosEncoded++
                }
            }
            teachersArr.put(obj)
        }
        root.put("teachers", teachersArr)

        // Attendance (Full Daily Logs)
        val attendance = db.attendanceDao().getAllAttendanceDirect()
        val attendanceArr = JSONArray()
        attendance.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("personId", a.personId)
            obj.put("personType", a.personType)
            obj.put("date", a.date)
            obj.put("status", a.status)
            obj.put("checkInTime", a.checkInTime)
            obj.put("remarks", a.remarks)
            attendanceArr.put(obj)
        }
        root.put("attendance", attendanceArr)

        // Sabaq Progress (Hifz)
        val hifzList = db.progressDao().getAllHifzDirect()
        val hifzArr = JSONArray()
        hifzList.forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("studentId", h.studentId)
            obj.put("date", h.date)
            obj.put("sabaqPara", h.sabaqPara)
            obj.put("sabaqSurah", h.sabaqSurah)
            obj.put("sabaqAyahRange", h.sabaqAyahRange)
            obj.put("sabqiRange", h.sabqiRange)
            obj.put("manzilParas", h.manzilParas)
            obj.put("grade", h.grade)
            obj.put("mistakesCount", h.mistakesCount)
            obj.put("totalParasCompleted", h.totalParasCompleted)
            obj.put("remarks", h.remarks)
            hifzArr.put(obj)
        }
        root.put("hifz_progress", hifzArr)

        // Nazra Progress
        val nazraList = db.progressDao().getAllNazraDirect()
        val nazraArr = JSONArray()
        nazraList.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("studentId", n.studentId)
            obj.put("date", n.date)
            obj.put("currentPara", n.currentPara)
            obj.put("pageRange", n.pageRange)
            obj.put("mistakesCount", n.mistakesCount)
            obj.put("grade", n.grade)
            obj.put("remarks", n.remarks)
            nazraArr.put(obj)
        }
        root.put("nazra_progress", nazraArr)

        // Noorani Qaida Progress
        val qaidaList = db.progressDao().getAllQaidaDirect()
        val qaidaArr = JSONArray()
        qaidaList.forEach { q ->
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("studentId", q.studentId)
            obj.put("date", q.date)
            obj.put("lessonNo", q.lessonNo)
            obj.put("topic", q.topic)
            obj.put("grade", q.grade)
            obj.put("passOrRepeat", q.passOrRepeat)
            obj.put("remarks", q.remarks)
            qaidaArr.put(obj)
        }
        root.put("qaida_progress", qaidaArr)

        // Tajweed Progress
        val tajweedList = db.progressDao().getAllTajweedDirect()
        val tajweedArr = JSONArray()
        tajweedList.forEach { tj ->
            val obj = JSONObject()
            obj.put("id", tj.id)
            obj.put("studentId", tj.studentId)
            obj.put("date", tj.date)
            obj.put("topic", tj.topic)
            obj.put("practicalAssessment", tj.practicalAssessment)
            obj.put("style", tj.style)
            obj.put("grade", tj.grade)
            obj.put("remarks", tj.remarks)
            tajweedArr.put(obj)
        }
        root.put("tajweed_progress", tajweedArr)

        // Courses
        val courses = db.courseDao().getAllCoursesDirect()
        val coursesArr = JSONArray()
        courses.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("nameUr", c.nameUr)
            obj.put("nameEn", c.nameEn)
            obj.put("description", c.description)
            obj.put("duration", c.duration)
            obj.put("teacherName", c.teacherName)
            obj.put("monthlyFee", c.monthlyFee)
            obj.put("status", c.status)
            coursesArr.put(obj)
        }
        root.put("courses", coursesArr)

        // Exams
        val exams = db.examDao().getAllExamsDirect()
        val examsArr = JSONArray()
        exams.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("title", e.title)
            obj.put("type", e.type)
            obj.put("date", e.date)
            obj.put("department", e.department)
            obj.put("section", e.section)
            obj.put("totalMarks", e.totalMarks)
            obj.put("passMarks", e.passMarks)
            obj.put("status", e.status)
            examsArr.put(obj)
        }
        root.put("exams", examsArr)

        // Exam Results (Full Oral Assessment & Detailed Marks)
        val results = db.examDao().getAllExamResultsDirect()
        val resultsArr = JSONArray()
        results.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("examId", r.examId)
            obj.put("studentId", r.studentId)
            obj.put("studentName", r.studentName)
            obj.put("fatherName", r.fatherName)
            obj.put("studentClass", r.studentClass)
            obj.put("paray", r.paray)
            obj.put("question1", r.question1)
            obj.put("question2", r.question2)
            obj.put("question3", r.question3)
            obj.put("talaffuz", r.talaffuz)
            obj.put("kulNumber", r.kulNumber)
            obj.put("totalMaxMarks", r.totalMaxMarks)
            obj.put("kaifiyat", r.kaifiyat)
            obj.put("subject", r.subject)
            obj.put("marksObtained", r.marksObtained)
            obj.put("totalMarks", r.totalMarks)
            obj.put("grade", r.grade)
            obj.put("remarks", r.remarks)
            obj.put("isPassed", r.isPassed)
            resultsArr.put(obj)
        }
        root.put("exam_results", resultsArr)

        // Fee Vouchers & Payments
        val vouchers = db.feeDao().getAllVouchersDirect()
        val vouchersArr = JSONArray()
        vouchers.forEach { v ->
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("voucherNo", v.voucherNo)
            obj.put("studentId", v.studentId)
            obj.put("monthYear", v.monthYear)
            obj.put("tuitionFee", v.tuitionFee)
            obj.put("admissionFee", v.admissionFee)
            obj.put("examFee", v.examFee)
            obj.put("discount", v.discount)
            obj.put("totalPayable", v.totalPayable)
            obj.put("paidAmount", v.paidAmount)
            obj.put("status", v.status)
            obj.put("dueDate", v.dueDate)
            vouchersArr.put(obj)
        }
        root.put("fee_vouchers", vouchersArr)

        val payments = db.feeDao().getAllPaymentsDirect()
        val paymentsArr = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("voucherId", p.voucherId)
            obj.put("studentId", p.studentId)
            obj.put("amountPaid", p.amountPaid)
            obj.put("paymentDate", p.paymentDate)
            obj.put("paymentMode", p.paymentMode)
            obj.put("receiptNo", p.receiptNo)
            obj.put("receiverName", p.receiverName)
            obj.put("notes", p.notes)
            paymentsArr.put(obj)
        }
        root.put("payments", paymentsArr)

        root.put("stats_photos_count", photosEncoded)
        root.toString(2)
    }

    // 2. Export Database to SQL Dump Script (All tables)
    suspend fun exportToSql(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("-- =====================================================\n")
        sb.append("-- Madrassa Manager SQLite Offline Database Dump\n")
        sb.append("-- Generated on: ${dateFormat.format(Date())}\n")
        sb.append("-- Complete Offline Local SQLite Database (All Tables)\n")
        sb.append("-- =====================================================\n\n")
        sb.append("BEGIN TRANSACTION;\n\n")

        // Madrassa Profile
        val profile = db.madrassaProfileDao().getProfileDirect()
        if (profile != null) {
            sb.append("DELETE FROM madrassa_profile;\n")
            sb.append("INSERT INTO madrassa_profile (id, nameUr, nameEn, mottoUr, mottoEn, addressUr, addressEn, phone1, phone2, email, website, registrationNo, sessionYear, principalName, receiptFooter, logoUri) VALUES ")
            sb.append("(${profile.id}, '${escapeSql(profile.nameUr)}', '${escapeSql(profile.nameEn)}', '${escapeSql(profile.mottoUr)}', '${escapeSql(profile.mottoEn)}', '${escapeSql(profile.addressUr)}', '${escapeSql(profile.addressEn)}', '${escapeSql(profile.phone1)}', '${escapeSql(profile.phone2)}', '${escapeSql(profile.email)}', '${escapeSql(profile.website)}', '${escapeSql(profile.registrationNo)}', '${escapeSql(profile.sessionYear)}', '${escapeSql(profile.principalName)}', '${escapeSql(profile.receiptFooter)}', '${escapeSql(profile.logoUri)}');\n\n")
        }

        // Students
        val students = db.studentDao().getAllStudentsDirect()
        if (students.isNotEmpty()) {
            sb.append("-- Students (${students.size} records)\n")
            sb.append("DELETE FROM students;\n")
            students.forEach { s ->
                sb.append("INSERT INTO students (id, rollNo, name, fatherName, guardianName, guardianPhone, phone2, dob, gender, cnicBForm, admissionDate, address, department, section, status, monthlyFee, discount, photoUri, notes, isDeleted, deletedAt, createdAt) VALUES ")
                sb.append("(${s.id}, '${escapeSql(s.rollNo)}', '${escapeSql(s.name)}', '${escapeSql(s.fatherName)}', '${escapeSql(s.guardianName)}', '${escapeSql(s.guardianPhone)}', '${escapeSql(s.phone2)}', '${escapeSql(s.dob)}', '${escapeSql(s.gender)}', '${escapeSql(s.cnicBForm)}', '${escapeSql(s.admissionDate)}', '${escapeSql(s.address)}', '${escapeSql(s.department)}', '${escapeSql(s.section)}', '${escapeSql(s.status)}', ${s.monthlyFee}, ${s.discount}, '${escapeSql(s.photoUri)}', '${escapeSql(s.notes)}', ${if (s.isDeleted) 1 else 0}, ${s.deletedAt ?: "NULL"}, ${s.createdAt});\n")
            }
            sb.append("\n")
        }

        // Teachers
        val teachers = db.teacherDao().getAllTeachersDirect()
        if (teachers.isNotEmpty()) {
            sb.append("-- Teachers (${teachers.size} records)\n")
            sb.append("DELETE FROM teachers;\n")
            teachers.forEach { t ->
                sb.append("INSERT INTO teachers (id, name, fatherName, phone, cnic, qualification, joinDate, monthlySalary, assignedDepartment, assignedSections, photoUri, role, status, isDeleted, deletedAt) VALUES ")
                sb.append("(${t.id}, '${escapeSql(t.name)}', '${escapeSql(t.fatherName)}', '${escapeSql(t.phone)}', '${escapeSql(t.cnic)}', '${escapeSql(t.qualification)}', '${escapeSql(t.joinDate)}', ${t.monthlySalary}, '${escapeSql(t.assignedDepartment)}', '${escapeSql(t.assignedSections)}', '${escapeSql(t.photoUri)}', '${escapeSql(t.role)}', '${escapeSql(t.status)}', ${if (t.isDeleted) 1 else 0}, ${t.deletedAt ?: "NULL"});\n")
            }
            sb.append("\n")
        }

        // Attendance
        val attendance = db.attendanceDao().getAllAttendanceDirect()
        if (attendance.isNotEmpty()) {
            sb.append("-- Attendance (${attendance.size} records)\n")
            sb.append("DELETE FROM attendance;\n")
            attendance.forEach { a ->
                sb.append("INSERT INTO attendance (id, personId, personType, date, status, checkInTime, remarks) VALUES ")
                sb.append("(${a.id}, ${a.personId}, '${escapeSql(a.personType)}', '${escapeSql(a.date)}', '${escapeSql(a.status)}', '${escapeSql(a.checkInTime)}', '${escapeSql(a.remarks)}');\n")
            }
            sb.append("\n")
        }

        // Hifz Progress
        val hifzList = db.progressDao().getAllHifzDirect()
        if (hifzList.isNotEmpty()) {
            sb.append("-- Hifz Progress (${hifzList.size} records)\n")
            sb.append("DELETE FROM hifz_progress;\n")
            hifzList.forEach { h ->
                sb.append("INSERT INTO hifz_progress (id, studentId, date, sabaqPara, sabaqSurah, sabaqAyahRange, sabqiRange, manzilParas, grade, mistakesCount, totalParasCompleted, remarks) VALUES ")
                sb.append("(${h.id}, ${h.studentId}, '${escapeSql(h.date)}', ${h.sabaqPara}, '${escapeSql(h.sabaqSurah)}', '${escapeSql(h.sabaqAyahRange)}', '${escapeSql(h.sabqiRange)}', '${escapeSql(h.manzilParas)}', '${escapeSql(h.grade)}', ${h.mistakesCount}, ${h.totalParasCompleted}, '${escapeSql(h.remarks)}');\n")
            }
            sb.append("\n")
        }

        // Exams
        val exams = db.examDao().getAllExamsDirect()
        if (exams.isNotEmpty()) {
            sb.append("-- Exams (${exams.size} records)\n")
            sb.append("DELETE FROM exams;\n")
            exams.forEach { e ->
                sb.append("INSERT INTO exams (id, title, type, date, department, section, totalMarks, passMarks, status) VALUES ")
                sb.append("(${e.id}, '${escapeSql(e.title)}', '${escapeSql(e.type)}', '${escapeSql(e.date)}', '${escapeSql(e.department)}', '${escapeSql(e.section)}', ${e.totalMarks}, ${e.passMarks}, '${escapeSql(e.status)}');\n")
            }
            sb.append("\n")
        }

        // Exam Results
        val examResults = db.examDao().getAllExamResultsDirect()
        if (examResults.isNotEmpty()) {
            sb.append("-- Exam Results (${examResults.size} records)\n")
            sb.append("DELETE FROM exam_results;\n")
            examResults.forEach { r ->
                sb.append("INSERT INTO exam_results (id, examId, studentId, studentName, fatherName, studentClass, paray, question1, question2, question3, talaffuz, kulNumber, totalMaxMarks, kaifiyat, subject, marksObtained, totalMarks, grade, remarks, isPassed) VALUES ")
                sb.append("(${r.id}, ${r.examId}, ${r.studentId}, '${escapeSql(r.studentName)}', '${escapeSql(r.fatherName)}', '${escapeSql(r.studentClass)}', '${escapeSql(r.paray)}', ${r.question1}, ${r.question2}, ${r.question3}, ${r.talaffuz}, ${r.kulNumber}, ${r.totalMaxMarks}, '${escapeSql(r.kaifiyat)}', '${escapeSql(r.subject)}', ${r.marksObtained}, ${r.totalMarks}, '${escapeSql(r.grade)}', '${escapeSql(r.remarks)}', ${if (r.isPassed) 1 else 0});\n")
            }
            sb.append("\n")
        }

        // Fee Vouchers & Payments
        val vouchers = db.feeDao().getAllVouchersDirect()
        if (vouchers.isNotEmpty()) {
            sb.append("-- Fee Vouchers (${vouchers.size} records)\n")
            sb.append("DELETE FROM fee_vouchers;\n")
            vouchers.forEach { v ->
                sb.append("INSERT INTO fee_vouchers (id, voucherNo, studentId, monthYear, tuitionFee, admissionFee, examFee, discount, totalPayable, paidAmount, status, dueDate) VALUES ")
                sb.append("(${v.id}, '${escapeSql(v.voucherNo)}', ${v.studentId}, '${escapeSql(v.monthYear)}', ${v.tuitionFee}, ${v.admissionFee}, ${v.examFee}, ${v.discount}, ${v.totalPayable}, ${v.paidAmount}, '${escapeSql(v.status)}', '${escapeSql(v.dueDate)}');\n")
            }
            sb.append("\n")
        }

        val payments = db.feeDao().getAllPaymentsDirect()
        if (payments.isNotEmpty()) {
            sb.append("-- Payments (${payments.size} records)\n")
            sb.append("DELETE FROM payments;\n")
            payments.forEach { p ->
                sb.append("INSERT INTO payments (id, voucherId, studentId, amountPaid, paymentDate, paymentMode, receiptNo, receiverName, notes) VALUES ")
                sb.append("(${p.id}, ${p.voucherId}, ${p.studentId}, ${p.amountPaid}, '${escapeSql(p.paymentDate)}', '${escapeSql(p.paymentMode)}', '${escapeSql(p.receiptNo)}', '${escapeSql(p.receiverName)}', '${escapeSql(p.notes)}');\n")
            }
            sb.append("\n")
        }

        sb.append("COMMIT;\n")
        sb.toString()
    }

    // 3. Import from JSON (Restores all tables, Bio Data, Images, Sabaq Progress & Exams)
    suspend fun importFromJson(
        context: Context? = null,
        jsonString: String,
        replaceExisting: Boolean
    ): BackupSummary = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonString)

        if (replaceExisting) {
            db.feeDao().clearAllPayments()
            db.feeDao().clearAllVouchers()
            db.progressDao().clearAllHifz()
            db.progressDao().clearAllNazra()
            db.progressDao().clearAllQaida()
            db.progressDao().clearAllTajweed()
            db.examDao().clearAllExamResults()
            db.examDao().clearAllExams()
            db.courseDao().clearAllCourses()
            db.attendanceDao().clearAllAttendance()
            db.teacherDao().clearAllTeachers()
            db.studentDao().clearAllStudents()
        }

        var photosRestored = 0

        // Profile
        var madrassaName = "جامعۃ العلوم الاسلامیہ"
        if (root.has("profile")) {
            val pObj = root.getJSONObject("profile")
            madrassaName = pObj.optString("nameUr", madrassaName)
            var restoredLogoUri = pObj.optString("logoUri", "")
            if (context != null && pObj.has("logoBase64")) {
                val logoB64 = pObj.getString("logoBase64")
                val savedFile = ImageStorageHelper.base64ToFile(context, logoB64, "logos", "madrassa_logo")
                if (savedFile != null) {
                    restoredLogoUri = savedFile
                    photosRestored++
                }
            }

            val prof = MadrassaProfile(
                id = pObj.optInt("id", 1),
                nameUr = pObj.optString("nameUr", "جامعۃ العلوم الاسلامیہ"),
                nameEn = pObj.optString("nameEn", "Jamia Uloom-ul-Islamia"),
                mottoUr = pObj.optString("mottoUr", "علم، عمل، اور اخلاص"),
                mottoEn = pObj.optString("mottoEn", "Knowledge, Action, and Sincerity"),
                addressUr = pObj.optString("addressUr", "مرکزی روڈ، لاہور"),
                addressEn = pObj.optString("addressEn", "Main Boulevard, Lahore"),
                phone1 = pObj.optString("phone1", "042-35890001"),
                phone2 = pObj.optString("phone2", "0300-1234567"),
                email = pObj.optString("email", "info@jamia.edu.pk"),
                website = pObj.optString("website", "www.jamia.edu.pk"),
                registrationNo = pObj.optString("registrationNo", "REG-2024-WIF-0912"),
                sessionYear = pObj.optString("sessionYear", "1446-1447 ھ / 2024-2025 ء"),
                principalName = pObj.optString("principalName", "مولانا عبد الغفور صاحب دامت برکاتہم"),
                receiptFooter = pObj.optString("receiptFooter", "جزاکم اللہ خیراً۔ فیس کی رسید سنبھال کر رکھیں۔"),
                logoUri = restoredLogoUri
            )
            db.madrassaProfileDao().insertOrUpdateProfile(prof)
        }

        // Students
        val studentList = mutableListOf<Student>()
        if (root.has("students")) {
            val arr = root.getJSONArray("students")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                var photoPath = o.optString("photoUri", "")
                if (context != null && o.has("photoBase64")) {
                    val pB64 = o.getString("photoBase64")
                    val saved = ImageStorageHelper.base64ToFile(context, pB64, "student_photos", "student_${o.optString("rollNo", "$i")}")
                    if (saved != null) {
                        photoPath = saved
                        photosRestored++
                    }
                }

                studentList.add(
                    Student(
                        id = o.optLong("id", 0),
                        rollNo = o.optString("rollNo", "STU-${i + 1}"),
                        name = o.optString("name", "Student ${i + 1}"),
                        fatherName = o.optString("fatherName", "Father"),
                        guardianName = o.optString("guardianName", o.optString("fatherName", "")),
                        guardianPhone = o.optString("guardianPhone", "0300-0000000"),
                        phone2 = o.optString("phone2", ""),
                        dob = o.optString("dob", "2010-01-01"),
                        gender = o.optString("gender", "MALE"),
                        cnicBForm = o.optString("cnicBForm", ""),
                        admissionDate = o.optString("admissionDate", "2024-01-01"),
                        address = o.optString("address", ""),
                        department = o.optString("department", "Hifz"),
                        section = o.optString("section", "Section A"),
                        status = o.optString("status", "ACTIVE"),
                        monthlyFee = o.optDouble("monthlyFee", 2500.0),
                        discount = o.optDouble("discount", 0.0),
                        photoUri = photoPath,
                        notes = o.optString("notes", ""),
                        isDeleted = o.optBoolean("isDeleted", false),
                        deletedAt = if (o.isNull("deletedAt")) null else o.optLong("deletedAt"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            db.studentDao().insertStudents(studentList)
        }

        // Teachers
        val teacherList = mutableListOf<Teacher>()
        if (root.has("teachers")) {
            val arr = root.getJSONArray("teachers")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                var photoPath = o.optString("photoUri", "")
                if (context != null && o.has("photoBase64")) {
                    val pB64 = o.getString("photoBase64")
                    val saved = ImageStorageHelper.base64ToFile(context, pB64, "teacher_photos", "teacher_${o.optLong("id", i.toLong())}")
                    if (saved != null) {
                        photoPath = saved
                        photosRestored++
                    }
                }

                teacherList.add(
                    Teacher(
                        id = o.optLong("id", 0),
                        name = o.optString("name", "Teacher"),
                        fatherName = o.optString("fatherName", ""),
                        phone = o.optString("phone", "0300-0000000"),
                        cnic = o.optString("cnic", ""),
                        qualification = o.optString("qualification", "Wafaq-ul-Madaris Al-Alimiyyah"),
                        joinDate = o.optString("joinDate", "2023-01-15"),
                        monthlySalary = o.optDouble("monthlySalary", 35000.0),
                        assignedDepartment = o.optString("assignedDepartment", "Hifz"),
                        assignedSections = o.optString("assignedSections", "Hifz Morning"),
                        photoUri = photoPath,
                        role = o.optString("role", "Ustaz"),
                        status = o.optString("status", "ACTIVE"),
                        isDeleted = o.optBoolean("isDeleted", false),
                        deletedAt = if (o.isNull("deletedAt")) null else o.optLong("deletedAt")
                    )
                )
            }
            db.teacherDao().insertTeachers(teacherList)
        }

        // Attendance
        val attendanceList = mutableListOf<AttendanceRecord>()
        if (root.has("attendance")) {
            val arr = root.getJSONArray("attendance")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                attendanceList.add(
                    AttendanceRecord(
                        id = o.optLong("id", 0),
                        personId = o.optLong("personId", 0),
                        personType = o.optString("personType", "STUDENT"),
                        date = o.optString("date", "2025-10-01"),
                        status = o.optString("status", "PRESENT"),
                        checkInTime = o.optString("checkInTime", "08:00 AM"),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
            db.attendanceDao().insertAll(attendanceList)
        }

        // Sabaq Progress (Hifz)
        val hifzList = mutableListOf<HifzProgress>()
        if (root.has("hifz_progress")) {
            val arr = root.getJSONArray("hifz_progress")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                hifzList.add(
                    HifzProgress(
                        id = o.optLong("id", 0),
                        studentId = o.optLong("studentId", 0),
                        date = o.optString("date", "2025-10-01"),
                        sabaqPara = o.optInt("sabaqPara", 1),
                        sabaqSurah = o.optString("sabaqSurah", "Al-Baqarah"),
                        sabaqAyahRange = o.optString("sabaqAyahRange", "Ayah 1-10"),
                        sabqiRange = o.optString("sabqiRange", "Para 1"),
                        manzilParas = o.optString("manzilParas", "Para 29-30"),
                        grade = o.optString("grade", "Mumtaz"),
                        mistakesCount = o.optInt("mistakesCount", 0),
                        totalParasCompleted = o.optInt("totalParasCompleted", 0),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
            db.progressDao().insertAllHifz(hifzList)
        }

        // Nazra Progress
        val nazraList = mutableListOf<NazraProgress>()
        if (root.has("nazra_progress")) {
            val arr = root.getJSONArray("nazra_progress")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                nazraList.add(
                    NazraProgress(
                        id = o.optLong("id", 0),
                        studentId = o.optLong("studentId", 0),
                        date = o.optString("date", "2025-10-01"),
                        currentPara = o.optInt("currentPara", 1),
                        pageRange = o.optString("pageRange", "1-5"),
                        mistakesCount = o.optInt("mistakesCount", 0),
                        grade = o.optString("grade", "A"),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
            db.progressDao().insertAllNazra(nazraList)
        }

        // Noorani Qaida Progress
        val qaidaList = mutableListOf<QaidaProgress>()
        if (root.has("qaida_progress")) {
            val arr = root.getJSONArray("qaida_progress")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                qaidaList.add(
                    QaidaProgress(
                        id = o.optLong("id", 0),
                        studentId = o.optLong("studentId", 0),
                        date = o.optString("date", "2025-10-01"),
                        lessonNo = o.optInt("lessonNo", 1),
                        topic = o.optString("topic", "Huruf Mufradat"),
                        grade = o.optString("grade", "A"),
                        passOrRepeat = o.optString("passOrRepeat", "PASS"),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
            db.progressDao().insertAllQaida(qaidaList)
        }

        // Tajweed Progress
        val tajweedList = mutableListOf<TajweedProgress>()
        if (root.has("tajweed_progress")) {
            val arr = root.getJSONArray("tajweed_progress")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                tajweedList.add(
                    TajweedProgress(
                        id = o.optLong("id", 0),
                        studentId = o.optLong("studentId", 0),
                        date = o.optString("date", "2025-10-01"),
                        topic = o.optString("topic", "Makharij"),
                        practicalAssessment = o.optString("practicalAssessment", "Good"),
                        style = o.optString("style", "Hafs"),
                        grade = o.optString("grade", "Mumtaz"),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
            db.progressDao().insertAllTajweed(tajweedList)
        }

        // Courses
        val coursesList = mutableListOf<Course>()
        if (root.has("courses")) {
            val arr = root.getJSONArray("courses")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                coursesList.add(
                    Course(
                        id = o.optLong("id", 0),
                        nameUr = o.optString("nameUr", "کورس"),
                        nameEn = o.optString("nameEn", "Course"),
                        description = o.optString("description", ""),
                        duration = o.optString("duration", "1 Year"),
                        teacherName = o.optString("teacherName", ""),
                        monthlyFee = o.optDouble("monthlyFee", 2500.0),
                        status = o.optString("status", "ACTIVE")
                    )
                )
            }
            db.courseDao().insertCourses(coursesList)
        }

        // Exams
        val examsList = mutableListOf<Exam>()
        if (root.has("exams")) {
            val arr = root.getJSONArray("exams")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                examsList.add(
                    Exam(
                        id = o.optLong("id", 0),
                        title = o.optString("title", "Exam"),
                        type = o.optString("type", "ANNUAL"),
                        date = o.optString("date", "2025-10-15"),
                        department = o.optString("department", "Hifz"),
                        section = o.optString("section", "Section A"),
                        totalMarks = o.optInt("totalMarks", 100),
                        passMarks = o.optInt("passMarks", 40),
                        status = o.optString("status", "PUBLISHED")
                    )
                )
            }
            db.examDao().insertExams(examsList)
        }

        // Exam Results (Full Oral Assessment & Detailed Marks)
        val resultsList = mutableListOf<ExamResult>()
        if (root.has("exam_results")) {
            val arr = root.getJSONArray("exam_results")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                resultsList.add(
                    ExamResult(
                        id = o.optLong("id", 0),
                        examId = o.optLong("examId", 1),
                        studentId = o.optLong("studentId", 0),
                        studentName = o.optString("studentName", ""),
                        fatherName = o.optString("fatherName", ""),
                        studentClass = o.optString("studentClass", ""),
                        paray = o.optString("paray", ""),
                        question1 = o.optDouble("question1", 0.0),
                        question2 = o.optDouble("question2", 0.0),
                        question3 = o.optDouble("question3", 0.0),
                        talaffuz = o.optDouble("talaffuz", 0.0),
                        kulNumber = o.optDouble("kulNumber", 0.0),
                        totalMaxMarks = o.optDouble("totalMaxMarks", 100.0),
                        kaifiyat = o.optString("kaifiyat", "ممتاز"),
                        subject = o.optString("subject", "حفظ قرآن / تجوید"),
                        marksObtained = o.optDouble("marksObtained", o.optDouble("kulNumber", 0.0)),
                        totalMarks = o.optDouble("totalMarks", 100.0),
                        grade = o.optString("grade", "ممتاز"),
                        remarks = o.optString("remarks", ""),
                        isPassed = o.optBoolean("isPassed", true)
                    )
                )
            }
            db.examDao().insertResults(resultsList)
        }

        // Fee Vouchers & Payments
        val vouchersList = mutableListOf<FeeVoucher>()
        if (root.has("fee_vouchers")) {
            val arr = root.getJSONArray("fee_vouchers")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                vouchersList.add(
                    FeeVoucher(
                        id = o.optLong("id", 0),
                        voucherNo = o.optString("voucherNo", "VCH-$i"),
                        studentId = o.optLong("studentId", 0),
                        monthYear = o.optString("monthYear", "October 2025"),
                        tuitionFee = o.optDouble("tuitionFee", 2500.0),
                        admissionFee = o.optDouble("admissionFee", 0.0),
                        examFee = o.optDouble("examFee", 0.0),
                        discount = o.optDouble("discount", 0.0),
                        totalPayable = o.optDouble("totalPayable", 2500.0),
                        paidAmount = o.optDouble("paidAmount", 0.0),
                        status = o.optString("status", "UNPAID"),
                        dueDate = o.optString("dueDate", "2025-10-15")
                    )
                )
            }
            db.feeDao().insertVouchers(vouchersList)
        }

        val paymentsList = mutableListOf<PaymentRecord>()
        if (root.has("payments")) {
            val arr = root.getJSONArray("payments")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                paymentsList.add(
                    PaymentRecord(
                        id = o.optLong("id", 0),
                        voucherId = o.optLong("voucherId", 0),
                        studentId = o.optLong("studentId", 0),
                        amountPaid = o.optDouble("amountPaid", 2500.0),
                        paymentDate = o.optString("paymentDate", "2025-10-10"),
                        paymentMode = o.optString("paymentMode", "Cash"),
                        receiptNo = o.optString("receiptNo", "REC-$i"),
                        receiverName = o.optString("receiverName", "Admin"),
                        notes = o.optString("notes", "")
                    )
                )
            }
            db.feeDao().insertPayments(paymentsList)
        }

        BackupSummary(
            studentCount = studentList.size,
            teacherCount = teacherList.size,
            attendanceCount = attendanceList.size,
            hifzCount = hifzList.size,
            nazraCount = nazraList.size,
            qaidaCount = qaidaList.size,
            tajweedCount = tajweedList.size,
            courseCount = coursesList.size,
            examCount = examsList.size,
            examResultCount = resultsList.size,
            voucherCount = vouchersList.size,
            paymentCount = paymentsList.size,
            photoCount = photosRestored,
            exportedDate = root.optString("exportedDate", ""),
            madrassaName = madrassaName
        )
    }

    // 4. Import from SQL File Content
    suspend fun importFromSql(sqlScript: String): Int = withContext(Dispatchers.IO) {
        var statementsExecuted = 0
        val lines = sqlScript.split("\n")
        val currentStmt = StringBuilder()

        val sqliteDb = db.openHelper.writableDatabase
        sqliteDb.beginTransaction()
        try {
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("--") || trimmed.isBlank()) continue
                if (trimmed.equals("BEGIN TRANSACTION;", ignoreCase = true) ||
                    trimmed.equals("COMMIT;", ignoreCase = true)) continue

                currentStmt.append(" ").append(trimmed)
                if (trimmed.endsWith(";")) {
                    val stmt = currentStmt.toString().trim()
                    if (stmt.isNotBlank()) {
                        sqliteDb.execSQL(stmt)
                        statementsExecuted++
                    }
                    currentStmt.clear()
                }
            }
            sqliteDb.setTransactionSuccessful()
        } finally {
            sqliteDb.endTransaction()
        }
        statementsExecuted
    }

    // Helper: Share Backup file via Intent
    fun shareBackupFile(context: Context, content: String, filename: String, mimeType: String) {
        val cacheFile = File(context.cacheDir, filename)
        FileOutputStream(cacheFile).use { it.write(content.toByteArray()) }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            cacheFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Madrassa Database Backup ($filename)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Database Backup"))
    }

    // Helper: Save content to user-selected SAF Uri
    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            os.write(content.toByteArray())
        }
    }

    // Helper: Read content from user-selected SAF Uri
    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: ""
    }

    private fun escapeSql(value: String): String {
        return value.replace("'", "''")
    }
}
