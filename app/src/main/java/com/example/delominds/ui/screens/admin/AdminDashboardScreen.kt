package com.example.delominds.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.*
import com.example.delominds.ui.Screen
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
  schools: List<School>,
  teachers: List<Teacher>,
  curriculumParts: List<CurriculumPart>,
  followUpSubmissions: List<FollowUpSubmission>,
  scheduledLessons: List<ScheduledLesson>,
  onNavigate: (Screen) -> Unit,
  onSelectSchool: (String) -> Unit,
  onSelectCurriculumPart: (String) -> Unit
) {
  val totalSchools = schools.size
  val totalTeachers = teachers.size
  val totalPending = schools.sumOf { it.pendingFollowUps }
  val averageCoverage = if (schools.isNotEmpty()) schools.map { it.coveragePercent }.average().toInt() else 0

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // Top 4 Metric Cards (as shown in PDF Page 4)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      DashboardMetricCard(
        title = "Schools",
        value = "$totalSchools",
        modifier = Modifier.weight(1f).testTag("card_schools"),
        onClick = { onNavigate(Screen.ADMIN_SCHOOLS) }
      )
      DashboardMetricCard(
        title = "Teachers",
        value = "$totalTeachers",
        modifier = Modifier.weight(1f).testTag("card_teachers"),
        onClick = { onNavigate(Screen.ADMIN_TEACHERS) }
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      DashboardMetricCard(
        title = "Pending",
        value = "$totalPending",
        valueColor = DeloAmber,
        modifier = Modifier.weight(1f).testTag("card_pending"),
        onClick = { onNavigate(Screen.ADMIN_REPORTS) }
      )
      DashboardMetricCard(
        title = "Coverage",
        value = "$averageCoverage%",
        valueColor = DeloGreen,
        modifier = Modifier.weight(1f).testTag("card_coverage"),
        onClick = { onNavigate(Screen.ADMIN_CURRICULUM) }
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Curriculum Coverage Section (as shown on PDF Page 4)
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth().testTag("curriculum_coverage_section")
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Curriculum coverage",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DeloOnBackground
          )
          TextButton(onClick = { onNavigate(Screen.ADMIN_CURRICULUM) }) {
            Text("View All", color = DeloGold, fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        curriculumParts.forEach { part ->
          val barColor = Color(part.colorHex)
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectCurriculumPart(part.id) }
              .padding(vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = part.code,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DeloOnSurface
              )
              Text(
                text = "${part.coveragePercent}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Progress Bar
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(DeloSurfaceVariant)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(part.coveragePercent / 100f)
                  .fillMaxHeight()
                  .clip(RoundedCornerShape(5.dp))
                  .background(barColor)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Schools Needing Attention (Pending follow-ups first, per user guide)
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Schools needing attention",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DeloOnBackground
          )
          Icon(Icons.Default.WarningAmber, contentDescription = null, tint = DeloAmber)
        }

        Spacer(modifier = Modifier.height(12.dp))

        schools.sortedByDescending { it.pendingFollowUps }.forEach { school ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectSchool(school.id) }
              .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = school.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeloOnBackground
              )
              Text(
                text = "${school.location} • ${school.studentCount} students",
                fontSize = 12.sp,
                color = DeloOnSurfaceMuted
              )
            }
            Surface(
              color = if (school.pendingFollowUps > 2) DeloRedContainer else DeloAmberContainer,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (school.pendingFollowUps > 2) DeloRed else DeloAmber
              )
            ) {
              Text(
                text = "${school.pendingFollowUps} pending",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (school.pendingFollowUps > 2) DeloRed else DeloAmber,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Divider(color = DeloBorder)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Today's Timetable / Schedule
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Today's Schedule",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DeloOnBackground
          )
          Icon(Icons.Default.Schedule, contentDescription = null, tint = DeloGold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        scheduledLessons.forEach { lesson ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DeloGold)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${lesson.schoolName} (${lesson.grade})",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeloOnBackground
              )
              Text(
                text = "${lesson.time} • ${lesson.room} • ${lesson.teacherName}",
                fontSize = 11.sp,
                color = DeloOnSurfaceMuted
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Recent Follow-up Submissions
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Recent Follow-up Submissions",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = DeloOnBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        followUpSubmissions.take(4).forEach { sub ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${sub.teacherName} — ${sub.grade}, Wk ${sub.weekNumber}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DeloOnBackground
              )
              Text(
                text = "${sub.schoolName} • ${sub.submissionDate}",
                fontSize = 11.sp,
                color = DeloOnSurfaceMuted
              )
            }
            Surface(
              color = if (sub.coveragePercentage >= 80) DeloGreenContainer else DeloAmberContainer,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (sub.coveragePercentage >= 80) DeloGreen else DeloAmber
              )
            ) {
              Text(
                text = "${sub.coveragePercentage}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (sub.coveragePercentage >= 80) DeloGreen else DeloAmber,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Divider(color = DeloBorder)
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun DashboardMetricCard(
  title: String,
  value: String,
  valueColor: Color = DeloOnBackground,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    color = DeloSurface,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Text(
        text = title,
        fontSize = 12.sp,
        color = DeloOnSurfaceMuted,
        fontWeight = FontWeight.Normal
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        color = valueColor
      )
    }
  }
}
