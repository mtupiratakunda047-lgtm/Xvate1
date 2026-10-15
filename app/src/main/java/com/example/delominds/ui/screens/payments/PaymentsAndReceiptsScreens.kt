package com.example.delominds.ui.screens.payments

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.Payment
import com.example.delominds.data.model.PaymentStatus
import com.example.delominds.data.model.Student
import com.example.ui.theme.*

@Composable
fun PaymentsListScreen(
  payments: List<Payment>,
  students: List<Student>,
  onOpenReceipt: (Payment) -> Unit,
  onRecordPayment: (studentId: String, studentName: String, schoolId: String, schoolName: String, grade: String, month: String, amount: Double, method: String, status: PaymentStatus, notes: String) -> Unit
) {
  var showRecordDialog by remember { mutableStateOf(false) }
  var filterStatus by remember { mutableStateOf<PaymentStatus?>(null) }

  val filteredPayments = payments.filter {
    filterStatus == null || it.status == filterStatus
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
        Text("Fee Payments & Receipts", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
        Text("Monthly Robotics & AI programme fees", fontSize = 12.sp, color = DeloOnSurfaceMuted)
      }

      Button(
        onClick = { showRecordDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("record_payment_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Record Payment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = filterStatus == null,
        onClick = { filterStatus = null },
        label = { Text("All", fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = DeloGold,
          selectedLabelColor = DeloGoldDark,
          containerColor = DeloSurfaceVariant,
          labelColor = DeloOnSurface
        )
      )
      FilterChip(
        selected = filterStatus == PaymentStatus.PAID,
        onClick = { filterStatus = PaymentStatus.PAID },
        label = { Text("Paid", fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = DeloGreen,
          selectedLabelColor = Color.White,
          containerColor = DeloSurfaceVariant,
          labelColor = DeloOnSurface
        )
      )
      FilterChip(
        selected = filterStatus == PaymentStatus.PARTIAL,
        onClick = { filterStatus = PaymentStatus.PARTIAL },
        label = { Text("Partial", fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = DeloAmber,
          selectedLabelColor = Color.White,
          containerColor = DeloSurfaceVariant,
          labelColor = DeloOnSurface
        )
      )
      FilterChip(
        selected = filterStatus == PaymentStatus.PENDING,
        onClick = { filterStatus = PaymentStatus.PENDING },
        label = { Text("Pending", fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = DeloBorder,
          selectedLabelColor = DeloOnSurface,
          containerColor = DeloSurfaceVariant,
          labelColor = DeloOnSurface
        )
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filteredPayments) { payment ->
        Surface(
          color = DeloSurface,
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenReceipt(payment) }
            .testTag("payment_item_${payment.receiptNumber}")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(payment.studentName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeloOnBackground)
              Text("${payment.schoolName} • ${payment.grade} • ${payment.month}", fontSize = 12.sp, color = DeloOnSurfaceMuted)
              Text("Receipt: ${payment.receiptNumber} • ${payment.paymentMethod}", fontSize = 11.sp, color = DeloGold, modifier = Modifier.padding(top = 2.dp))
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "USD ${String.format("%.2f", payment.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = DeloOnBackground
              )
              Spacer(modifier = Modifier.height(4.dp))
              val statusColor = when (payment.status) {
                PaymentStatus.PAID -> DeloGreen
                PaymentStatus.PARTIAL -> DeloAmber
                PaymentStatus.PENDING -> DeloOnSurfaceMuted
                PaymentStatus.OVERDUE -> DeloRed
              }
              Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
              ) {
                Text(
                  text = payment.status.name,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = statusColor,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  if (showRecordDialog) {
    RecordPaymentDialog(
      students = students,
      onDismiss = { showRecordDialog = false },
      onConfirm = { stu, month, amt, method, status, notes ->
        onRecordPayment(stu.id, stu.fullName, stu.schoolId, stu.schoolName, stu.grade, month, amt, method, status, notes)
        showRecordDialog = false
      }
    )
  }
}

@Composable
fun RecordPaymentDialog(
  students: List<Student>,
  onDismiss: () -> Unit,
  onConfirm: (student: Student, month: String, amount: Double, method: String, status: PaymentStatus, notes: String) -> Unit
) {
  var selectedStudent by remember { mutableStateOf(students.firstOrNull() ?: Student("1", "STU-1", "Learner", "1", "School", "Grade 1", "Class", "Guardian", "123")) }
  var month by remember { mutableStateOf("October 2026") }
  var amountText by remember { mutableStateOf("10.00") }
  var paymentMethod by remember { mutableStateOf("EcoCash") }
  var status by remember { mutableStateOf(PaymentStatus.PAID) }
  var notes by remember { mutableStateOf("Monthly robotics curriculum fee") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record Fee Payment", color = DeloGold, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("Select Student:", fontSize = 12.sp, color = DeloOnSurfaceMuted)
        students.forEach { s ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedStudent = s }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = selectedStudent.id == s.id, onClick = { selectedStudent = s })
            Text("${s.fullName} (${s.studentId})", fontSize = 13.sp, color = DeloOnSurface)
          }
        }

        OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Coverage Month") }, singleLine = true)
        OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount (USD)") }, singleLine = true)
        OutlinedTextField(value = paymentMethod, onValueChange = { paymentMethod = it }, label = { Text("Payment Method (EcoCash/Cash/Card)") }, singleLine = true)
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, singleLine = true)
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: 10.0
          onConfirm(selectedStudent, month, amt, paymentMethod, status, notes)
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
      ) {
        Text("Generate Receipt")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = DeloOnSurfaceMuted) }
    },
    containerColor = DeloSurface
  )
}

// -------------------------------------------------------------------------
// RECEIPT DETAIL SCREEN (matching PDF Page 7)
// -------------------------------------------------------------------------
@Composable
fun ReceiptDetailScreen(
  payment: Payment,
  onNavigateBack: () -> Unit
) {
  var printedNotice by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(10.dp))

    // Authentic Receipt Box with White Background (matching PDF Page 7!)
    Surface(
      color = Color.White,
      shape = RoundedCornerShape(16.dp),
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("receipt_card")
    ) {
      Column(
        modifier = Modifier.padding(24.dp)
      ) {
        // Receipt Header
        Text(
          text = "CREATVATE",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF1B1E15),
          letterSpacing = 1.sp
        )
        Text(
          text = "DELO MINDS",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF555B46),
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        ReceiptRow(label = "Receipt no", value = payment.receiptNumber, isBold = true)
        ReceiptDivider()

        ReceiptRow(label = "Student", value = payment.studentName, isBold = true)
        ReceiptDivider()

        ReceiptRow(label = "School", value = payment.schoolName)
        ReceiptDivider()

        ReceiptRow(label = "Month", value = payment.month)
        ReceiptDivider()

        ReceiptRow(label = "Amount", value = "USD ${String.format("%.2f", payment.amount)}", isBold = true)
        ReceiptDivider()

        ReceiptRow(
          label = "Status",
          value = payment.status.name,
          valueColor = if (payment.status == PaymentStatus.PAID) Color(0xFF2E7D32) else Color(0xFFC62828),
          isBold = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
          text = "Official STEM, Robotics & AI Education 5.0 Receipt",
          fontSize = 11.sp,
          color = Color.Gray,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    if (printedNotice) {
      Spacer(modifier = Modifier.height(16.dp))
      Surface(
        color = DeloGreenContainer,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeloGreen)
      ) {
        Text(
          text = "Receipt formatted for PDF export & printer spooling.",
          color = DeloGreen,
          fontSize = 12.sp,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(30.dp))

    // "Print or save as PDF" Gold Button (matching PDF Page 7)
    Button(
      onClick = { printedNotice = true },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("print_receipt_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = DeloGold,
        contentColor = DeloGoldDark
      )
    ) {
      Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Print or save as PDF",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    TextButton(onClick = onNavigateBack) {
      Text("Close Receipt", color = DeloOnSurfaceMuted)
    }
  }
}

@Composable
fun ReceiptRow(
  label: String,
  value: String,
  valueColor: Color = Color(0xFF222222),
  isBold: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = Color(0xFF777777)
    )
    Text(
      text = value,
      fontSize = 14.sp,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
      color = valueColor
    )
  }
}

@Composable
fun ReceiptDivider() {
  Divider(color = Color(0xFFE8E8E8), thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
}
