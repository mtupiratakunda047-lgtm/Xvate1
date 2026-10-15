package com.example.delominds.data.model

enum class UserRole(val displayName: String) {
  SUPER_ADMIN("Super Admin"),
  ADMIN("Admin"),
  SCHOOL_ADMIN("School Admin"),
  TEACHER("Teacher")
}

data class UserAccount(
  val id: String,
  val name: String,
  val email: String,
  val phone: String,
  val role: UserRole,
  val assignedSchoolIds: List<String> = emptyList(),
  val assignedGradeGroups: List<String> = emptyList(),
  val temporaryPassword: Boolean = false
)

data class School(
  val id: String,
  val schoolCode: String,
  val name: String,
  val location: String,
  val address: String,
  val phone: String,
  val email: String,
  val principal: String,
  val contactPerson: String,
  val contactPhone: String,
  val contactEmail: String,
  val gradesOffered: List<String>,
  val studentCount: Int,
  val teacherCount: Int,
  val coveragePercent: Int,
  val pendingFollowUps: Int,
  val programmeStartDate: String,
  val isArchived: Boolean = false,
  val notes: String = ""
)

data class Teacher(
  val id: String,
  val name: String,
  val email: String,
  val phone: String,
  val address: String,
  val status: String = "Active",
  val pendingFollowUps: Int,
  val completedFollowUps: Int,
  val coveragePercent: Int
)

data class TeacherAssignment(
  val id: String,
  val teacherId: String,
  val teacherName: String,
  val schoolId: String,
  val schoolName: String,
  val gradeGroupId: String, // "part_a", "part_b", "part_c", "part_d"
  val grade: String,        // "Grade 1", "Grade 6", etc.
  val className: String = "Class A",
  val academicYear: String = "2026",
  val term: Int = 1,
  val status: String = "Active",
  val coveragePercent: Int = 83,
  val weeklyCoverage: Map<Int, Int?> = emptyMap() // Week 1..8 with coverage % or null
)

data class CurriculumPart(
  val id: String,
  val code: String,       // Part A, Part B, Part C, Part D
  val title: String,
  val gradeRange: String, // ECD-A & ECD-B, Grades 1–3, etc.
  val colorHex: Long,
  val totalWeeks: Int = 36,
  val coveragePercent: Int
)

data class CurriculumWeek(
  val partId: String,
  val weekNumber: Int,
  val term: Int,
  val topic: String,
  val learningObjectives: List<String>,
  val bigIdea: String,
  val keyWords: List<String>,
  val day1CoreConcept: String,
  val day2HandsOn: String,
  val day3Application: String,
  val workedExamples: String = "",
  val troubleshooting: String = "",
  val gradeDifferentiation: String = "",
  val selfCheck: String = "",
  val sketchNotes: String = "",
  val checkpointQuestions: List<String> = emptyList()
)

data class FollowUpQuestion(
  val questionNumber: Int,
  val questionText: String,
  val checkpointLabel: String,
  var isCovered: Boolean? = null
)

data class FollowUpSubmission(
  val id: String,
  val teacherId: String,
  val teacherName: String,
  val schoolId: String,
  val schoolName: String,
  val grade: String,
  val weekNumber: Int,
  val topic: String,
  val submissionDate: String,
  val answers: Map<Int, Boolean>,
  val coveredCount: Int,
  val notCoveredCount: Int,
  val coveragePercentage: Int,
  val isSubmitted: Boolean = true,
  val reopenedByAdmin: Boolean = false
)

enum class AttendanceStatus {
  PRESENT, LATE, ABSENT
}

data class Student(
  val id: String,
  val studentId: String,
  val fullName: String,
  val schoolId: String,
  val schoolName: String,
  val grade: String,
  val className: String,
  val guardianName: String,
  val guardianPhone: String,
  val attendancePresent: Int = 18,
  val attendanceLate: Int = 2,
  val attendanceAbsent: Int = 1,
  val expoScore: Int = 0,
  val isArchived: Boolean = false
)

data class AttendanceRecord(
  val id: String,
  val schoolId: String,
  val schoolName: String,
  val grade: String,
  val date: String,
  val teacherId: String,
  val studentAttendance: Map<String, AttendanceStatus>
)

enum class PaymentStatus {
  PAID, PENDING, PARTIAL, OVERDUE
}

data class Payment(
  val id: String,
  val receiptNumber: String,
  val studentId: String,
  val studentName: String,
  val schoolId: String,
  val schoolName: String,
  val grade: String,
  val month: String,
  val amount: Double,
  val currency: String = "USD",
  val paymentDate: String,
  val paymentMethod: String,
  val status: PaymentStatus,
  val notes: String = ""
)

data class ExpoRubricCriterion(
  val criterionNumber: Int,
  val title: String,
  val description: String,
  val level1: String, // Beginning (1 pt)
  val level2: String, // Developing (2 pts)
  val level3: String, // Proficient (3 pts)
  val level4: String  // Exemplary (4 pts)
)

data class ExpoEvaluation(
  val studentId: String,
  val studentName: String,
  val projectTitle: String,
  val term: Int,
  val scores: List<Int>, // 6 criteria (1..4) -> Total out of 24
  val totalScore: Int,
  val notes: String
)

data class NotificationItem(
  val id: String,
  val title: String,
  val message: String,
  val timestamp: String,
  val isRead: Boolean = false,
  val type: String
)

data class AuditLogEntry(
  val id: String,
  val user: String,
  val action: String,
  val record: String,
  val details: String,
  val timestamp: String
)

data class SystemSettings(
  val organisationName: String = "CREATVATE DELO MINDS",
  val currency: String = "USD",
  val monthlyFee: Double = 10.0,
  val academicYear: String = "2026",
  val programmeStartDate: String = "15 Jan 2026",
  val currentWeek: Int = 8,
  val receiptFooter: String = "Thank you for supporting STEM, Robotics & AI Education 5.0",
  val appWebAddress: String = "https://delominds.creatvate.org",
  val smtpHost: String = "smtp.gmail.com",
  val smtpPort: Int = 587,
  val smsWebhookUrl: String = "https://api.sms-gateway.com/webhook"
)

data class ScheduledLesson(
  val id: String,
  val schoolName: String,
  val grade: String,
  val teacherName: String,
  val room: String,
  val time: String,
  val topic: String,
  val dayOfWeek: String
)
