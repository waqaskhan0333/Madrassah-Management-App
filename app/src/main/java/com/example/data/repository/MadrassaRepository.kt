package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class MadrassaRepository(private val db: AppDatabase) {
    val activeStudents: Flow<List<Student>> = db.studentDao().getAllActiveStudents()
    val allStudents: Flow<List<Student>> = db.studentDao().getAllStudents()
    val recycleBinStudents: Flow<List<Student>> = db.studentDao().getRecycleBinStudents()
    val activeTeachers: Flow<List<Teacher>> = db.teacherDao().getAllActiveTeachers()
    val recycleBinTeachers: Flow<List<Teacher>> = db.teacherDao().getRecycleBinTeachers()
    val courses: Flow<List<Course>> = db.courseDao().getAllCourses()
    val exams: Flow<List<Exam>> = db.examDao().getAllExams()
    val allExamResults: Flow<List<ExamResult>> = db.examDao().getAllExamResults()
    val feeVouchers: Flow<List<FeeVoucher>> = db.feeDao().getAllVouchers()
    val payments: Flow<List<PaymentRecord>> = db.feeDao().getAllPayments()
    val profile: Flow<MadrassaProfile?> = db.madrassaProfileDao().getProfile()
    val recentHifz: Flow<List<HifzProgress>> = db.progressDao().getAllRecentHifz()
    val recentNazra: Flow<List<NazraProgress>> = db.progressDao().getAllRecentNazra()
    val recentQaida: Flow<List<QaidaProgress>> = db.progressDao().getAllRecentQaida()
    val recentTajweed: Flow<List<TajweedProgress>> = db.progressDao().getAllRecentTajweed()

    fun searchStudents(query: String): Flow<List<Student>> = db.studentDao().searchStudents(query)

    suspend fun getStudentById(id: Long): Student? = db.studentDao().getStudentById(id)
    suspend fun getStudentByRollNo(rollNo: String): Student? = db.studentDao().getStudentByRollNo(rollNo)
    suspend fun getAllStudentsDirect(): List<Student> = db.studentDao().getAllStudentsDirect()

    suspend fun insertStudent(student: Student): Long = db.studentDao().insertStudent(student)
    suspend fun updateStudent(student: Student) = db.studentDao().updateStudent(student)
    suspend fun softDeleteStudent(id: Long) = db.studentDao().softDeleteStudent(id)
    suspend fun restoreStudent(id: Long) = db.studentDao().restoreStudent(id)
    suspend fun permanentlyDeleteStudent(id: Long) = db.studentDao().permanentlyDeleteStudent(id)

    suspend fun insertTeacher(teacher: Teacher): Long = db.teacherDao().insertTeacher(teacher)
    suspend fun updateTeacher(teacher: Teacher) = db.teacherDao().updateTeacher(teacher)
    suspend fun softDeleteTeacher(id: Long) = db.teacherDao().softDeleteTeacher(id)
    suspend fun restoreTeacher(id: Long) = db.teacherDao().restoreTeacher(id)
    suspend fun permanentlyDeleteTeacher(id: Long) = db.teacherDao().permanentlyDeleteTeacher(id)

    fun getStudentAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        db.attendanceDao().getAttendanceForDate(date, "STUDENT")

    fun getTeacherAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        db.attendanceDao().getAttendanceForDate(date, "TEACHER")

    fun getAttendanceForPerson(personId: Long, personType: String): Flow<List<AttendanceRecord>> =
        db.attendanceDao().getAttendanceForPerson(personId, personType)

    suspend fun getAttendanceRecord(date: String, personId: Long, personType: String): AttendanceRecord? =
        db.attendanceDao().getRecord(date, personId, personType)

    suspend fun markAttendance(record: AttendanceRecord) = db.attendanceDao().insertOrUpdate(record)

    suspend fun markAllStudentsPresent(date: String, studentIds: List<Long>) {
        val currentTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val records = studentIds.map { studentId ->
            val existing = db.attendanceDao().getRecord(date, studentId, "STUDENT")
            AttendanceRecord(
                id = existing?.id ?: 0,
                personId = studentId,
                personType = "STUDENT",
                date = date,
                status = "PRESENT",
                checkInTime = existing?.checkInTime?.ifBlank { currentTime } ?: currentTime,
                remarks = if (existing != null && existing.remarks.isNotBlank()) existing.remarks else "Marked All Present"
            )
        }
        db.attendanceDao().insertAll(records)
    }

    suspend fun markAllTeachersPresent(date: String, teacherIds: List<Long>) {
        val currentTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val records = teacherIds.map { teacherId ->
            val existing = db.attendanceDao().getRecord(date, teacherId, "TEACHER")
            AttendanceRecord(
                id = existing?.id ?: 0,
                personId = teacherId,
                personType = "TEACHER",
                date = date,
                status = "PRESENT",
                checkInTime = existing?.checkInTime?.ifBlank { currentTime } ?: currentTime,
                remarks = if (existing != null && existing.remarks.isNotBlank()) existing.remarks else "Marked All Asatiza Present"
            )
        }
        db.attendanceDao().insertAll(records)
    }

    suspend fun getAllAttendanceDirect(): List<AttendanceRecord> =
        db.attendanceDao().getAllAttendanceDirect()

    fun getHifzHistory(studentId: Long): Flow<List<HifzProgress>> = db.progressDao().getHifzByStudent(studentId)
    fun getNazraHistory(studentId: Long): Flow<List<NazraProgress>> = db.progressDao().getNazraByStudent(studentId)
    fun getQaidaHistory(studentId: Long): Flow<List<QaidaProgress>> = db.progressDao().getQaidaByStudent(studentId)
    fun getTajweedHistory(studentId: Long): Flow<List<TajweedProgress>> = db.progressDao().getTajweedByStudent(studentId)

    suspend fun insertHifz(progress: HifzProgress) = db.progressDao().insertHifz(progress)
    suspend fun updateHifz(progress: HifzProgress) = db.progressDao().updateHifz(progress)
    suspend fun deleteHifz(id: Long) = db.progressDao().deleteHifz(id)

    suspend fun insertNazra(progress: NazraProgress) = db.progressDao().insertNazra(progress)
    suspend fun updateNazra(progress: NazraProgress) = db.progressDao().updateNazra(progress)
    suspend fun deleteNazra(id: Long) = db.progressDao().deleteNazra(id)

    suspend fun insertQaida(progress: QaidaProgress) = db.progressDao().insertQaida(progress)
    suspend fun updateQaida(progress: QaidaProgress) = db.progressDao().updateQaida(progress)
    suspend fun deleteQaida(id: Long) = db.progressDao().deleteQaida(id)

    suspend fun insertTajweed(progress: TajweedProgress) = db.progressDao().insertTajweed(progress)
    suspend fun updateTajweed(progress: TajweedProgress) = db.progressDao().updateTajweed(progress)
    suspend fun deleteTajweed(id: Long) = db.progressDao().deleteTajweed(id)

    suspend fun insertExam(exam: Exam) = db.examDao().insertExam(exam)
    suspend fun updateExam(exam: Exam) = db.examDao().updateExam(exam)
    suspend fun deleteExam(id: Long) = db.examDao().deleteExam(id)
    fun getResultsForExam(examId: Long): Flow<List<ExamResult>> = db.examDao().getResultsForExam(examId)
    fun getResultsForStudent(studentId: Long): Flow<List<ExamResult>> = db.examDao().getResultsForStudent(studentId)
    suspend fun insertExamResult(result: ExamResult) = db.examDao().insertResult(result)
    suspend fun updateExamResult(result: ExamResult) = db.examDao().updateResult(result)
    suspend fun deleteExamResult(id: Long) = db.examDao().deleteResult(id)
    suspend fun getAllExamResultsDirect(): List<ExamResult> = db.examDao().getAllExamResultsDirect()

    suspend fun insertCourse(course: Course) = db.courseDao().insertCourse(course)
    suspend fun updateCourse(course: Course) = db.courseDao().updateCourse(course)
    suspend fun deleteCourse(id: Long) = db.courseDao().deleteCourse(id)

    suspend fun insertVoucher(voucher: FeeVoucher) = db.feeDao().insertVoucher(voucher)
    suspend fun updateVoucher(voucher: FeeVoucher) = db.feeDao().updateVoucher(voucher)
    suspend fun deleteVoucher(id: Long) = db.feeDao().deleteVoucher(id)
    fun getVouchersForStudent(studentId: Long): Flow<List<FeeVoucher>> = db.feeDao().getVouchersForStudent(studentId)
    fun getPaymentsForVoucher(voucherId: Long): Flow<List<PaymentRecord>> = db.feeDao().getPaymentsForVoucher(voucherId)
    suspend fun insertPayment(payment: PaymentRecord) = db.feeDao().insertPayment(payment)
    suspend fun deletePayment(id: Long) = db.feeDao().deletePayment(id)

    suspend fun updateProfile(profile: MadrassaProfile) = db.madrassaProfileDao().insertOrUpdateProfile(profile)
}
