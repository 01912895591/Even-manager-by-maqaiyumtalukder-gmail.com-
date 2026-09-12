package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordSheet(
  initialEmail: String = "",
  onDismiss: () -> Unit,
  onRequestReset: (email: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onResetPassword: (email: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onPasswordResetSuccess: (email: String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var email by remember { mutableStateOf(initialEmail) }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }

  var isEmailVerified by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSuccess by remember { mutableStateOf(false) }

  fun handleVerifyEmail() {
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty()) {
      errorMessage = "Please enter your email address"
      return
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
      errorMessage = "Please enter a valid email address"
      return
    }

    errorMessage = null
    isSubmitting = true

    onRequestReset(
      cleanEmail,
      {
        isSubmitting = false
        isEmailVerified = true
      },
      { error ->
        isSubmitting = false
        errorMessage = error
      }
    )
  }

  fun handleUpdatePassword() {
    val cleanEmail = email.trim()
    if (newPassword.length < 8) {
      errorMessage = "Password must be at least 8 characters long"
      return
    }
    if (!newPassword.any { it.isDigit() }) {
      errorMessage = "Password must include at least one number"
      return
    }
    if (newPassword != confirmPassword) {
      errorMessage = "Passwords do not match"
      return
    }

    errorMessage = null
    isSubmitting = true

    onResetPassword(
      cleanEmail,
      newPassword,
      {
        isSubmitting = false
        isSuccess = true
      },
      { error ->
        isSubmitting = false
        errorMessage = error
      }
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("forgot_password_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .navigationBarsPadding()
        .padding(bottom = 24.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(DeepPlum.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Key,
              contentDescription = null,
              tint = DeepPlum,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = if (isSuccess) "Password Reset Complete" else "Reset Password",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isSuccess) "Your account is secured" else "Enter your registered email to reset",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              fontSize = 12.sp
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("forgot_password_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Error Banner
      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .testTag("forgot_password_error_banner"),
          color = MaterialTheme.colorScheme.errorContainer
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Error",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = errorMessage.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp
            )
          }
        }
      }

      if (isSuccess) {
        // Success View
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Success",
              tint = Color(0xFF2E7D32),
              modifier = Modifier.size(34.dp)
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Password Changed Successfully!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Your password has been updated. You can now sign in with your new credentials.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = {
              onPasswordResetSuccess(email.trim())
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("forgot_password_success_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentGold,
              contentColor = PlumDark
            )
          ) {
            Text("Back to Sign In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      } else if (!isEmailVerified) {
        // Step 1: Request Reset
        Text(
          text = "Enter the email address registered with your account. A reset token will be verified and you will be able to set a new password immediately.",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = email,
          onValueChange = {
            email = it
            if (errorMessage != null) errorMessage = null
          },
          label = { Text("Email Address") },
          placeholder = { Text("your.email@gmail.com") },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = "Email", tint = DeepPlum)
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = { handleVerifyEmail() }),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepPlum,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("forgot_password_email_field")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = { handleVerifyEmail() },
          enabled = !isSubmitting,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("forgot_password_verify_button"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark,
            disabledContainerColor = AccentGold.copy(alpha = 0.5f)
          )
        ) {
          if (isSubmitting) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = PlumDark,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Checking email...", fontWeight = FontWeight.Bold)
          } else {
            Text("Send Reset Link & Continue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      } else {
        // Step 2: Set New Password
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(8.dp)),
          color = DeepPlum.copy(alpha = 0.08f)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = DeepPlum,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Account verified: ${email.trim()}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = DeepPlum,
              fontSize = 12.sp
            )
          }
        }

        OutlinedTextField(
          value = newPassword,
          onValueChange = {
            newPassword = it
            if (errorMessage != null) errorMessage = null
          },
          label = { Text("New Password") },
          placeholder = { Text("Minimum 8 characters") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = "New Password", tint = DeepPlum)
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle password visibility",
                tint = TextMuted
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
          ),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepPlum,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("forgot_password_new_password_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            if (errorMessage != null) errorMessage = null
          },
          label = { Text("Confirm New Password") },
          placeholder = { Text("Re-enter new password") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = "Confirm New Password", tint = DeepPlum)
          },
          trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
              Icon(
                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle confirm password visibility",
                tint = TextMuted
              )
            }
          },
          visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = { handleUpdatePassword() }),
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepPlum,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("forgot_password_confirm_password_field")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Requirement: Minimum 8 characters and at least 1 number",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = { handleUpdatePassword() },
          enabled = !isSubmitting,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("forgot_password_submit_button"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark,
            disabledContainerColor = AccentGold.copy(alpha = 0.5f)
          )
        ) {
          if (isSubmitting) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = PlumDark,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Updating password...", fontWeight = FontWeight.Bold)
          } else {
            Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    }
  }
}
