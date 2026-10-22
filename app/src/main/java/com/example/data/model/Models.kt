package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "madrassa_profile")
data class MadrassaProfile(
    @PrimaryKey val id: Int = 1,
    val nameUr: String = "جامعہ دارالعلوم الاسلامیہ",
    val nameEn: String = "Jamia Darul Uloom Al-Islamia",
    val mottoUr: String = "علم • تربیت • بہترین مستقبل",
    val mottoEn: String = "Ilm • Tarbiyat • Behtareen Mustaqbil",
    val addressUr: String = "ماڈل ٹاؤن، لاہور، پاکستان",
    val addressEn: String = "Model Town, Lahore, Pakistan",
    val phone1: String = "0300-1234567",
    val phone2: String = "0321-7654321",
    val email: String = "info@darululoom.edu.pk",
    val website: String = "www.darululoom.edu.pk",
    val registrationNo: String = "REG-2025-482",
    val sessionYear: String = "1446-1447 / 2025-2026",
    val principalName: String = "مولانا مفتی محمد طارق صاحب",
    val receiptFooter: String = "جزاکم اللہ خیراً۔ فیس کی ادائیگی بروقت یقینی بنائیں۔",
    val logoUri: String = ""
)

@Entity(
    tableName = "students",
    indices = [Index(value = ["rollNo"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rollNo: String,
    val name: String,
    val fatherName: String,
    val guardianName: String = "",
    val guardianPhone: String,
    val phone2: String = "",
    val dob: String = "2012-05-10",
    val gender: String = "Male",
    val cnicBForm: String = "",
    val admissionDate: String = "2024-09-01",
    val address: String = "Lahore",
    val department: String, // "Hifz", "Nazra", "Qaida", "Qirat & Tajweed"
    val section: String = "Section A",
    val campus: String = "Main Campus", // Main Campus, Campus 2, Campus 3
    val paray: String = "پارہ 1",
    val status: String = "ACTIVE", // "ACTIVE", "LEFT", "GRADUATED"
    val monthlyFee: Double = 2500.0,
    val discount: Double = 0.0,
    val photoUri: String = "",
    val notes: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isBiometricEnrolled: Boolean = false,
    val fingerprintTemplate: String = "",
    val faceEmbedding: String = "",
    val biometricRegisteredDate: String = "",
    val biometricRegisteredAt: Long? = null
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fatherName: String = "",
    val phone: String,
    val cnic: String = "",
    val qualification: String = "Wafaq-ul-Madaris Al-Alimiyyah",
    val joinDate: String = "2023-01-15",
    val monthlySalary: Double = 35000.0,
    val assignedDepartment: String = "Hifz",
    val assignedSections: String = "Hifz Morning, Section 1A",
    val photoUri: String = "",
    val role: String = "Ustaz", // Ustaz, Ustaza, Mohtamim, Accountant
    val status: String = "ACTIVE",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["personId", "personType", "date"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val personType: String, // "STUDENT" or "TEACHER"
    val date: String, // YYYY-MM-DD
    val status: String, // "PRESENT", "ABSENT", "LATE", "LEAVE"
    val checkInTime: String = "",
    val remarks: String = ""
)

@Entity(tableName = "hifz_progress")
data class HifzProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val sabaqPara: Int = 1,
    val sabaqSurah: String = "Al-Baqarah",
    val sabaqAyahRange: String = "Ayah 1-10",
    val sabqiRange: String = "Para 1 (Quarter)",
    val manzilParas: String = "Para 29-30",
    val grade: String = "Mumtaz", // Mumtaz (Excellent), Jayyid (Good), Maqbool (Average), Daeef (Weak)
    val mistakesCount: Int = 0,
    val totalParasCompleted: Int = 0,
    val remarks: String = ""
)

@Entity(tableName = "nazra_progress")
data class NazraProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val currentPara: Int = 1,
    val pageRange: String = "Page 1-3",
    val mistakesCount: Int = 1,
    val grade: String = "Jayyid",
    val remarks: String = ""
)

@Entity(tableName = "qaida_progress")
data class QaidaProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val lessonNo: Int = 1,
    val topic: String = "Mufradat (Separate Letters)",
    val grade: String = "Mumtaz",
    val passOrRepeat: String = "PASS", // PASS, REPEAT
    val remarks: String = ""
)

@Entity(tableName = "tajweed_progress")
data class TajweedProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String,
    val topic: String = "Makharij-ul-Huroof",
    val practicalAssessment: String = "Halqi Letters Pronunciation",
    val style: String = "Hafs 'an 'Asim",
    val grade: String = "Mumtaz",
    val remarks: String = ""
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameUr: String,
    val nameEn: String,
    val description: String,
    val duration: String = "1 Year",
    val teacherName: String = "",
    val monthlyFee: Double = 2500.0,
    val status: String = "ACTIVE"
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "MID_TERM", "ANNUAL", "MONTHLY", "HIFZ_ORAL", "TAJWEED_TEST"
    val date: String,
    val department: String,
    val section: String,
    val totalMarks: Int = 100,
    val passMarks: Int = 40,
    val status: String = "PUBLISHED"
)

@Entity(tableName = "exam_results")
data class ExamResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val studentName: String = "",
    val fatherName: String = "",
    val studentClass: String = "",
    val paray: String = "", // e.g. "پارہ 1 تا 5" / "پارہ 30"
    val question1: Double = 0.0,
    val question2: Double = 0.0,
    val question3: Double = 0.0,
    val talaffuz: Double = 0.0,
    val kulNumber: Double = 0.0,
    val totalMaxMarks: Double = 100.0,
    val kaifiyat: String = "ممتاز", // ممتاز (Mumtaz), جید جداً (Jayyid Jiddan), جید (Jayyid), مقبول (Maqbool), راسب (Fail)
    val subject: String = "حفظ قرآن / تجوید",
    val marksObtained: Double = 0.0,
    val totalMarks: Double = 100.0,
    val grade: String = "A",
    val remarks: String = "Passed",
    val isPassed: Boolean = true
)

@Entity(tableName = "fee_vouchers")
data class FeeVoucher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherNo: String,
    val studentId: Long,
    val monthYear: String, // e.g. "October 2025"
    val tuitionFee: Double = 2500.0,
    val admissionFee: Double = 0.0,
    val examFee: Double = 0.0,
    val discount: Double = 0.0,
    val totalPayable: Double = 2500.0,
    val paidAmount: Double = 0.0,
    val status: String = "UNPAID", // "PAID", "PARTIAL", "UNPAID"
    val dueDate: String = "2025-10-15"
)

@Entity(tableName = "payments")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherId: Long,
    val studentId: Long,
    val amountPaid: Double,
    val paymentDate: String,
    val paymentMode: String = "Cash", // Cash, EasyPaisa, JazzCash, Bank
    val receiptNo: String,
    val receiverName: String = "Admin",
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String, // INSERT, UPDATE, DELETE, RESTORE
    val entity: String,
    val entityId: Long,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
