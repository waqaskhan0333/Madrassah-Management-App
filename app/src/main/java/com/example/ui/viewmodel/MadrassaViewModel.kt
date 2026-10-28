package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.local.InitialDataPreloader
import com.example.data.model.*
import com.example.data.repository.MadrassaRepository
import com.example.data.sync.FirebaseSyncManager
import com.example.data.sync.SyncState
import com.example.util.ConnectivityStatus
import com.example.util.NetworkConnectivityObserver
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MadrassaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = MadrassaRepository(db)

    val networkObserver = NetworkConnectivityObserver(application)
    val authManager = AuthManager(application)
    val syncManager = FirebaseSyncManager(application, db, authManager, networkObserver)

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val networkStatus: StateFlow<ConnectivityStatus> = networkObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = if (networkObserver.isCurrentlyConnected()) ConnectivityStatus.ONLINE else ConnectivityStatus.OFFLINE
        )

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDate: String = dateFormat.format(Date())

    val selectedDate = MutableStateFlow(todayDate)
    val searchQuery = MutableStateFlow("")
    val selectedDepartmentFilter = MutableStateFlow("ALL")
    val selectedStatusFilter = MutableStateFlow("ALL")

    val profile: StateFlow<MadrassaProfile> = repository.profile
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MadrassaProfile()
        )

    val allStudents: StateFlow<List<Student>> = repository.activeStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allStudentsIncludingDeleted: StateFlow<List<Student>> = repository.allStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val studentLookupMap: StateFlow<Map<Long, Student>> = allStudentsIncludingDeleted
        .map { list -> list.associateBy { it.id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val recycleBinStudents: StateFlow<List<Student>> = repository.recycleBinStudents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        searchQuery,
        selectedDepartmentFilter,
        selectedStatusFilter
    ) { students, query, dept, status ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.rollNo.contains(query, ignoreCase = true) ||
                    student.fatherName.contains(query, ignoreCase = true) ||
                    student.guardianPhone.contains(query, ignoreCase = true)

            val matchesDept = dept == "ALL" || student.department.equals(dept, ignoreCase = true)
            val matchesStatus = status == "ALL" || student.status.equals(status, ignoreCase = true)

            matchesQuery && matchesDept && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val teachers: StateFlow<List<Teacher>> = repository.activeTeachers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recycleBinTeachers: StateFlow<List<Teacher>> = repository.recycleBinTeachers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val courses: StateFlow<List<Course>> = repository.courses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val exams: StateFlow<List<Exam>> = repository.exams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allExamResults: StateFlow<List<ExamResult>> = repository.allExamResults
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val vouchers: StateFlow<List<FeeVoucher>> = repository.feeVouchers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val payments: StateFlow<List<PaymentRecord>> = repository.payments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val studentAttendance: StateFlow<List<AttendanceRecord>> = selectedDate
        .flatMapLatest { date -> repository.getStudentAttendanceForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val teacherAttendance: StateFlow<List<AttendanceRecord>> = selectedDate
        .flatMapLatest { date -> repository.getTeacherAttendanceForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentHifz: StateFlow<List<HifzProgress>> = repository.recentHifz
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentNazra: StateFlow<List<NazraProgress>> = repository.recentNazra
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentQaida: StateFlow<List<QaidaProgress>> = repository.recentQaida
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTajweed: StateFlow<List<TajweedProgress>> = repository.recentTajweed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Seed on startup if empty & ensure Hifz Oral Assessment exists
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.studentDao().getStudentById(1)
            if (count == null) {
                InitialDataPreloader.seedDatabase(db)
            } else {
                val existingExams = db.examDao().getAllExamsDirect()
                if (existingExams.none { it.type == "HIFZ_ORAL" || it.title.contains("Oral", ignoreCase = true) || it.title.contains("Hifz", ignoreCase = true) }) {
                    val hifzExam = Exam(
                        id = 0,
                        title = "Hifz Oral Assessment (Para 29-30)",
                        type = "HIFZ_ORAL",
                        date = "2025-10-25",
                        department = "Hifz",
                        section = "Class 1A",
                        totalMarks = 100,
                        passMarks = 50,
                        status = "SCHEDULED"
                    )
                    val examId = db.examDao().insertExam(hifzExam)
                    val students = db.studentDao().getAllStudentsDirect()
                    val sampleResults = students.take(4).mapIndexed { idx, st ->
                        ExamResult(
                            id = 0,
                            examId = examId,
                            studentId = st.id,
                            studentName = st.name,
                            fatherName = st.fatherName,
                            studentClass = st.section.ifBlank { "Class 1A" },
                            paray = if (idx == 0) "پارہ 28 تا 30" else "پارہ 29 تا 30",
                            question1 = 23.0 + (idx % 3),
                            question2 = 22.0 + (idx % 3),
                            question3 = 23.0 + (idx % 2),
                            talaffuz = 24.0,
                            kulNumber = 92.0 - (idx * 3),
                            totalMaxMarks = 100.0,
                            kaifiyat = if (idx < 2) "ممتاز" else "جید جداً",
                            subject = "حفظ القرآن الکریم (زبانی جائزہ)",
                            marksObtained = 92.0 - (idx * 3),
                            totalMarks = 100.0,
                            grade = if (idx < 2) "ممتاز" else "جید جداً",
                            remarks = "بہترین روانی، عمدہ تجوید و ترتیل",
                            isPassed = true
                        )
                    }
                    if (sampleResults.isNotEmpty()) {
                        db.examDao().insertResults(sampleResults)
                    }
                }
            }
        }

        // Attempt silent Google auto sign-in
        authManager.attemptAutoSignIn(viewModelScope) { signedIn ->
            if (signedIn && networkObserver.isCurrentlyConnected()) {
                viewModelScope.launch {
                    syncManager.syncAll(isAuto = true)
                }
            }
        }

        // Automatic background sync whenever device connects to internet
        viewModelScope.launch {
            networkObserver.observe().collect { status ->
                if (status == ConnectivityStatus.ONLINE) {
                    syncManager.syncAll(isAuto = true)
                }
            }
        }
    }

    fun triggerAutoSync() {
        if (networkObserver.isCurrentlyConnected() && authManager.isUserSignedIn()) {
            viewModelScope.launch {
                syncManager.syncAll(isAuto = true)
            }
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            syncManager.syncAll(isAuto = false)
        }
    }

    fun autoPopulateExamForClass(exam: Exam) {
        viewModelScope.launch(Dispatchers.IO) {
            val classStudents = db.studentDao().getAllStudentsDirect().filter { !it.isDeleted }
            val targetStudents = if (exam.section.isNotBlank() && exam.section != "All") {
                classStudents.filter { it.section.equals(exam.section, ignoreCase = true) || it.department.equals(exam.department, ignoreCase = true) }
            } else classStudents

            val studentsToPopulate = if (targetStudents.isNotEmpty()) targetStudents else classStudents
            val existingResults = db.examDao().getAllExamResultsDirect().filter { it.examId == exam.id }
            val existingStudentIds = existingResults.map { it.studentId }.toSet()

            val newResults = studentsToPopulate.filter { it.id !in existingStudentIds }.mapIndexed { idx, st ->
                ExamResult(
                    id = 0,
                    examId = exam.id,
                    studentId = st.id,
                    studentName = st.name,
                    fatherName = st.fatherName,
                    studentClass = st.section.ifBlank { exam.section },
                    paray = if (exam.type == "HIFZ_ORAL") "پارہ 28 تا 30" else "پارہ 1 تا 5",
                    question1 = 23.0,
                    question2 = 22.0,
                    question3 = 23.0,
                    talaffuz = 24.0,
                    kulNumber = 92.0,
                    totalMaxMarks = 100.0,
                    kaifiyat = "ممتاز",
                    subject = if (exam.type == "HIFZ_ORAL") "حفظ القرآن الکریم (زبانی جائزہ)" else exam.title,
                    marksObtained = 92.0,
                    totalMarks = 100.0,
                    grade = "ممتاز",
                    remarks = "کامیاب",
                    isPassed = true
                )
            }
            if (newResults.isNotEmpty()) {
                db.examDao().insertResults(newResults)
            }
        }
    }

    // Student CRUD
    fun saveStudent(
        student: Student,
        isEdit: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!isEdit) {
                    val existing = repository.getStudentByRollNo(student.rollNo)
                    if (existing != null && !existing.isDeleted) {
                        withContext(Dispatchers.Main) {
                            onError("Roll No ${student.rollNo} already exists!")
                        }
                        return@launch
                    } else if (existing != null && existing.isDeleted) {
                        val revived = student.copy(id = existing.id, isDeleted = false, deletedAt = null)
                        repository.updateStudent(revived)
                        withContext(Dispatchers.Main) { onSuccess() }
                        return@launch
                    }
                    val newId = repository.insertStudent(student)
                    try {
                        val voucher = FeeVoucher(
                            id = 0,
                            voucherNo = "VCH-${System.currentTimeMillis() % 100000}",
                            studentId = newId,
                            monthYear = "October 2025",
                            tuitionFee = student.monthlyFee,
                            discount = student.discount,
                            totalPayable = (student.monthlyFee - student.discount).coerceAtLeast(0.0),
                            paidAmount = 0.0,
                            status = "UNPAID"
                        )
                        repository.insertVoucher(voucher)
                    } catch (_: Exception) {}
                } else {
                    repository.updateStudent(student)
                }
                triggerAutoSync()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to save student")
                }
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudent(student)
            triggerAutoSync()
        }
    }

    fun softDeleteStudent(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeleteStudent(id)
            triggerAutoSync()
        }
    }

    fun restoreStudent(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreStudent(id)
            triggerAutoSync()
        }
    }

    fun permanentlyDeleteStudent(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteStudent(id)
            triggerAutoSync()
        }
    }

    // Teacher CRUD
    fun saveTeacher(teacher: Teacher, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isEdit) {
                repository.updateTeacher(teacher)
            } else {
                repository.insertTeacher(teacher)
            }
            triggerAutoSync()
        }
    }

    fun softDeleteTeacher(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeleteTeacher(id)
            triggerAutoSync()
        }
    }

    fun restoreTeacher(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreTeacher(id)
            triggerAutoSync()
        }
    }

    // Attendance Actions
    fun markAttendance(personId: Long, personType: String, status: String, remarks: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val date = selectedDate.value
            val existing = repository.getAttendanceRecord(date, personId, personType)
            val record = AttendanceRecord(
                id = existing?.id ?: 0,
                personId = personId,
                personType = personType,
                date = date,
                status = status,
                checkInTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                remarks = remarks
            )
            repository.markAttendance(record)
            triggerAutoSync()
        }
    }

    fun markAllStudentsPresent(studentIds: List<Long>? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = allStudents.value.filter { !it.isDeleted && it.status == "ACTIVE" }.map { it.id }
            val ids: List<Long> = if (!studentIds.isNullOrEmpty()) {
                studentIds
            } else if (current.isNotEmpty()) {
                current
            } else {
                repository.getAllStudentsDirect().filter { !it.isDeleted && it.status == "ACTIVE" }.map { it.id }
            }
            repository.markAllStudentsPresent(selectedDate.value, ids)
            triggerAutoSync()
        }
    }

    fun markAllTeachersPresent(teacherIds: List<Long>? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = if (!teacherIds.isNullOrEmpty()) {
                teacherIds
            } else {
                teachers.value.filter { !it.isDeleted && it.status == "ACTIVE" }.map { it.id }
            }
            repository.markAllTeachersPresent(selectedDate.value, ids)
            triggerAutoSync()
        }
    }

    fun enrollStudentBiometrics(
        studentId: Long,
        fingerprintTemplate: String? = null,
        faceEmbedding: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val student = repository.getStudentById(studentId) ?: return@launch
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val updated = student.copy(
                isBiometricEnrolled = true,
                fingerprintTemplate = fingerprintTemplate ?: student.fingerprintTemplate,
                faceEmbedding = faceEmbedding ?: student.faceEmbedding,
                biometricRegisteredDate = todayStr,
                biometricRegisteredAt = System.currentTimeMillis()
            )
            repository.updateStudent(updated)
        }
    }

    // Progress
    fun addHifzProgress(progress: HifzProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertHifz(progress)
        }
    }

    fun updateHifzProgress(progress: HifzProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateHifz(progress)
        }
    }

    fun deleteHifzProgress(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHifz(id)
        }
    }

    fun addNazraProgress(progress: NazraProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNazra(progress)
        }
    }

    fun updateNazraProgress(progress: NazraProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNazra(progress)
        }
    }

    fun deleteNazraProgress(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNazra(id)
        }
    }

    fun addQaidaProgress(progress: QaidaProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertQaida(progress)
        }
    }

    fun updateQaidaProgress(progress: QaidaProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQaida(progress)
        }
    }

    fun deleteQaidaProgress(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQaida(id)
        }
    }

    fun addTajweedProgress(progress: TajweedProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTajweed(progress)
        }
    }

    fun updateTajweedProgress(progress: TajweedProgress) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTajweed(progress)
        }
    }

    fun deleteTajweedProgress(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTajweed(id)
        }
    }

    // Fee Payment
    fun collectFeePayment(
        voucher: FeeVoucher,
        amount: Double,
        mode: String,
        notes: String,
        onSuccess: (PaymentRecord) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val receiptNo = "REC-${System.currentTimeMillis() % 100000}"
            val payment = PaymentRecord(
                id = 0,
                voucherId = voucher.id,
                studentId = voucher.studentId,
                amountPaid = amount,
                paymentDate = todayDate,
                paymentMode = mode,
                receiptNo = receiptNo,
                receiverName = "Admin",
                notes = notes
            )
            repository.insertPayment(payment)

            val newPaid = voucher.paidAmount + amount
            val newStatus = if (newPaid >= voucher.totalPayable) "PAID" else "PARTIAL"
            val updatedVoucher = voucher.copy(paidAmount = newPaid, status = newStatus)
            repository.updateVoucher(updatedVoucher)

            triggerAutoSync()
            withContext(Dispatchers.Main) {
                onSuccess(payment)
            }
        }
    }

    // Invoice Generation & Management
    fun generateInvoice(
        studentId: Long,
        monthYear: String,
        tuitionFee: Double,
        admissionFee: Double = 0.0,
        examFee: Double = 0.0,
        discount: Double = 0.0,
        dueDate: String = "2025-11-10",
        onSuccess: (FeeVoucher) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalPayable = (tuitionFee + admissionFee + examFee - discount).coerceAtLeast(0.0)
            val voucherNo = "INV-${System.currentTimeMillis() % 100000}"
            val voucher = FeeVoucher(
                id = 0,
                voucherNo = voucherNo,
                studentId = studentId,
                monthYear = monthYear,
                tuitionFee = tuitionFee,
                admissionFee = admissionFee,
                examFee = examFee,
                discount = discount,
                totalPayable = totalPayable,
                paidAmount = 0.0,
                status = "UNPAID",
                dueDate = dueDate
            )
            val id = repository.insertVoucher(voucher)
            triggerAutoSync()
            withContext(Dispatchers.Main) {
                onSuccess(voucher.copy(id = id))
            }
        }
    }

    fun bulkGenerateMonthlyInvoices(
        monthYear: String,
        dueDate: String,
        onComplete: (Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val active = allStudents.value.filter { it.status == "ACTIVE" }
            var createdCount = 0
            val existing = vouchers.value
            active.forEach { st ->
                val alreadyExists = existing.any { it.studentId == st.id && it.monthYear.equals(monthYear, ignoreCase = true) }
                if (!alreadyExists) {
                    val payable = (st.monthlyFee - st.discount).coerceAtLeast(0.0)
                    val voucherNo = "INV-${(10000..99999).random()}"
                    val voucher = FeeVoucher(
                        id = 0,
                        voucherNo = voucherNo,
                        studentId = st.id,
                        monthYear = monthYear,
                        tuitionFee = st.monthlyFee,
                        discount = st.discount,
                        totalPayable = payable,
                        paidAmount = 0.0,
                        status = "UNPAID",
                        dueDate = dueDate
                    )
                    repository.insertVoucher(voucher)
                    createdCount++
                }
            }
            withContext(Dispatchers.Main) {
                onComplete(createdCount)
            }
        }
    }

    fun deleteVoucher(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVoucher(id)
        }
    }

    fun deletePayment(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePayment(id)
        }
    }

    // Courses CRUD
    fun saveCourse(course: Course, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isEdit) {
                repository.updateCourse(course)
            } else {
                repository.insertCourse(course)
            }
        }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCourse(id)
        }
    }

    fun permanentlyDeleteTeacher(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteTeacher(id)
        }
    }

    // Exam CRUD
    fun addExam(exam: Exam) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExam(exam)
        }
    }

    fun updateExam(exam: Exam) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExam(exam)
        }
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExam(id)
        }
    }

    fun getResultsForExam(examId: Long) = repository.getResultsForExam(examId)

    fun getResultsForStudent(studentId: Long) = repository.getResultsForStudent(studentId)

    fun saveExamResult(result: ExamResult, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isEdit) {
                repository.updateExamResult(result)
            } else {
                repository.insertExamResult(result)
            }
        }
    }

    fun deleteExamResult(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExamResult(id)
        }
    }

    fun clearAllExamResults(onSuccess: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllExamResultsDirect()
            all.forEach { repository.deleteExamResult(it.id) }
            withContext(Dispatchers.Main) {
                onSuccess(all.size)
            }
        }
    }

    fun setAttendanceDate(date: String) {
        selectedDate.value = date
    }

    // Profile
    fun updateProfile(newProfile: MadrassaProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(newProfile)
            triggerAutoSync()
        }
    }

    // PDF Report Generator
    fun generatePdfReport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            PdfReportGenerator.generateAndShareReport(
                context = context,
                profile = profile.value,
                students = allStudents.value,
                teachers = teachers.value,
                todayAttendance = studentAttendance.value,
                vouchers = vouchers.value,
                hifzList = recentHifz.value
            )
        }
    }

    fun generateStudentReportCardPdf(context: Context, student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            val allResults = repository.getAllExamResultsDirect()
            val allAtt = repository.getAllAttendanceDirect()
            val allVouchers = vouchers.value
            PdfReportGenerator.generateStudentReportCard(
                context = context,
                profile = profile.value,
                student = student,
                examResults = allResults,
                attendanceRecords = allAtt,
                vouchers = allVouchers
            )
        }
    }

    fun generateAttendanceSheetPdf(
        context: Context,
        isMonthly: Boolean,
        department: String = "",
        section: String = "",
        campus: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val att = if (isMonthly) repository.getAllAttendanceDirect() else studentAttendance.value
            PdfReportGenerator.generateAttendanceSheetPdf(
                context = context,
                profile = profile.value,
                dateOrMonth = selectedDate.value,
                isMonthly = isMonthly,
                students = allStudents.value,
                attendanceList = att,
                department = department,
                section = section,
                campus = campus
            )
        }
    }

    fun generateClassExamMarksheetPdf(context: Context, exam: Exam) {
        viewModelScope.launch(Dispatchers.IO) {
            val results: List<ExamResult> = repository.getAllExamResultsDirect().filter { it.examId == exam.id }
            PdfReportGenerator.generateClassExamMarksheetPdf(
                context = context,
                profile = profile.value,
                exam = exam,
                students = allStudents.value,
                results = results
            )
        }
    }

    fun generateIdCardsPdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            PdfReportGenerator.generateIdCardsPdf(
                context = context,
                profile = profile.value,
                students = allStudents.value
            )
        }
    }

    // Database Backup & Restore (JSON / SQL)
    val backupManager = com.example.data.backup.DatabaseBackupManager(db)

    fun exportDatabaseJson(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = backupManager.exportToJson(context = getApplication())
            withContext(Dispatchers.Main) {
                onReady(json)
            }
        }
    }

    fun exportDatabaseSql(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val sql = backupManager.exportToSql()
            withContext(Dispatchers.Main) {
                onReady(sql)
            }
        }
    }

    fun importDatabaseJson(
        json: String,
        replaceExisting: Boolean,
        onSuccess: (com.example.data.backup.BackupSummary) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val summary = backupManager.importFromJson(
                    context = getApplication(),
                    jsonString = json,
                    replaceExisting = replaceExisting
                )
                withContext(Dispatchers.Main) {
                    onSuccess(summary)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to parse backup")
                }
            }
        }
    }

    fun importDatabaseSql(
        sql: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = backupManager.importFromSql(sql)
                withContext(Dispatchers.Main) {
                    onSuccess(count)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to execute SQL backup")
                }
            }
        }
    }

    // Dedicated Student & Teacher JSON Export / Import
    fun exportStudentsJson(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = backupManager.exportStudentsToJson(context = getApplication())
            withContext(Dispatchers.Main) {
                onReady(json)
            }
        }
    }

    fun exportTeachersJson(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = backupManager.exportTeachersToJson(context = getApplication())
            withContext(Dispatchers.Main) {
                onReady(json)
            }
        }
    }

    fun exportStudentsAndTeachersJson(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = backupManager.exportStudentsAndTeachersToJson(context = getApplication())
            withContext(Dispatchers.Main) {
                onReady(json)
            }
        }
    }

    fun importStudentsJson(
        json: String,
        replaceExisting: Boolean,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = backupManager.importStudentsFromJson(
                    json = json,
                    context = getApplication(),
                    replaceExisting = replaceExisting
                )
                triggerAutoSync()
                withContext(Dispatchers.Main) {
                    onSuccess(count)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to import students JSON")
                }
            }
        }
    }

    fun importTeachersJson(
        json: String,
        replaceExisting: Boolean,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = backupManager.importTeachersFromJson(
                    json = json,
                    context = getApplication(),
                    replaceExisting = replaceExisting
                )
                triggerAutoSync()
                withContext(Dispatchers.Main) {
                    onSuccess(count)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to import teachers JSON")
                }
            }
        }
    }

    fun purgeOldDatabaseRecords(
        cutoffDate: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deleted = backupManager.purgeOldRecords(cutoffDate)
                withContext(Dispatchers.Main) {
                    onSuccess(deleted)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to purge old records")
                }
            }
        }
    }

    fun resetEntireDatabase(
        reseed: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                backupManager.resetDatabase(reseed = reseed)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to reset database")
                }
            }
        }
    }
}
