package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MadrassaProfile::class,
        Student::class,
        Teacher::class,
        AttendanceRecord::class,
        HifzProgress::class,
        NazraProgress::class,
        QaidaProgress::class,
        TajweedProgress::class,
        Course::class,
        Exam::class,
        ExamResult::class,
        FeeVoucher::class,
        PaymentRecord::class,
        AuditLog::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun progressDao(): ProgressDao
    abstract fun courseDao(): CourseDao
    abstract fun examDao(): ExamDao
    abstract fun feeDao(): FeeDao
    abstract fun madrassaProfileDao(): MadrassaProfileDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "madrassa_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    InitialDataPreloader.seedDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
