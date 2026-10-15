package com.example.delominds.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delominds.data.model.*
import com.example.delominds.data.repository.DeloMindsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Screen {
  SIGN_IN,
  ADMIN_DASHBOARD,
  ADMIN_CURRICULUM,
  ADMIN_CURRICULUM_DETAIL,
  ADMIN_SCHOOLS,
  ADMIN_SCHOOL_DETAIL,
  ADMIN_TEACHERS,
  ADMIN_TEACHER_DETAIL,
  ADMIN_STUDENTS,
  ADMIN_PAYMENTS,
  ADMIN_RECEIPT_DETAIL,
  ADMIN_ATTENDANCE,
  ADMIN_EXPO_RUBRIC,
  ADMIN_REPORTS,
  ADMIN_SETTINGS,
  ADMIN_AUDIT_LOG,
  TEACHER_HOME,
  TEACHER_LESSONS,
  TEACHER_LESSON_DETAIL,
  TEACHER_FOLLOW_UP_QUESTION,
  TEACHER_FOLLOW_UP_REVIEW,
  TEACHER_FOLLOW_UP_SUCCESS,
  TEACHER_ATTENDANCE,
  TEACHER_PROGRESS,
  TEACHER_MORE
}

data class FollowUpActiveSession(
  val teacherId: String,
  val teacherName: String,
  val schoolId: String,
  val schoolName: String,
  val grade: String,
  val weekNumber: Int,
  val topic: String,
  val questions: List<FollowUpQuestion>,
  val currentQuestionIndex: Int = 0,
  val isReviewing: Boolean = false,
  val submissionResult: FollowUpSubmission? = null
)

class DeloMindsViewModel(
  private val repository: DeloMindsRepository = DeloMindsRepository()
) : ViewModel() {

  /** Set by the activity once the offline sync layer is running. Null means server sync is not set up. */
  var serverBridge: com.example.delominds.data.sync.ServerBridge? = null

  /**
   * Loads whatever the cached server snapshot currently holds (schools, teachers, students,
   * curriculum, follow-ups, payments, settings, and the signed-in account) into the screens.
   * Pure data load — it never changes which screen is showing; MainActivity decides navigation
   * once it knows the signed-in role. Call with the offline copy right after launch, and again
   * after every sync, whether or not the sync reached the server.
   */
  fun applySnapshot(json: org.json.JSONObject?) {
    if (json == null) return
    repository.loadFromServerSnapshot(json)
  }

  val currentUser: StateFlow<UserAccount> = repository.currentUser
  val availableAccounts = repository.availableAccounts

  val schools = repository.schools
  val teachers = repository.teachers
  val assignments = repository.assignments
  val curriculumParts = repository.curriculumParts
  val curriculumWeeks = repository.curriculumWeeks
  val followUpSubmissions = repository.followUpSubmissions
  val students = repository.students
  val payments = repository.payments
  val attendanceRecords = repository.attendanceRecords
  val expoCriteria = repository.expoCriteria
  val expoEvaluations = repository.expoEvaluations
  val notifications = repository.notifications
  val auditLogs = repository.auditLogs
  val systemSettings = repository.systemSettings
  val scheduledLessons = repository.scheduledLessons

  private val _currentScreen = MutableStateFlow(Screen.SIGN_IN)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  // Selection states
  private val _selectedSchoolId = MutableStateFlow<String?>(null)
  val selectedSchoolId: StateFlow<String?> = _selectedSchoolId.asStateFlow()

  private val _selectedTeacherId = MutableStateFlow<String?>(null)
  val selectedTeacherId: StateFlow<String?> = _selectedTeacherId.asStateFlow()

  private val _selectedPartId = MutableStateFlow<String?>("part_d")
  val selectedPartId: StateFlow<String?> = _selectedPartId.asStateFlow()

  private val _selectedWeekNumber = MutableStateFlow<Int>(8)
  val selectedWeekNumber: StateFlow<Int> = _selectedWeekNumber.asStateFlow()

  private val _selectedReceipt = MutableStateFlow<Payment?>(null)
  val selectedReceipt: StateFlow<Payment?> = _selectedReceipt.asStateFlow()

  // Follow-up flow state
  private val _activeFollowUp = MutableStateFlow<FollowUpActiveSession?>(null)
  val activeFollowUp: StateFlow<FollowUpActiveSession?> = _activeFollowUp.asStateFlow()

  // Notifications bell dialog
  private val _showNotificationsDialog = MutableStateFlow(false)
  val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

  // Toast / feedback message
  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  // Always starts at Screen.SIGN_IN. MainActivity's launch effect checks whether the phone
  // already has a server session and, only then, calls applySnapshot(...) and navigates past
  // sign-in — so a teacher who has never signed in never sees another teacher's (or the seeded
  // sample) dashboard.

  fun navigateTo(screen: Screen) {
    _currentScreen.value = screen
  }

  fun switchRole(account: UserAccount) {
    repository.switchUser(account)
    if (account.role == UserRole.TEACHER) {
      _currentScreen.value = Screen.TEACHER_HOME
    } else {
      _currentScreen.value = Screen.ADMIN_DASHBOARD
    }
    _userMessage.value = "Switched to ${account.name} (${account.role.displayName})"
  }

  fun signOut() {
    _currentScreen.value = Screen.SIGN_IN
    _userMessage.value = "Signed out"
  }

fun selectSchool(schoolId: String) {
    _selectedSchoolId.value = schoolId
    navigateTo(Screen.ADMIN_SCHOOL_DETAIL)
  }

  fun selectTeacher(teacherId: String) {
    _selectedTeacherId.value = teacherId
    navigateTo(Screen.ADMIN_TEACHER_DETAIL)
  }

  fun selectCurriculumWeek(partId: String, weekNumber: Int, fromTeacher: Boolean = false) {
    _selectedPartId.value = partId
    _selectedWeekNumber.value = weekNumber
    if (fromTeacher) {
      navigateTo(Screen.TEACHER_LESSON_DETAIL)
    } else {
      navigateTo(Screen.ADMIN_CURRICULUM_DETAIL)
    }
  }

  fun openReceipt(payment: Payment) {
    _selectedReceipt.value = payment
    navigateTo(Screen.ADMIN_RECEIPT_DETAIL)
  }

  // Follow-up Core Workflow
  fun startFollowUp(
    schoolId: String = "SCH-001",
    schoolName: String = "Riverside Primary School",
    grade: String = "Grade 6",
    weekNumber: Int = 8
  ) {
    val week = curriculumWeeks.value.find { it.partId == "part_d" && it.weekNumber == weekNumber }
      ?: curriculumWeeks.value.first()

    val defaultQuestions = listOf(
      FollowUpQuestion(
        questionNumber = 1,
        questionText = "Did you cover the Core Concept & Theory section?",
        checkpointLabel = "Core Concept & Theory"
      ),
      FollowUpQuestion(
        questionNumber = 2,
        questionText = "Did you conduct the Hands-on Building activity?",
        checkpointLabel = "Hands-on Building"
      ),
      FollowUpQuestion(
        questionNumber = 3,
        questionText = "Did you cover the Education 5.0 Application?",
        checkpointLabel = "Education 5.0 Application"
      )
    )

    _activeFollowUp.value = FollowUpActiveSession(
      teacherId = currentUser.value.id,
      teacherName = currentUser.value.name,
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      weekNumber = weekNumber,
      topic = week.topic,
      questions = defaultQuestions,
      currentQuestionIndex = 0,
      isReviewing = false
    )

    navigateTo(Screen.TEACHER_FOLLOW_UP_QUESTION)
  }

  fun answerQuestion(questionIndex: Int, isCovered: Boolean) {
    val current = _activeFollowUp.value ?: return
    val updatedQuestions = current.questions.toMutableList()
    val q = updatedQuestions[questionIndex]
    updatedQuestions[questionIndex] = q.copy(isCovered = isCovered)

    val nextIdx = questionIndex + 1
    if (nextIdx < updatedQuestions.size) {
      // Auto move to next question per user guide!
      _activeFollowUp.value = current.copy(
        questions = updatedQuestions,
        currentQuestionIndex = nextIdx
      )
    } else {
      // Finished all questions -> go to "Check your answers" review screen!
      _activeFollowUp.value = current.copy(
        questions = updatedQuestions,
        isReviewing = true
      )
      navigateTo(Screen.TEACHER_FOLLOW_UP_REVIEW)
    }
  }

  fun toggleAnswerInReview(questionIndex: Int) {
    val current = _activeFollowUp.value ?: return
    val updatedQuestions = current.questions.toMutableList()
    val q = updatedQuestions[questionIndex]
    val newAnswer = if (q.isCovered == true) false else true
    updatedQuestions[questionIndex] = q.copy(isCovered = newAnswer)
    _activeFollowUp.value = current.copy(questions = updatedQuestions)
  }

  fun submitFollowUpSession() {
    val current = _activeFollowUp.value ?: return
    val answersMap = current.questions.associate { it.questionNumber to (it.isCovered ?: false) }

    val submission = repository.submitFollowUp(
      teacherId = current.teacherId,
      teacherName = current.teacherName,
      schoolId = current.schoolId,
      schoolName = current.schoolName,
      grade = current.grade,
      weekNumber = current.weekNumber,
      topic = current.topic,
      answers = answersMap
    )

    serverBridge?.queueFollowUp(current.schoolId, current.grade, current.weekNumber, current.questions.map { it.isCovered ?: false })

    _activeFollowUp.value = current.copy(submissionResult = submission)
    navigateTo(Screen.TEACHER_FOLLOW_UP_SUCCESS)
  }

  fun reopenFollowUp(submissionId: String) {
    repository.reopenFollowUp(submissionId, currentUser.value.name)
    _userMessage.value = "Follow-up reopened for teacher to re-submit."
  }

  fun saveAttendance(
    schoolId: String,
    schoolName: String,
    grade: String,
    date: String,
    statusMap: Map<String, AttendanceStatus>
  ) {
    repository.saveAttendance(
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      date = date,
      teacherId = currentUser.value.id,
      studentStatus = statusMap
    )
    serverBridge?.queueAttendance(schoolId, grade, date, statusMap.entries.associate { it.key to it.value.name.lowercase() })
    _userMessage.value = "Attendance saved for $grade on $date"
  }

  fun recordPayment(
    studentId: String,
    studentName: String,
    schoolId: String,
    schoolName: String,
    grade: String,
    month: String,
    amount: Double,
    paymentMethod: String,
    status: PaymentStatus,
    notes: String
  ) {
    val payment = repository.recordPayment(
      studentId = studentId,
      studentName = studentName,
      schoolId = schoolId,
      schoolName = schoolName,
      grade = grade,
      month = month,
      amount = amount,
      paymentMethod = paymentMethod,
      status = status,
      notes = notes
    )
    val queued = serverBridge?.queuePayment(
      receiptNumber = payment.receiptNumber,
      studentCode = studentId,
      schoolCode = repository.schools.value.find { it.id == schoolId }?.schoolCode ?: schoolId,
      grade = grade,
      month = month,
      amount = amount,
      currency = payment.currency,
      paymentMethod = paymentMethod,
      status = status.name,
      notes = notes
    )
    _userMessage.value = "Payment recorded: Receipt ${payment.receiptNumber}" +
      if (queued == false) " (will sync once signed in to the server)" else ""
  }

  fun addSchool(
    name: String,
    location: String,
    address: String,
    phone: String,
    email: String,
    principal: String,
    gradesOffered: List<String>
  ) {
    repository.addSchool(name, location, address, phone, email, principal, gradesOffered)
    val newSchool = repository.schools.value.lastOrNull { it.name == name }
    serverBridge?.queueNewSchool(
      name = name,
      schoolCode = newSchool?.schoolCode ?: name.take(3).uppercase(),
      location = location,
      address = address,
      phone = phone,
      email = email,
      principal = principal,
      gradesOffered = gradesOffered
    )
    _userMessage.value = "School $name added successfully"
  }

  fun addTeacher(name: String, email: String, phone: String, address: String) {
    repository.addTeacher(name, email, phone, address)
    serverBridge?.queueNewTeacher(name, email, phone, address)
    _userMessage.value = "Teacher $name added successfully"
  }

  fun assignTeacher(
    teacherId: String,
    teacherName: String,
    schoolId: String,
    schoolName: String,
    gradeGroupId: String,
    grade: String,
    className: String
  ) {
    repository.assignTeacher(teacherId, teacherName, schoolId, schoolName, gradeGroupId, grade, className)
    val teacherEmail = repository.teachers.value.find { it.id == teacherId }?.email
    val schoolCode = repository.schools.value.find { it.id == schoolId }?.schoolCode
    if (teacherEmail != null && schoolCode != null) {
      serverBridge?.queueAssignment(
        teacherEmail = teacherEmail,
        schoolCode = schoolCode,
        gradeGroupId = gradeGroupId,
        grade = grade,
        className = className,
        academicYear = systemSettings.value.academicYear,
        term = 1
      )
    }
    _userMessage.value = "$teacherName assigned to $schoolName ($grade)"
  }

  fun addStudent(
    fullName: String,
    schoolId: String,
    schoolName: String,
    grade: String,
    className: String,
    guardianName: String,
    guardianPhone: String
  ) {
    repository.addStudent(fullName, schoolId, schoolName, grade, className, guardianName, guardianPhone)
    val newStudent = repository.students.value.lastOrNull { it.fullName == fullName && it.schoolId == schoolId }
    val schoolCode = repository.schools.value.find { it.id == schoolId }?.schoolCode ?: schoolId
    serverBridge?.queueNewStudent(
      studentCode = newStudent?.studentId ?: fullName,
      fullName = fullName,
      schoolCode = schoolCode,
      grade = grade,
      className = className,
      guardianName = guardianName,
      guardianPhone = guardianPhone
    )
    _userMessage.value = "Student $fullName enrolled successfully"
  }

  fun scoreExpoProject(
    studentId: String,
    studentName: String,
    projectTitle: String,
    term: Int,
    scores: List<Int>,
    notes: String
  ) {
    repository.saveExpoEvaluation(studentId, studentName, projectTitle, term, scores, notes)
    serverBridge?.queueExpoScore(studentId, projectTitle, term, scores, notes)
    _userMessage.value = "Expo evaluation saved for $studentName: ${scores.sum()} / 24"
  }

  fun updateSettings(newSettings: SystemSettings) {
    repository.updateSettings(newSettings)
    serverBridge?.queueSettings(
      organisationName = newSettings.organisationName,
      currency = newSettings.currency,
      monthlyFee = newSettings.monthlyFee,
      academicYear = newSettings.academicYear,
      programmeStartDate = newSettings.programmeStartDate,
      currentWeek = newSettings.currentWeek,
      receiptFooter = newSettings.receiptFooter,
      appWebAddress = newSettings.appWebAddress,
      smtpHost = newSettings.smtpHost,
      smtpPort = newSettings.smtpPort,
      smsWebhookUrl = newSettings.smsWebhookUrl
    )
    _userMessage.value = "System settings updated"
  }

  fun toggleNotificationsDialog(show: Boolean) {
    _showNotificationsDialog.value = show
    if (show) {
      repository.markNotificationsAsRead()
    }
  }

  fun addFollowUpCheckpoint(gradeGroupId: String, weekNumber: Int?, shortLabel: String, question: String) {
    repository.addFollowUpCheckpoint(gradeGroupId, weekNumber, shortLabel, question)
    _userMessage.value = "Checkpoint added to $gradeGroupId"
  }

  fun scheduleLesson(
    schoolName: String,
    grade: String,
    teacherName: String,
    room: String,
    time: String,
    topic: String,
    dayOfWeek: String
  ) {
    repository.scheduleLesson(schoolName, grade, teacherName, room, time, topic, dayOfWeek)
    _userMessage.value = "Scheduled lesson for $grade at $schoolName"
  }

  fun removeScheduledLesson(lessonId: String) {
    repository.removeScheduledLesson(lessonId)
    _userMessage.value = "Removed scheduled lesson"
  }

  fun importStudentsCsv(csvContent: String): Int {
    val count = repository.importStudentsCsv(csvContent)
    _userMessage.value = "Imported $count learners from CSV"
    return count
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }
}
