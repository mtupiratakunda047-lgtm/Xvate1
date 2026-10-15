package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.delominds.data.model.Student
import com.example.delominds.data.model.UserRole
import com.example.delominds.ui.DeloMindsViewModel
import com.example.delominds.ui.Screen
import com.example.delominds.ui.components.AdminChipNavBar
import com.example.delominds.ui.components.DeloTopBar
import com.example.delominds.ui.components.TeacherBottomBar
import com.example.delominds.ui.screens.admin.*
import com.example.delominds.ui.screens.attendance.AttendanceScreen
import com.example.delominds.ui.screens.auth.SignInScreen
import com.example.delominds.ui.screens.auth.ServerSignInScreen
import com.example.delominds.data.sync.ServerClient
import com.example.delominds.data.sync.OfflineStore
import com.example.delominds.data.sync.SyncEngine
import com.example.delominds.data.sync.SyncWorker
import com.example.delominds.data.sync.ServerBridge
import com.example.delominds.data.sync.ServerException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.example.delominds.ui.screens.curriculum.AdminCurriculumScreen
import com.example.delominds.ui.screens.curriculum.CurriculumWeekDetailScreen
import com.example.delominds.ui.screens.expo.ExpoRubricScreen
import com.example.delominds.ui.screens.payments.PaymentsListScreen
import com.example.delominds.ui.screens.payments.ReceiptDetailScreen
import com.example.delominds.ui.screens.reports.AuditLogScreen
import com.example.delominds.ui.screens.reports.ReportsScreen
import com.example.delominds.ui.screens.reports.SettingsScreen
import com.example.delominds.ui.screens.teacher.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        DeloMindsApp()
      }
    }
  }
}

@Composable
fun DeloMindsApp(viewModel: DeloMindsViewModel = viewModel()) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val appContext = androidx.compose.ui.platform.LocalContext.current
  val serverClient = remember { ServerClient(appContext) }
  val syncEngine = remember { SyncEngine(serverClient, OfflineStore(appContext)) }
  val serverBridge = remember { ServerBridge(OfflineStore(appContext), syncEngine) { serverClient.isSignedIn() } }
  val scope = rememberCoroutineScope()
  LaunchedEffect(serverBridge) { viewModel.serverBridge = serverBridge }
  var showServerSignIn by remember { mutableStateOf(false) }

  // When the app opens for a signed-in teacher or admin: show whatever was cached last (works
  // offline), then try to reach the server, send any queued changes, and refresh the cache.
  // Only a phone with an active session is ever navigated past Screen.SIGN_IN here, so someone
  // who has never signed in always lands on the real sign-in screen, never a sample dashboard.
  LaunchedEffect(Unit) {
    if (serverClient.isSignedIn()) {
      syncEngine.cachedSnapshot()?.let { viewModel.applySnapshot(it) }
      val report = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { syncEngine.syncNow() }
      if (report.online) syncEngine.cachedSnapshot()?.let { viewModel.applySnapshot(it) }
      if (report.needsSignIn) {
        // The session has expired: forget it on this phone and stay on Screen.SIGN_IN (the
        // default) so the teacher signs in again rather than seeing a stale/empty dashboard.
        serverClient.forgetSession()
        showServerSignIn = false
      } else {
        val role = syncEngine.cachedRole()
        viewModel.navigateTo(if (role == null || role == "teacher") Screen.TEACHER_HOME else Screen.ADMIN_DASHBOARD)
      }
    }
    // Background sync: every 15 minutes, only when the phone is connected.
    val work = androidx.work.PeriodicWorkRequestBuilder<SyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
      .setConstraints(androidx.work.Constraints.Builder().setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build())
      .build()
    androidx.work.WorkManager.getInstance(appContext)
      .enqueueUniquePeriodicWork("delominds-sync", androidx.work.ExistingPeriodicWorkPolicy.KEEP, work)
  }
  val currentUser by viewModel.currentUser.collectAsState()
  val schools by viewModel.schools.collectAsState()
  val teachers by viewModel.teachers.collectAsState()
  val assignments by viewModel.assignments.collectAsState()
  val curriculumParts by viewModel.curriculumParts.collectAsState()
  val curriculumWeeks by viewModel.curriculumWeeks.collectAsState()
  val followUpSubmissions by viewModel.followUpSubmissions.collectAsState()
  val students by viewModel.students.collectAsState()
  val payments by viewModel.payments.collectAsState()
  val expoCriteria by viewModel.expoCriteria.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()
  val settings by viewModel.systemSettings.collectAsState()
  val scheduledLessons by viewModel.scheduledLessons.collectAsState()

  val selectedSchoolId by viewModel.selectedSchoolId.collectAsState()
  val selectedTeacherId by viewModel.selectedTeacherId.collectAsState()
  val selectedPartId by viewModel.selectedPartId.collectAsState()
  val selectedWeekNumber by viewModel.selectedWeekNumber.collectAsState()
  val selectedReceipt by viewModel.selectedReceipt.collectAsState()
  val activeFollowUp by viewModel.activeFollowUp.collectAsState()
  val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
  val userMessage by viewModel.userMessage.collectAsState()

  var studentForExpo by remember { mutableStateOf<Student?>(null) }

  // Sends any still-queued changes, then ends the session on the server and forgets it on this
  // phone, so a later launch shows Screen.SIGN_IN instead of silently resuming as this person.
  val signOutOfServer: () -> Unit = {
    scope.launch {
      withContext(Dispatchers.IO) {
        try { syncEngine.flush() } catch (e: Exception) { /* best-effort: sign out continues regardless */ }
        try { serverClient.signOut() } catch (e: Exception) { serverClient.forgetSession() }
      }
      viewModel.signOut()
    }
  }

  val unreadNotifications = notifications.count { !it.isRead }

  // System snackbar / toast feedback
  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  // BackHandler logic
  BackHandler(enabled = currentScreen != Screen.SIGN_IN && currentScreen != Screen.ADMIN_DASHBOARD && currentScreen != Screen.TEACHER_HOME) {
    when (currentScreen) {
      Screen.ADMIN_CURRICULUM_DETAIL -> viewModel.navigateTo(Screen.ADMIN_CURRICULUM)
      Screen.ADMIN_SCHOOL_DETAIL -> viewModel.navigateTo(Screen.ADMIN_SCHOOLS)
      Screen.ADMIN_TEACHER_DETAIL -> viewModel.navigateTo(Screen.ADMIN_TEACHERS)
      Screen.ADMIN_RECEIPT_DETAIL -> viewModel.navigateTo(Screen.ADMIN_PAYMENTS)
      Screen.TEACHER_FOLLOW_UP_QUESTION -> viewModel.navigateTo(Screen.TEACHER_HOME)
      Screen.TEACHER_FOLLOW_UP_REVIEW -> viewModel.navigateTo(Screen.TEACHER_FOLLOW_UP_QUESTION)
      Screen.TEACHER_FOLLOW_UP_SUCCESS -> viewModel.navigateTo(Screen.TEACHER_HOME)
      Screen.TEACHER_LESSON_DETAIL -> viewModel.navigateTo(Screen.TEACHER_LESSONS)
      Screen.ADMIN_EXPO_RUBRIC -> {
        if (currentUser.role == UserRole.TEACHER) viewModel.navigateTo(Screen.TEACHER_HOME)
        else viewModel.navigateTo(Screen.ADMIN_STUDENTS)
      }
      else -> {
        if (currentUser.role == UserRole.TEACHER) {
          viewModel.navigateTo(Screen.TEACHER_HOME)
        } else {
          viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
        }
      }
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (currentScreen != Screen.SIGN_IN) {
        val canGoBack = currentScreen != Screen.ADMIN_DASHBOARD && currentScreen != Screen.TEACHER_HOME
        Column {
          DeloTopBar(
            currentScreen = currentScreen,
            currentUser = currentUser,
            unreadNotifications = unreadNotifications,
            availableAccounts = viewModel.availableAccounts,
            onSwitchRole = { viewModel.switchRole(it) },
            onOpenNotifications = { viewModel.toggleNotificationsDialog(true) },
            onSignOut = signOutOfServer,
            onNavigateBack = if (canGoBack) {
              {
                when (currentScreen) {
                  Screen.ADMIN_CURRICULUM_DETAIL -> viewModel.navigateTo(Screen.ADMIN_CURRICULUM)
                  Screen.ADMIN_SCHOOL_DETAIL -> viewModel.navigateTo(Screen.ADMIN_SCHOOLS)
                  Screen.ADMIN_TEACHER_DETAIL -> viewModel.navigateTo(Screen.ADMIN_TEACHERS)
                  Screen.ADMIN_RECEIPT_DETAIL -> viewModel.navigateTo(Screen.ADMIN_PAYMENTS)
                  Screen.TEACHER_FOLLOW_UP_QUESTION -> viewModel.navigateTo(Screen.TEACHER_HOME)
                  Screen.TEACHER_FOLLOW_UP_REVIEW -> viewModel.navigateTo(Screen.TEACHER_FOLLOW_UP_QUESTION)
                  Screen.TEACHER_FOLLOW_UP_SUCCESS -> viewModel.navigateTo(Screen.TEACHER_HOME)
                  Screen.TEACHER_LESSON_DETAIL -> viewModel.navigateTo(Screen.TEACHER_LESSONS)
                  else -> {
                    if (currentUser.role == UserRole.TEACHER) viewModel.navigateTo(Screen.TEACHER_HOME)
                    else viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
                  }
                }
              }
            } else null
          )

          // Admin Chip Sub-Navigation
          if (currentUser.role != UserRole.TEACHER) {
            AdminChipNavBar(
              currentScreen = currentScreen,
              onNavigate = { viewModel.navigateTo(it) }
            )
          }
        }
      }
    },
    bottomBar = {
      if (currentScreen != Screen.SIGN_IN && currentUser.role == UserRole.TEACHER) {
        TeacherBottomBar(
          currentScreen = currentScreen,
          onNavigate = { viewModel.navigateTo(it) }
        )
      }
    },
    containerColor = DeloBackground,
    modifier = Modifier.fillMaxSize().testTag("delo_minds_root")
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        Screen.SIGN_IN -> {
          if (showServerSignIn) {
            ServerSignInScreen(
              client = serverClient,
              onSignedIn = { role ->
                showServerSignIn = false
                scope.launch {
                  withContext(Dispatchers.IO) { syncEngine.syncNow() }
                  syncEngine.cachedSnapshot()?.let { viewModel.applySnapshot(it) }
                  viewModel.navigateTo(if (role == "teacher") Screen.TEACHER_HOME else Screen.ADMIN_DASHBOARD)
                }
              },
              onBack = { showServerSignIn = false }
            )
          } else {
            SignInScreen(
              availableAccounts = viewModel.availableAccounts,
              onSignIn = { _, _ -> false }, // unused: password sign-in goes to the server below
              onQuickRoleSelect = { }, // demo one-tap sign-in removed
              onServerSignIn = { showServerSignIn = true },
              onServerPasswordSignIn = { email, password, onResult ->
                scope.launch {
                  val error: String? = withContext(Dispatchers.IO) {
                    try {
                      serverClient.signInWithPassword(email, password)
                      if (serverClient.currentAccount().optBoolean("must_change"))
                        "Your password must be changed first. Use 'Sign in with a code' to choose your own."
                      else null
                    } catch (e: ServerException) {
                      e.message
                    } catch (e: IOException) {
                      "Can't reach the server. Check the address and your connection."
                    }
                  }
                  if (error == null) {
                    val role = withContext(Dispatchers.IO) {
                      val r = serverClient.currentRole()
                      syncEngine.syncNow()
                      r
                    }
                    syncEngine.cachedSnapshot()?.let { viewModel.applySnapshot(it) }
                    viewModel.navigateTo(if (role == "teacher") Screen.TEACHER_HOME else Screen.ADMIN_DASHBOARD)
                  }
                  onResult(error)
                }
              }
            )
          }
        }

        Screen.ADMIN_DASHBOARD -> {
          AdminDashboardScreen(
            schools = schools,
            teachers = teachers,
            curriculumParts = curriculumParts,
            followUpSubmissions = followUpSubmissions,
            scheduledLessons = scheduledLessons,
            onNavigate = { viewModel.navigateTo(it) },
            onSelectSchool = { viewModel.selectSchool(it) },
            onSelectCurriculumPart = {
              viewModel.selectCurriculumWeek(it, 8)
            }
          )
        }

        Screen.ADMIN_CURRICULUM -> {
          AdminCurriculumScreen(
            curriculumParts = curriculumParts,
            curriculumWeeks = curriculumWeeks,
            onSelectWeek = { partId, weekNum ->
              viewModel.selectCurriculumWeek(partId, weekNum)
            },
            onAddCheckpoint = { partId, weekNum, label, q ->
              viewModel.addFollowUpCheckpoint(partId, weekNum, label, q)
            }
          )
        }

        Screen.ADMIN_CURRICULUM_DETAIL -> {
          CurriculumWeekDetailScreen(
            partId = selectedPartId ?: "part_d",
            weekNumber = selectedWeekNumber,
            curriculumWeeks = curriculumWeeks,
            curriculumParts = curriculumParts,
            isTeacherView = false,
            onNavigateBack = { viewModel.navigateTo(Screen.ADMIN_CURRICULUM) }
          )
        }

        Screen.ADMIN_SCHOOLS -> {
          SchoolsListScreen(
            schools = schools,
            onSelectSchool = { viewModel.selectSchool(it) },
            onAddSchool = { name, loc, addr, ph, em, princ, gr ->
              viewModel.addSchool(name, loc, addr, ph, em, princ, gr)
            }
          )
        }

        Screen.ADMIN_SCHOOL_DETAIL -> {
          val school = schools.find { it.id == selectedSchoolId } ?: schools.first()
          val schoolAssignments = assignments.filter { it.schoolId == school.id }
          Column(
            modifier = Modifier
              .fillMaxSize()
              .background(DeloBackground)
              .padding(16.dp)
          ) {
            Surface(
              color = DeloSurface,
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(school.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
                Text("${school.location} • ${school.address}", fontSize = 13.sp, color = DeloOnSurfaceMuted)
                Text("Principal: ${school.principal} • Phone: ${school.phone}", fontSize = 12.sp, color = DeloGold, modifier = Modifier.padding(top = 4.dp))
                Divider(color = DeloBorder, modifier = Modifier.padding(vertical = 12.dp))
                Text("Grades Offered: ${school.gradesOffered.joinToString()}", fontSize = 13.sp, color = DeloOnSurface)
                Text("Enrolled Students: ${school.studentCount} • Curriculum Coverage: ${school.coveragePercent}%", fontSize = 13.sp, color = DeloGreen, fontWeight = FontWeight.SemiBold)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Assigned Robotics Instructors:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)

            Spacer(modifier = Modifier.height(10.dp))

            schoolAssignments.forEach { asn ->
              Surface(
                color = DeloSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(asn.teacherName, fontWeight = FontWeight.Bold, color = DeloOnBackground)
                    Text("${asn.grade} (${asn.className})", fontSize = 12.sp, color = DeloOnSurfaceMuted)
                  }
                  Text("${asn.coveragePercent}% coverage", color = DeloGreen, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        Screen.ADMIN_TEACHERS -> {
          TeachersListScreen(
            teachers = teachers,
            assignments = assignments,
            onSelectTeacher = { viewModel.selectTeacher(it) },
            onAddTeacher = { name, em, ph, addr ->
              viewModel.addTeacher(name, em, ph, addr)
            }
          )
        }

        Screen.ADMIN_TEACHER_DETAIL -> {
          TeacherDetailScreen(
            teacherId = selectedTeacherId ?: teachers.first().id,
            teachers = teachers,
            schools = schools,
            assignments = assignments,
            followUpSubmissions = followUpSubmissions,
            onAssignSchool = { schId, schName, grade, cls ->
              viewModel.assignTeacher(
                teacherId = selectedTeacherId ?: teachers.first().id,
                teacherName = teachers.find { it.id == selectedTeacherId }?.name ?: "Teacher",
                schoolId = schId,
                schoolName = schName,
                gradeGroupId = "part_d",
                grade = grade,
                className = cls
              )
            },
            onNavigateBack = { viewModel.navigateTo(Screen.ADMIN_TEACHERS) }
          )
        }

        Screen.ADMIN_STUDENTS -> {
          StudentsListScreen(
            students = students,
            schools = schools,
            onAddStudent = { name, schId, schName, gr, cls, gd, ph ->
              viewModel.addStudent(name, schId, schName, gr, cls, gd, ph)
            },
            onOpenExpoScore = { stu ->
              studentForExpo = stu
              viewModel.navigateTo(Screen.ADMIN_EXPO_RUBRIC)
            },
            onImportCsv = { csv ->
              viewModel.importStudentsCsv(csv)
            }
          )
        }

        Screen.ADMIN_PAYMENTS -> {
          PaymentsListScreen(
            payments = payments,
            students = students,
            onOpenReceipt = { viewModel.openReceipt(it) },
            onRecordPayment = { sId, sName, schId, schName, gr, m, amt, meth, st, notes ->
              viewModel.recordPayment(sId, sName, schId, schName, gr, m, amt, meth, st, notes)
            }
          )
        }

        Screen.ADMIN_RECEIPT_DETAIL -> {
          val receipt = selectedReceipt ?: payments.first()
          ReceiptDetailScreen(
            payment = receipt,
            onNavigateBack = { viewModel.navigateTo(Screen.ADMIN_PAYMENTS) }
          )
        }

        Screen.ADMIN_ATTENDANCE -> {
          AttendanceScreen(
            students = students,
            onSaveAttendance = { schId, schName, gr, dt, marks ->
              viewModel.saveAttendance(schId, schName, gr, dt, marks)
            }
          )
        }

        Screen.ADMIN_EXPO_RUBRIC -> {
          ExpoRubricScreen(
            student = studentForExpo,
            criteria = expoCriteria,
            onSaveScore = { sId, sName, proj, term, sc, notes ->
              viewModel.scoreExpoProject(sId, sName, proj, term, sc, notes)
            },
            onNavigateBack = {
              if (currentUser.role == UserRole.TEACHER) viewModel.navigateTo(Screen.TEACHER_HOME)
              else viewModel.navigateTo(Screen.ADMIN_STUDENTS)
            }
          )
        }

        Screen.ADMIN_REPORTS -> {
          ReportsScreen(
            teachers = teachers,
            onSelectTeacher = { viewModel.selectTeacher(it) }
          )
        }

        Screen.ADMIN_SETTINGS -> {
          SettingsScreen(
            settings = settings,
            onSaveSettings = { viewModel.updateSettings(it) }
          )
        }

        Screen.ADMIN_AUDIT_LOG -> {
          AuditLogScreen(auditLogs = auditLogs)
        }

        // Teacher Screens
        Screen.TEACHER_HOME -> {
          TeacherHomeScreen(
            currentUser = currentUser,
            assignments = assignments.filter { it.teacherId == currentUser.id },
            scheduledLessons = scheduledLessons,
            onStartFollowUp = { schId, schName, grade, weekNum ->
              viewModel.startFollowUp(schId, schName, grade, weekNum)
            },
            onNavigate = { viewModel.navigateTo(it) }
          )
        }

        Screen.TEACHER_LESSONS -> {
          AdminCurriculumScreen(
            curriculumParts = curriculumParts,
            curriculumWeeks = curriculumWeeks,
            onSelectWeek = { partId, weekNum ->
              viewModel.selectCurriculumWeek(partId, weekNum, fromTeacher = true)
            }
          )
        }

        Screen.TEACHER_LESSON_DETAIL -> {
          CurriculumWeekDetailScreen(
            partId = selectedPartId ?: "part_d",
            weekNumber = selectedWeekNumber,
            curriculumWeeks = curriculumWeeks,
            curriculumParts = curriculumParts,
            isTeacherView = true,
            onStartFollowUp = {
              viewModel.startFollowUp(
                schoolId = "SCH-001",
                schoolName = "Riverside Primary School",
                grade = "Grade 6",
                weekNumber = selectedWeekNumber
              )
            },
            onNavigateBack = { viewModel.navigateTo(Screen.TEACHER_LESSONS) }
          )
        }

        Screen.TEACHER_FOLLOW_UP_QUESTION -> {
          val session = activeFollowUp
          if (session != null) {
            TeacherFollowUpQuestionScreen(
              session = session,
              onAnswerQuestion = { idx, covered -> viewModel.answerQuestion(idx, covered) },
              onNavigateBack = { viewModel.navigateTo(Screen.TEACHER_HOME) }
            )
          } else {
            viewModel.navigateTo(Screen.TEACHER_HOME)
          }
        }

        Screen.TEACHER_FOLLOW_UP_REVIEW -> {
          val session = activeFollowUp
          if (session != null) {
            TeacherFollowUpReviewScreen(
              session = session,
              onToggleAnswer = { viewModel.toggleAnswerInReview(it) },
              onSubmit = { viewModel.submitFollowUpSession() },
              onBackToQuestions = { viewModel.navigateTo(Screen.TEACHER_FOLLOW_UP_QUESTION) }
            )
          } else {
            viewModel.navigateTo(Screen.TEACHER_HOME)
          }
        }

        Screen.TEACHER_FOLLOW_UP_SUCCESS -> {
          val session = activeFollowUp
          if (session != null) {
            TeacherFollowUpSuccessScreen(
              session = session,
              onBackToDashboard = { viewModel.navigateTo(Screen.TEACHER_HOME) }
            )
          } else {
            viewModel.navigateTo(Screen.TEACHER_HOME)
          }
        }

        Screen.TEACHER_ATTENDANCE -> {
          AttendanceScreen(
            students = students,
            onSaveAttendance = { schId, schName, gr, dt, marks ->
              viewModel.saveAttendance(schId, schName, gr, dt, marks)
            }
          )
        }

        Screen.TEACHER_PROGRESS -> {
          TeacherProgressScreen(
            assignments = assignments.filter { it.teacherId == currentUser.id },
            onSelectWeek = { weekNum ->
              viewModel.selectCurriculumWeek("part_d", weekNum, fromTeacher = true)
            }
          )
        }

        Screen.TEACHER_MORE -> {
          TeacherMoreScreen(
            currentUser = currentUser,
            onNavigate = { viewModel.navigateTo(it) },
            onSignOut = signOutOfServer
          )
        }
      }
    }
  }

  // Notification Dialog
  if (showNotificationsDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.toggleNotificationsDialog(false) },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Notifications, contentDescription = null, tint = DeloGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Notifications", color = DeloGold, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          notifications.forEach { notif ->
            Surface(
              color = DeloSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeloOnBackground)
                  Text(notif.timestamp, fontSize = 10.sp, color = DeloGold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(notif.message, fontSize = 12.sp, color = DeloOnSurface)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.toggleNotificationsDialog(false) },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) {
          Text("Close")
        }
      },
      containerColor = DeloSurface
    )
  }
}
