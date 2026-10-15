package com.example.delominds.data.sync

import org.json.JSONObject

/**
 * Connects the app's own ids to the server's ids, using the cached snapshot, and queues
 * teacher actions through the SyncEngine. Each teacher-facing method returns false when the
 * change cannot be matched to the server data (for example a school or student the server does
 * not know).
 *
 * The admin-facing methods below (queueNewSchool, queueNewTeacher, queueAssignment,
 * queueNewStudent, queuePayment, queueSettings) create or reference records by business key
 * (school code, teacher email, student code) rather than by server id, because the admin may be
 * creating several related records offline, in one go, before any of them has a server id yet.
 * The server resolves those keys when it applies the batch; see SYNC_STATUS.md for the payload
 * shape of each.
 */
class ServerBridge(
  private val store: OfflineStore,
  private val engine: SyncEngine,
  private val isSignedIn: () -> Boolean
) {

  private fun snapshot() = store.loadSnapshot()

  /** Server assignment id for an active school code and grade, or null if there is none. */
  fun assignmentId(schoolCode: String, grade: String): Long? {
    val list = snapshot()?.optJSONArray("assignments") ?: return null
    for (i in 0 until list.length()) {
      val a = list.getJSONObject(i)
      if (a.optString("school_code") == schoolCode && a.optString("grade") == grade && a.optString("status") == "active") {
        return a.getLong("id")
      }
    }
    return null
  }

  /** Question keys in the server's order for a grade and week, or null if the snapshot has none. */
  fun questionKeys(grade: String, week: Int): List<String>? {
    val part = partOfGrade(grade) ?: return null
    val list = snapshot()?.optJSONObject("questions")?.optJSONObject(part)?.optJSONArray(week.toString()) ?: return null
    return (0 until list.length()).map { list.getJSONObject(it).getString("key") }
  }

  fun queueFollowUp(schoolCode: String, grade: String, week: Int, covered: List<Boolean>): Boolean {
    if (!isSignedIn()) return false
    val assignment = assignmentId(schoolCode, grade) ?: return false
    val keys = questionKeys(grade, week) ?: return false
    if (keys.size != covered.size) return false
    engine.queueFollowUp(assignment, week, keys.zip(covered) { key, c -> key to (if (c) "C" else "N") })
    return true
  }

  /** statuses: app student code -> "present" | "late" | "absent". Students the server does not know are skipped. */
  fun queueAttendance(schoolCode: String, grade: String, date: String, statuses: Map<String, String>): Boolean {
    if (!isSignedIn()) return false
    val assignment = assignmentId(schoolCode, grade) ?: return false
    val list = snapshot()?.optJSONArray("students") ?: return false
    val byCode = HashMap<String, Long>()
    for (i in 0 until list.length()) {
      val s = list.getJSONObject(i)
      byCode[s.optString("code")] = s.getLong("id")
    }
    val records = statuses.mapNotNull { (code, status) -> byCode[code]?.let { it to status } }
    if (records.isEmpty()) return false
    engine.queueAttendance(assignment, date, records)
    return true
  }

  /** scores: the six rubric scores (1 to 4) in the rubric's order. */
  fun queueExpoScore(studentCode: String, projectTitle: String, term: Int, scores: List<Int>, notes: String): Boolean {
    if (!isSignedIn() || scores.size != 6) return false
    val list = snapshot()?.optJSONArray("students") ?: return false
    var studentId: Long? = null
    for (i in 0 until list.length()) {
      val s = list.getJSONObject(i)
      if (s.optString("code") == studentCode) studentId = s.getLong("id")
    }
    val id = studentId ?: return false
    val keys = listOf("problem", "design", "prototype", "relevance", "presentation", "iteration")
    val payload = org.json.JSONObject().put("student_id", id).put("term", term).put("project_title", projectTitle).put("notes", notes)
    keys.zip(scores).forEach { (k, v) -> payload.put(k, v) }
    engine.queueExpoScore(payload)
    return true
  }

  // ----- admin actions (safe to call offline; resolved by the server on sync) -----

  /** New school. [schoolCode] should be the same short code shown in the app (e.g. "RIV-01"). */
  fun queueNewSchool(
    name: String,
    schoolCode: String,
    location: String,
    address: String,
    phone: String,
    email: String,
    principal: String,
    gradesOffered: List<String>
  ): Boolean {
    if (!isSignedIn()) return false
    val payload = JSONObject()
      .put("name", name)
      .put("school_code", schoolCode)
      .put("location", location)
      .put("address", address)
      .put("phone", phone)
      .put("email", email)
      .put("principal", principal)
      .put("grades_offered", org.json.JSONArray(gradesOffered))
    engine.queueAdminChange("school", payload)
    return true
  }

  fun queueNewTeacher(name: String, email: String, phone: String, address: String): Boolean {
    if (!isSignedIn()) return false
    val payload = JSONObject().put("name", name).put("email", email).put("phone", phone).put("address", address)
    engine.queueAdminChange("teacher", payload)
    return true
  }

  /** Assigns a teacher (by email) to a school (by school code) and grade. */
  fun queueAssignment(
    teacherEmail: String,
    schoolCode: String,
    gradeGroupId: String,
    grade: String,
    className: String,
    academicYear: String,
    term: Int
  ): Boolean {
    if (!isSignedIn()) return false
    val payload = JSONObject()
      .put("teacher_email", teacherEmail)
      .put("school_code", schoolCode)
      .put("grade_group", gradeGroupId)
      .put("grade", grade)
      .put("class_name", className)
      .put("academic_year", academicYear)
      .put("term", term)
    engine.queueAdminChange("assignment", payload)
    return true
  }

  /** New student. [studentCode] is the human-readable id shown in the app (e.g. "STU-0012"). */
  fun queueNewStudent(
    studentCode: String,
    fullName: String,
    schoolCode: String,
    grade: String,
    className: String,
    guardianName: String,
    guardianPhone: String
  ): Boolean {
    if (!isSignedIn()) return false
    val payload = JSONObject()
      .put("code", studentCode)
      .put("full_name", fullName)
      .put("school_code", schoolCode)
      .put("grade", grade)
      .put("class_name", className)
      .put("guardian_name", guardianName)
      .put("guardian_phone", guardianPhone)
    engine.queueAdminChange("student", payload)
    return true
  }

  /** Records a payment and the receipt number already shown to the admin on the phone. */
  fun queuePayment(
    receiptNumber: String,
    studentCode: String,
    schoolCode: String,
    grade: String,
    month: String,
    amount: Double,
    currency: String,
    paymentMethod: String,
    status: String,
    notes: String
  ): Boolean {
    if (!isSignedIn()) return false
    val payload = JSONObject()
      .put("receipt_number", receiptNumber)
      .put("student_code", studentCode)
      .put("school_code", schoolCode)
      .put("grade", grade)
      .put("month", month)
      .put("amount", amount)
      .put("currency", currency)
      .put("payment_method", paymentMethod)
      .put("status", status)
      .put("notes", notes)
    engine.queueAdminChange("payment", payload)
    return true
  }

  /** Replaces the system settings. The server should treat this as a full, last-write-wins update. */
  fun queueSettings(
    organisationName: String,
    currency: String,
    monthlyFee: Double,
    academicYear: String,
    programmeStartDate: String,
    currentWeek: Int,
    receiptFooter: String,
    appWebAddress: String,
    smtpHost: String,
    smtpPort: Int,
    smsWebhookUrl: String
  ): Boolean {
    if (!isSignedIn()) return false
    val payload = JSONObject()
      .put("organisation_name", organisationName)
      .put("currency", currency)
      .put("monthly_fee", monthlyFee)
      .put("academic_year", academicYear)
      .put("programme_start_date", programmeStartDate)
      .put("current_week", currentWeek)
      .put("receipt_footer", receiptFooter)
      .put("app_web_address", appWebAddress)
      .put("smtp_host", smtpHost)
      .put("smtp_port", smtpPort)
      .put("sms_webhook_url", smsWebhookUrl)
    engine.queueAdminChange("settings", payload)
    return true
  }

  companion object {
    /** "ECD-A" or "ECD-B" -> A; Grades 1-3 -> B; 4-5 -> C; 6-7 -> D. Same grouping as the server. */
    fun partOfGrade(grade: String): String? {
      if (grade.contains("ECD", ignoreCase = true)) return "A"
      val n = Regex("\\d+").find(grade)?.value?.toIntOrNull() ?: return null
      return when (n) {
        1, 2, 3 -> "B"
        4, 5 -> "C"
        6, 7 -> "D"
        else -> null
      }
    }
  }
}
