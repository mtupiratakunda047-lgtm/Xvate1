package com.example.delominds.data.repository

import com.example.delominds.data.model.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Turns the cached server snapshot (data/sync/OfflineStore's snapshot.json, fetched by either
 * ServerClient.snapshot() for a teacher or ServerClient.adminSnapshot() for an admin role) into
 * this app's domain models.
 *
 * Every section is optional and every field is read defensively: a snapshot that omits a
 * section, or a record missing a field, never throws. A teacher's snapshot will only have a few
 * of these sections filled in (their own assignments, students and questions); an admin's
 * snapshot is expected to carry all of them. Where a section is absent from the JSON the
 * matching list is left as null so the caller can tell "the server sent nothing here" apart from
 * "the server sent an empty list" and keep whatever was already loaded.
 *
 * Expected top-level keys (all optional, all arrays unless noted):
 *   schools, teachers, assignments, curriculum_parts, curriculum_weeks, students, payments,
 *   follow_ups, notifications, audit_logs, settings (object), me (object)
 *
 * This is a client-side contract: the server is a separate project, not part of this repo.
 * SYNC_STATUS.md lists the field names each record is expected to carry.
 */
internal object SnapshotMapper {

  fun schools(json: JSONObject): List<School>? = json.optJSONArray("schools")?.objects()?.map {
    School(
      id = it.optString("id", it.optString("school_code")),
      schoolCode = it.optString("school_code"),
      name = it.optString("name"),
      location = it.optString("location"),
      address = it.optString("address"),
      phone = it.optString("phone"),
      email = it.optString("email"),
      principal = it.optString("principal"),
      contactPerson = it.optString("contact_person", it.optString("principal")),
      contactPhone = it.optString("contact_phone", it.optString("phone")),
      contactEmail = it.optString("contact_email", it.optString("email")),
      gradesOffered = it.optJSONArray("grades_offered")?.strings() ?: emptyList(),
      studentCount = it.optInt("student_count"),
      teacherCount = it.optInt("teacher_count"),
      coveragePercent = it.optInt("coverage_percent"),
      pendingFollowUps = it.optInt("pending_follow_ups"),
      programmeStartDate = it.optString("programme_start_date"),
      isArchived = it.optBoolean("is_archived", false),
      notes = it.optString("notes")
    )
  }

  fun teachers(json: JSONObject): List<Teacher>? = json.optJSONArray("teachers")?.objects()?.map {
    Teacher(
      id = it.optString("id", it.optString("email")),
      name = it.optString("name"),
      email = it.optString("email"),
      phone = it.optString("phone"),
      address = it.optString("address"),
      status = it.optString("status", "Active"),
      pendingFollowUps = it.optInt("pending_follow_ups"),
      completedFollowUps = it.optInt("completed_follow_ups"),
      coveragePercent = it.optInt("coverage_percent")
    )
  }

  fun assignments(json: JSONObject): List<TeacherAssignment>? = json.optJSONArray("assignments")?.objects()?.map {
    val weekly = HashMap<Int, Int?>()
    it.optJSONObject("weekly_coverage")?.let { wc ->
      wc.keys().forEach { key ->
        val weekNum = key.toIntOrNull()
        if (weekNum != null) weekly[weekNum] = if (wc.isNull(key)) null else wc.optInt(key)
      }
    }
    TeacherAssignment(
      id = it.optString("id"),
      teacherId = it.optString("teacher_id", it.optString("teacher_email")),
      teacherName = it.optString("teacher_name"),
      schoolId = it.optString("school_id", it.optString("school_code")),
      schoolName = it.optString("school_name"),
      gradeGroupId = it.optString("grade_group", it.optString("grade_group_id")),
      grade = it.optString("grade"),
      className = it.optString("class_name", "Class A"),
      academicYear = it.optString("academic_year", "2026"),
      term = it.optInt("term", 1),
      status = it.optString("status", "Active"),
      coveragePercent = it.optInt("coverage_percent"),
      weeklyCoverage = weekly
    )
  }

  fun curriculumParts(json: JSONObject): List<CurriculumPart>? = json.optJSONArray("curriculum_parts")?.objects()?.map {
    CurriculumPart(
      id = it.optString("id"),
      code = it.optString("code"),
      title = it.optString("title"),
      gradeRange = it.optString("grade_range"),
      colorHex = if (it.has("color_hex")) it.optLong("color_hex") else 0xFF2196F3,
      totalWeeks = it.optInt("total_weeks", 36),
      coveragePercent = it.optInt("coverage_percent")
    )
  }

  fun curriculumWeeks(json: JSONObject): List<CurriculumWeek>? = json.optJSONArray("curriculum_weeks")?.objects()?.map {
    CurriculumWeek(
      partId = it.optString("part_id"),
      weekNumber = it.optInt("week_number"),
      term = it.optInt("term", 1),
      topic = it.optString("topic"),
      learningObjectives = it.optJSONArray("learning_objectives")?.strings() ?: emptyList(),
      bigIdea = it.optString("big_idea"),
      keyWords = it.optJSONArray("key_words")?.strings() ?: emptyList(),
      day1CoreConcept = it.optString("day1_core_concept"),
      day2HandsOn = it.optString("day2_hands_on"),
      day3Application = it.optString("day3_application"),
      workedExamples = it.optString("worked_examples"),
      troubleshooting = it.optString("troubleshooting"),
      gradeDifferentiation = it.optString("grade_differentiation"),
      selfCheck = it.optString("self_check"),
      sketchNotes = it.optString("sketch_notes"),
      checkpointQuestions = it.optJSONArray("checkpoint_questions")?.strings() ?: emptyList()
    )
  }

  fun students(json: JSONObject): List<Student>? = json.optJSONArray("students")?.objects()?.map {
    Student(
      id = it.optString("id", it.optString("code")),
      studentId = it.optString("code"),
      fullName = it.optString("full_name"),
      schoolId = it.optString("school_id", it.optString("school_code")),
      schoolName = it.optString("school_name"),
      grade = it.optString("grade"),
      className = it.optString("class_name"),
      guardianName = it.optString("guardian_name"),
      guardianPhone = it.optString("guardian_phone"),
      attendancePresent = it.optInt("attendance_present"),
      attendanceLate = it.optInt("attendance_late"),
      attendanceAbsent = it.optInt("attendance_absent"),
      expoScore = it.optInt("expo_score"),
      isArchived = it.optBoolean("is_archived", false)
    )
  }

  fun payments(json: JSONObject): List<Payment>? = json.optJSONArray("payments")?.objects()?.map {
    Payment(
      id = it.optString("id", it.optString("receipt_number")),
      receiptNumber = it.optString("receipt_number"),
      studentId = it.optString("student_id", it.optString("student_code")),
      studentName = it.optString("student_name"),
      schoolId = it.optString("school_id", it.optString("school_code")),
      schoolName = it.optString("school_name"),
      grade = it.optString("grade"),
      month = it.optString("month"),
      amount = it.optDouble("amount", 0.0),
      currency = it.optString("currency", "USD"),
      paymentDate = it.optString("payment_date"),
      paymentMethod = it.optString("payment_method"),
      status = runCatching { PaymentStatus.valueOf(it.optString("status", "PENDING").uppercase()) }.getOrDefault(PaymentStatus.PENDING),
      notes = it.optString("notes")
    )
  }

  fun followUpSubmissions(json: JSONObject): List<FollowUpSubmission>? = json.optJSONArray("follow_ups")?.objects()?.map {
    val answers = HashMap<Int, Boolean>()
    it.optJSONObject("answers")?.let { a -> a.keys().forEach { k -> k.toIntOrNull()?.let { n -> answers[n] = a.optBoolean(k) } } }
    FollowUpSubmission(
      id = it.optString("id"),
      teacherId = it.optString("teacher_id"),
      teacherName = it.optString("teacher_name"),
      schoolId = it.optString("school_id", it.optString("school_code")),
      schoolName = it.optString("school_name"),
      grade = it.optString("grade"),
      weekNumber = it.optInt("week_number"),
      topic = it.optString("topic"),
      submissionDate = it.optString("submission_date"),
      answers = answers,
      coveredCount = it.optInt("covered_count"),
      notCoveredCount = it.optInt("not_covered_count"),
      coveragePercentage = it.optInt("coverage_percentage"),
      isSubmitted = it.optBoolean("is_submitted", true),
      reopenedByAdmin = it.optBoolean("reopened_by_admin", false)
    )
  }

  fun notifications(json: JSONObject): List<NotificationItem>? = json.optJSONArray("notifications")?.objects()?.map {
    NotificationItem(
      id = it.optString("id"),
      title = it.optString("title"),
      message = it.optString("message"),
      timestamp = it.optString("timestamp"),
      isRead = it.optBoolean("is_read", false),
      type = it.optString("type")
    )
  }

  fun auditLogs(json: JSONObject): List<AuditLogEntry>? = json.optJSONArray("audit_logs")?.objects()?.map {
    AuditLogEntry(
      id = it.optString("id"),
      user = it.optString("user"),
      action = it.optString("action"),
      record = it.optString("record"),
      details = it.optString("details"),
      timestamp = it.optString("timestamp")
    )
  }

  fun settings(json: JSONObject): SystemSettings? {
    val s = json.optJSONObject("settings") ?: return null
    return SystemSettings(
      organisationName = s.optString("organisation_name", "CREATVATE DELO MINDS"),
      currency = s.optString("currency", "USD"),
      monthlyFee = s.optDouble("monthly_fee", 10.0),
      academicYear = s.optString("academic_year", "2026"),
      programmeStartDate = s.optString("programme_start_date"),
      currentWeek = s.optInt("current_week", 1),
      receiptFooter = s.optString("receipt_footer"),
      appWebAddress = s.optString("app_web_address"),
      smtpHost = s.optString("smtp_host"),
      smtpPort = s.optInt("smtp_port", 587),
      smsWebhookUrl = s.optString("sms_webhook_url")
    )
  }

  /** The signed-in account, from the snapshot's "me" section, or null if the snapshot has none. */
  fun account(json: JSONObject): UserAccount? {
    val u = json.optJSONObject("me") ?: return null
    val role = when (u.optString("role")) {
      "super_admin" -> UserRole.SUPER_ADMIN
      "admin" -> UserRole.ADMIN
      "school_admin" -> UserRole.SCHOOL_ADMIN
      else -> UserRole.TEACHER
    }
    return UserAccount(
      id = u.optString("id", u.optString("email")),
      name = u.optString("name"),
      email = u.optString("email"),
      phone = u.optString("phone"),
      role = role,
      assignedSchoolIds = u.optJSONArray("assigned_school_ids")?.strings() ?: emptyList(),
      assignedGradeGroups = u.optJSONArray("assigned_grade_groups")?.strings() ?: emptyList(),
      temporaryPassword = u.optBoolean("must_change", false)
    )
  }

  /**
   * Server-confirmed records first, then any local record whose [key] isn't among them — so a
   * school/teacher/student/payment the admin just added while offline keeps showing up until the
   * next sync confirms it (at which point it arrives in [serverList] and the stand-in is dropped).
   */
  fun <T> mergeKeepingUnsynced(serverList: List<T>, localList: List<T>, key: (T) -> String): List<T> {
    val known = serverList.map(key).toHashSet()
    return serverList + localList.filter { key(it) !in known }
  }

  private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
  private fun JSONArray.strings(): List<String> = (0 until length()).map { optString(it) }
}
