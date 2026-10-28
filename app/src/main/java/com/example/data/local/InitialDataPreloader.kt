package com.example.data.local

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object InitialDataPreloader {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today: String = dateFormat.format(Date())

    suspend fun seedDatabase(database: AppDatabase) {
        // 1. Madrassa Profile
        val profile = MadrassaProfile()
        database.madrassaProfileDao().insertOrUpdateProfile(profile)

        // 2. Teachers
        val teachers = listOf(
            Teacher(
                id = 1,
                name = "Ustaz Muhammad Saeed",
                fatherName = "Hafiz Abdul Ghafoor",
                phone = "0300-1112233",
                cnic = "35201-1234567-1",
                qualification = "Al-Shahadah Al-Alimiyyah & Hafiz",
                joinDate = "2020-03-01",
                monthlySalary = 45000.0,
                assignedDepartment = "Hifz",
                assignedSections = "Class 1A, Class 2B",
                role = "Ustaz",
                status = "ACTIVE"
            ),
            Teacher(
                id = 2,
                name = "Qari Abdul Rehman",
                fatherName = "Qari Noor Muhammad",
                phone = "0301-2223344",
                cnic = "35201-2345678-3",
                qualification = "Qirat Sab'ah & Tajweed Specialist",
                joinDate = "2021-06-15",
                monthlySalary = 42000.0,
                assignedDepartment = "Qirat & Tajweed",
                assignedSections = "Tajweed Section A",
                role = "Ustaz",
                status = "ACTIVE"
            ),
            Teacher(
                id = 3,
                name = "Maulana Tariq Mahmood",
                fatherName = "Bashir Ahmad",
                phone = "0302-3334455",
                cnic = "35201-3456789-5",
                qualification = "Dars-e-Nizami Graduate",
                joinDate = "2022-01-10",
                monthlySalary = 38000.0,
                assignedDepartment = "Nazra",
                assignedSections = "Nazra Morning",
                role = "Ustaz",
                status = "ACTIVE"
            ),
            Teacher(
                id = 4,
                name = "Muallima Zainab Bibi",
                fatherName = "Muhammad Aslam",
                phone = "0303-4445566",
                cnic = "35201-4567890-7",
                qualification = "Fazila-e-Dars-e-Nizami",
                joinDate = "2023-08-01",
                monthlySalary = 32000.0,
                assignedDepartment = "Qaida",
                assignedSections = "Qaida Beginners",
                role = "Ustaza",
                status = "ACTIVE"
            )
        )
        database.teacherDao().insertTeachers(teachers)

        // 3. Courses
        val courses = listOf(
            Course(1, "حفظ القرآن الکریم", "Hifz-ul-Quran", "Complete Quran Memorization with Tajweed", "3 Years", "Ustaz Muhammad Saeed", 3000.0),
            Course(2, "ناظرہ قرآن مع تجوید", "Nazra Quran with Tajweed", "Recitation with fluent phonetic accuracy", "1 Year", "Maulana Tariq Mahmood", 2500.0),
            Course(3, "نورانی قاعدہ", "Noorani Qaida", "Basic Arabic phonetics and Quranic reading", "6 Months", "Muallima Zainab Bibi", 2000.0),
            Course(4, "قراءت و تجوید", "Qirat & Tajweed Rules", "Advanced Ahkam-e-Tajweed and Makharij", "1 Year", "Qari Abdul Rehman", 2500.0),
            Course(5, "دینیات و اخلاقیات", "Islamic Studies & Ethics", "Essential Islamic beliefs, Masnoon Duas, and Fiqh", "1 Year", "Maulana Tariq Mahmood", 1500.0)
        )
        database.courseDao().insertCourses(courses)

        // 4. Students
        val students = listOf(
            Student(
                id = 1,
                rollNo = "STU001",
                name = "Ahmed Raza",
                fatherName = "Muhammad Raza",
                guardianName = "Muhammad Raza",
                guardianPhone = "0300-1234567",
                dob = "2013-04-12",
                gender = "Male",
                admissionDate = "2024-09-01",
                address = "House No. 123, Model Town, Lahore",
                department = "Hifz",
                section = "Class 1A",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            ),
            Student(
                id = 2,
                rollNo = "STU002",
                name = "Ayesha Khan",
                fatherName = "Ali Khan",
                guardianName = "Ali Khan",
                guardianPhone = "0301-9876543",
                dob = "2014-06-18",
                gender = "Female",
                admissionDate = "2024-09-01",
                address = "Gulberg III, Lahore",
                department = "Nazra",
                section = "Class 2B",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 500.0
            ),
            Student(
                id = 3,
                rollNo = "STU003",
                name = "Usman Ali",
                fatherName = "Imran Ali",
                guardianName = "Imran Ali",
                guardianPhone = "0321-4567890",
                dob = "2015-02-14",
                gender = "Male",
                admissionDate = "2024-09-05",
                address = "Johar Town, Lahore",
                department = "Qaida",
                section = "Class 3A",
                status = "ACTIVE",
                monthlyFee = 2000.0,
                discount = 0.0
            ),
            Student(
                id = 4,
                rollNo = "STU004",
                name = "Fatima Noor",
                fatherName = "Saeed Ahmad",
                guardianName = "Saeed Ahmad",
                guardianPhone = "0333-7890123",
                dob = "2013-11-20",
                gender = "Female",
                admissionDate = "2024-08-15",
                address = "Faisal Town, Lahore",
                department = "Hifz",
                section = "Class 4B",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            ),
            Student(
                id = 5,
                rollNo = "STU005",
                name = "Hassan Shah",
                fatherName = "Abdul Wahab",
                guardianName = "Abdul Wahab",
                guardianPhone = "0345-6789012",
                dob = "2012-08-09",
                gender = "Male",
                admissionDate = "2024-08-20",
                address = "Allama Iqbal Town, Lahore",
                department = "Qirat & Tajweed",
                section = "Class 5A",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            ),
            Student(
                id = 6,
                rollNo = "STU006",
                name = "Zainab Bibi",
                fatherName = "Khurram Shehzad",
                guardianName = "Khurram Shehzad",
                guardianPhone = "0312-3456789",
                dob = "2016-01-25",
                gender = "Female",
                admissionDate = "2024-09-10",
                address = "DHA Phase 5, Lahore",
                department = "Qaida",
                section = "Class 1B",
                status = "ACTIVE",
                monthlyFee = 2000.0,
                discount = 0.0
            ),
            Student(
                id = 7,
                rollNo = "STU007",
                name = "Ali Raza",
                fatherName = "Ahmed Raza",
                guardianName = "Ahmed Raza",
                guardianPhone = "0300-8765432",
                dob = "2011-09-15",
                gender = "Male",
                admissionDate = "2023-09-01",
                address = "Wapda Town, Lahore",
                department = "Hifz",
                section = "Class 1A",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            ),
            Student(
                id = 8,
                rollNo = "STU008",
                name = "Maryam Fatima",
                fatherName = "Tariq Aziz",
                guardianName = "Tariq Aziz",
                guardianPhone = "0305-1122334",
                dob = "2014-12-05",
                gender = "Female",
                admissionDate = "2024-09-15",
                address = "Samanabad, Lahore",
                department = "Nazra",
                section = "Class 2B",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            ),
            Student(
                id = 9,
                rollNo = "STU009",
                name = "Bilal Tariq",
                fatherName = "Tariq Mahmood",
                guardianName = "Tariq Mahmood",
                guardianPhone = "0322-9988776",
                dob = "2013-03-30",
                gender = "Male",
                admissionDate = "2024-08-01",
                address = "Cantt, Lahore",
                department = "Hifz",
                section = "Class 1A",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            ),
            Student(
                id = 10,
                rollNo = "STU010",
                name = "Hamza Farooq",
                fatherName = "Farooq Umar",
                guardianName = "Farooq Umar",
                guardianPhone = "0308-5544332",
                dob = "2012-07-19",
                gender = "Male",
                admissionDate = "2024-07-15",
                address = "Garden Town, Lahore",
                department = "Qirat & Tajweed",
                section = "Class 5A",
                status = "ACTIVE",
                monthlyFee = 2500.0,
                discount = 0.0
            )
        )
        val enrolledStudents = students.map { st ->
            st.copy(
                isBiometricEnrolled = true,
                fingerprintTemplate = com.example.util.BiometricSecurityManager.generateFingerprintTemplate(st),
                faceEmbedding = com.example.util.BiometricSecurityManager.generateFaceEmbedding(st),
                biometricRegisteredDate = "2025-10-15",
                biometricRegisteredAt = System.currentTimeMillis() - 86400000L * 5
            )
        }
        database.studentDao().insertStudents(enrolledStudents)

        // 5. Today's Attendance
        val attendances = listOf(
            AttendanceRecord(0, 1, "STUDENT", today, "PRESENT", "07:45 AM", "On time"),
            AttendanceRecord(0, 2, "STUDENT", today, "PRESENT", "07:50 AM", "On time"),
            AttendanceRecord(0, 3, "STUDENT", today, "PRESENT", "07:52 AM", "On time"),
            AttendanceRecord(0, 4, "STUDENT", today, "ABSENT", "", "Informed illness"),
            AttendanceRecord(0, 5, "STUDENT", today, "PRESENT", "07:40 AM", "On time"),
            AttendanceRecord(0, 6, "STUDENT", today, "LATE", "08:15 AM", "Traffic delay"),
            AttendanceRecord(0, 7, "STUDENT", today, "PRESENT", "07:48 AM", "On time"),
            AttendanceRecord(0, 8, "STUDENT", today, "PRESENT", "07:55 AM", "On time"),
            AttendanceRecord(0, 9, "STUDENT", today, "PRESENT", "07:42 AM", "On time"),
            AttendanceRecord(0, 10, "STUDENT", today, "PRESENT", "07:45 AM", "On time"),
            // Teachers attendance
            AttendanceRecord(0, 1, "TEACHER", today, "PRESENT", "07:30 AM", "On time"),
            AttendanceRecord(0, 2, "TEACHER", today, "PRESENT", "07:35 AM", "On time"),
            AttendanceRecord(0, 3, "TEACHER", today, "PRESENT", "07:28 AM", "On time"),
            AttendanceRecord(0, 4, "TEACHER", today, "PRESENT", "07:40 AM", "On time")
        )
        database.attendanceDao().insertAll(attendances)

        // 6. Progress Records
        database.progressDao().insertHifz(
            HifzProgress(
                id = 0,
                studentId = 1, // Ahmed Raza
                date = today,
                sabaqPara = 29,
                sabaqSurah = "Al-Mulk",
                sabaqAyahRange = "Ayah 1-15",
                sabqiRange = "Surah Al-Qalam",
                manzilParas = "Para 30 (Complete)",
                grade = "Mumtaz",
                mistakesCount = 0,
                totalParasCompleted = 2,
                remarks = "Masha'Allah fluent memorization and tajweed"
            )
        )
        database.progressDao().insertHifz(
            HifzProgress(
                id = 0,
                studentId = 7, // Ali Raza
                date = today,
                sabaqPara = 15,
                sabaqSurah = "Al-Isra",
                sabaqAyahRange = "Ayah 23-35",
                sabqiRange = "Para 14 (Half)",
                manzilParas = "Para 1 to 5",
                grade = "Jayyid",
                mistakesCount = 2,
                totalParasCompleted = 14,
                remarks = "Good recitation, focus on Sabqi revision"
            )
        )
        database.progressDao().insertNazra(
            NazraProgress(
                id = 0,
                studentId = 2, // Ayesha Khan
                date = today,
                currentPara = 5,
                pageRange = "Page 45-47",
                mistakesCount = 1,
                grade = "Mumtaz",
                remarks = "Clear pronunciation of Makharij"
            )
        )
        database.progressDao().insertQaida(
            QaidaProgress(
                id = 0,
                studentId = 3, // Usman Ali
                date = today,
                lessonNo = 6,
                topic = "Harkat (Fatha, Kasra, Damma)",
                grade = "Mumtaz",
                passOrRepeat = "PASS",
                remarks = "Passed lesson smoothly without prolongation"
            )
        )
        database.progressDao().insertTajweed(
            TajweedProgress(
                id = 0,
                studentId = 5, // Hassan Shah
                date = today,
                topic = "Ahkam-e-Noon Sakinah & Tanween (Idgham)",
                practicalAssessment = "Surah Al-Qari'ah recitation with Ghunna",
                style = "Hafs 'an 'Asim",
                grade = "Mumtaz",
                remarks = "Excellent rule application"
            )
        )

        // 7. Exams & Results
        val midTermExam = Exam(
            id = 1,
            title = "Mid Term Exam 2025",
            type = "MID_TERM",
            date = "2025-10-15",
            department = "Hifz",
            section = "Class 1A",
            totalMarks = 100,
            passMarks = 40,
            status = "PUBLISHED"
        )
        val hifzOralExam = Exam(
            id = 2,
            title = "Hifz Oral Assessment (Para 29-30)",
            type = "HIFZ_ORAL",
            date = "2025-10-25",
            department = "Hifz",
            section = "Class 1A",
            totalMarks = 100,
            passMarks = 50,
            status = "SCHEDULED"
        )
        database.examDao().insertExams(listOf(midTermExam, hifzOralExam))

        // Real exam results are recorded by teachers/examiners in the Exam Marksheet or DMC preview.
        val results = emptyList<ExamResult>()
        val dummyResultsIgnored = listOf(
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 1,
                studentName = "Ahmed Raza",
                fatherName = "Muhammad Raza",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 28 تا 30",
                question1 = 24.0,
                question2 = 23.0,
                question3 = 23.0,
                talaffuz = 22.0,
                kulNumber = 92.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "حفظ القرآن الکریم",
                marksObtained = 92.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "ماشاء اللہ بہت عمدہ حفظ و تجوید",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 2,
                studentName = "Ayesha Khan",
                fatherName = "Ali Khan",
                studentClass = "Class 2B (Nazra)",
                paray = "پارہ 1 تا 5",
                question1 = 22.0,
                question2 = 22.0,
                question3 = 21.0,
                talaffuz = 23.0,
                kulNumber = 88.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "ناظرہ مع تجوید",
                marksObtained = 88.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "عمدہ روانی، مخارج کی درستگی",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 3,
                studentName = "Usman Ali",
                fatherName = "Imran Ali",
                studentClass = "Class 3A (Qaida)",
                paray = "نورانی قاعدہ مکمل",
                question1 = 21.0,
                question2 = 20.0,
                question3 = 22.0,
                talaffuz = 21.0,
                kulNumber = 84.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید",
                subject = "نورانی قاعدہ",
                marksObtained = 84.0,
                totalMarks = 100.0,
                grade = "جید",
                remarks = "کامیاب، اگلے درجہ کیلئے تیار",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 4,
                studentName = "Muhammad Ibrahim",
                fatherName = "Rashid Minhas",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 30",
                question1 = 23.0,
                question2 = 22.0,
                question3 = 22.0,
                talaffuz = 22.0,
                kulNumber = 89.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "حفظ القرآن الکریم",
                marksObtained = 89.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "اچھی رفتار اور حسن ادائیگی",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 5,
                studentName = "Hassan Shah",
                fatherName = "Abdul Wahab",
                studentClass = "Class 5A (Qirat & Tajweed)",
                paray = "پارہ 1 تا 10",
                question1 = 24.0,
                question2 = 24.0,
                question3 = 24.0,
                talaffuz = 24.0,
                kulNumber = 96.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "قراءت و احکام تجوید",
                marksObtained = 96.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "شاندار ترتیل اور مخارج کی صفائی",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 6,
                studentName = "Zainab Bibi",
                fatherName = "Khurram Shehzad",
                studentClass = "Class 1B (Qaida)",
                paray = "تختی 1 تا 14",
                question1 = 22.0,
                question2 = 21.0,
                question3 = 22.0,
                talaffuz = 22.0,
                kulNumber = 87.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "نورانی قاعدہ",
                marksObtained = 87.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "حروف کے مخارج کی اچھی سمجھ",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 7,
                studentName = "Ali Raza",
                fatherName = "Ahmed Raza",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 1 تا 3",
                question1 = 21.0,
                question2 = 20.0,
                question3 = 21.0,
                talaffuz = 21.0,
                kulNumber = 83.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید",
                subject = "حفظ القرآن الکریم",
                marksObtained = 83.0,
                totalMarks = 100.0,
                grade = "جید",
                remarks = "کامیاب، دہرائی میں باقاعدگی رکھیں",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 8,
                studentName = "Maryam Fatima",
                fatherName = "Tariq Aziz",
                studentClass = "Class 2B (Nazra)",
                paray = "پارہ 1 تا 8",
                question1 = 24.0,
                question2 = 24.0,
                question3 = 25.0,
                talaffuz = 24.0,
                kulNumber = 97.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "ناظرہ مع تجوید",
                marksObtained = 97.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "بہترین روانی اور مخارج کی درستگی",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 9,
                studentName = "Bilal Tariq",
                fatherName = "Tariq Mahmood",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 29 تا 30",
                question1 = 22.0,
                question2 = 21.0,
                question3 = 22.0,
                talaffuz = 21.0,
                kulNumber = 86.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "حفظ القرآن الکریم",
                marksObtained = 86.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "محنتی طالب علم، مسلسل بہتری",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 1,
                studentId = 10,
                studentName = "Hamza Farooq",
                fatherName = "Farooq Umar",
                studentClass = "Class 5A (Qirat & Tajweed)",
                paray = "پارہ 1 تا 12",
                question1 = 24.0,
                question2 = 24.0,
                question3 = 23.0,
                talaffuz = 24.0,
                kulNumber = 95.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "قراءت و تجوید",
                marksObtained = 95.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "عمدہ لحن اور تجوید کے قواعد پر عبور",
                isPassed = true
            ),
            // Exam 2: Hifz Oral Assessment
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 1,
                studentName = "Ahmed Raza",
                fatherName = "Muhammad Raza",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 29 تا 30",
                question1 = 25.0,
                question2 = 24.0,
                question3 = 24.0,
                talaffuz = 24.0,
                kulNumber = 97.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز (First)",
                subject = "حفظ و ترتیل",
                marksObtained = 97.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "حفظ قوی، لب و لہجہ خوش الحان",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 2,
                studentName = "Ayesha Khan",
                fatherName = "Ali Khan",
                studentClass = "Class 2B (Nazra)",
                paray = "پارہ 1 تا 5",
                question1 = 23.0,
                question2 = 23.0,
                question3 = 22.0,
                talaffuz = 23.0,
                kulNumber = 91.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "ناظرہ و مخارج",
                marksObtained = 91.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "وقف و وصل کا اچھا خیال",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 3,
                studentName = "Usman Ali",
                fatherName = "Imran Ali",
                studentClass = "Class 3A (Qaida)",
                paray = "نورانی قاعدہ",
                question1 = 22.0,
                question2 = 21.0,
                question3 = 22.0,
                talaffuz = 22.0,
                kulNumber = 87.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "قاعدہ و تلفظ",
                marksObtained = 87.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "مرکبات کی عمدہ پہچان",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 4,
                studentName = "Muhammad Ibrahim",
                fatherName = "Rashid Minhas",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 30",
                question1 = 23.0,
                question2 = 22.0,
                question3 = 23.0,
                talaffuz = 22.0,
                kulNumber = 90.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "حفظ و ترتیل",
                marksObtained = 90.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "اچھی تیاری، وقف و وصل کا خیال رکھیں",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 5,
                studentName = "Hassan Shah",
                fatherName = "Abdul Wahab",
                studentClass = "Class 5A (Qirat & Tajweed)",
                paray = "پارہ 1 تا 10",
                question1 = 24.0,
                question2 = 25.0,
                question3 = 24.0,
                talaffuz = 25.0,
                kulNumber = 98.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "احکام تجوید و صفات",
                marksObtained = 98.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "صفات لازمہ و عارضہ پر کمال گرفت",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 6,
                studentName = "Zainab Bibi",
                fatherName = "Khurram Shehzad",
                studentClass = "Class 1B (Qaida)",
                paray = "تختی 1 تا 14",
                question1 = 23.0,
                question2 = 22.0,
                question3 = 22.0,
                talaffuz = 23.0,
                kulNumber = 90.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "مفردات و مرکبات",
                marksObtained = 90.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "بہت شاندار کارکردگی",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 7,
                studentName = "Ali Raza",
                fatherName = "Ahmed Raza",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 1 تا 3",
                question1 = 22.0,
                question2 = 21.0,
                question3 = 22.0,
                talaffuz = 21.0,
                kulNumber = 86.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید جداً",
                subject = "حفظ و تکرار",
                marksObtained = 86.0,
                totalMarks = 100.0,
                grade = "جید جداً",
                remarks = "حفظ میں مزید بہتری لائیں",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 8,
                studentName = "Maryam Fatima",
                fatherName = "Tariq Aziz",
                studentClass = "Class 2B (Nazra)",
                paray = "پارہ 1 تا 8",
                question1 = 25.0,
                question2 = 24.0,
                question3 = 25.0,
                talaffuz = 24.0,
                kulNumber = 98.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "تجوید و روانی",
                marksObtained = 98.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "مخارج کی باریکیوں کا خاص دھیان",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 9,
                studentName = "Bilal Tariq",
                fatherName = "Tariq Mehmood",
                studentClass = "Class 1A (Hifz)",
                paray = "پارہ 29 تا 30",
                question1 = 22.0,
                question2 = 21.0,
                question3 = 20.0,
                talaffuz = 21.0,
                kulNumber = 84.0,
                totalMaxMarks = 100.0,
                kaifiyat = "جید",
                subject = "حفظ و ترتیل",
                marksObtained = 84.0,
                totalMarks = 100.0,
                grade = "جید",
                remarks = "کامیاب، روزانہ دہرائی ضروری ہے",
                isPassed = true
            ),
            ExamResult(
                id = 0,
                examId = 2,
                studentId = 10,
                studentName = "Hamza Farooq",
                fatherName = "Farooq Umar",
                studentClass = "Class 5A (Qirat & Tajweed)",
                paray = "پارہ 1 تا 12",
                question1 = 24.0,
                question2 = 24.0,
                question3 = 24.0,
                talaffuz = 24.0,
                kulNumber = 96.0,
                totalMaxMarks = 100.0,
                kaifiyat = "ممتاز",
                subject = "مخارج و ترتیل",
                marksObtained = 96.0,
                totalMarks = 100.0,
                grade = "ممتاز",
                remarks = "خوش آواز، قواعد کا عمدہ اطلاق",
                isPassed = true
            )
        )
        database.examDao().insertResults(results)

        // 8. Fee Vouchers & Payments
        val currentMonth = "October 2025"
        val vouchers = listOf(
            FeeVoucher(1, "VCH-2025-001", 1, currentMonth, 2500.0, 0.0, 0.0, 0.0, 2500.0, 2500.0, "PAID", "2025-10-15"),
            FeeVoucher(2, "VCH-2025-002", 2, currentMonth, 2500.0, 0.0, 0.0, 500.0, 2000.0, 2000.0, "PAID", "2025-10-15"),
            FeeVoucher(3, "VCH-2025-003", 3, currentMonth, 2000.0, 0.0, 0.0, 0.0, 2000.0, 0.0, "UNPAID", "2025-10-15"),
            FeeVoucher(4, "VCH-2025-004", 4, currentMonth, 2500.0, 0.0, 0.0, 0.0, 2500.0, 1000.0, "PARTIAL", "2025-10-15"),
            FeeVoucher(5, "VCH-2025-005", 5, currentMonth, 2500.0, 0.0, 0.0, 0.0, 2500.0, 2500.0, "PAID", "2025-10-15"),
            FeeVoucher(6, "VCH-2025-006", 6, currentMonth, 2000.0, 0.0, 0.0, 0.0, 2000.0, 0.0, "UNPAID", "2025-10-15")
        )
        database.feeDao().insertVouchers(vouchers)

        val payments = listOf(
            PaymentRecord(1, 1, 1, 2500.0, today, "Cash", "REC-2025-101", "Admin", "Full monthly tuition fee"),
            PaymentRecord(2, 2, 2, 2000.0, today, "EasyPaisa", "REC-2025-102", "Admin", "Monthly fee after 500 discount"),
            PaymentRecord(3, 4, 4, 1000.0, today, "Cash", "REC-2025-103", "Admin", "Partial installment payment"),
            PaymentRecord(4, 5, 5, 2500.0, today, "JazzCash", "REC-2025-104", "Admin", "Full monthly fee payment")
        )
        database.feeDao().insertPayments(payments)
    }
}
