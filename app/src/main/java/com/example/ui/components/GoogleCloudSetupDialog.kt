package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted

@Composable
fun GoogleCloudSetupDialog(
  currentClientId: String,
  onSaveClientId: (String) -> Unit,
  onDismiss: () -> Unit,
  language: String = "en"
) {
  val clipboardManager = LocalClipboardManager.current
  var inputClientId by remember { mutableStateOf(currentClientId) }
  var copiedPackage by remember { mutableStateOf(false) }
  val packageName = "com.aistudio.eventmanager.vzkpmt"

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(AccentGold.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Cloud,
          contentDescription = null,
          tint = DeepPlum,
          modifier = Modifier.size(26.dp)
        )
      }
    },
    title = {
      Text(
        text = if (language == "bn") "Google Cloud Console সেটআপ গাইড" else "Google Cloud Console Setup",
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = if (language == "bn") {
            "রিয়েল Google One-Tap ও Auto Login চালু করতে Google Cloud Console থেকে একটি Web Client ID প্রয়োজন:"
          } else {
            "To enable real Google Auto-Login and One-Tap Sign-In, configure a Web Client ID from Google Cloud Console:"
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 13.sp
        )

        // Step 1: Package Name
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = if (language == "bn") "১. আপনার অ্যাপ প্যাকেজ নেম:" else "1. App Package Name:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(packageName))
                  copiedPackage = true
                },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = if (copiedPackage) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                  contentDescription = "Copy Package Name",
                  tint = if (copiedPackage) Emerald else DeepPlum,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        // Step 2: Cloud Console Steps
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (language == "bn") "২. ক্লাউড কনসোলে করণীয়:" else "2. Google Cloud Console Steps:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )
          val instructions = if (language == "bn") listOf(
            "console.cloud.google.com এ যান এবং প্রজেক্ট সিলেক্ট করুন।",
            "APIs & Services > OAuth consent screen এ যান এবং User Type 'External' দিয়ে সেভ করুন।",
            "APIs & Services > Credentials > Create Credentials > 'OAuth client ID' সিলেক্ট করুন।",
            "Application type নির্বাচন করুন: 'Web application' (গুরুত্বপূর্ণ: Credential Manager এর জন্য Web Client ID লাগে)।",
            "জেনারেট হওয়া Web Client ID টি কপি করে নিচের বক্সে পেস্ট করুন।"
          ) else listOf(
            "Open console.cloud.google.com and select your project.",
            "Go to APIs & Services > OAuth consent screen (choose 'External', add email/profile scopes).",
            "Go to APIs & Services > Credentials > Create Credentials > 'OAuth client ID'.",
            "Select Application type: 'Web application' (Credential Manager uses Web Client ID).",
            "Copy the generated Web Client ID and paste it in the box below."
          )

          instructions.forEachIndexed { idx, item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = "${idx + 1}. ",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = DeepPlum,
                fontSize = 12.sp
              )
              Text(
                text = item,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }

        // Step 3: Input Field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (language == "bn") "৩. Google Web Client ID পেস্ট করুন:" else "3. Enter Google Web Client ID:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )
          OutlinedTextField(
            value = inputClientId,
            onValueChange = { inputClientId = it },
            placeholder = {
              Text("xxxx.apps.googleusercontent.com", fontSize = 11.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Key, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DeepPlum,
              unfocusedBorderColor = BorderSubtle
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("google_web_client_id_input")
          )
        }

        // Native Account Picker note
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
          color = Emerald.copy(alpha = 0.08f)
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (language == "bn") {
                "ক্লাউড আইডি ছাড়াও আপনার ডিভাইসের যেকোনো গুগল অ্যাকাউন্ট সিলেক্ট করে আপনি সরাসরি লগইন করতে পারেন।"
              } else {
                "Note: Even without Cloud ID, native on-device Google Account selection is fully supported!"
              },
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = Emerald
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSaveClientId(inputClientId.trim())
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("save_google_client_id_button")
      ) {
        Text(
          text = if (language == "bn") "সংরক্ষণ করুন" else "Save Client ID",
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("close_google_setup_button")
      ) {
        Text(
          text = if (language == "bn") "বন্ধ করুন" else "Close",
          color = TextMuted,
          fontWeight = FontWeight.Medium
        )
      }
    },
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface
  )
}
