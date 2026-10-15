package com.example.delominds.ui.screens.curriculum

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.delominds.data.model.CurriculumPart
import com.example.delominds.data.model.CurriculumWeek
import com.example.delominds.ui.Screen
import com.example.ui.theme.*

@Composable
fun AdminCurriculumScreen(
  curriculumParts: List<CurriculumPart>,
  curriculumWeeks: List<CurriculumWeek>,
  onSelectWeek: (partId: String, weekNumber: Int) -> Unit,
  onAddCheckpoint: ((gradeGroupId: String, weekNumber: Int?, shortLabel: String, question: String) -> Unit)? = null
) {
  var selectedPartId by remember { mutableStateOf("part_d") }
  var showAddCheckpointDialog by remember { mutableStateOf(false) }

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
        Text(
          text = "Curriculum Structure",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = DeloOnBackground
        )
        Text(
          text = "DELO MINDS Robotics, Coding & AI 36-Week Programme",
          fontSize = 12.sp,
          color = DeloOnSurfaceMuted,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      if (onAddCheckpoint != null) {
        Button(
          onClick = { showAddCheckpointDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("add_checkpoint_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Checkpoint", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Part selector tabs
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      curriculumParts.forEach { part ->
        val isSelected = part.id == selectedPartId
        val partColor = Color(part.colorHex)
        Surface(
          color = if (isSelected) partColor else DeloSurface,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) partColor else DeloBorder),
          modifier = Modifier
            .weight(1f)
            .clickable { selectedPartId = part.id }
            .testTag("part_tab_${part.id}")
        ) {
          Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = part.code,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) DeloGoldDark else DeloOnBackground
            )
            Text(
              text = "${part.coveragePercent}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isSelected) DeloGoldDark else partColor
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val currentPart = curriculumParts.find { it.id == selectedPartId } ?: curriculumParts.first()
    Text(
      text = "${currentPart.title} (${currentPart.gradeRange})",
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color(currentPart.colorHex),
      modifier = Modifier.padding(bottom = 8.dp)
    )

    // 36-Week grid / list
    val weeks = curriculumWeeks.filter { it.partId == selectedPartId }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Display seeded weeks first
      items(weeks) { week ->
        CurriculumWeekItemCard(
          week = week,
          partColor = Color(currentPart.colorHex),
          onClick = { onSelectWeek(week.partId, week.weekNumber) }
        )
      }

      // Display remaining weeks placeholder up to 36 weeks per textbook
      val existingWeekNumbers = weeks.map { it.weekNumber }.toSet()
      val placeholderWeeks = (1..12).filter { it !in existingWeekNumbers }
      items(placeholderWeeks) { weekNum ->
        CurriculumWeekPlaceholderCard(
          weekNumber = weekNum,
          partId = selectedPartId,
          partColor = Color(currentPart.colorHex),
          onClick = { onSelectWeek(selectedPartId, weekNum) }
        )
      }
    }

    if (showAddCheckpointDialog && onAddCheckpoint != null) {
      var shortLabel by remember { mutableStateOf("") }
      var question by remember { mutableStateOf("") }

      AlertDialog(
        onDismissRequest = { showAddCheckpointDialog = false },
        title = { Text("Add Follow-up Checkpoint", color = DeloGold, fontWeight = FontWeight.Bold) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Target Grade Group: ${currentPart.title}", fontSize = 12.sp, color = DeloOnSurfaceMuted)
            OutlinedTextField(
              value = shortLabel,
              onValueChange = { shortLabel = it },
              label = { Text("Short Label* (e.g., Code Verification)") },
              singleLine = true
            )
            OutlinedTextField(
              value = question,
              onValueChange = { question = it },
              label = { Text("Question*") },
              placeholder = { Text("Did you verify student source code?") }
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (shortLabel.isNotBlank() && question.isNotBlank()) {
                onAddCheckpoint(selectedPartId, null, shortLabel, question)
                showAddCheckpointDialog = false
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
          ) {
            Text("Add Checkpoint")
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddCheckpointDialog = false }) {
            Text("Cancel", color = DeloOnSurfaceMuted)
          }
        },
        containerColor = DeloSurface
      )
    }
  }
}

@Composable
fun CurriculumWeekItemCard(
  week: CurriculumWeek,
  partColor: Color,
  onClick: () -> Unit
) {
  Surface(
    color = DeloSurface,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("curriculum_week_${week.weekNumber}")
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(partColor.copy(alpha = 0.2f))
          .border(1.dp, partColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "W${week.weekNumber}",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp,
          color = partColor
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = week.topic,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = DeloOnBackground
        )
        Text(
          text = "Term ${week.term} • 3 Daily Lessons • Day 1 / 2 / 3",
          fontSize = 12.sp,
          color = DeloOnSurfaceMuted,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DeloGold)
    }
  }
}

@Composable
fun CurriculumWeekPlaceholderCard(
  weekNumber: Int,
  partId: String,
  partColor: Color,
  onClick: () -> Unit
) {
  Surface(
    color = DeloSurface.copy(alpha = 0.6f),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Week $weekNumber",
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = DeloOnSurfaceMuted,
        modifier = Modifier.width(64.dp)
      )
      Text(
        text = "Curriculum Unit $weekNumber: Robotics & Innovation",
        fontSize = 13.sp,
        color = DeloOnSurface,
        modifier = Modifier.weight(1f)
      )
      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DeloBorder)
    }
  }
}

@Composable
fun CurriculumWeekDetailScreen(
  partId: String,
  weekNumber: Int,
  curriculumWeeks: List<CurriculumWeek>,
  curriculumParts: List<CurriculumPart>,
  isTeacherView: Boolean = false,
  onStartFollowUp: (() -> Unit)? = null,
  onNavigateBack: () -> Unit
) {
  val week = curriculumWeeks.find { it.partId == partId && it.weekNumber == weekNumber }
    ?: CurriculumWeek(
      partId = partId,
      weekNumber = weekNumber,
      term = 1,
      topic = "Robotics & Innovation Week $weekNumber",
      learningObjectives = listOf("Master weekly core concepts", "Complete hands-on assembly", "Apply Education 5.0 project solutions"),
      bigIdea = "Foundational STEM principles powering autonomous and mechanical engineering.",
      keyWords = listOf("Robotics", "Coding", "Engineering"),
      day1CoreConcept = "NO DATA AVAILABLE",
      day2HandsOn = "NO DATA AVAILABLE",
      day3Application = "NO DATA AVAILABLE"
    )

  val part = curriculumParts.find { it.id == partId } ?: curriculumParts.first()
  val partColor = Color(part.colorHex)

  var teacherNotes by remember { mutableStateOf("Verified kit inventory. Students engaged actively during lab session.") }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Surface(
      color = partColor.copy(alpha = 0.15f),
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, partColor)
    ) {
      Text(
        text = "${part.code} • ${part.gradeRange} • Term ${week.term} • Week ${week.weekNumber}",
        color = partColor,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = week.topic,
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      color = DeloOnBackground
    )

    if (week.bigIdea.isNotBlank()) {
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        color = DeloSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "THE BIG IDEA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeloGold)
          Text(text = week.bigIdea, fontSize = 13.sp, color = DeloOnSurface, modifier = Modifier.padding(top = 4.dp))
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Learning Objectives
    if (week.learningObjectives.isNotEmpty()) {
      Text(text = "LEARNING OBJECTIVES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloGold)
      Spacer(modifier = Modifier.height(6.dp))
      week.learningObjectives.forEach { obj ->
        Row(modifier = Modifier.padding(vertical = 3.dp)) {
          Text("• ", color = DeloGold, fontWeight = FontWeight.Bold)
          Text(obj, fontSize = 13.sp, color = DeloOnSurface)
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Key Words
    if (week.keyWords.isNotEmpty()) {
      Text(text = "KEY WORDS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloGold)
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        week.keyWords.forEach { word ->
          Surface(
            color = DeloSurfaceVariant,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(word, fontSize = 11.sp, color = DeloOnSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // 3 Daily Lessons (Core structure required by Textbook & Prompt)
    LessonDaySectionCard(
      dayNumber = 1,
      dayTitle = "Core Concept & Theory",
      content = week.day1CoreConcept
    )

    Spacer(modifier = Modifier.height(12.dp))

    LessonDaySectionCard(
      dayNumber = 2,
      dayTitle = "Hands-on Building",
      content = week.day2HandsOn
    )

    Spacer(modifier = Modifier.height(12.dp))

    LessonDaySectionCard(
      dayNumber = 3,
      dayTitle = "Education 5.0 Application",
      content = week.day3Application
    )

    // Optional Textbook Fields (Troubleshooting, Worked Examples, Grade Differentiation)
    if (week.workedExamples.isNotBlank() || week.troubleshooting.isNotBlank() || week.gradeDifferentiation.isNotBlank()) {
      Spacer(modifier = Modifier.height(16.dp))
      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          if (week.workedExamples.isNotBlank()) {
            Text("Worked Example:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloGold)
            Text(week.workedExamples, fontSize = 12.sp, color = DeloOnSurface, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
          }
          if (week.troubleshooting.isNotBlank()) {
            Text("Troubleshooting:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloAmber)
            Text(week.troubleshooting, fontSize = 12.sp, color = DeloOnSurface, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
          }
          if (week.gradeDifferentiation.isNotBlank()) {
            Text("Grade Differentiation:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = partColor)
            Text(week.gradeDifferentiation, fontSize = 12.sp, color = DeloOnSurface, modifier = Modifier.padding(top = 2.dp))
          }
        }
      }
    }

    // Teacher Notes
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "TEACHER NOTES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloGold)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
      value = teacherNotes,
      onValueChange = { teacherNotes = it },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DeloGold,
        unfocusedBorderColor = DeloBorder,
        focusedContainerColor = DeloSurface,
        unfocusedContainerColor = DeloSurface,
        focusedTextColor = DeloOnBackground,
        unfocusedTextColor = DeloOnBackground
      )
    )

    // Action button if Teacher view
    if (isTeacherView && onStartFollowUp != null) {
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = onStartFollowUp,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("open_follow_up_from_lesson"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DeloGold,
          contentColor = DeloGoldDark
        )
      ) {
        Icon(Icons.Default.FactCheck, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("ANSWER WEEK ${week.weekNumber} FOLLOW-UP", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun LessonDaySectionCard(
  dayNumber: Int,
  dayTitle: String,
  content: String
) {
  val isNoData = content.equals("NO DATA AVAILABLE", ignoreCase = true)

  Surface(
    color = DeloSurface,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DAY $dayNumber",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = DeloGold,
          letterSpacing = 1.sp
        )
        Text(
          text = dayTitle,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = DeloOnBackground
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (isNoData) {
        Surface(
          color = DeloSurfaceVariant,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = "NO DATA AVAILABLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DeloOnSurfaceMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      } else {
        Text(
          text = content,
          fontSize = 13.sp,
          color = DeloOnSurface,
          lineHeight = 19.sp
        )
      }
    }
  }
}
