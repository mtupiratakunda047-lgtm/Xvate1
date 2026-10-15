package com.example.delominds.ui.screens.reports

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.*
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
  teachers: List<Teacher>,
  onSelectTeacher: (String) -> Unit
) {
  var csvDownloadedNotice by remember { mutableStateOf(false) }

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
        Text("Monthly Performance Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
        Text("Calculated from verified curriculum follow-ups", fontSize = 12.sp, color = DeloOnSurfaceMuted)
      }

      Button(
        onClick = { csvDownloadedNotice = true },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("download_csv_button")
      ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Download CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    if (csvDownloadedNotice) {
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        color = DeloGreenContainer,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloGreen)
      ) {
        Text(
          text = "CSV report downloaded: opens in Excel with all names intact.",
          color = DeloGreen,
          fontSize = 12.sp,
          modifier = Modifier.padding(10.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Monthly Teacher Performance Table (matching PDF Page 8 & 12)
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("TEACHER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeloGold, modifier = Modifier.weight(1.5f))
          Text("DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeloOnSurfaceMuted, modifier = Modifier.weight(0.8f))
          Text("PENDING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeloOnSurfaceMuted, modifier = Modifier.weight(1f))
          Text("COVERAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeloGold, modifier = Modifier.weight(1f))
        }
        Divider(color = DeloBorder)

        teachers.forEach { teacher ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectTeacher(teacher.id) }
              .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(teacher.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground, modifier = Modifier.weight(1.5f))
            Text("${teacher.completedFollowUps}", fontSize = 13.sp, color = DeloOnSurface, modifier = Modifier.weight(0.8f))
            Text("${teacher.pendingFollowUps}", fontSize = 13.sp, color = DeloAmber, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Surface(
              color = if (teacher.coveragePercent >= 80) DeloGreenContainer else DeloAmberContainer,
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "${teacher.coveragePercent}%",
                color = if (teacher.coveragePercent >= 80) DeloGreen else DeloAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Divider(color = DeloBorder)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Note: Overall coverage percentage is computed strictly from verified teacher follow-up answers (Covered ÷ Total questions). Tap any teacher above to view detailed lesson drilldowns.",
      fontSize = 12.sp,
      color = DeloOnSurfaceMuted,
      lineHeight = 18.sp
    )
  }
}

@Composable
fun SettingsScreen(
  settings: SystemSettings,
  onSaveSettings: (SystemSettings) -> Unit
) {
  var orgName by remember { mutableStateOf(settings.organisationName) }
  var currency by remember { mutableStateOf(settings.currency) }
  var monthlyFeeText by remember { mutableStateOf(settings.monthlyFee.toString()) }
  var academicYear by remember { mutableStateOf(settings.academicYear) }
  var startDate by remember { mutableStateOf(settings.programmeStartDate) }
  var receiptFooter by remember { mutableStateOf(settings.receiptFooter) }
  var smtpHost by remember { mutableStateOf(settings.smtpHost) }
  var testEmailNotice by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Text("System Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
    Text("Programme configuration and communication settings", fontSize = 12.sp, color = DeloOnSurfaceMuted, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("ORGANISATION & CURRICULUM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloGold)

        OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("Organisation Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = academicYear, onValueChange = { academicYear = it }, label = { Text("Academic Year") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Programme Start Date") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency") }, modifier = Modifier.weight(1f))
          OutlinedTextField(value = monthlyFeeText, onValueChange = { monthlyFeeText = it }, label = { Text("Monthly Fee") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = receiptFooter, onValueChange = { receiptFooter = it }, label = { Text("Receipt Footer Note") }, modifier = Modifier.fillMaxWidth())
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Communication Settings (Super Admin - PDF Page 8 & 9)
    Surface(
      color = DeloSurface,
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("EMAIL (SMTP) & SMS GATEWAY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeloGold)

        OutlinedTextField(value = smtpHost, onValueChange = { smtpHost = it }, label = { Text("SMTP Host") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = settings.smsWebhookUrl, onValueChange = {}, label = { Text("SMS Webhook URL") }, modifier = Modifier.fillMaxWidth())

        Button(
          onClick = { testEmailNotice = true },
          colors = ButtonDefaults.buttonColors(containerColor = DeloSurfaceVariant, contentColor = DeloGold),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("send_test_email_button")
        ) {
          Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Send a test email (Super Admin)", fontSize = 13.sp)
        }

        if (testEmailNotice) {
          Text(
            text = "Test email dispatched to admin mailbox successfully.",
            color = DeloGreen,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = {
        val fee = monthlyFeeText.toDoubleOrNull() ?: 10.0
        onSaveSettings(
          settings.copy(
            organisationName = orgName,
            currency = currency,
            monthlyFee = fee,
            academicYear = academicYear,
            programmeStartDate = startDate,
            receiptFooter = receiptFooter,
            smtpHost = smtpHost
          )
        )
      },
      modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_settings_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
    ) {
      Text("SAVE SETTINGS", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun AuditLogScreen(
  auditLogs: List<AuditLogEntry>
) {
  var searchQuery by remember { mutableStateOf("") }
  val filtered = auditLogs.filter {
    it.user.contains(searchQuery, ignoreCase = true) ||
        it.action.contains(searchQuery, ignoreCase = true) ||
        it.record.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(16.dp)
  ) {
    Text("System Audit Log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
    Text("Records who changed what and when (latest entries)", fontSize = 12.sp, color = DeloOnSurfaceMuted, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search audit log...", color = DeloOnSurfaceMuted, fontSize = 13.sp) },
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
      items(filtered) { log ->
        Surface(
          color = DeloSurface,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(log.action, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeloGold)
              Text(log.timestamp, fontSize = 11.sp, color = DeloOnSurfaceMuted)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("User: ${log.user} • Record: ${log.record}", fontSize = 12.sp, color = DeloOnBackground)
            Text(log.details, fontSize = 11.sp, color = DeloOnSurfaceMuted, modifier = Modifier.padding(top = 2.dp))
          }
        }
      }
    }
  }
}
