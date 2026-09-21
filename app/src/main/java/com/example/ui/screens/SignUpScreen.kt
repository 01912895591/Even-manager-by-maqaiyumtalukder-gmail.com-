package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.GoogleAccountSignInDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings
import kotlinx.coroutines.launch

enum class PasswordStrengthLevel(val label: String, val color: Color, val progress: Float) {
  NONE("", Color.Transparent, 0f),
  WEAK("Weak", Color(0xFFE53935), 0.33f),
  MEDIUM("Medium", Color(0xFFFB8C00), 0.66f),
  STRONG("Strong", Color(0xFF43A047), 1.0f)
}

fun evaluatePasswordStrength(password: String): PasswordStrengthLevel {
  if (password.isEmpty()) return PasswordStrengthLevel.NONE
  val lengthValid = password.length >= 8
  val hasDigit = password.any { it.isDigit() }
  val hasLetter = password.any { it.isLetter() }
  val hasSpecialOrUpper = password.any { it.isUpperCase() || !it.isLetterOrDigit() }

  return when {
    lengthValid && hasDigit && hasLetter && hasSpecialOrUpper -> PasswordStrengthLevel.STRONG
    lengthValid && hasDigit -> PasswordStrengthLevel.MEDIUM
    else -> PasswordStrengthLevel.WEAK
  }
}

@Composable
fun SignUpScreen(
  onSignUpWithCredentials: (name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
  onGoogleSignInConfirmed: (name: String, email: String, photoUrl: String?) -> Unit,
  onNavigateToSignIn: () -> Unit,
  googleWebClientId: String = "",
  onSaveGoogleWebClientId: ((String) -> Unit)? = null,
  language: String = "en",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }
  var isGoogleSubmitting by remember { mutableStateOf(false) }
  var showGoogleSignInDialog by remember { mutableStateOf(false) }

  val strength = evaluatePasswordStrength(password)
  val animatedProgress by animateFloatAsState(targetValue = strength.progress, label = "strength_progress")
  val animatedColor by animateColorAsState(targetValue = strength.color, label = "strength_color")

  fun triggerGoogleSignUp() {
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
          errorMessage = null
          showGoogleSignInDialog = true
        }
      }
    }
  }

  fun executeSignUp() {
    focusManager.clearFocus()
    val cleanName = fullName.trim()
    val cleanEmail = email.trim()

    if (cleanName.isEmpty()) {
      errorMessage = "Please enter your full name"
      return
    }
    if (cleanEmail.isEmpty()) {
      errorMessage = "Please enter your email address"
      return
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
      errorMessage = "Please enter a valid email address"
      return
    }
    // Password requirement: minimum 8 characters
    if (password.length < 8) {
      errorMessage = "Password must be at least 8 characters long"
      return
    }
    // Password requirement: at least 1 number
    if (!password.any { it.isDigit() }) {
      errorMessage = "Password must include at least one number (0-9)"
      return
    }
    if (password != confirmPassword) {
      errorMessage = "Passwords do not match"
      return
    }

    errorMessage = null
    isSubmitting = true

    onSignUpWithCredentials(
      cleanName,
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

  // Google Account Chooser & Fallback Dialog
  if (showGoogleSignInDialog) {
    val suggestedAccounts = remember(email, fullName) {
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
        val finalName = if (fullName.isNotBlank()) fullName.trim() else name
        onGoogleSignInConfirmed(finalName, confirmedEmail, null)
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
    // Header Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(PlumDark)
        .padding(horizontal = 20.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AccentGold.copy(alpha = 0.2f))
            .border(1.5.dp, AccentGold, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "User Plus",
            tint = AccentGold,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = AppStrings.get("create_account", language),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = AccentGoldLight
          )
          Text(
            text = "Join to create and manage events effortlessly",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp
          )
        }
      }
    }

    // Form Container (Constrained width for pristine CTA button placement across all screen sizes)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .padding(horizontal = 18.dp, vertical = 14.dp)
        .padding(bottom = 20.dp),
      horizontalAlignment = Alignment.Start
    ) {
      // Screen Title & Subtitle (No duplicate top tab switcher)
      Text(
        text = AppStrings.get("create_account", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = DeepPlum
      )
      Text(
        text = "Fill in the details below to create your account.",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
      )

      // Error Alert Banner
      AnimatedVisibility(visible = errorMessage != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .testTag("signup_error_banner"),
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
                modifier = Modifier.size(16.dp)
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
                  .height(36.dp)
                  .testTag("signup_error_google_recovery_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = DeepPlum,
                  contentColor = Color.White
                )
              ) {
                Text(
                  text = if (language == "bn") "Google অ্যাকাউন্ট দিয়ে চালিয়ে যান" else "Sign in with Google Account",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      // 1. Full Name
      OutlinedTextField(
        value = fullName,
        onValueChange = {
          fullName = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text("Full Name", fontSize = 13.sp) },
        placeholder = { Text("e.g. Alex Morgan", fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Person, contentDescription = "Name", tint = DeepPlum, modifier = Modifier.size(20.dp))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = DeepPlum,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("signup_name_field")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Email Address
      OutlinedTextField(
        value = email,
        onValueChange = {
          email = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text("Email Address", fontSize = 13.sp) },
        placeholder = { Text("your.email@gmail.com", fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Email, contentDescription = "Email", tint = DeepPlum, modifier = Modifier.size(20.dp))
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
          .testTag("signup_email_field")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Password
      OutlinedTextField(
        value = password,
        onValueChange = {
          password = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text("Password", fontSize = 13.sp) },
        placeholder = { Text("Min 8 characters & 1 number", fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = "Password", tint = DeepPlum, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Toggle password visibility",
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
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
          .testTag("signup_password_field")
      )

      // Password Requirement Hint & Strength Feedback (Issue 4)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Min 8 chars, at least 1 number",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = TextMuted,
              modifier = Modifier.testTag("password_requirements_hint")
            )
          }

          if (strength != PasswordStrengthLevel.NONE) {
            Text(
              text = "Strength: ${strength.label}",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = animatedColor,
              modifier = Modifier.testTag("password_strength_label")
            )
          }
        }

        if (password.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .testTag("password_strength_indicator"),
            color = animatedColor,
            trackColor = BorderSubtle
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 4. Confirm Password
      OutlinedTextField(
        value = confirmPassword,
        onValueChange = {
          confirmPassword = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text("Confirm Password", fontSize = 13.sp) },
        placeholder = { Text("Re-enter password", fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = "Confirm password", tint = DeepPlum, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
          IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
            Icon(
              imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Toggle confirm password visibility",
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { executeSignUp() }),
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
          .testTag("signup_confirm_password_field")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Primary Sign Up CTA Button
      Button(
        onClick = { executeSignUp() },
        enabled = !isSubmitting && !isGoogleSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("signup_submit_button"),
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
          Text(text = "Creating account...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        } else {
          Text(
            text = AppStrings.get("create_account", language),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

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

      // Sleek, Beautiful Google Sign-Up Button
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
          .clickable(enabled = !isSubmitting && !isGoogleSubmitting) {
            triggerGoogleSignUp()
          }
          .testTag("signup_google_button"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if (isGoogleSubmitting) {
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

      // Single, Clean Link to Sign In
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToSignIn() }
          .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (language == "bn") "ইতিমধ্যে অ্যাকাউন্ট আছে? " else "Already have an account? ",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          fontSize = 13.sp
        )
        Text(
          text = if (language == "bn") "সাইন-ইন করুন" else AppStrings.get("sign_in", language),
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = DeepPlum,
          fontSize = 13.sp,
          modifier = Modifier.testTag("signup_signin_link")
        )
      }
    }
  }
}
