package com.example.delominds.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.delominds.data.model.TeacherAssignment
import com.example.delominds.data.model.UserAccount
import com.example.delominds.ui.Screen
import com.example.ui.theme.*

@Composable
fun TeacherProgressScreen(
  assignments: List<TeacherAssignment>,
  onSelectWeek: (Int) -> Unit
) {
  val assignment = assignments.firstOrNull() ?: TeacherAssignment("1", "T", "Teacher", "S", "Riverside Primary", "part_d", "Grade 6")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Text(
      text = "Teacher progress",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = DeloOnBackground
    )
    Text(
      text = "${assignment.schoolName}, ${assignment.grade}",
      fontSize = 14.sp,
      color = DeloGold,
      modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
    )

    Text(
      text = "Coverage",
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = DeloOnSurfaceMuted
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Weekly Progress Grid W1..W8 (matching PDF Page 4 layout exactly!)
    val weeks = listOf(
      1 to 100,
      2 to 83,
      3 to 100,
      4 to null,
      5 to null,
      6 to null,
      7 to null,
      8 to null
    )

    LazyVerticalGrid(
      columns = GridCells.Fixed(4),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth().weight(1f)
    ) {
      items(weeks) { (weekNum, pct) ->
        val chipColor = when {
          pct == null -> DeloSurfaceVariant
          pct >= 90 -> DeloGreenContainer
          pct >= 70 -> DeloAmberContainer
          else -> DeloRedContainer
        }
        val textColor = when {
          pct == null -> DeloOnSurfaceMuted
          pct >= 90 -> DeloGreen
          pct >= 70 -> DeloAmber
          else -> DeloRed
        }

        Surface(
          color = chipColor,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
          modifier = Modifier
            .aspectRatio(1f)
            .clickable { onSelectWeek(weekNum) }
            .testTag("teacher_week_progress_$weekNum")
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "W$weekNum",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = DeloOnSurfaceMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (pct != null) "$pct%" else "-",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = textColor
            )
          }
        }
      }
    }

    // Legend as shown on PDF Page 4
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Green: fully covered   Amber: partly   Red: missed",
        fontSize = 12.sp,
        color = DeloOnSurfaceMuted,
        modifier = Modifier.padding(14.dp)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
fun TeacherMoreScreen(
  currentUser: UserAccount,
  onNavigate: (Screen) -> Unit,
  onSignOut: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Text("Teacher Account & Tools", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
    Text(currentUser.email, fontSize = 12.sp, color = DeloGold, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        MoreMenuItem(
          icon = Icons.Default.MenuBook,
          title = "Textbook Curriculum",
          subtitle = "36 weeks of Robotics & AI lessons",
          onClick = { onNavigate(Screen.TEACHER_LESSONS) }
        )
        Divider(color = DeloBorder)
        MoreMenuItem(
          icon = Icons.Default.Star,
          title = "Innovation Expo Rubric",
          subtitle = "View textbook 6-criteria scoring rubric",
          onClick = { onNavigate(Screen.ADMIN_EXPO_RUBRIC) }
        )
        Divider(color = DeloBorder)
        MoreMenuItem(
          icon = Icons.Default.Checklist,
          title = "Class Attendance Roster",
          subtitle = "Record daily attendance",
          onClick = { onNavigate(Screen.TEACHER_ATTENDANCE) }
        )
        Divider(color = DeloBorder)
        MoreMenuItem(
          icon = Icons.Default.TrendingUp,
          title = "My Teaching Progress",
          subtitle = "Weekly follow-up coverage chips",
          onClick = { onNavigate(Screen.TEACHER_PROGRESS) }
        )
        Divider(color = DeloBorder)
        MoreMenuItem(
          icon = Icons.Default.ExitToApp,
          title = "Sign Out",
          subtitle = "Switch account or end session",
          textColor = DeloRed,
          onClick = onSignOut
        )
      }
    }
  }
}

@Composable
fun MoreMenuItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  textColor: Color = DeloOnBackground,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = DeloGold, modifier = Modifier.size(24.dp))
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
      Text(subtitle, fontSize = 12.sp, color = DeloOnSurfaceMuted)
    }
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DeloOnSurfaceMuted)
  }
}
