package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.GoogleAuthService
import com.example.ui.components.ForgotPasswordSheet
import com.example.ui.components.GoogleAccountSignInDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
  onSignInWithCredentials: (email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onGoogleSignInConfirmed: (name: String, email: String, photoUrl: String?) -> Unit,
  onRequestPasswordReset: (email: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onResetPassword: (email: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onNavigateToSignUp: () -> Unit,
  googleWebClientId: String = "",
  autoLoginEnabled: Boolean = true,
  onSaveGoogleWebClientId: ((String) -> Unit)? = null,
  language: String = "en",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }
  var isGoogleSubmitting by remember { mutableStateOf(false) }
  var isAutoLoggingIn by remember { mutableStateOf(false) }
  var showForgotPasswordSheet by remember { mutableStateOf(false) }
  var showGoogleSignInDialog by remember { mutableStateOf(false) }

  // Automatic Google Auto-Login quietly on screen launch
  LaunchedEffect(Unit) {
    if (autoLoginEnabled && activity != null) {
      val activeClientId = GoogleAuthService.getActiveClientId(googleWebClientId)
      if (!activeClientId.isNullOrBlank()) {
        isAutoLoggingIn = true
        val user = GoogleAuthService.attemptAutoLogin(activity, activeClientId)
        isAutoLoggingIn = false
        if (user != null) {
          onGoogleSignInConfirmed(user.displayName, user.email, user.photoUrl)
        }
      }
    }
  }

  fun triggerGoogleSignIn() {
    focusManager.clearFocus()
    errorMessage = null
    val activeClientId = GoogleAuthService.getActiveClientId(googleWebClientId)

    if (activity == null) {
      showGoogleSignInDialog = true
      return
    }

    isGoogleSubmitting = true
    coroutineScope.launch {
      val result = GoogleAuthService.signInWithGoogleCredentialManager(
        activity,
        activeClientId ?: GoogleAuthService.DEFAULT_WEB_CLIENT_ID
      )
      isGoogleSubmitting = false
      result.onSuccess { user ->
        onGoogleSignInConfirmed(user.displayName, user.email, user.photoUrl)
      }.onFailure { err ->
        val msg = err.message ?: ""
        if (msg.contains("cancelled", ignoreCase = true)) {
          // User swiped or dismissed Google account prompt
        } else {
          // Device/emulator has no Google credentials registered or restriction encountered
          errorMessage = null
          showGoogleSignInDialog = true
        }
      }
    }
  }

  fun executeSignIn() {
    focusManager.clearFocus()
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty()) {
      errorMessage = "Please enter your email address"
      return
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
      errorMessage = "Please enter a valid email address"
      return
    }
    if (password.isEmpty()) {
      errorMessage = "Please enter your password"
      return
    }

    errorMessage = null
    isSubmitting = true

    onSignInWithCredentials(
      cleanEmail,
      password,
      {
        isSubmitting = false
      },
      { error ->
        isSubmitting = false
        errorMessage = error
      }
    )
  }

  // Forgot Password Modal Sheet
  if (showForgotPasswordSheet) {
    ForgotPasswordSheet(
      initialEmail = email.trim(),
      onDismiss = { showForgotPasswordSheet = false },
      onRequestReset = onRequestPasswordReset,
      onResetPassword = onResetPassword,
      onPasswordResetSuccess = { resetEmail ->
        showForgotPasswordSheet = false
        email = resetEmail
        password = ""
        errorMessage = null
      }
    )
  }

  // Google Account Chooser & Fallback Dialog
  if (showGoogleSignInDialog) {
    val suggestedAccounts = remember(email) {
      val list = mutableListOf<String>()
      if (email.isNotBlank() && email.contains("@")) {
        list.add(email.trim())
      }
      if (!list.contains("maqaiyumtalukder@gmail.com")) {
        list.add("maqaiyumtalukder@gmail.com")
      }
      list
    }

    GoogleAccountSignInDialog(
      deviceAccounts = suggestedAccounts,
      initialEmail = if (email.isNotBlank()) email.trim() else "maqaiyumtalukder@gmail.com",
      onConfirm = { name, confirmedEmail ->
        showGoogleSignInDialog = false
        errorMessage = null
        onGoogleSignInConfirmed(name, confirmedEmail, null)
      },
      onDismiss = { showGoogleSignInDialog = false },
      language = language
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Brand Banner Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(PlumDark)
        .padding(horizontal = 20.dp, vertical = 14.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AccentGold.copy(alpha = 0.2f))
            .border(1.5.dp, AccentGold, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "Event Manager Logo",
            tint = AccentGold,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = AppStrings.get("app_name", language),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = AccentGoldLight
          )
          Text(
            text = "Sign in to manage your events",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
          )
        }
      }
    }

    // Form Container (Constrained width for pristine CTA button placement across all screen sizes)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .padding(bottom = 24.dp),
      horizontalAlignment = Alignment.Start
    ) {
      // Screen Title & Subtitle (Replacing redundant top tab switcher)
      Text(
        text = AppStrings.get("sign_in", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = DeepPlum
      )
      Text(
        text = "Welcome back! Enter your details to continue.",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
      )

      // Error Alert Banner
      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .testTag("signin_error_banner"),
          color = MaterialTheme.colorScheme.errorContainer
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
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
            if (errorMessage?.contains("Google", ignoreCase = true) == true ||
                errorMessage?.contains("credential", ignoreCase = true) == true) {
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  errorMessage = null
                  showGoogleSignInDialog = true
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(38.dp)
                  .testTag("signin_error_google_recovery_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = DeepPlum,
                  contentColor = Color.White
                )
              ) {
                Text(
                  text = if (language == "bn") "Google অ্যাকাউন্ট দিয়ে সাইন-ইন করুন" else "Sign in with Google Account",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // Email Field
      OutlinedTextField(
        value = email,
        onValueChange = {
          email = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text("Email address") },
        placeholder = { Text("your.email@gmail.com") },
        leadingIcon = {
          Icon(Icons.Default.Email, contentDescription = "Email", tint = DeepPlum)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = DeepPlum,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("signin_email_field")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Password Field
      OutlinedTextField(
        value = password,
        onValueChange = {
          password = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text("Password") },
        placeholder = { Text("Enter password") },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = "Password", tint = DeepPlum)
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Toggle password",
              tint = TextMuted
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { executeSignIn() }),
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
          .testTag("signin_password_field")
      )

      // Forgot Password Link (Directly below Password field)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Forgot password?",
          style = MaterialTheme.typography.bodySmall,
          fontSize = 12.sp,
          color = DeepPlum,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable { showForgotPasswordSheet = true }
            .testTag("signin_forgot_password_link")
        )
      }

      // Sign In Button
      Button(
        onClick = { executeSignIn() },
        enabled = !isSubmitting && !isGoogleSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("signin_submit_button"),
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
          Text("Signing in...", fontWeight = FontWeight.Bold)
        } else {
          Text(
            text = AppStrings.get("sign_in", language),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Divider
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
        Text(
          text = if (language == "bn") "  অথবা  " else "  or continue with  ",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          fontSize = 12.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Sleek, Beautiful Google Sign-In Button
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
          .clickable(enabled = !isSubmitting && !isGoogleSubmitting && !isAutoLoggingIn) {
            triggerGoogleSignIn()
          }
          .testTag("signin_google_button"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if (isGoogleSubmitting || isAutoLoggingIn) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = DeepPlum,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = if (language == "bn") "সংযুক্ত হচ্ছে..." else "Signing in...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DeepPlum
              )
            }
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              // Stylized Google 'G' Mark
              Surface(
                modifier = Modifier.size(26.dp),
                shape = CircleShape,
                color = Color(0xFFF8F9FA),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = if (language == "bn") "Google দিয়ে চালিয়ে যান" else "Continue with Google",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Single, Clean Way to Switch to Sign Up
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToSignUp() }
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (language == "bn") "অ্যাকাউন্ট নেই? " else "Don't have an account? ",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          fontSize = 13.sp
        )
        Text(
          text = if (language == "bn") "নতুন অ্যাকাউন্ট তৈরি করুন" else AppStrings.get("create_account", language),
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = DeepPlum,
          fontSize = 13.sp,
          modifier = Modifier.testTag("signin_signup_link")
        )
      }
    }
  }
}
