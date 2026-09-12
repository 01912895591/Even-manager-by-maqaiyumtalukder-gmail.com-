package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.parseColorHex
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings

data class CoverThemePreset(
  val name: String,
  val hex1: String,
  val hex2: String,
  val label: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewEventScreen(
  onClose: () -> Unit,
  onCreateEvent: (
    title: String,
    category: String,
    colorHex: String,
    coverPhotoUri: String?,
    date: String,
    time: String,
    location: String,
    budget: Double,
    description: String,
    dateTimeMillis: Long
  ) -> Unit,
  defaultCurrency: String = "৳",
  language: String = "en",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var title by remember { mutableStateOf("") }
  var titleError by remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf("Wedding") }

  // Dynamic calendar initialization
  val calendar = remember {
    Calendar.getInstance().apply {
      add(Calendar.DAY_OF_MONTH, 7)
      set(Calendar.HOUR_OF_DAY, 19)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
  }
  val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
  val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }

  var selectedDateTimeMillis by remember { mutableStateOf(calendar.timeInMillis) }
  var date by remember { mutableStateOf(dateFormatter.format(calendar.time)) }
  var time by remember { mutableStateOf(timeFormatter.format(calendar.time)) }

  val datePickerDialog = remember(context) {
    DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        selectedDateTimeMillis = calendar.timeInMillis
        date = dateFormatter.format(calendar.time)
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH)
    )
  }

  val timePickerDialog = remember(context) {
    TimePickerDialog(
      context,
      { _, hourOfDay, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        selectedDateTimeMillis = calendar.timeInMillis
        time = timeFormatter.format(calendar.time)
      },
      calendar.get(Calendar.HOUR_OF_DAY),
      calendar.get(Calendar.MINUTE),
      false
    )
  }

  var location by remember { mutableStateOf("") }
  var budgetText by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  // Cover photo & Theme state
  var selectedCoverImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedThemeHex by remember { mutableStateOf("#1F6E52") }
  var selectedThemeName by remember { mutableStateOf("Royal Emerald") }
  var showCoverThemePicker by remember { mutableStateOf(false) }

  // System photo picker with persistent local file copy
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val coversDir = File(context.filesDir, "event_covers").apply { mkdirs() }
        val destFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
          destFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        selectedCoverImageUri = Uri.fromFile(destFile)
        selectedThemeName = "Custom Gallery Photo"
      } catch (e: Exception) {
        selectedCoverImageUri = uri
        selectedThemeName = "Custom Gallery Photo"
      }
    }
  }

  val themePresets = listOf(
    CoverThemePreset("Royal Emerald", "#1F6E52", "#134937", "Walima & Nikah"),
    CoverThemePreset("Imperial Plum", "#4A1030", "#2E0A1E", "Eid & Gala"),
    CoverThemePreset("Golden Amber", "#D4AF6A", "#99732B", "Birthday & Jubilee"),
    CoverThemePreset("Sapphire Blue", "#2E6B8E", "#1B435B", "Aqiqah & Family"),
    CoverThemePreset("Sunset Coral", "#C85A32", "#843214", "Anniversary"),
    CoverThemePreset("Modern Slate", "#4A4458", "#2E2A36", "Ceremony & Memorial")
  )

  val categories = listOf("Wedding", "Eid", "Birthday", "Aqiqah", "Funeral", "Custom")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Bar with Close (X) Icon & Title
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onClose,
          modifier = Modifier.testTag("new_event_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = AppStrings.get("new_event", language),
          style = MaterialTheme.typography.titleLarge,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    // Scrollable Form Body
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      // Interactive Cover Photo / Theme Box
      val currentColor = parseColorHex(selectedThemeHex)
      val currentGradient = Brush.verticalGradient(
        colors = listOf(currentColor, currentColor.copy(alpha = 0.75f))
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(currentGradient)
          .border(
            width = 2.dp,
            color = AccentGold.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp)
          )
          .clickable { showCoverThemePicker = true }
          .testTag("new_event_cover_photo_box"),
        contentAlignment = Alignment.Center
      ) {
        if (selectedCoverImageUri != null) {
          AsyncImage(
            model = selectedCoverImageUri,
            contentDescription = "Event Cover Photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          // Gradient scrim for text contrast
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.25f),
                    Color.Black.copy(alpha = 0.75f)
                  )
                )
              )
          )
        }

        // Decorative foreground & label
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.35f))
              .border(1.5.dp, AccentGold, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (selectedCoverImageUri != null) Icons.Default.Image else getCategoryIcon(selectedCategory),
              contentDescription = "Event Cover",
              tint = AccentGold,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = if (title.isNotBlank()) title else "Select Cover Theme & Photo",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = if (selectedCoverImageUri != null) "Gallery Photo Loaded • Tap to Change" else "$selectedCategory • $selectedThemeName",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
          )
        }

        // "Change Theme / Photo" Badge in top-right
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp)
            .clip(RoundedCornerShape(20.dp)),
          color = Color.Black.copy(alpha = 0.6f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = AccentGold,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (selectedCoverImageUri != null) "Photo Set" else "Change",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontSize = 10.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Event Title
      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          if (titleError && it.isNotBlank()) titleError = false
        },
        label = { Text(AppStrings.get("event_title", language)) },
        placeholder = { Text("e.g. Farhan & Maya's Walima") },
        isError = titleError,
        supportingText = {
          if (titleError) {
            Text("Please enter an event title", color = MaterialTheme.colorScheme.error)
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = DeepPlum,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_event_title_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Category Chips
      Text(
        text = AppStrings.get("category", language),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        categories.forEach { cat ->
          val isSelected = selectedCategory.equals(cat, ignoreCase = true)
          val catColor = getCategoryColor(cat)

          FilterChip(
            selected = isSelected,
            onClick = {
              selectedCategory = cat
              // Auto-update theme color to category default if using default
              val matchedPreset = themePresets.find {
                it.name.contains(cat, ignoreCase = true) || it.label.contains(cat, ignoreCase = true)
              }
              if (matchedPreset != null) {
                selectedThemeHex = matchedPreset.hex1
                selectedThemeName = matchedPreset.name
              }
            },
            label = {
              Text(
                text = cat,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
              )
            },
            leadingIcon = {
              Icon(
                imageVector = getCategoryIcon(cat),
                contentDescription = null,
                tint = if (isSelected) Color.White else catColor,
                modifier = Modifier.size(16.dp)
              )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = catColor,
              selectedLabelColor = Color.White,
              containerColor = MaterialTheme.colorScheme.surface,
              labelColor = MaterialTheme.colorScheme.onSurface
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) catColor else BorderSubtle,
              enabled = true,
              selected = isSelected
            ),
            modifier = Modifier.testTag("new_event_cat_$cat")
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Date & Time (Row of two OutlinedTextFields with interactive Calendar and Clock dialogs)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Date Picker Field
        Box(
          modifier = Modifier
            .weight(1f)
            .testTag("new_event_date_input")
        ) {
          OutlinedTextField(
            value = date,
            onValueChange = { },
            readOnly = true,
            label = { Text(AppStrings.get("date", language)) },
            leadingIcon = {
              IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date from Calendar", tint = DeepPlum)
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DeepPlum,
              unfocusedBorderColor = BorderSubtle,
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
          )
          // Transparent clickable overlay so tapping anywhere in the field pops up the calendar
          Box(
            modifier = Modifier
              .matchParentSize()
              .clip(RoundedCornerShape(12.dp))
              .clickable { datePickerDialog.show() }
          )
        }

        // Time Picker Field
        Box(
          modifier = Modifier
            .weight(1f)
            .testTag("new_event_time_input")
        ) {
          OutlinedTextField(
            value = time,
            onValueChange = { },
            readOnly = true,
            label = { Text(AppStrings.get("time", language)) },
            leadingIcon = {
              IconButton(onClick = { timePickerDialog.show() }) {
                Icon(Icons.Default.Schedule, contentDescription = "Pick Time", tint = DeepPlum)
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DeepPlum,
              unfocusedBorderColor = BorderSubtle,
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
          )
          // Transparent clickable overlay so tapping anywhere in the field pops up the time picker
          Box(
            modifier = Modifier
              .matchParentSize()
              .clip(RoundedCornerShape(12.dp))
              .clickable { timePickerDialog.show() }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Location / Venue
      OutlinedTextField(
        value = location,
        onValueChange = { location = it },
        label = { Text(AppStrings.get("location", language)) },
        placeholder = { Text("e.g. Radisson Blu Grand Ballroom") },
        leadingIcon = {
          Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = DeepPlum)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = DeepPlum,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_event_location_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Budget Input with currency prefix
      OutlinedTextField(
        value = budgetText,
        onValueChange = { budgetText = it },
        label = { Text(AppStrings.get("budget", language)) },
        placeholder = { Text("250000") },
        prefix = {
          Text(
            text = "$defaultCurrency ",
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = DeepPlum,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_event_budget_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Description / Note
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Notes / Description") },
        placeholder = { Text("Special arrangements, guest guidelines...") },
        maxLines = 3,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = DeepPlum,
          unfocusedBorderColor = BorderSubtle,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_event_desc_input")
      )

      Spacer(modifier = Modifier.height(28.dp))
    }

    // Bottom Fixed Create Event Button
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Box(modifier = Modifier.padding(16.dp)) {
        Button(
          onClick = {
            if (title.isBlank()) {
              titleError = true
              return@Button
            }
            val validBudget = budgetText.toDoubleOrNull() ?: 200000.0
            val validLocation = location.ifBlank { "Dhaka Banquet Hall" }
            onCreateEvent(
              title.trim(),
              selectedCategory,
              selectedThemeHex,
              selectedCoverImageUri?.toString(),
              date,
              time,
              validLocation,
              validBudget,
              description,
              selectedDateTimeMillis
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("new_event_submit_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = DeepPlum,
            contentColor = Color.White
          )
        ) {
          Text(
            text = AppStrings.get("create_event_button", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }

  // Cover Theme & Photo Picker Dialog
  if (showCoverThemePicker) {
    AlertDialog(
      onDismissRequest = { showCoverThemePicker = false },
      title = {
        Text(
          text = "Choose Event Theme & Cover",
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ) {
          Text(
            text = "Pick a curated color theme or upload a custom cover from your phone gallery.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Option 1: Pick from Device Photos
          OutlinedButton(
            onClick = {
              showCoverThemePicker = false
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("picker_gallery_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surface
            )
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = null,
              tint = DeepPlum,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (selectedCoverImageUri != null) "Change Photo from Gallery" else "Choose from Device Gallery",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = DeepPlum
            )
          }

          if (selectedCoverImageUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = {
                selectedCoverImageUri = null
                selectedThemeName = "Royal Emerald"
                selectedThemeHex = "#1F6E52"
                showCoverThemePicker = false
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              )
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Remove Photo & Use Color Palette", fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          HorizontalDivider(color = BorderSubtle)

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Curated Event Palettes",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Presets Grid / Column
          themePresets.forEach { preset ->
            val isChosen = selectedThemeHex.equals(preset.hex1, ignoreCase = true)
            val pColor = parseColorHex(preset.hex1)

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                  width = if (isChosen) 2.dp else 1.dp,
                  color = if (isChosen) DeepPlum else BorderSubtle,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                  selectedThemeHex = preset.hex1
                  selectedThemeName = preset.name
                  selectedCoverImageUri = null
                  showCoverThemePicker = false
                },
              color = if (isChosen) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(pColor)
                    .border(1.dp, Color.White, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  if (isChosen) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = preset.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCoverThemePicker = false }) {
          Text("Cancel", color = DeepPlum, fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
