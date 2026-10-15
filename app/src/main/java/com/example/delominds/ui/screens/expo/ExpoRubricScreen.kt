package com.example.delominds.ui.screens.expo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.ExpoRubricCriterion
import com.example.delominds.data.model.Student
import com.example.ui.theme.*

@Composable
fun ExpoRubricScreen(
  student: Student?,
  criteria: List<ExpoRubricCriterion>,
  onSaveScore: (studentId: String, studentName: String, projectTitle: String, term: Int, scores: List<Int>, notes: String) -> Unit,
  onNavigateBack: () -> Unit
) {
  var projectTitle by remember { mutableStateOf("Autonomous Smart Agricultural Sprayer") }
  var term by remember { mutableStateOf(1) }
  var notes by remember { mutableStateOf("Excellent prototype. Chassis fabricated neatly with laser-cut components.") }

  // 6 scores, initialized to 3 (Proficient)
  val scores = remember { mutableStateListOf(3, 4, 3, 4, 3, 4) }
  val totalScore = scores.sum()

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Innovation Expo Rubric", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
        Text("6 criteria • 4 achievement levels • Total out of 24", fontSize = 12.sp, color = DeloOnSurfaceMuted)
      }

      Surface(
        color = DeloGold,
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Star, contentDescription = null, tint = DeloGoldDark, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("$totalScore / 24", color = DeloGoldDark, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
      }
    }

    if (student != null) {
      Spacer(modifier = Modifier.height(14.dp))
      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Scoring Learner: ${student.fullName} (${student.studentId})", fontWeight = FontWeight.Bold, color = DeloGold, fontSize = 14.sp)
          Text("${student.schoolName} • ${student.grade}", fontSize = 12.sp, color = DeloOnSurfaceMuted)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = projectTitle,
      onValueChange = { projectTitle = it },
      label = { Text("Project Title") },
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

    Spacer(modifier = Modifier.height(20.dp))

    // 6 Criteria Cards
    criteria.forEachIndexed { index, crit ->
      val currentScore = if (index < scores.size) scores[index] else 3

      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${crit.criterionNumber}. ${crit.title}",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = DeloOnBackground,
              modifier = Modifier.weight(1f)
            )
            Surface(
              color = DeloSurfaceVariant,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "$currentScore / 4 pts",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DeloGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Text(
            text = crit.description,
            fontSize = 12.sp,
            color = DeloOnSurfaceMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
          )

          // 4 Score Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (1..4).forEach { lvl ->
              val isSelected = currentScore == lvl
              Surface(
                color = if (isSelected) DeloGold else DeloSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) DeloGold else DeloBorder
                ),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    if (index < scores.size) {
                      scores[index] = lvl
                    }
                  }
                  .testTag("rubric_${crit.criterionNumber}_lvl_$lvl")
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = when (lvl) {
                      1 -> "Beg (1)"
                      2 -> "Dev (2)"
                      3 -> "Prof (3)"
                      else -> "Exemp (4)"
                    },
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                    color = if (isSelected) DeloGoldDark else DeloOnSurface
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = notes,
      onValueChange = { notes = it },
      label = { Text("Judge / Teacher Evaluation Notes") },
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

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = {
        val sId = student?.id ?: "STU-001"
        val sName = student?.fullName ?: "Learner"
        onSaveScore(sId, sName, projectTitle, term, scores.toList(), notes)
        onNavigateBack()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("save_expo_score_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = DeloGold,
        contentColor = DeloGoldDark
      )
    ) {
      Text("SAVE EXPO SCORE ($totalScore / 24)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}
