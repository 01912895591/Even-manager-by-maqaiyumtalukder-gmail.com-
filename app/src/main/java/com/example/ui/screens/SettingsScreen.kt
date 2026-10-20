package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.AppDatabase
import com.example.data.util.BackupRestoreManager
import kotlinx.coroutines.launch
import com.example.data.model.AppSettingsEntity
import com.example.data.model.UserEntity
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.parseColorHex
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings

@Composable
fun SettingsScreen(
  currentUser: UserEntity?,
  settings: AppSettingsEntity,
  appDatabase: AppDatabase? = null,
  onUpdateLanguage: (String) -> Unit,
  onToggleDarkMode: (Boolean) -> Unit,
  onUpdateCurrency: (String) -> Unit,
  onUpdateAccentColor: (String) -> Unit,
  onToggleNotifications: (Boolean) -> Unit,
  onUpdateProfile: (name: String, email: String) -> Unit = { _, _ -> },
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var currencyMenuExpanded by remember { mutableStateOf(false) }
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var editName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "Event Planner") }
  var editEmail by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }

  // Backup & Restore State
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var isBackingUp by remember { mutableStateOf(false) }
  var isRestoring by remember { mutableStateOf(false) }
  var backupMessage by remember { mutableStateOf<String?>(null) }
  var restoreMessage by remember { mutableStateOf<String?>(null) }
  var showRestoreConfirmDialog by remember { mutableStateOf(false) }
  var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

  // Launcher to Export / Save Backup File (JSON)
  val exportBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    if (uri != null && appDatabase != null) {
      coroutineScope.launch {
        isBackingUp = true
        try {
          val json = BackupRestoreManager.exportDataToJson(appDatabase)
          val success = BackupRestoreManager.writeBackupToUri(context, uri, json)
          if (success) {
            backupMessage = if (settings.language == "bn") "ব্যাকআপ ফাইল সফলভাবে সেভ করা হয়েছে! (গুগল ড্রাইভ বা মেমরিতে রাখতে পারেন)" else "Backup file exported successfully! You can store it in Google Drive or device storage."
          } else {
            backupMessage = if (settings.language == "bn") "ব্যাকআপ তৈরিতে সমস্যা হয়েছে।" else "Failed to write backup file."
          }
        } catch (e: Exception) {
          backupMessage = "Error: ${e.message}"
        } finally {
          isBackingUp = false
        }
      }
    }
  }

  // Launcher to Import / Restore Backup File (JSON)
  val importBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      pendingRestoreUri = uri
      showRestoreConfirmDialog = true
    }
  }

  val currencies = listOf("৳", "$", "€", "£", "₹", "SAR", "AED")
  val accentColors = listOf(
    "#D4AF6A" to "Royal Gold",
    "#1F6E52" to "Emerald",
    "#4A1030" to "Deep Plum",
    "#285496" to "Sapphire",
    "#C85A32" to "Warm Terracotta"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Bar: "Settings" Heading
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (onBackClick != null) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier
              .testTag("settings_back_button")
              .size(38.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
          text = AppStrings.get("settings", settings.language),
          style = MaterialTheme.typography.headlineLarge,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. User Profile & Local Identity Card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Column(
          modifier = Modifier.padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            InitialsAvatar(
              name = currentUser?.name?.ifBlank { "Event Planner" } ?: "Event Planner",
              backgroundColorHex = settings.accentColorHex,
              size = 54.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentUser?.name?.ifBlank { "Event Planner" } ?: "Event Planner",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (currentUser?.email.isNullOrBlank()) {
                  if (settings.language == "bn") "ডিভাইস লোকাল প্রোফাইল" else "Device Local Profile"
                } else {
                  currentUser?.email.orEmpty()
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
              )
            }

            IconButton(
              onClick = {
                editName = currentUser?.name?.ifBlank { "Event Planner" } ?: "Event Planner"
                editEmail = currentUser?.email ?: ""
                showEditProfileDialog = true
              },
              modifier = Modifier.testTag("settings_edit_profile_button")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile",
                tint = DeepPlum
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Offline Status Badge
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Emerald.copy(alpha = 0.08f))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Emerald,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (settings.language == "bn") "সরাসরি ব্যবহারের জন্য সক্রিয় • কোনো পাসওয়ার্ডের প্রয়োজন নেই" else "Ready to use • Direct local access, no login needed",
              style = MaterialTheme.typography.bodySmall,
              color = Emerald,
              fontWeight = FontWeight.Medium,
              fontSize = 11.sp
            )
          }
        }
      }

      // Dialog to Edit Profile Name and Email
      if (showEditProfileDialog) {
        AlertDialog(
          onDismissRequest = { showEditProfileDialog = false },
          title = {
            Text(
              text = if (settings.language == "bn") "প্রোফাইল পরিবর্তন" else "Edit Profile",
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold
            )
          },
          text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text(if (settings.language == "bn") "আপনার নাম" else "Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              OutlinedTextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                label = { Text(if (settings.language == "bn") "ইমেইল (ঐচ্ছিক)" else "Email (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }
          },
          confirmButton = {
            Button(
              onClick = {
                onUpdateProfile(editName, editEmail)
                showEditProfileDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = PlumDark)
            ) {
              Text(if (settings.language == "bn") "সংরক্ষণ" else "Save")
            }
          },
          dismissButton = {
            OutlinedButton(onClick = { showEditProfileDialog = false }) {
              Text(if (settings.language == "bn") "বাতিল" else "Cancel")
            }
          }
        )
      }

      // Local Data Security & Privacy Section
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = if (settings.language == "bn") "ডিভাইস ও ডেটা নিরাপত্তা" else "Data Privacy & Storage",
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
          color = MaterialTheme.colorScheme.surface
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Emerald.copy(alpha = 0.08f))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Surface(
                  shape = CircleShape,
                  color = Emerald.copy(alpha = 0.15f),
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Security,
                      contentDescription = "Security Status",
                      tint = Emerald,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = if (settings.language == "bn") "১০০% প্রাইভেট ও অফলাইন" else "100% Private & Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald
                  )
                  Text(
                    text = if (settings.language == "bn") "কোনো অনলাইন সার্ভারে ডাটা যায় না, ফোনেই সুরক্ষিত থাকে" else "Data stays strictly inside your device's isolated storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Emerald.copy(alpha = 0.15f)
              ) {
                Text(
                  text = if (settings.language == "bn") "সুরক্ষিত" else "Private",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Emerald,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            // Local Data Vault Card
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepPlum.copy(alpha = 0.05f))
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = DeepPlum.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Data Privacy",
                    tint = DeepPlum,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (settings.language == "bn") "স্থানীয় ডাটাবেজ (Room Database)" else "Local Room Database",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (settings.language == "bn") "আপনার ইভেন্ট, বাজেট ও পরিচিতির সমস্ত তথ্য ফোনে সুরক্ষিত।" else "Events, checklists, guests and budgets are saved locally.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // OFFLINE DATA SAFETY: Local Backup & Restore Action Buttons
            Text(
              text = if (settings.language == "bn") "অফলাইন ডাটা সেফটি (ব্যাকআপ ও রিস্টোর)" else "Offline Data Safety (Backup & Restore)",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Backup Data (Download File) Button
              Button(
                onClick = {
                  val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US).format(java.util.Date())
                  val fileName = "EventPlanner_Backup_$dateStr.json"
                  exportBackupLauncher.launch(fileName)
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("backup_data_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = DeepPlum,
                  contentColor = Color.White
                ),
                enabled = !isBackingUp && !isRestoring
              ) {
                Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (settings.language == "bn") "Backup (Download)" else "Backup Data",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              // Restore Data Button
              OutlinedButton(
                onClick = {
                  importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("restore_data_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepPlum),
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = DeepPlum
                ),
                enabled = !isBackingUp && !isRestoring
              ) {
                Icon(
                  imageVector = Icons.Default.Restore,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (settings.language == "bn") "Restore Data" else "Restore Data",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Text(
              text = if (settings.language == "bn")
                "💡 ফোন পরিবর্তন বা সুরক্ষার জন্য ব্যাকআপ ফাইলটি গুগল ড্রাইভ অথবা মেমরিতে সেভ করে রাখতে পারেন।"
              else
                "💡 Save your backup JSON file to Google Drive or SD card to restore anytime.",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              fontSize = 11.sp
            )
          }
        }
      }

      // 2. Preferences Section
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = AppStrings.get("preferences", settings.language),
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
          color = MaterialTheme.colorScheme.surface
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Language Selection
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = AppStrings.get("language", settings.language),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }

              // Language Toggle Pill: English / বাংলা
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(4.dp)
              ) {
                val isEn = settings.language == "en"
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isEn) DeepPlum else Color.Transparent)
                    .clickable { onUpdateLanguage("en") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("settings_lang_en")
                ) {
                  Text(
                    text = "English",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isEn) Color.White else TextMuted
                  )
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isEn) DeepPlum else Color.Transparent)
                    .clickable { onUpdateLanguage("bn") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("settings_lang_bn")
                ) {
                  Text(
                    text = "বাংলা",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (!isEn) Color.White else TextMuted
                  )
                }
              }
            }

            // Currency Selector
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Paid, contentDescription = null, tint = AccentGold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = AppStrings.get("currency", settings.language),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }

              Box {
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .clickable { currencyMenuExpanded = true }
                    .testTag("settings_currency_dropdown"),
                  color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = "${settings.defaultCurrency} ▼",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepPlum,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                  )
                }

                DropdownMenu(
                  expanded = currencyMenuExpanded,
                  onDismissRequest = { currencyMenuExpanded = false }
                ) {
                  currencies.forEach { curr ->
                    DropdownMenuItem(
                      text = { Text(curr, fontWeight = FontWeight.Bold) },
                      onClick = {
                        onUpdateCurrency(curr)
                        currencyMenuExpanded = false
                      }
                    )
                  }
                }
              }
            }

            // Accent Color Chooser
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = AppStrings.get("accent_color", settings.language),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                accentColors.forEach { (hex, _) ->
                  val isSelected = settings.accentColorHex.equals(hex, ignoreCase = true)
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(parseColorHex(hex))
                      .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) DeepPlum else BorderSubtle,
                        shape = CircleShape
                      )
                      .clickable { onUpdateAccentColor(hex) }
                      .testTag("settings_accent_$hex")
                  )
                }
              }
            }

            // Dark Mode Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = AppStrings.get("dark_mode", settings.language),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }

              Switch(
                checked = settings.isDarkMode,
                onCheckedChange = onToggleDarkMode,
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = DeepPlum
                ),
                modifier = Modifier.testTag("settings_dark_mode_switch")
              )
            }
          }
        }
      }

      // 3. Data & Backup Section
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = AppStrings.get("data_and_backup", settings.language),
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
          color = MaterialTheme.colorScheme.surface
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Emerald, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = AppStrings.get("backup_cloud", settings.language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                  )
                  Text(
                    text = "Last synced: Today, 08:30 AM",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }
              }

              Text(
                text = "Active",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Emerald
              )
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { /* export action */ },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Download, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Export guest list to CSV",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Excel and Google Sheets compatible format",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              }
            }
          }
        }
      }

      // 4. About App Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Event Manager • v1.0.0",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Crafted with elegance for weddings, festivals & gatherings",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(60.dp))
    }
  }

  // Restore Confirmation Dialog
  if (showRestoreConfirmDialog && pendingRestoreUri != null) {
    val targetUri = pendingRestoreUri!!
    AlertDialog(
      onDismissRequest = {
        showRestoreConfirmDialog = false
        pendingRestoreUri = null
      },
      title = {
        Text(
          text = if (settings.language == "bn") "ডেটা রিস্টোর নিশ্চিতকরণ" else "Confirm Data Restore",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = if (settings.language == "bn")
            "আপনি কি ব্যাকআপ ফাইলটি থেকে ডেটা রিস্টোর করতে চান? পূর্বের বিদ্যমান ডেটার সাথে ব্যাকআপ ডেটা মার্জ ও রিস্টোর হবে।"
          else
            "Are you sure you want to restore data from this backup file? Your events, contacts, guests, and budgets will be restored."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showRestoreConfirmDialog = false
            if (appDatabase != null) {
              coroutineScope.launch {
                isRestoring = true
                try {
                  val (success, summary) = BackupRestoreManager.restoreDataFromUri(context, appDatabase, targetUri)
                  if (success && summary != null) {
                    restoreMessage = if (settings.language == "bn")
                      "সফলভাবে রিস্টোর হয়েছে!\n• ইভেন্ট: ${summary.eventsCount}টি\n• কন্ট্যাক্ট: ${summary.contactsCount}টি\n• খরচ এন্ট্রি: ${summary.expensesCount}টি\n• চেকলিস্ট: ${summary.checklistCount}টি"
                    else
                      "Data Restored Successfully!\n• Events: ${summary.eventsCount}\n• Contacts: ${summary.contactsCount}\n• Expenses: ${summary.expensesCount}\n• Checklist: ${summary.checklistCount}"
                  } else {
                    restoreMessage = if (settings.language == "bn") "রিস্টোর ব্যর্থ হয়েছে। ফাইলটি সঠিক ব্যাকআপ ফাইল নয়।" else "Failed to restore. Invalid backup file format."
                  }
                } catch (e: Exception) {
                  restoreMessage = "Restore Error: ${e.message}"
                } finally {
                  isRestoring = false
                  pendingRestoreUri = null
                }
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("Confirm Restore")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = {
          showRestoreConfirmDialog = false
          pendingRestoreUri = null
        }) {
          Text("Cancel")
        }
      }
    )
  }

  // Backup Success / Error Dialog
  if (backupMessage != null) {
    AlertDialog(
      onDismissRequest = { backupMessage = null },
      title = {
        Text(
          text = if (settings.language == "bn") "ব্যাকআপ স্ট্যাটাস" else "Backup Status",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(text = backupMessage ?: "")
      },
      confirmButton = {
        Button(
          onClick = { backupMessage = null },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("OK")
        }
      }
    )
  }

  // Restore Result Dialog
  if (restoreMessage != null) {
    AlertDialog(
      onDismissRequest = { restoreMessage = null },
      title = {
        Text(
          text = if (settings.language == "bn") "রিস্টোর ফলাফল" else "Restore Result",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(text = restoreMessage ?: "")
      },
      confirmButton = {
        Button(
          onClick = { restoreMessage = null },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("OK")
        }
      }
    )
  }
}
