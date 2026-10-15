package com.example.delominds.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.delominds.data.sync.ServerClient
import com.example.delominds.data.sync.ServerException
import com.example.ui.theme.DeloBackground
import com.example.ui.theme.DeloGold
import com.example.ui.theme.DeloGoldDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Sign-in against the Delo Minds server with a one-time code.
 * Steps: server address -> email -> 6-digit code -> (if new) choose your own password.
 * Network calls run on Dispatchers.IO.
 */
@Composable
fun ServerSignInScreen(
  client: ServerClient,
  onSignedIn: (role: String) -> Unit,
  onBack: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  var step by remember { mutableStateOf(Step.ADDRESS) }
  var address by remember { mutableStateOf(client.baseUrl) }
  var email by remember { mutableStateOf("") }
  var code by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var busy by remember { mutableStateOf(false) }
  var message by remember { mutableStateOf<String?>(null) }

  fun fail(e: Exception) {
    message = when (e) {
      is ServerException -> e.message
      is IOException -> "Can't reach the server. Check the address and your connection."
      else -> "Something went wrong. Try again."
    }
  }

  // Runs [work] off the UI thread, then [then] on the main thread with its result.
  fun <T> networkCall(work: suspend () -> T, then: (T) -> Unit) {
    busy = true; message = null
    scope.launch {
      try {
        val result = withContext(Dispatchers.IO) { work() }
        then(result)
      } catch (e: Exception) {
        fail(e)
      } finally {
        busy = false
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DeloBackground)
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(32.dp))
    Text("Sign in with a code", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      when (step) {
        Step.ADDRESS -> "Enter the address of your school's Delo Minds server."
        Step.EMAIL -> "Enter your email. We'll send a 6-digit code to it."
        Step.CODE -> "Enter the code from your email. It works once and expires in 10 minutes."
        Step.PASSWORD -> "Choose the password you will use from now on (at least 8 characters)."
      },
      fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(24.dp))

    when (step) {
      Step.ADDRESS -> {
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Server address") },
          singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { client.baseUrl = address; step = Step.EMAIL }, enabled = address.isNotBlank() && !busy,
          modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Continue") }
      }

      Step.EMAIL -> {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
          singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = { networkCall(work = { client.requestSignInCode(email) }) { step = Step.CODE } },
          enabled = email.isNotBlank() && !busy,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("send_code_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) { Text("Send code") }
      }

      Step.CODE -> {
        OutlinedTextField(value = code, onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit) },
          label = { Text("6-digit code") }, singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = {
            networkCall(work = {
              val mustSetPassword = client.verifySignInCode(email, code)
              mustSetPassword to (if (mustSetPassword) "" else client.currentRole())
            }) { (mustSetPassword, role) ->
              if (mustSetPassword) step = Step.PASSWORD else onSignedIn(role)
            }
          },
          enabled = code.length == 6 && !busy,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("verify_code_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) { Text("Sign in") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { code = ""; step = Step.EMAIL; message = null }) { Text("Send a new code") }
      }

      Step.PASSWORD -> {
        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New password") },
          singleLine = true, visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Type it again") },
          singleLine = true, visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = {
            if (newPassword != confirmPassword) { message = "The two passwords do not match."; return@Button }
            networkCall(work = { client.setPassword(newPassword); client.currentRole() }) { role -> onSignedIn(role) }
          },
          enabled = newPassword.length >= 8 && confirmPassword.isNotEmpty() && !busy,
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_password_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeloGold, contentColor = DeloGoldDark)
        ) { Text("Save password and continue") }
      }
    }

    message?.let {
      Spacer(modifier = Modifier.height(16.dp))
      Text(it, color = androidx.compose.ui.graphics.Color(0xFFE55B4A), fontSize = 14.sp)
    }
    if (busy) {
      Spacer(modifier = Modifier.height(16.dp))
      CircularProgressIndicator(color = DeloGold)
    }
    Spacer(modifier = Modifier.height(16.dp))
    TextButton(onClick = { if (step == Step.ADDRESS) onBack() else { step = Step.ADDRESS; message = null } }) {
      Text(if (step == Step.ADDRESS) "Back to password sign-in" else "Change server address")
    }
  }
}

private enum class Step { ADDRESS, EMAIL, CODE, PASSWORD }
