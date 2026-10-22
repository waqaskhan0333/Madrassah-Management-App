package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isDeleted = 0 ORDER BY rollNo ASC")
    fun getAllActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE rollNo = :rollNo LIMIT 1")
    suspend fun getStudentByRollNo(rollNo: String): Student?

    @Query("""
        SELECT * FROM students 
        WHERE isDeleted = 0 AND (
            name LIKE '%' || :query || '%' OR 
            rollNo LIKE '%' || :query || '%' OR 
            fatherName LIKE '%' || :query || '%' OR 
            guardianPhone LIKE '%' || :query || '%'
        )
        ORDER BY rollNo ASC
    """)
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Query("UPDATE students SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteStudent(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE students SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreStudent(id: Long)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun permanentlyDeleteStudent(id: Long)

    @Query("SELECT COUNT(*) FROM students WHERE isDeleted = 0")
    fun getActiveStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE isDeleted = 0 AND status = 'ACTIVE'")
    fun getEnrolledActiveStudentCount(): Flow<Int>

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsDirect(): List<Student>

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllActiveTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: Long): Teacher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Query("UPDATE teachers SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteTeacher(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE teachers SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreTeacher(id: Long)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun permanentlyDeleteTeacher(id: Long)

    @Query("SELECT COUNT(*) FROM teachers WHERE isDeleted = 0")
    fun getTeacherCount(): Flow<Int>

    @Query("SELECT * FROM teachers")
    suspend fun getAllTeachersDirect(): List<Teacher>

    @Query("DELETE FROM teachers")
    suspend fun clearAllTeachers()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date AND personType = :personType")
    fun getAttendanceForDate(date: String, personType: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE personId = :personId AND personType = :personType ORDER BY date DESC")
    fun getAttendanceForPerson(personId: Long, personType: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE date = :date AND personId = :personId AND personType = :personType LIMIT 1")
    suspend fun getRecord(date: String, personId: Long, personType: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceDirect(): List<AttendanceRecord>

    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM hifz_progress WHERE studentId = :studentId ORDER BY date DESC")
    fun getHifzByStudent(studentId: Long): Flow<List<HifzProgress>>

    @Query("SELECT * FROM hifz_progress ORDER BY date DESC LIMIT 50")
    fun getAllRecentHifz(): Flow<List<HifzProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHifz(progress: HifzProgress): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHifz(list: List<HifzProgress>)

    @Update
    suspend fun updateHifz(progress: HifzProgress)

    @Query("DELETE FROM hifz_progress WHERE id = :id")
    suspend fun deleteHifz(id: Long)

    @Query("DELETE FROM hifz_progress")
    suspend fun clearAllHifz()

    @Query("SELECT * FROM hifz_progress")
    suspend fun getAllHifzDirect(): List<HifzProgress>

    @Query("SELECT * FROM nazra_progress WHERE studentId = :studentId ORDER BY date DESC")
    fun getNazraByStudent(studentId: Long): Flow<List<NazraProgress>>

    @Query("SELECT * FROM nazra_progress ORDER BY date DESC LIMIT 50")
    fun getAllRecentNazra(): Flow<List<NazraProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNazra(progress: NazraProgress): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNazra(list: List<NazraProgress>)

    @Update
    suspend fun updateNazra(progress: NazraProgress)

    @Query("DELETE FROM nazra_progress WHERE id = :id")
    suspend fun deleteNazra(id: Long)

    @Query("DELETE FROM nazra_progress")
    suspend fun clearAllNazra()

    @Query("SELECT * FROM nazra_progress")
    suspend fun getAllNazraDirect(): List<NazraProgress>

    @Query("SELECT * FROM qaida_progress WHERE studentId = :studentId ORDER BY date DESC")
    fun getQaidaByStudent(studentId: Long): Flow<List<QaidaProgress>>

    @Query("SELECT * FROM qaida_progress ORDER BY date DESC LIMIT 50")
    fun getAllRecentQaida(): Flow<List<QaidaProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQaida(progress: QaidaProgress): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQaida(list: List<QaidaProgress>)

    @Update
    suspend fun updateQaida(progress: QaidaProgress)

    @Query("DELETE FROM qaida_progress WHERE id = :id")
    suspend fun deleteQaida(id: Long)

    @Query("DELETE FROM qaida_progress")
    suspend fun clearAllQaida()

    @Query("SELECT * FROM qaida_progress")
    suspend fun getAllQaidaDirect(): List<QaidaProgress>

    @Query("SELECT * FROM tajweed_progress WHERE studentId = :studentId ORDER BY date DESC")
    fun getTajweedByStudent(studentId: Long): Flow<List<TajweedProgress>>

    @Query("SELECT * FROM tajweed_progress ORDER BY date DESC LIMIT 50")
    fun getAllRecentTajweed(): Flow<List<TajweedProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTajweed(progress: TajweedProgress): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTajweed(list: List<TajweedProgress>)

    @Update
    suspend fun updateTajweed(progress: TajweedProgress)

    @Query("DELETE FROM tajweed_progress WHERE id = :id")
    suspend fun deleteTajweed(id: Long)

    @Query("DELETE FROM tajweed_progress")
    suspend fun clearAllTajweed()

    @Query("SELECT * FROM tajweed_progress")
    suspend fun getAllTajweedDirect(): List<TajweedProgress>
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE status = 'ACTIVE' ORDER BY id ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Update
    suspend fun updateCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: Long)

    @Query("SELECT * FROM courses")
    suspend fun getAllCoursesDirect(): List<Course>

    @Query("DELETE FROM courses")
    suspend fun clearAllCourses()
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY date DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamById(id: Long): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>)

    @Update
    suspend fun updateExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExam(id: Long)

    @Query("SELECT * FROM exams")
    suspend fun getAllExamsDirect(): List<Exam>

    @Query("DELETE FROM exams")
    suspend fun clearAllExams()

    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getResultsForExam(examId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results ORDER BY id DESC")
    fun getAllExamResults(): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getResultsForStudent(studentId: Long): Flow<List<ExamResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ExamResult>)

    @Update
    suspend fun updateResult(result: ExamResult)

    @Query("DELETE FROM exam_results WHERE id = :id")
    suspend fun deleteResult(id: Long)

    @Query("SELECT * FROM exam_results")
    suspend fun getAllExamResultsDirect(): List<ExamResult>

    @Query("DELETE FROM exam_results")
    suspend fun clearAllExamResults()
}

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_vouchers ORDER BY id DESC")
    fun getAllVouchers(): Flow<List<FeeVoucher>>

    @Query("SELECT * FROM fee_vouchers WHERE studentId = :studentId ORDER BY id DESC")
    fun getVouchersForStudent(studentId: Long): Flow<List<FeeVoucher>>

    @Query("SELECT * FROM fee_vouchers WHERE status != 'PAID' ORDER BY dueDate ASC")
    fun getDefaulters(): Flow<List<FeeVoucher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: FeeVoucher): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<FeeVoucher>)

    @Update
    suspend fun updateVoucher(voucher: FeeVoucher)

    @Query("DELETE FROM fee_vouchers WHERE id = :id")
    suspend fun deleteVoucher(id: Long)

    @Query("SELECT * FROM fee_vouchers")
    suspend fun getAllVouchersDirect(): List<FeeVoucher>

    @Query("DELETE FROM fee_vouchers")
    suspend fun clearAllVouchers()

    @Query("SELECT * FROM payments WHERE voucherId = :voucherId ORDER BY id DESC")
    fun getPaymentsForVoucher(voucherId: Long): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentRecord>)

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsDirect(): List<PaymentRecord>

    @Query("DELETE FROM payments")
    suspend fun clearAllPayments()
}

@Dao
interface MadrassaProfileDao {
    @Query("SELECT * FROM madrassa_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<MadrassaProfile?>

    @Query("SELECT * FROM madrassa_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): MadrassaProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: MadrassaProfile)

    @Query("DELETE FROM madrassa_profile")
    suspend fun clearProfile()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Insert
    suspend fun insertLog(log: AuditLog): Long
}
