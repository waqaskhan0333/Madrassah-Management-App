package com.example.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*

enum class AppLanguage {
    ENGLISH,
    URDU
}

object LanguageState {
    var currentLanguage by mutableStateOf(AppLanguage.ENGLISH)

    fun toggle() {
        currentLanguage = if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.URDU else AppLanguage.ENGLISH
    }

    fun setLanguage(lang: AppLanguage) {
        currentLanguage = lang
    }

    val isUrdu: Boolean
        get() = currentLanguage == AppLanguage.URDU
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object Strings {
    private val en = mapOf(
        "app_title" to "Madrassa Management System",
        "app_tagline" to "Ilm • Tarbiyat • Noble Future",
        "greeting" to "Assalam-o-Alaikum, Admin",
        "dashboard" to "Dashboard",
        "home" to "Home",
        "students" to "Students",
        "attendance" to "Attendance",
        "progress" to "Progress",
        "more" to "More",
        "asatiza" to "Teachers",
        "teachers" to "Teachers",
        "exams" to "Exams & Results",
        "fees" to "Fees & Accounts",
        "courses" to "Courses & Classes",
        "classes" to "Classes",
        "reports" to "Reports & Analytics",
        "settings" to "Settings & Profile",
        "recycle_bin" to "Recycle Bin",
        "total_students" to "Total Students",
        "total_teachers" to "Total Teachers",
        "total_classes" to "Total Classes",
        "total_campuses" to "Total Campuses",
        "today_attendance" to "Today's Attendance",
        "quick_actions" to "Quick Actions",
        "student_progress" to "Student Progress",
        "todays_classes" to "Today's Classes",
        "recent_activity" to "Recent Activity",
        "view_all" to "View All >",
        "ongoing" to "Ongoing",
        "upcoming" to "Upcoming",
        "enrolled_active" to "Active Enrolled",
        "faculty_members" to "Faculty Members",
        "active_courses" to "Active Courses",
        "marked_today" to "Marked Today",
        "present" to "Present",
        "absent" to "Absent",
        "late" to "Late",
        "leave" to "Leave",
        "fee_collection" to "Fee Collection",
        "pending_fees" to "Pending Fees",
        "hifz" to "Hifz",
        "nazra" to "Nazra",
        "qaida" to "Qaida",
        "tajweed" to "Qirat & Tajweed",
        "add_student" to "Add New Student",
        "edit_student" to "Edit Student Profile",
        "add_teacher" to "Add New Teacher",
        "edit_teacher" to "Edit Teacher Profile",
        "mark_attendance" to "Mark Attendance",
        "mark_all_present" to "Mark All Present",
        "collect_fee" to "Collect Fee",
        "search_hint" to "Search by roll no, name, father or phone...",
        "pdf_backup" to "Generate PDF Report",
        "backup_restore" to "Backup & Restore",
        "sync_status" to "Database Status",
        "online_ready" to "100% Offline SQLite Database",
        "roll_no" to "Roll No",
        "name" to "Name",
        "father_name" to "Father's Name",
        "guardian_phone" to "Guardian Mobile",
        "phone" to "Phone",
        "address" to "Address",
        "department" to "Class",
        "section" to "Class / Section",
        "campus" to "Campus / Branch",
        "paray" to "Paray (Juz)",
        "change_campus" to "Change Campus",
        "change_paray" to "Change Para",
        "status" to "Status",
        "active" to "Active",
        "inactive" to "Inactive",
        "action" to "Action",
        "save" to "Save",
        "cancel" to "Cancel",
        "edit" to "Edit",
        "delete" to "Delete",
        "restore" to "Restore",
        "permanent_delete" to "Permanent Delete",
        "call" to "Call Guardian",
        "whatsapp" to "WhatsApp",
        "view_profile" to "View Profile",
        "view_receipt" to "View Receipt",
        "pay_now" to "Pay Now",
        "amount" to "Amount",
        "payment_mode" to "Payment Mode",
        "paid" to "Paid",
        "unpaid" to "Unpaid",
        "partial" to "Partial",
        "sabaq" to "Sabaq (New Lesson)",
        "sabqi" to "Sabqi (Revision)",
        "manzil" to "Manzil (Old)",
        "grade" to "Grade",
        "mistakes" to "Mistakes",
        "total_paras" to "Total Paras",
        "madrassa_profile" to "Madrassa Profile",
        "madrassa_name_ur" to "Madrassa Name (Urdu)",
        "madrassa_name_en" to "Madrassa Name (English)",
        "principal_name" to "Principal / Mohtamim Name",
        "session_year" to "Academic Session",
        "registration_no" to "Registration No",
        "switch_lang" to "اردو میں دیکھیں",
        "lang_name" to "Language (Urdu / English)",
        "no_records" to "No records found",
        "empty_bin" to "Recycle bin is empty",
        "delete_confirm" to "Move to Recycle Bin? Items can be restored anytime.",
        "success_saved" to "Saved successfully!",
        "duplicate_roll" to "Roll Number already exists! Please use a unique Roll Number.",
        "exam_marksheet" to "Exam Marksheet Table",
        "create_exam" to "Create New Exam",
        "edit_exam" to "Edit Exam",
        "delete_exam" to "Delete Exam",
        "question_1" to "Question 1",
        "question_2" to "Question 2",
        "question_3" to "Question 3",
        "talaffuz" to "Talaffuz & Tajweed",
        "kul_number" to "Total Marks (Kul Number)",
        "kaifiyat" to "Remarks (Kaifiyat)",
        "add_result" to "Add Student Result",
        "edit_result" to "Edit Result",
        "delete_result" to "Delete Result",
        "upload_logo" to "Upload Madrassa Logo",
        "change_logo" to "Change Logo",
        "upload_photo" to "Upload Photo",
        "change_photo" to "Change Photo",
        "full_calendar" to "Full Attendance Calendar",
        "pick_date" to "Select Date from Calendar",
        "no_classes_yet" to "No courses or classes registered yet",
        "no_activity_yet" to "No recent activity recorded yet"
    )

    private val ur = mapOf(
        "app_title" to "جامعہ مدنیہ مینیجمنٹ سسٹم",
        "app_tagline" to "علم • تربیت • بہترین مستقبل",
        "greeting" to "السلام علیکم، منتظم",
        "dashboard" to "ڈیش بورڈ",
        "home" to "ہوم",
        "students" to "طلبہ",
        "attendance" to "حاضری",
        "progress" to "تعلیمی کارکردگی",
        "more" to "مزید",
        "asatiza" to "اساتذہ کرام",
        "teachers" to "اساتذہ کرام",
        "exams" to "امتحانات و جدول نتائج",
        "fees" to "فیس و مالیات",
        "courses" to "کلاسز و شعبہ جات",
        "classes" to "کلاسز",
        "reports" to "رپورٹس و پی ڈی ایف",
        "settings" to "سیٹنگز و پروفائل",
        "recycle_bin" to "ری سائیکل بن",
        "total_students" to "کل طلبہ",
        "total_teachers" to "کل اساتذہ",
        "total_classes" to "کل کلاسز",
        "total_campuses" to "کل شاخیں",
        "today_attendance" to "آج کی حاضری",
        "quick_actions" to "فوری کارروائیاں",
        "student_progress" to "طلبہ کی کارکردگی",
        "todays_classes" to "آج کی کلاسز",
        "recent_activity" to "حالیہ سرگرمیاں",
        "view_all" to "تمام دیکھیں >",
        "ongoing" to "جاری ہے",
        "upcoming" to "آئندہ",
        "enrolled_active" to "فعال زیرِ تعلیم",
        "faculty_members" to "اساتذہ کرام",
        "active_courses" to "جاری شعبہ جات",
        "marked_today" to "آج کی حاضری",
        "present" to "حاضر",
        "absent" to "غیر حاضر",
        "late" to "دیر سے",
        "leave" to "رخصت",
        "fee_collection" to "فیس وصولی",
        "pending_fees" to "بقایا فیس",
        "hifz" to "حفظ القرآن",
        "nazra" to "ناظرہ قرآن",
        "qaida" to "نورانی قاعدہ",
        "tajweed" to "قراءت و تجوید",
        "add_student" to "نیا طالب علم داخل کریں",
        "edit_student" to "طالب علم پروفائل میں ترمیم",
        "add_teacher" to "نیا استاد شامل کریں",
        "edit_teacher" to "استاد کی تفصیلات میں ترمیم",
        "mark_attendance" to "حاضری درج کریں",
        "mark_all_present" to "تمام حاضر لگائیں",
        "collect_fee" to "فیس وصول کریں",
        "search_hint" to "رول نمبر، نام، ولدیت یا موبائل نمبر تلاش کریں...",
        "pdf_backup" to "پی ڈی ایف رپورٹ بنائیں",
        "backup_restore" to "بیک اپ اور بحالی",
        "sync_status" to "ڈیٹا بیس حالت",
        "online_ready" to "100% مکمل آف لائن SQLite ڈیٹا بیس",
        "roll_no" to "رول نمبر",
        "name" to "نام طالب علم",
        "father_name" to "والد کا نام",
        "guardian_phone" to "سرپرست کا رابطہ",
        "phone" to "فون نمبر",
        "address" to "پتہ",
        "department" to "کلاس / درجہ",
        "section" to "کلاس / سیکشن",
        "campus" to "کیمپس / برانچ",
        "paray" to "پارے / نصاب",
        "change_campus" to "کیمپس تبدیل کریں",
        "change_paray" to "پارہ تبدیل کریں",
        "status" to "حالت",
        "active" to "فعال",
        "inactive" to "غیر فعال",
        "action" to "کارروائی",
        "save" to "محفوظ کریں",
        "cancel" to "منسوخ",
        "edit" to "ترمیم",
        "delete" to "حذف کریں",
        "restore" to "بحال کریں",
        "permanent_delete" to "مکمل ختم کریں",
        "call" to "سرپرست کو کال کریں",
        "whatsapp" to "واٹس ایپ",
        "view_profile" to "پروفائل دیکھیں",
        "view_receipt" to "رسید دیکھیں",
        "pay_now" to "فیس ادا کریں",
        "amount" to "رقم",
        "payment_mode" to "ادائیگی کا طریقہ",
        "paid" to "ادا شدہ",
        "unpaid" to "غیر ادا شدہ",
        "partial" to "جزوی ادا",
        "sabaq" to "سبق (نیا سبق)",
        "sabqi" to "سبقی (حالیہ دہرائی)",
        "manzil" to "منزل (پرانی دہرائی)",
        "grade" to "درجہ / معیار",
        "mistakes" to "غلطیاں",
        "total_paras" to "مکمل پارے",
        "madrassa_profile" to "مدرسہ پروفائل",
        "madrassa_name_ur" to "مدرسہ کا نام (اردو)",
        "madrassa_name_en" to "مدرسہ کا نام (انگریزی)",
        "principal_name" to "مہتمم / ناظم اعلیٰ",
        "session_year" to "تعلیمی سال",
        "registration_no" to "رجسٹریشن نمبر",
        "switch_lang" to "View in English",
        "lang_name" to "زبان (اردو / انگریزی)",
        "no_records" to "کوئی ریکارڈ نہیں ملا",
        "empty_bin" to "ری سائیکل بن خالی ہے",
        "delete_confirm" to "کیا آپ ری سائیکل بن میں منتقل کرنا چاہتے ہیں؟ اسے بعد میں بحال کیا جا سکتا ہے۔",
        "success_saved" to "کامیابی سے محفوظ ہو گیا!",
        "duplicate_roll" to "یہ رول نمبر پہلے سے موجود ہے! برائے مہربانی مختلف رول نمبر درج کریں۔",
        "exam_marksheet" to "امتحانی جدول مارک شیٹ",
        "create_exam" to "نیا امتحان شامل کریں",
        "edit_exam" to "امتحان میں ترمیم",
        "delete_exam" to "امتحان حذف کریں",
        "question_1" to "سوال اول",
        "question_2" to "سوال دوم",
        "question_3" to "سوال سوم",
        "talaffuz" to "تلفظ و تجوید",
        "kul_number" to "کل نمبر",
        "kaifiyat" to "کیفیت / نتیجہ",
        "add_result" to "طالب علم کا نتیجہ درج کریں",
        "edit_result" to "نتیجہ تبدیل کریں",
        "delete_result" to "نتیجہ حذف کریں",
        "upload_logo" to "مدرسہ کا لوگو اپلوڈ کریں",
        "change_logo" to "لوگو تبدیل کریں",
        "upload_photo" to "تصویر اپلوڈ کریں",
        "change_photo" to "تصویر تبدیل کریں",
        "full_calendar" to "مکمل حاضری کیلنڈر",
        "pick_date" to "کیلنڈر سے تاریخ منتخب کریں",
        "no_classes_yet" to "ابھی کوئی کلاس درج نہیں کی گئی",
        "no_activity_yet" to "ابھی کوئی حالیہ سرگرمی موجود نہیں ہے"
    )

    fun get(key: String, lang: AppLanguage = LanguageState.currentLanguage): String {
        val map = if (lang == AppLanguage.URDU) ur else en
        return map[key] ?: en[key] ?: key
    }
}

fun t(key: String): String = Strings.get(key, LanguageState.currentLanguage)

object HijriDateUtil {
    fun getFormattedDates(lang: AppLanguage): Pair<String, String> {
        val gregorianCal = Calendar.getInstance()
        val gregFormatEn = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val gregFormatUr = SimpleDateFormat("dd MMMM yyyy", Locale("ur"))
        val gregDate = if (lang == AppLanguage.URDU) gregFormatUr.format(gregorianCal.time) else gregFormatEn.format(gregorianCal.time)

        val hijriDateEn = "15 Rabi-ul-Awwal 1447 AH"
        val hijriDateUr = "۱۵ ربیع الاول ۱۴۴۷ھ"

        val hijriDisplay = if (lang == AppLanguage.URDU) hijriDateUr else hijriDateEn
        return Pair(hijriDisplay, gregDate)
    }

    fun getTodayFullFormatted(lang: AppLanguage): String {
        val cal = Calendar.getInstance()
        val formatEn = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH)
        val formatUr = SimpleDateFormat("EEEE، dd MMMM yyyy", Locale("ur"))
        return if (lang == AppLanguage.URDU) formatUr.format(cal.time) else formatEn.format(cal.time)
    }
}
