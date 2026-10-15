package com.example.delominds.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.AttendanceStatus
import com.example.delominds.data.model.Student
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

@Composable
fun AttendanceScreen(
  students: List<Student>,
  onSaveAttendance: (schoolId: String, schoolName: String, grade: String, date: String, statusMap: Map<String, AttendanceStatus>) -> Unit
) {
  val selectedDate by remember { mutableStateOf("13 Oct 2026") }
  val schoolName by remember { mutableStateOf("Riverside Primary, Grade 1") }

  // Student attendance marks map
  val attendanceMap = remember {
    mutableStateMapOf<String, AttendanceStatus>().apply {
      students.forEach { put(it.id, AttendanceStatus.PRESENT) }
    }
  }

  var savedSuccess by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Text(
      text = "Riverside Primary, Grade 1",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = DeloOnBackground
    )
    Text(
      text = "Date: $selectedDate",
      fontSize = 13.sp,
      color = DeloGold,
      modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
    )

    if (savedSuccess) {
      Surface(
        color = DeloGreenContainer,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloGreen),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Check, contentDescription = null, tint = DeloGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Attendance recorded and saved successfully!", color = DeloGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(students) { student ->
        val currentStatus = attendanceMap[student.id] ?: AttendanceStatus.PRESENT

        Surface(
          color = DeloSurface,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
          modifier = Modifier.fillMaxWidth().testTag("attendance_student_${student.studentId}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(student.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
              Text(student.studentId, fontSize = 11.sp, color = DeloOnSurfaceMuted, modifier = Modifier.padding(top = 2.dp))
            }

            // Present / Late / Absent chips (matching PDF Page 6)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              StatusSelectChip(
                label = "Present",
                isSelected = currentStatus == AttendanceStatus.PRESENT,
                activeColor = DeloGreen,
                onClick = { attendanceMap[student.id] = AttendanceStatus.PRESENT }
              )

              StatusSelectChip(
                label = "Late",
                isSelected = currentStatus == AttendanceStatus.LATE,
                activeColor = DeloAmber,
                onClick = { attendanceMap[student.id] = AttendanceStatus.LATE }
              )

              StatusSelectChip(
                label = "Absent",
                isSelected = currentStatus == AttendanceStatus.ABSENT,
                activeColor = DeloRed,
                onClick = { attendanceMap[student.id] = AttendanceStatus.ABSENT }
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Save attendance Button (matching PDF Page 6)
    Button(
      onClick = {
        onSaveAttendance(
          "SCH-001",
          "Riverside Primary School",
          "Grade 1",
          selectedDate,
          attendanceMap.toMap()
        )
        savedSuccess = true
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("save_attendance_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = DeloGold,
        contentColor = DeloGoldDark
      )
    ) {
      Text(
        text = "Save attendance",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun StatusSelectChip(
  label: String,
  isSelected: Boolean,
  activeColor: Color,
  onClick: () -> Unit
) {
  Surface(
    color = if (isSelected) activeColor.copy(alpha = 0.25f) else DeloSurfaceVariant,
    shape = RoundedCornerShape(8.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isSelected) activeColor else DeloBorder
    ),
    modifier = Modifier
      .clickable { onClick() }
  ) {
    Text(
      text = label,
      color = if (isSelected) activeColor else DeloOnSurfaceMuted,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
  }
}
