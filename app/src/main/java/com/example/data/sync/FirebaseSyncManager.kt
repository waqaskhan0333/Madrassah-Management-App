package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.util.NetworkConnectivityObserver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    OFFLINE,
    NOT_AUTHENTICATED
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val lastSyncTime: Long = 0,
    val syncedCount: Int = 0,
    val currentEntity: String = "",
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    val currentUserEmail: String? = null
)

class FirebaseSyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val authManager: AuthManager,
    private val connectivityObserver: NetworkConnectivityObserver
) {
    private val tag = "FirebaseSyncManager"

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private fun getFirestore(): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        return FirebaseFirestore.getInstance(databaseId)
    }

    suspend fun syncAll(isAuto: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val isConnected = connectivityObserver.isCurrentlyConnected()
        if (!isConnected) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.OFFLINE,
                isOnline = false,
                errorMessage = "Device is offline. Data saved in local offline database."
            )
            Log.d(tag, "Sync skipped: device is offline")
            return@withContext false
        }

        val user = authManager.currentUser.value
        if (user == null) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.NOT_AUTHENTICATED,
                isOnline = true,
                errorMessage = "Connect Google Cloud Account to backup and sync."
            )
            Log.d(tag, "Sync skipped: user not authenticated")
            return@withContext false
        }

        val uid = user.uid
        val firestore = getFirestore()

        _syncState.value = _syncState.value.copy(
            status = SyncStatus.SYNCING,
            isOnline = true,
            currentUserEmail = user.email,
            currentEntity = "Preparing sync...",
            errorMessage = null
        )

        var totalSynced = 0

        try {
            // 1. Sync Profile
            _syncState.value = _syncState.value.copy(currentEntity = "Madrasa Profile")
            val localProfile = database.madrassaProfileDao().getProfileDirect()
            if (localProfile != null) {
                val profileMap = hashMapOf<String, Any>(
                    "id" to localProfile.id,
                    "nameUr" to localProfile.nameUr,
                    "nameEn" to localProfile.nameEn,
                    "addressUr" to localProfile.addressUr,
                    "addressEn" to localProfile.addressEn,
                    "phone1" to localProfile.phone1,
                    "phone2" to localProfile.phone2,
                    "email" to localProfile.email,
                    "principalName" to localProfile.principalName,
                    "registrationNo" to localProfile.registrationNo,
                    "sessionYear" to localProfile.sessionYear,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("profile").document("main")
                    .set(profileMap, SetOptions.merge()).await()
                totalSynced++
            }

            // 2. Sync Students
            _syncState.value = _syncState.value.copy(currentEntity = "Students")
            val localStudents = database.studentDao().getAllStudentsDirect()
            for (st in localStudents) {
                val studentMap = hashMapOf<String, Any>(
                    "id" to st.id,
                    "rollNo" to st.rollNo,
                    "name" to st.name,
                    "fatherName" to st.fatherName,
                    "guardianPhone" to st.guardianPhone,
                    "department" to st.department,
                    "section" to st.section,
                    "campus" to st.campus,
                    "status" to st.status,
                    "monthlyFee" to st.monthlyFee,
                    "isDeleted" to st.isDeleted,
                    "deletedAt" to (st.deletedAt ?: 0L),
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("students").document(st.id.toString())
                    .set(studentMap, SetOptions.merge()).await()
                totalSynced++
            }

            // Pull remote students if not local
            val remoteStudentsSnapshot = firestore.collection("users").document(uid)
                .collection("students").get().await()
            val existingRollNos = localStudents.map { it.rollNo }.toSet()
            val remoteStudentsToInsert = mutableListOf<Student>()
            for (doc in remoteStudentsSnapshot.documents) {
                val rollNo = doc.getString("rollNo") ?: continue
                if (!existingRollNos.contains(rollNo)) {
                    val newSt = Student(
                        rollNo = rollNo,
                        name = doc.getString("name") ?: "Student",
                        fatherName = doc.getString("fatherName") ?: "",
                        guardianPhone = doc.getString("guardianPhone") ?: "",
                        department = doc.getString("department") ?: "Hifz",
                        section = doc.getString("section") ?: "Section A",
                        campus = doc.getString("campus") ?: "Main Campus",
                        status = doc.getString("status") ?: "ACTIVE",
                        monthlyFee = doc.getDouble("monthlyFee") ?: 2500.0,
                        isDeleted = doc.getBoolean("isDeleted") ?: false
                    )
                    remoteStudentsToInsert.add(newSt)
                }
            }
            if (remoteStudentsToInsert.isNotEmpty()) {
                database.studentDao().insertStudents(remoteStudentsToInsert)
            }

            // 3. Sync Teachers
            _syncState.value = _syncState.value.copy(currentEntity = "Teachers")
            val localTeachers = database.teacherDao().getAllTeachersDirect()
            for (tc in localTeachers) {
                val teacherMap = hashMapOf<String, Any>(
                    "id" to tc.id,
                    "name" to tc.name,
                    "phone" to tc.phone,
                    "role" to tc.role,
                    "assignedDepartment" to tc.assignedDepartment,
                    "assignedSections" to tc.assignedSections,
                    "status" to tc.status,
                    "monthlySalary" to tc.monthlySalary,
                    "isDeleted" to tc.isDeleted,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("teachers").document(tc.id.toString())
                    .set(teacherMap, SetOptions.merge()).await()
                totalSynced++
            }

            // 4. Sync Attendance
            _syncState.value = _syncState.value.copy(currentEntity = "Attendance Records")
            val localAttendance = database.attendanceDao().getAllAttendanceDirect()
            for (att in localAttendance) {
                val attMap = hashMapOf<String, Any>(
                    "id" to att.id,
                    "personId" to att.personId,
                    "personType" to att.personType,
                    "date" to att.date,
                    "status" to att.status,
                    "checkInTime" to att.checkInTime,
                    "remarks" to att.remarks,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("student_attendance").document(att.id.toString())
                    .set(attMap, SetOptions.merge()).await()
                totalSynced++
            }

            // 5. Sync Fee Vouchers & Payments
            _syncState.value = _syncState.value.copy(currentEntity = "Fee Invoices & Payments")
            val localVouchers = database.feeDao().getAllVouchersDirect()
            for (v in localVouchers) {
                val vMap = hashMapOf<String, Any>(
                    "id" to v.id,
                    "studentId" to v.studentId,
                    "voucherNo" to v.voucherNo,
                    "monthYear" to v.monthYear,
                    "tuitionFee" to v.tuitionFee,
                    "admissionFee" to v.admissionFee,
                    "examFee" to v.examFee,
                    "discount" to v.discount,
                    "totalPayable" to v.totalPayable,
                    "paidAmount" to v.paidAmount,
                    "status" to v.status,
                    "dueDate" to v.dueDate,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("fee_vouchers").document(v.id.toString())
                    .set(vMap, SetOptions.merge()).await()
                totalSynced++
            }

            val localPayments = database.feeDao().getAllPaymentsDirect()
            for (p in localPayments) {
                val pMap = hashMapOf<String, Any>(
                    "id" to p.id,
                    "voucherId" to p.voucherId,
                    "studentId" to p.studentId,
                    "amountPaid" to p.amountPaid,
                    "paymentDate" to p.paymentDate,
                    "paymentMode" to p.paymentMode,
                    "receiptNo" to p.receiptNo,
                    "receiverName" to p.receiverName,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("payment_records").document(p.id.toString())
                    .set(pMap, SetOptions.merge()).await()
                totalSynced++
            }

            // 6. Sync Exams & Results
            _syncState.value = _syncState.value.copy(currentEntity = "Exams & Results")
            val localExams = database.examDao().getAllExamsDirect()
            for (ex in localExams) {
                val exMap = hashMapOf<String, Any>(
                    "id" to ex.id,
                    "title" to ex.title,
                    "type" to ex.type,
                    "date" to ex.date,
                    "department" to ex.department,
                    "section" to ex.section,
                    "totalMarks" to ex.totalMarks,
                    "passMarks" to ex.passMarks,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("exams").document(ex.id.toString())
                    .set(exMap, SetOptions.merge()).await()
                totalSynced++
            }

            val localResults = database.examDao().getAllExamResultsDirect()
            for (res in localResults) {
                val resMap = hashMapOf<String, Any>(
                    "id" to res.id,
                    "examId" to res.examId,
                    "studentId" to res.studentId,
                    "studentName" to res.studentName,
                    "fatherName" to res.fatherName,
                    "marksObtained" to res.marksObtained,
                    "totalMarks" to res.totalMarks,
                    "grade" to res.grade,
                    "kaifiyat" to res.kaifiyat,
                    "isPassed" to res.isPassed,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid)
                    .collection("exam_results").document(res.id.toString())
                    .set(resMap, SetOptions.merge()).await()
                totalSynced++
            }

            _syncState.value = _syncState.value.copy(
                status = SyncStatus.SUCCESS,
                lastSyncTime = System.currentTimeMillis(),
                syncedCount = totalSynced,
                currentEntity = "",
                errorMessage = null
            )
            Log.d(tag, "Sync completed successfully: $totalSynced records synced")
            return@withContext true
        } catch (e: Exception) {
            Log.e(tag, "Sync error occurred", e)
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Sync encountered an error",
                currentEntity = ""
            )
            return@withContext false
        }
    }
}
