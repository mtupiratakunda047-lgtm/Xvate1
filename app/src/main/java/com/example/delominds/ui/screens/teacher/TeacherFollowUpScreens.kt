package com.example.delominds.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.ui.FollowUpActiveSession
import com.example.delominds.ui.Screen
import com.example.ui.theme.*

@Composable
fun TeacherFollowUpQuestionScreen(
  session: FollowUpActiveSession,
  onAnswerQuestion: (questionIndex: Int, isCovered: Boolean) -> Unit,
  onNavigateBack: () -> Unit
) {
  val currentIndex = session.currentQuestionIndex.coerceIn(0, session.questions.lastIndex)
  val currentQuestion = session.questions[currentIndex]
  val totalQuestions = session.questions.size

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(10.dp))

    // Header info (as shown on PDF Page 5)
    Text(
      text = "${session.grade}, Week ${session.weekNumber}, ${session.schoolName}",
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      color = DeloGold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Step Progress indicators (Pills as shown on PDF Page 5)
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      session.questions.forEachIndexed { idx, q ->
        val pillColor = when {
          q.isCovered == true -> DeloGreen
          q.isCovered == false -> DeloRed
          idx == currentIndex -> DeloGold
          else -> DeloBorder
        }
        Box(
          modifier = Modifier
            .width(42.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(pillColor)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Question ${currentIndex + 1} of $totalQuestions",
      fontSize = 13.sp,
      color = DeloOnSurfaceMuted,
      fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Big Question Card
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = currentQuestion.questionText,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = DeloOnBackground,
          textAlign = TextAlign.Center,
          lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          color = DeloSurfaceVariant,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Area: ${currentQuestion.checkpointLabel}",
            fontSize = 12.sp,
            color = DeloGold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))

    // COVERED Button (Green as shown on PDF Page 5)
    OutlinedButton(
      onClick = { onAnswerQuestion(currentIndex, true) },
      modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .testTag("button_covered"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        containerColor = if (currentQuestion.isCovered == true) DeloGreenContainer else Color.Transparent,
        contentColor = DeloGreen
      ),
      border = androidx.compose.foundation.BorderStroke(2.dp, DeloGreen)
    ) {
      Icon(
        Icons.Default.CheckCircle,
        contentDescription = null,
        tint = DeloGreen,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "COVERED",
        fontSize = 17.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // NOT COVERED Button (Red as shown on PDF Page 5)
    OutlinedButton(
      onClick = { onAnswerQuestion(currentIndex, false) },
      modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .testTag("button_not_covered"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        containerColor = if (currentQuestion.isCovered == false) DeloRedContainer else Color.Transparent,
        contentColor = DeloRed
      ),
      border = androidx.compose.foundation.BorderStroke(2.dp, DeloRed)
    ) {
      Icon(
        Icons.Default.Cancel,
        contentDescription = null,
        tint = DeloRed,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "NOT COVERED",
        fontSize = 17.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
      )
    }

    Spacer(modifier = Modifier.weight(1f))

    // Back button
    TextButton(
      onClick = onNavigateBack,
      modifier = Modifier.testTag("follow_up_back_button")
    ) {
      Icon(Icons.Default.ArrowBack, contentDescription = null, tint = DeloOnSurfaceMuted)
      Spacer(modifier = Modifier.width(4.dp))
      Text("Back", color = DeloOnSurfaceMuted, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun TeacherFollowUpReviewScreen(
  session: FollowUpActiveSession,
  onToggleAnswer: (Int) -> Unit,
  onSubmit: () -> Unit,
  onBackToQuestions: () -> Unit
) {
  var showConfirmDialog by remember { mutableStateOf(false) }
  val allAnswered = session.questions.all { it.isCovered != null }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "Check your answers",
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = DeloOnBackground
    )

    Text(
      text = "${session.grade}, Week ${session.weekNumber} • Tap any row to change",
      fontSize = 13.sp,
      color = DeloOnSurfaceMuted,
      modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )

    // Review List matching PDF Page 5
    session.questions.forEachIndexed { index, q ->
      Surface(
        color = DeloSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
          .clickable { onToggleAnswer(index) }
          .testTag("review_item_$index")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = if (q.isCovered == true) DeloGreenContainer else DeloRedContainer,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (q.isCovered == true) DeloGreen else DeloRed
              )
            ) {
              Text(
                text = if (q.isCovered == true) "Covered" else "Not covered",
                color = if (q.isCovered == true) DeloGreen else DeloRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            Text(
              text = "Tap to toggle",
              fontSize = 11.sp,
              color = DeloGold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = q.questionText,
            fontSize = 13.sp,
            color = DeloOnSurface,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))

    // ENTER button (strictly visible ONLY when all questions answered, per PDF Page 5!)
    if (allAnswered) {
      Button(
        onClick = { showConfirmDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("button_enter_submit"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DeloGold,
          contentColor = DeloGoldDark
        )
      ) {
        Text(
          text = "ENTER",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.sp
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Submitted answers are stored permanently.",
        fontSize = 12.sp,
        color = DeloOnSurfaceMuted,
        textAlign = TextAlign.Center
      )
    } else {
      Surface(
        color = DeloAmberContainer,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloAmber),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Please answer all questions before submitting.",
          color = DeloAmber,
          fontSize = 13.sp,
          modifier = Modifier.padding(12.dp),
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    TextButton(onClick = onBackToQuestions) {
      Text("Return to Questions", color = DeloOnSurfaceMuted)
    }
  }

  // Confirmation Modal Dialog (per Prompt & PDF)
  if (showConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDialog = false },
      title = {
        Text(
          text = "Submit Follow-up?",
          color = DeloGold,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = "Are you sure you have answered all follow-up questions? Once submitted, answers can only be reopened by an administrator.",
          color = DeloOnSurface
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showConfirmDialog = false
            onSubmit()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = DeloGold,
            contentColor = DeloGoldDark
          ),
          modifier = Modifier.testTag("confirm_submit_button")
        ) {
          Text("SUBMIT FOLLOW-UP", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmDialog = false }) {
          Text("GO BACK", color = DeloOnSurfaceMuted)
        }
      },
      containerColor = DeloSurface,
      shape = RoundedCornerShape(16.dp)
    )
  }
}

@Composable
fun TeacherFollowUpSuccessScreen(
  session: FollowUpActiveSession,
  onBackToDashboard: () -> Unit
) {
  val result = session.submissionResult
  val coverage = result?.coveragePercentage ?: 83
  val covered = result?.coveredCount ?: 2
  val notCovered = result?.notCoveredCount ?: 1

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .background(DeloGreenContainer)
        .border(2.dp, DeloGreen, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        Icons.Default.Check,
        contentDescription = "Success",
        tint = DeloGreen,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "FOLLOW-UP SUBMITTED",
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      color = DeloGreen,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Grade:", color = DeloOnSurfaceMuted, fontSize = 13.sp)
          Text(session.grade, color = DeloOnBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Divider(color = DeloBorder, modifier = Modifier.padding(vertical = 10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Week:", color = DeloOnSurfaceMuted, fontSize = 13.sp)
          Text("Week ${session.weekNumber}", color = DeloOnBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Divider(color = DeloBorder, modifier = Modifier.padding(vertical = 10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Coverage:", color = DeloOnSurfaceMuted, fontSize = 13.sp)
          Text("$coverage%", color = DeloGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Divider(color = DeloBorder, modifier = Modifier.padding(vertical = 10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Breakdown:", color = DeloOnSurfaceMuted, fontSize = 13.sp)
          Text("✓ $covered Covered   ✕ $notCovered Missed", color = DeloOnBackground, fontSize = 13.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(36.dp))

    Button(
      onClick = onBackToDashboard,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("back_to_dashboard_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = DeloGold,
        contentColor = DeloGoldDark
      )
    ) {
      Text(
        text = "BACK TO DASHBOARD",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
      )
    }
  }
}
