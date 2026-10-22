package com.example

import com.example.data.model.Student
import com.example.util.AppLanguage
import com.example.util.Strings
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun studentEntity_creationAndDefaults() {
    val student = Student(
      rollNo = "STU001",
      name = "Ahmed Raza",
      fatherName = "Muhammad Raza",
      guardianPhone = "0300-1234567",
      department = "Hifz",
      section = "Class 1A"
    )
    assertEquals("STU001", student.rollNo)
    assertEquals("Ahmed Raza", student.name)
    assertEquals("ACTIVE", student.status)
    assertFalse(student.isDeleted)
  }

  @Test
  fun feeInvoice_statusTrackingAndCalculations() {
    val invoice = com.example.data.model.FeeVoucher(
      id = 1,
      voucherNo = "INV-202511-1001",
      studentId = 1,
      monthYear = "November 2025",
      tuitionFee = 2500.0,
      admissionFee = 500.0,
      examFee = 200.0,
      discount = 200.0,
      totalPayable = 3000.0,
      paidAmount = 1000.0,
      status = "PARTIAL",
      dueDate = "2025-11-15"
    )

    val balanceDue = invoice.totalPayable - invoice.paidAmount
    assertEquals(2000.0, balanceDue, 0.01)
    assertEquals("PARTIAL", invoice.status)
    assertEquals("INV-202511-1001", invoice.voucherNo)
  }

  @Test
  fun databaseBackup_summaryValidation() {
    val summary = com.example.data.backup.BackupSummary(
      studentCount = 25,
      teacherCount = 6,
      attendanceCount = 150,
      hifzCount = 45,
      courseCount = 4,
      examCount = 2,
      voucherCount = 25,
      paymentCount = 20,
      exportedDate = "2026-10-04 12:00:00",
      madrassaName = "Jamia Darul Uloom"
    )

    assertEquals(25, summary.studentCount)
    assertEquals(6, summary.teacherCount)
    assertEquals(150, summary.attendanceCount)
    assertEquals("Jamia Darul Uloom", summary.madrassaName)
  }

  @Test
  fun biometricSecurity_enrollmentAndVerification() {
    val student = Student(
      id = 101,
      rollNo = "STU101",
      name = "Zaid Ali",
      fatherName = "Ali Raza",
      guardianPhone = "0300-9988776",
      department = "Hifz",
      section = "Class 1A"
    )

    // 1. Enrollment
    val fpTemplate = com.example.util.BiometricSecurityManager.generateFingerprintTemplate(student)
    val faceEmbedding = com.example.util.BiometricSecurityManager.generateFaceEmbedding(student)
    assertTrue(fpTemplate.startsWith("FP-TPL-"))
    assertTrue(faceEmbedding.startsWith("FACE_V1:"))

    val enrolledStudent = student.copy(
      isBiometricEnrolled = true,
      fingerprintTemplate = fpTemplate,
      faceEmbedding = faceEmbedding,
      biometricRegisteredDate = "2025-10-15"
    )

    // 2. Successful Fingerprint Verification
    val result = com.example.util.BiometricSecurityManager.verifyFingerprintScan(
      targetStudent = enrolledStudent,
      enrolledStudents = listOf(enrolledStudent),
      existingAttendance = emptyList(),
      todayDate = "2025-10-15"
    )
    assertTrue("Verification should succeed for enrolled student", result is com.example.util.BiometricVerificationResult.Success)
    val success = result as com.example.util.BiometricVerificationResult.Success
    assertEquals("PRESENT", success.attendanceRecord.status)
    assertEquals(101L, success.attendanceRecord.personId)
    assertTrue(success.attendanceRecord.remarks.contains("Biometric Hardware Fingerprint Verified"))

    // 3. Duplicate Attendance Prevention
    val existingAttendance = listOf(success.attendanceRecord)
    val duplicateResult = com.example.util.BiometricSecurityManager.verifyFingerprintScan(
      targetStudent = enrolledStudent,
      enrolledStudents = listOf(enrolledStudent),
      existingAttendance = existingAttendance,
      todayDate = "2025-10-15"
    )
    assertTrue("Should detect and prevent duplicate entry", duplicateResult is com.example.util.BiometricVerificationResult.Duplicate)

    // 4. Non-enrolled Student Rejection
    val notEnrolledStudent = Student(
      id = 102,
      rollNo = "STU102",
      name = "Unregistered Student",
      fatherName = "Father",
      guardianPhone = "0300-0000000",
      department = "Nazra",
      isBiometricEnrolled = false
    )
    val notEnrolledResult = com.example.util.BiometricSecurityManager.verifyFingerprintScan(
      targetStudent = notEnrolledStudent,
      enrolledStudents = listOf(enrolledStudent),
      existingAttendance = emptyList(),
      todayDate = "2025-10-15"
    )
    assertTrue("Should flag student as not enrolled", notEnrolledResult is com.example.util.BiometricVerificationResult.NotEnrolled)

    // 5. False Match Prevention in Facial Recognition
    val randomNoiseVector = FloatArray(32) { (it * 0.03f) }
    val faceResult = com.example.util.BiometricSecurityManager.verifyFaceScan(
      scannedFaceVector = randomNoiseVector,
      preferredStudent = null,
      enrolledStudents = listOf(enrolledStudent),
      existingAttendance = emptyList(),
      todayDate = "2025-10-15"
    )
    assertTrue("False match should be rejected when similarity is low", faceResult is com.example.util.BiometricVerificationResult.FalseMatchRejected)
  }

  @Test
  fun dmcReport_scoreAndGradeCalculation() {
    val results = listOf(
      com.example.data.model.ExamResult(
        id = 1,
        examId = 1,
        studentId = 1,
        studentName = "Ahmed Raza",
        fatherName = "Muhammad Raza",
        studentClass = "Class 1A (Hifz)",
        question1 = 24.0,
        question2 = 23.0,
        question3 = 23.0,
        talaffuz = 22.0,
        kulNumber = 92.0,
        totalMaxMarks = 100.0,
        grade = "ممتاز",
        subject = "حفظ القرآن الکریم",
        isPassed = true
      ),
      com.example.data.model.ExamResult(
        id = 2,
        examId = 2,
        studentId = 1,
        studentName = "Ahmed Raza",
        fatherName = "Muhammad Raza",
        studentClass = "Class 1A (Hifz)",
        question1 = 25.0,
        question2 = 24.0,
        question3 = 24.0,
        talaffuz = 24.0,
        kulNumber = 97.0,
        totalMaxMarks = 100.0,
        grade = "ممتاز",
        subject = "حفظ و ترتیل",
        isPassed = true
      )
    )

    val totalObtained = results.sumOf { it.kulNumber.toInt() }
    val totalMax = results.sumOf { it.totalMaxMarks.toInt() }
    val percentage = (totalObtained * 100) / totalMax
    assertEquals(189, totalObtained)
    assertEquals(200, totalMax)
    assertEquals(94, percentage)
    assertTrue("Overall score should be Mumtaz", percentage >= 90)
  }
}

