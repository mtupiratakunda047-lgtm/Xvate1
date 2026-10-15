package com.example.delominds.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.UserAccount
import com.example.ui.theme.*

@Composable
fun SignInScreen(
  availableAccounts: List<UserAccount>,
  onSignIn: (String, String) -> Boolean,
  onQuickRoleSelect: (UserAccount) -> Unit,
  onServerSignIn: (() -> Unit)? = null,
  onServerPasswordSignIn: ((String, String, (String?) -> Unit) -> Unit)? = null
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showForgotDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(32.dp))

      // Logo Icon as shown on PDF Page 3
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(DeloGold),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "D",
          color = DeloGoldDark,
          fontWeight = FontWeight.Black,
          fontSize = 32.sp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "CREATVATE DELO MINDS",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DeloGold,
        letterSpacing = 1.2.sp
      )

      Text(
        text = "Programme management system",
        fontSize = 13.sp,
        color = DeloOnSurfaceMuted,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(36.dp))

      // Email field
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Email",
          fontSize = 13.sp,
          color = DeloOnSurfaceMuted,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
          value = email,
          onValueChange = {
            email = it
            errorMessage = null
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("email_input"),
          singleLine = true,
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
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Password field
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Password",
          fontSize = 13.sp,
          color = DeloOnSurfaceMuted,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("password_input"),
          singleLine = true,
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
      }

      // Error message block (as seen on PDF Page 3)
      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          color = DeloRedContainer,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeloRed),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = errorMessage!!,
            color = DeloRed,
            fontSize = 13.sp,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Sign In Button (Gold button as shown on PDF Page 3)
      Button(
        onClick = {
          val signIn = onServerPasswordSignIn
          if (signIn == null) {
            errorMessage = "Sign-in is not available."
          } else {
            signIn(email, password) { error -> errorMessage = error }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("sign_in_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = DeloGold,
          contentColor = DeloGoldDark
        )
      ) {
        Text(
          text = "Sign in",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (onServerSignIn != null) {
        OutlinedButton(
          onClick = onServerSignIn,
          modifier = Modifier.fillMaxWidth().height(50.dp),
          shape = RoundedCornerShape(12.dp)
        ) { Text("Sign in with a code", fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(8.dp))
      }

      TextButton(
        onClick = { showForgotDialog = true },
        modifier = Modifier.testTag("forgot_password_button")
      ) {
        Text(
          text = "Forgot password?",
          color = DeloOnSurfaceMuted,
          fontSize = 13.sp
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showForgotDialog) {
    AlertDialog(
      onDismissRequest = { showForgotDialog = false },
      title = { Text("Reset Password", color = DeloGold) },
      text = {
        Text(
          "A password reset link will be sent to $email. It expires in one hour and works once per system policy.",
          color = DeloOnSurface
        )
      },
      confirmButton = {
        Button(
          onClick = { showForgotDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) {
          Text("Send Link")
        }
      },
      dismissButton = {
        TextButton(onClick = { showForgotDialog = false }) {
          Text("Cancel", color = DeloOnSurfaceMuted)
        }
      },
      containerColor = DeloSurface
    )
  }
}
