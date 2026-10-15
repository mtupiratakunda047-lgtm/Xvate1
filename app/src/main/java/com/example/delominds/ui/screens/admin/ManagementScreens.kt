package com.example.delominds.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.*
import com.example.ui.theme.*

// -------------------------------------------------------------------------
// SCHOOLS SCREEN
// -------------------------------------------------------------------------
@Composable
fun SchoolsListScreen(
  schools: List<School>,
  onSelectSchool: (String) -> Unit,
  onAddSchool: (name: String, location: String, address: String, phone: String, email: String, principal: String, grades: List<String>) -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredSchools = schools.filter {
    it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Participating Schools", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
        Text("${schools.size} partner schools enrolled", fontSize = 12.sp, color = DeloOnSurfaceMuted)
      }

      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("add_school_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add School", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search schools...", color = DeloOnSurfaceMuted, fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeloGold) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DeloGold,
        unfocusedBorderColor = DeloBorder,
        focusedContainerColor = DeloSurface,
        unfocusedContainerColor = DeloSurface,
        focusedTextColor = DeloOnBackground,
        unfocusedTextColor = DeloOnBackground
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filteredSchools) { school ->
        Surface(
          color = DeloSurface,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectSchool(school.id) }
            .testTag("school_item_${school.id}")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeloSurfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AccountBalance, contentDescription = null, tint = DeloGold)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(school.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
              Text("${school.location} • ${school.principal}", fontSize = 12.sp, color = DeloOnSurfaceMuted)
              Text("${school.studentCount} students • ${school.teacherCount} teachers • ${school.gradesOffered.joinToString()}", fontSize = 11.sp, color = DeloGold, modifier = Modifier.padding(top = 2.dp))
            }

            Surface(
              color = DeloGreenContainer,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, DeloGreen)
            ) {
              Text("${school.coveragePercent}%", color = DeloGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddSchoolDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, loc, addr, ph, em, princ, grades ->
        onAddSchool(name, loc, addr, ph, em, princ, grades)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun AddSchoolDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, location: String, address: String, phone: String, email: String, principal: String, grades: List<String>) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }
  var principal by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+263 ") }
  var email by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add New School", color = DeloGold, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("School Name*") }, singleLine = true)
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location/City*") }, singleLine = true)
        OutlinedTextField(value = principal, onValueChange = { principal = it }, label = { Text("Principal / Head") }, singleLine = true)
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(name, location, "$location, Zimbabwe", phone, email, principal, listOf("Grade 1", "Grade 2", "Grade 6"))
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
      ) {
        Text("Create School")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = DeloOnSurfaceMuted) }
    },
    containerColor = DeloSurface
  )
}

// -------------------------------------------------------------------------
// TEACHERS SCREEN
// -------------------------------------------------------------------------
@Composable
fun TeachersListScreen(
  teachers: List<Teacher>,
  assignments: List<TeacherAssignment>,
  onSelectTeacher: (String) -> Unit,
  onAddTeacher: (name: String, email: String, phone: String, address: String) -> Unit
) {
  var showAddDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Robotics Teachers", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
        Text("${teachers.size} certified instructors", fontSize = 12.sp, color = DeloOnSurfaceMuted)
      }

      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("add_teacher_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Teacher", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(teachers) { teacher ->
        val teacherAssignments = assignments.filter { it.teacherId == teacher.id }
        Surface(
          color = DeloSurface,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectTeacher(teacher.id) }
            .testTag("teacher_item_${teacher.id}")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(DeloGold),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = teacher.name.take(1),
                fontWeight = FontWeight.Black,
                color = DeloGoldDark,
                fontSize = 18.sp
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(teacher.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
              Text(teacher.email, fontSize = 12.sp, color = DeloOnSurfaceMuted)
              Text(
                text = "${teacherAssignments.size} active school assignments • ${teacher.pendingFollowUps} pending follow-ups",
                fontSize = 11.sp,
                color = if (teacher.pendingFollowUps > 0) DeloAmber else DeloGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Surface(
              color = DeloGreenContainer,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, DeloGreen)
            ) {
              Text("${teacher.coveragePercent}%", color = DeloGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddTeacherDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, email, phone, addr ->
        onAddTeacher(name, email, phone, addr)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun AddTeacherDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, email: String, phone: String, address: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+263 77 ") }
  var address by remember { mutableStateOf("Harare, Zimbabwe") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Teacher", color = DeloGold, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name*") }, singleLine = true)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address*") }, singleLine = true)
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("City/Address") }, singleLine = true)
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && email.isNotBlank()) {
            onConfirm(name, email, phone, address)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
      ) {
        Text("Save Teacher")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = DeloOnSurfaceMuted) }
    },
    containerColor = DeloSurface
  )
}

// -------------------------------------------------------------------------
// TEACHER PROFILE DETAIL SCREEN (Section 9 in Master Prompt)
// -------------------------------------------------------------------------
@Composable
fun TeacherDetailScreen(
  teacherId: String,
  teachers: List<Teacher>,
  schools: List<School>,
  assignments: List<TeacherAssignment>,
  followUpSubmissions: List<FollowUpSubmission>,
  onAssignSchool: (schoolId: String, schoolName: String, grade: String, className: String) -> Unit,
  onNavigateBack: () -> Unit
) {
  val teacher = teachers.find { it.id == teacherId } ?: teachers.first()
  val teacherAssignments = assignments.filter { it.teacherId == teacherId }
  val submissions = followUpSubmissions.filter { it.teacherId == teacherId }

  var showAssignDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // Teacher Header Card
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(DeloGold),
            contentAlignment = Alignment.Center
          ) {
            Text(teacher.name.take(1), fontSize = 24.sp, fontWeight = FontWeight.Black, color = DeloGoldDark)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(teacher.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
            Text("Teacher ID: ${teacher.id} • ${teacher.status}", fontSize = 12.sp, color = DeloGold)
            Text("${teacher.email} • ${teacher.phone}", fontSize = 12.sp, color = DeloOnSurfaceMuted)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Assigned Schools & Classes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
      TextButton(onClick = { showAssignDialog = true }) {
        Icon(Icons.Default.Add, contentDescription = null, tint = DeloGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Assignment", color = DeloGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Drilldown by School and Grade (as requested in Prompt Section 9)
    teacherAssignments.forEach { assignment ->
      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(assignment.schoolName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
              Text("${assignment.grade} (${assignment.className}) • Academic Year ${assignment.academicYear}", fontSize = 12.sp, color = DeloOnSurfaceMuted)
            }
            Surface(
              color = DeloGreenContainer,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text("${assignment.coveragePercent}%", color = DeloGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text("Weekly Curriculum Progress:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeloGold)

          Spacer(modifier = Modifier.height(6.dp))

          // Weekly Progress Chips W1..W8
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (1..8).forEach { weekNum ->
              val cov = assignment.weeklyCoverage[weekNum]
              val chipColor = when {
                cov == null -> DeloSurfaceVariant
                cov >= 80 -> DeloGreenContainer
                else -> DeloAmberContainer
              }
              val textColor = when {
                cov == null -> DeloOnSurfaceMuted
                cov >= 80 -> DeloGreen
                else -> DeloAmber
              }
              Surface(
                color = chipColor,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text("W$weekNum", fontSize = 10.sp, color = DeloOnSurfaceMuted)
                  Text(
                    text = if (cov != null) "$cov%" else "-",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text("Follow-up Submission History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)

    Spacer(modifier = Modifier.height(10.dp))

    submissions.forEach { sub ->
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
            Text("${sub.grade}, Week ${sub.weekNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
            Text(sub.topic, fontSize = 12.sp, color = DeloOnSurfaceMuted)
            Text("Submitted: ${sub.submissionDate}", fontSize = 11.sp, color = DeloGold)
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("${sub.coveragePercentage}%", fontSize = 15.sp, fontWeight = FontWeight.Black, color = if (sub.coveragePercentage >= 80) DeloGreen else DeloAmber)
            Text("${sub.coveredCount}✓ / ${sub.notCoveredCount}✕", fontSize = 11.sp, color = DeloOnSurfaceMuted)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }

  if (showAssignDialog) {
    var selectedSchool by remember { mutableStateOf(schools.firstOrNull()?.id ?: "") }
    var selectedGrade by remember { mutableStateOf("Grade 6") }
    var className by remember { mutableStateOf("Grade 6 Blue") }

    AlertDialog(
      onDismissRequest = { showAssignDialog = false },
      title = { Text("Assign Teacher to School", color = DeloGold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Select School:", fontSize = 12.sp, color = DeloOnSurfaceMuted)
          schools.forEach { sch ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedSchool = sch.id }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(selected = selectedSchool == sch.id, onClick = { selectedSchool = sch.id })
              Text(sch.name, fontSize = 13.sp, color = DeloOnSurface)
            }
          }
          OutlinedTextField(value = selectedGrade, onValueChange = { selectedGrade = it }, label = { Text("Grade") }, singleLine = true)
          OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name") }, singleLine = true)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val sch = schools.find { it.id == selectedSchool } ?: schools.first()
            onAssignSchool(sch.id, sch.name, selectedGrade, className)
            showAssignDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) {
          Text("Assign")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAssignDialog = false }) { Text("Cancel", color = DeloOnSurfaceMuted) }
      },
      containerColor = DeloSurface
    )
  }
}

// -------------------------------------------------------------------------
// STUDENTS SCREEN
// -------------------------------------------------------------------------
@Composable
fun StudentsListScreen(
  students: List<Student>,
  schools: List<School>,
  onAddStudent: (name: String, schoolId: String, schoolName: String, grade: String, className: String, guardian: String, phone: String) -> Unit,
  onOpenExpoScore: (Student) -> Unit,
  onImportCsv: ((String) -> Unit)? = null
) {
  var searchQuery by remember { mutableStateOf("") }
  var showAddDialog by remember { mutableStateOf(false) }
  var showCsvDialog by remember { mutableStateOf(false) }

  val filtered = students.filter {
    it.fullName.contains(searchQuery, ignoreCase = true) || it.studentId.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Enrolled Students", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
        Text("${students.size} registered learners", fontSize = 12.sp, color = DeloOnSurfaceMuted)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onImportCsv != null) {
          OutlinedButton(
            onClick = { showCsvDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeloGold),
            border = androidx.compose.foundation.BorderStroke(1.dp, DeloGold),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("import_csv_button")
          ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Import CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        Button(
          onClick = { showAddDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("add_student_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Student", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search by student name or ID...", color = DeloOnSurfaceMuted, fontSize = 13.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeloGold) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DeloGold,
        unfocusedBorderColor = DeloBorder,
        focusedContainerColor = DeloSurface,
        unfocusedContainerColor = DeloSurface,
        focusedTextColor = DeloOnBackground,
        unfocusedTextColor = DeloOnBackground
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filtered) { student ->
        Surface(
          color = DeloSurface,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  color = DeloSurfaceVariant,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(student.studentId, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeloGold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(student.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("${student.schoolName} • ${student.grade} (${student.className})", fontSize = 12.sp, color = DeloOnSurfaceMuted)
              Text("Guardian: ${student.guardianName} (${student.guardianPhone})", fontSize = 11.sp, color = DeloOnSurfaceMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
              OutlinedButton(
                onClick = { onOpenExpoScore(student) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeloGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeloGold),
                modifier = Modifier.testTag("score_expo_${student.studentId}")
              ) {
                Text(
                  text = if (student.expoScore > 0) "Expo: ${student.expoScore}/24" else "Score Expo",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    var name by remember { mutableStateOf("") }
    var selectedSchoolId by remember { mutableStateOf(schools.firstOrNull()?.id ?: "") }
    var grade by remember { mutableStateOf("Grade 1") }
    var className by remember { mutableStateOf("Grade 1 Eagles") }
    var guardian by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+263 77 ") }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Enroll Student", color = DeloGold, fontWeight = FontWeight.Bold) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name*") }, singleLine = true)
          OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade*") }, singleLine = true)
          OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name") }, singleLine = true)
          OutlinedTextField(value = guardian, onValueChange = { guardian = it }, label = { Text("Parent/Guardian") }, singleLine = true)
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Parent Phone") }, singleLine = true)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              val sch = schools.find { it.id == selectedSchoolId } ?: schools.first()
              onAddStudent(name, sch.id, sch.name, grade, className, guardian, phone)
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) {
          Text("Enroll")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = DeloOnSurfaceMuted) }
      },
      containerColor = DeloSurface
    )
  }

  if (showCsvDialog && onImportCsv != null) {
    var csvText by remember {
      mutableStateOf(
        "full_name, school, grade, class_name, guardian_name, guardian_phone\n" +
            "Simbarashe Chitepo, Riverside Primary, Grade 6, Grade 6 Red, Brian Chitepo, +263 77 123 9988\n" +
            "Anesu Mutasa, Riverside Primary, Grade 6, Grade 6 Red, Mary Mutasa, +263 77 444 8877"
      )
    }

    AlertDialog(
      onDismissRequest = { showCsvDialog = false },
      title = { Text("Import Students (CSV)", color = DeloGold, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Format: full_name, school, grade, class_name, guardian_name, guardian_phone",
            fontSize = 11.sp,
            color = DeloOnSurfaceMuted
          )
          OutlinedTextField(
            value = csvText,
            onValueChange = { csvText = it },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            placeholder = { Text("Paste CSV records here...") }
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onImportCsv(csvText)
            showCsvDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) {
          Text("Import Records")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCsvDialog = false }) { Text("Cancel", color = DeloOnSurfaceMuted) }
      },
      containerColor = DeloSurface
    )
  }
}
