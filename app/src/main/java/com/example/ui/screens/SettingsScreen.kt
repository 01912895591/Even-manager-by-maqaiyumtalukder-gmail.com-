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
}
