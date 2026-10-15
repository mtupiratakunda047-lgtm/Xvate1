package com.example.delominds.ui.screens.teacher

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.*
import com.example.delominds.ui.Screen
import com.example.ui.theme.*

@Composable
fun TeacherHomeScreen(
  currentUser: UserAccount,
  assignments: List<TeacherAssignment>,
  scheduledLessons: List<ScheduledLesson>,
  onStartFollowUp: (schoolId: String, schoolName: String, grade: String, weekNumber: Int) -> Unit,
  onNavigate: (Screen) -> Unit
) {
  val pendingFollowUpsCount = 2
  val overallCoverage = 83 // As shown in PDF Page 6

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // Greeting as shown on PDF Page 6
    Text(
      text = "Hello, ${currentUser.name.split(" ").first()}",
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = DeloOnBackground
    )
    Text(
      text = "Current week: Week 8",
      fontSize = 14.sp,
      color = DeloOnSurfaceMuted,
      modifier = Modifier.padding(top = 4.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    // 2 Metric cards: Pending (2) and Coverage (83%) as shown on PDF Page 6
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier
          .weight(1f)
          .clickable { onNavigate(Screen.TEACHER_PROGRESS) }
          .testTag("teacher_card_pending")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Pending", fontSize = 12.sp, color = DeloOnSurfaceMuted)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "$pendingFollowUpsCount",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = DeloAmber
          )
        }
      }

      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier
          .weight(1f)
          .clickable { onNavigate(Screen.TEACHER_PROGRESS) }
          .testTag("teacher_card_coverage")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Coverage", fontSize = 12.sp, color = DeloOnSurfaceMuted)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "$overallCoverage%",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = DeloGreen
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(26.dp))

    // Follow-ups waiting section (as shown on PDF Page 6)
    Text(
      text = "Follow-ups waiting",
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = DeloOnBackground
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Item 1: Grade 6, Week 8
    FollowUpWaitingCard(
      title = "Grade 6, Week 8",
      subtitle = "Riverside Primary School",
      topic = "Autonomous Obstacle Avoidance",
      onAnswerClick = {
        onStartFollowUp("SCH-001", "Riverside Primary School", "Grade 6", 8)
      },
      tag = "followup_g6_w8"
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Item 2: Grade 7, Week 7
    FollowUpWaitingCard(
      title = "Grade 7, Week 7",
      subtitle = "Riverside Primary School",
      topic = "H-Bridge DC Motor Controllers",
      onAnswerClick = {
        onStartFollowUp("SCH-001", "Riverside Primary School", "Grade 7", 7)
      },
      tag = "followup_g7_w7"
    )

    Spacer(modifier = Modifier.height(26.dp))

    // Today's Teaching Schedule
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

        Spacer(modifier = Modifier.height(12.dp))

        scheduledLessons.forEach { lesson ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = DeloSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.padding(end = 12.dp)
            ) {
              Text(
                text = lesson.time.split(" - ").first(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DeloGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${lesson.grade} • ${lesson.room}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeloOnBackground
              )
              Text(
                text = lesson.topic,
                fontSize = 11.sp,
                color = DeloOnSurfaceMuted,
                maxLines = 1
              )
            }
          }
          Divider(color = DeloBorder)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Quick Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = { onNavigate(Screen.TEACHER_ATTENDANCE) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeloOnBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder)
      ) {
        Icon(Icons.Default.Checklist, contentDescription = null, tint = DeloGold)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Attendance", fontSize = 13.sp)
      }

      OutlinedButton(
        onClick = { onNavigate(Screen.TEACHER_LESSONS) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeloOnBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder)
      ) {
        Icon(Icons.Default.MenuBook, contentDescription = null, tint = DeloGold)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Lessons", fontSize = 13.sp)
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun FollowUpWaitingCard(
  title: String,
  subtitle: String,
  topic: String,
  onAnswerClick: () -> Unit,
  tag: String
) {
  Surface(
    color = DeloSurface,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = DeloOnBackground
        )
        Text(
          text = "$subtitle • $topic",
          fontSize = 12.sp,
          color = DeloOnSurfaceMuted,
          maxLines = 1,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Button(
        onClick = onAnswerClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DeloGold,
          contentColor = DeloGoldDark
        ),
        modifier = Modifier.testTag(tag)
      ) {
        Text(
          text = "Answer",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }
  }
}
