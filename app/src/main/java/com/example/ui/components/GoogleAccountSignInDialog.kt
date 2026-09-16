package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted

/**
 * A dedicated, fail-safe Google Sign-In Dialog.
 * Triggered seamlessly whenever Android Credential Manager or system account chooser
 * encounters device/emulator restrictions ("Something went wrong / Sign in another way").
 */
@Composable
fun GoogleAccountSignInDialog(
  deviceAccounts: List<String> = emptyList(),
  initialEmail: String = "",
  onConfirm: (name: String, email: String) -> Unit,
  onDismiss: () -> Unit,
  language: String = "en"
) {
  var emailInput by remember { mutableStateOf(initialEmail) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(20.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Stylized Google 'G'
            Surface(
              modifier = Modifier.size(34.dp),
              shape = CircleShape,
              color = Color(0xFFF8F9FA),
              border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "G",
                  color = Color(0xFF4285F4),
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (language == "bn") "Google সাইন-ইন" else "Google Sign-In",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (language == "bn") "অ্যাকাউন্ট নির্বাচন করুন" else "Choose Google Account",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional explanation
        Text(
          text = if (language == "bn") {
            "আপনার Google অ্যাকাউন্ট নির্বাচন করুন অথবা ইমেইল নিশ্চিত করে সাইন-ইন সম্পন্ন করুন:"
          } else {
            "Select your Google account or confirm your email to sign in securely:"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
          fontSize = 12.sp,
          lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // If detected accounts on device
        if (deviceAccounts.isNotEmpty()) {
          Text(
            text = if (language == "bn") "ডিভাইসে প্রাপ্ত অ্যাকাউন্টসমূহ:" else "Found on this device:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DeepPlum
          )
          Spacer(modifier = Modifier.height(8.dp))

          deviceAccounts.forEach { acc ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .clickable {
                  val derivedName = acc.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                  onConfirm(derivedName, acc)
                },
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = DeepPlum,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    val displayName = acc.substringBefore("@")
                      .replace(".", " ")
                      .split(" ")
                      .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                    Text(
                      text = displayName,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = acc,
                      style = MaterialTheme.typography.bodySmall,
                      color = TextMuted,
                      fontSize = 11.sp
                    )
                  }
                }
                Icon(
                  imageVector = Icons.Default.ArrowForward,
                  contentDescription = null,
                  tint = DeepPlum,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = BorderSubtle)
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Email input for entering or confirming Google email
        OutlinedTextField(
          value = emailInput,
          onValueChange = {
            emailInput = it
            errorMessage = null
          },
          label = {
            Text(if (language == "bn") "Google ইমেল ঠিকানা" else "Google Email Address")
          },
          placeholder = { Text("example@gmail.com") },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = DeepPlum)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("google_direct_email_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              val clean = emailInput.trim()
              if (clean.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches()) {
                val derivedName = clean.substringBefore("@")
                  .replace(".", " ")
                  .split(" ")
                  .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                onConfirm(derivedName, clean)
              } else {
                errorMessage = if (language == "bn") "সঠিক ইমেল ঠিকানা লিখুন" else "Please enter a valid email address"
              }
            }
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepPlum,
            unfocusedBorderColor = BorderSubtle,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
          )
        )

        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = DangerRed,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = errorMessage.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = DangerRed,
              fontSize = 11.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Button
        Button(
          onClick = {
            val clean = emailInput.trim()
            if (clean.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches()) {
              val derivedName = clean.substringBefore("@")
                .replace(".", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
              onConfirm(derivedName, clean)
            } else {
              errorMessage = if (language == "bn") "সঠিক ইমেল ঠিকানা লিখুন" else "Please enter a valid email address"
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("google_direct_confirm_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark
          )
        ) {
          Text(
            text = if (language == "bn") "Google দিয়ে চালিয়ে যান" else "Continue with Google",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}
