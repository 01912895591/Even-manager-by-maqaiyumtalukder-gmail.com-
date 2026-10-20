package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.core.content.ContextCompat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.delay
import com.example.service.LocalEventReminderReceiver
import android.widget.Toast
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CateringPlanEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventDayEntity
import com.example.data.model.EventEntity
import com.example.ui.components.CategoryExpensePieChart
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.StatMiniCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.WhatsAppIconButton
import com.example.ui.components.openWhatsAppMessage
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.PlumDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.util.AppStrings
import com.example.ui.util.EventSummaryExporter
import com.example.ui.viewmodel.CateringEstimate
import com.example.ui.viewmodel.EventBudgetSummary
import com.example.ui.viewmodel.EventGuestSummary
import kotlin.math.ceil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
  event: EventEntity?,
  checklist: List<ChecklistItemEntity>,
  allContacts: List<ContactEntity>,
  eventGuests: List<EventContactCrossRef>,
  budgetSummary: EventBudgetSummary,
  guestSummary: EventGuestSummary,
  currencySymbol: String,
  language: String,
  onBack: () -> Unit,
  onToggleChecklistItem: (ChecklistItemEntity) -> Unit,
  onAddChecklistItem: (title: String, dueDate: String, category: String) -> Unit = { _, _, _ -> },
  onUpdateChecklistItem: (ChecklistItemEntity) -> Unit = {},
  onDeleteChecklistItem: (ChecklistItemEntity) -> Unit,
  onUpdateGuestStatus: (contactId: Long, newStatus: String) -> Unit,
  onMarkInviteSent: (contactId: Long) -> Unit = {},
  onNavigateToAddGuests: () -> Unit,
  onNavigateToBudget: () -> Unit,
  onEditEvent: () -> Unit = {},
  onUpdateContact: (ContactEntity) -> Unit = {},
  onAddVendorToEvent: (name: String, phone: String, note: String) -> Unit = { _, _, _ -> },
  onRemoveVendorFromEvent: (contactId: Long) -> Unit = {},
  onLinkContactToEvent: (contactId: Long) -> Unit = {},
  onImportPhoneVendors: (List<Triple<String, String, String>>) -> Unit = {},
  eventDays: List<EventDayEntity> = emptyList(),
  onAddEventDay: (dayTitle: String, dateFormatted: String, timeFormatted: String, dateTimeMillis: Long, location: String, notes: String) -> Unit = { _, _, _, _, _, _ -> },
  onUpdateEventDay: (EventDayEntity) -> Unit = {},
  onDeleteEventDay: (EventDayEntity) -> Unit = {},
  cateringPlan: CateringPlanEntity? = null,
  cateringEstimate: CateringEstimate = CateringEstimate(),
  onSaveCateringPlan: (perPlateCost: Double, bufferPercent: Int) -> Unit = { _, _ -> },
  onAddCateringToBudget: (estimatedCost: Double, recommendedPlates: Int, perPlateCost: Double, bufferPercent: Int) -> Unit = { _, _, _, _ -> },
  onUpdatePlannedBudget: (Double) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf(
    AppStrings.get("checklist", language),
    AppStrings.get("guests", language),
    AppStrings.get("vendors", language),
    AppStrings.get("budget", language)
  )

  var showAddTaskDialog by remember { mutableStateOf(false) }
  var showAddVendorDialogTrigger by remember { mutableStateOf(false) }
  var editingChecklistItem by remember { mutableStateOf<ChecklistItemEntity?>(null) }
  var showShareSummaryDialog by remember { mutableStateOf(false) }

  // Days left calculation
  val daysLeft = if (event != null) {
    val diff = event.dateTimeMillis - System.currentTimeMillis()
    TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
  } else 0

  val coverBg = if (event != null) getCategoryColor(event.category) else Emerald

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.TopCenter
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
    // 1. Colored Cover Header (Matches Event Category)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = coverBg,
      tonalElevation = 4.dp
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        if (!event?.coverPhotoUri.isNullOrBlank()) {
          AsyncImage(
            model = event?.coverPhotoUri,
            contentDescription = "Event Cover",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .matchParentSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.35f),
                    Color.Black.copy(alpha = 0.8f)
                  )
                )
              )
          )
        }
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
        ) {
        // Back & Share
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("event_detail_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onEditEvent,
              modifier = Modifier.testTag("event_detail_edit_button")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Event",
                tint = Color.White
              )
            }

            IconButton(
              onClick = {
                if (event != null) {
                  showShareSummaryDialog = true
                }
              },
              modifier = Modifier.testTag("event_detail_share_button")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Summary",
                tint = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category Label
        Text(
          text = (event?.category ?: "EVENT").uppercase(),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = AccentGoldLight,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Title in Serif
        Text(
          text = event?.title ?: "Event Details",
          style = MaterialTheme.typography.headlineSmall,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Date + Location
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${event?.dateFormatted ?: ""} • ${event?.timeFormatted ?: ""}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
          )
        }

        if (!event?.location.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = AccentGoldLight,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = event?.location ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }

    // 1.5 Live Event Countdown Timer Card
    if (event != null) {
      Spacer(modifier = Modifier.height(4.dp))
      LiveEventCountdownCard(
        event = event,
        language = language
      )
    }

    // 2. Three Stat Mini-Cards Side by Side
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Days Left
      StatMiniCard(
        title = "$daysLeft",
        subtitle = AppStrings.get("days_left", language),
        modifier = Modifier.weight(1f),
        accentColor = DeepPlum
      )

      // Budget % Used
      StatMiniCard(
        title = "${budgetSummary.percentageSpent.toInt()}%",
        subtitle = "Budget used",
        modifier = Modifier.weight(1f),
        accentColor = AccentGold
      )

      // Guests Confirmed (e.g. "42/60")
      StatMiniCard(
        title = "${guestSummary.confirmed}/${guestSummary.totalInvited}",
        subtitle = "Confirmed",
        modifier = Modifier.weight(1f),
        accentColor = SuccessGreen
      )
    }

    // 3. Horizontal Tab Row (Checklist | Guests | Vendors | Budget)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DeepPlum,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
          if (selectedTabIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
              color = DeepPlum
            )
          }
        },
        divider = {
          HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        }
      ) {
        tabs.forEachIndexed { index, title ->
          val isSelected = selectedTabIndex == index
          Tab(
            selected = isSelected,
            onClick = { selectedTabIndex = index },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DeepPlum else TextMuted,
                maxLines = 1,
                softWrap = false
              )
            },
            modifier = Modifier.testTag("event_tab_$index")
          )
        }
      }
    }

    // 4. Tab Contents
    Box(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 20.dp)
    ) {
      when (selectedTabIndex) {
        0 -> ChecklistTabContent(
          checklist = checklist,
          onToggleItem = onToggleChecklistItem,
          onEditItem = { item -> editingChecklistItem = item },
          onDeleteItem = onDeleteChecklistItem,
          onAddTaskClick = { showAddTaskDialog = true },
          eventName = event?.title ?: "ইভেন্ট",
          language = language
        )

        1 -> GuestsTabContent(
          eventGuests = eventGuests,
          allContacts = allContacts,
          onUpdateStatus = onUpdateGuestStatus,
          onMarkInviteSent = onMarkInviteSent,
          onUpdateContact = onUpdateContact,
          onAddGuestClick = onNavigateToAddGuests,
          cateringEstimate = cateringEstimate,
          currencySymbol = currencySymbol,
          onGoToBudget = { selectedTabIndex = 3 },
          eventName = event?.title ?: "Event",
          eventDate = event?.dateFormatted ?: "",
          eventLocation = event?.location ?: "",
          language = language
        )

        2 -> VendorsTabContent(
          eventGuests = eventGuests,
          allContacts = allContacts,
          onAddVendorClick = { showAddVendorDialogTrigger = true },
          externalOpenAddDialog = showAddVendorDialogTrigger,
          onResetExternalOpenAddDialog = { showAddVendorDialogTrigger = false },
          onUpdateContact = onUpdateContact,
          onAddNewVendor = onAddVendorToEvent,
          onRemoveVendor = onRemoveVendorFromEvent,
          onLinkExistingContactAsVendor = onLinkContactToEvent,
          onImportPhoneVendors = onImportPhoneVendors,
          eventName = event?.title ?: "Event",
          eventDate = event?.dateFormatted ?: "",
          eventLocation = event?.location ?: "",
          language = language
        )

        3 -> BudgetTabContent(
          budgetSummary = budgetSummary,
          currencySymbol = currencySymbol,
          cateringPlan = cateringPlan,
          cateringEstimate = cateringEstimate,
          onSaveCateringPlan = onSaveCateringPlan,
          onAddCateringToBudget = onAddCateringToBudget,
          onOpenFullBudget = onNavigateToBudget,
          onUpdatePlannedBudget = onUpdatePlannedBudget,
          language = language
        )
      }

      // Contextual Bottom-Right Floating Action Button for active tab (Tabs 0, 1, 2 only)
      if (selectedTabIndex in 0..2) {
        val (fabIcon, fabTag, fabAction) = when (selectedTabIndex) {
          0 -> Triple(Icons.Default.Add, "event_fab_add_task", { showAddTaskDialog = true })
          1 -> Triple(Icons.Default.PersonAdd, "event_fab_add_guest", onNavigateToAddGuests)
          else -> Triple(Icons.Default.Storefront, "event_fab_add_vendor", { showAddVendorDialogTrigger = true })
        }

        FloatingActionButton(
          onClick = fabAction,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 20.dp, end = 4.dp)
            .testTag(fabTag),
          containerColor = AccentGold,
          contentColor = PlumDark,
          shape = CircleShape
        ) {
          Icon(
            imageVector = fabIcon,
            contentDescription = "Primary Action for Tab",
            modifier = Modifier.size(26.dp)
          )
        }
      }
    }
  }
    }
  }

  // Dialog to Add Task to Checklist
  if (showAddTaskDialog) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDueDate by remember { mutableStateOf(event?.dateFormatted ?: "") }
    var taskCategory by remember { mutableStateOf("General") }
    val taskCal = remember { Calendar.getInstance() }
    val taskDateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val taskDatePicker = remember(context) {
      DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
          taskCal.set(Calendar.YEAR, year)
          taskCal.set(Calendar.MONTH, month)
          taskCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
          taskDueDate = taskDateFormatter.format(taskCal.time)
        },
        taskCal.get(Calendar.YEAR),
        taskCal.get(Calendar.MONTH),
        taskCal.get(Calendar.DAY_OF_MONTH)
      )
    }

    val dialogCategories = listOf("General", "Venue", "Catering", "Decoration", "Attire", "Photography", "Guests")

    AlertDialog(
      onDismissRequest = { showAddTaskDialog = false },
      title = {
        Text(
          if (language == "bn") "নতুন চেকলিস্ট টাস্ক যুক্ত করুন" else "Add Checklist Task",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = DeepPlum
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text(if (language == "bn") "টাস্ক বিবরণ" else "Task description") },
            placeholder = { Text(if (language == "bn") "যেমন: স্টেজ ডেকোরেশন কনফার্ম করা" else "e.g. Confirm wedding stage decoration") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_checklist_title_input")
          )

          // Category Chips Selector
          Column {
            Text(
              text = if (language == "bn") "ক্যাটাগরি নির্বাচন করুন:" else "Select Category:",
              style = MaterialTheme.typography.labelMedium,
              color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(dialogCategories) { cat ->
                val isSelected = taskCategory == cat
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { taskCategory = cat },
                  color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = BorderStroke(1.dp, if (isSelected) DeepPlum else BorderSubtle)
                ) {
                  Text(
                    text = when (cat) {
                      "Venue" -> if (language == "bn") "🏢 ভেন্যু" else "🏢 Venue"
                      "Catering" -> if (language == "bn") "🍽️ খাবার" else "🍽️ Catering"
                      "Decoration" -> if (language == "bn") "🎨 ডেকোরেশন" else "🎨 Decor"
                      "Attire" -> if (language == "bn") "👗 পোশাক" else "👗 Attire"
                      "Photography" -> if (language == "bn") "📸 ছবি/ভিডিও" else "📸 Photo"
                      "Guests" -> if (language == "bn") "👥 অতিথি" else "👥 Guests"
                      else -> if (language == "bn") "📝 সাধারণ" else "📝 General"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = taskDueDate,
              onValueChange = { taskDueDate = it },
              label = { Text(if (language == "bn") "টার্গেট তারিখ (Due Date)" else "Target Due Date") },
              placeholder = { Text("e.g. Oct 22") },
              trailingIcon = {
                IconButton(onClick = { taskDatePicker.show() }) {
                  Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = DeepPlum)
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().testTag("add_checklist_date_input")
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (taskTitle.isNotBlank()) {
              onAddChecklistItem(taskTitle.trim(), taskDueDate.trim(), taskCategory)
              showAddTaskDialog = false
            }
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
          modifier = Modifier.testTag("add_checklist_save_button")
        ) {
          Text(if (language == "bn") "যোগ করুন" else "Add Task")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddTaskDialog = false }) {
          Text(if (language == "bn") "বাতিল" else "Cancel", color = TextMuted)
        }
      }
    )
  }

  // Dialog to Edit Checklist Task & Due Date
  editingChecklistItem?.let { itemToEdit ->
    var editTitle by remember(itemToEdit.id) { mutableStateOf(itemToEdit.title) }
    var editDueDate by remember(itemToEdit.id) { mutableStateOf(itemToEdit.dueDate) }
    var editCategory by remember(itemToEdit.id) { mutableStateOf(itemToEdit.category) }
    val editCal = remember { Calendar.getInstance() }
    val editDateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val editDatePicker = remember(context) {
      DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
          editCal.set(Calendar.YEAR, year)
          editCal.set(Calendar.MONTH, month)
          editCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
          editDueDate = editDateFormatter.format(editCal.time)
        },
        editCal.get(Calendar.YEAR),
        editCal.get(Calendar.MONTH),
        editCal.get(Calendar.DAY_OF_MONTH)
      )
    }

    val dialogCategories = listOf("General", "Venue", "Catering", "Decoration", "Attire", "Photography", "Guests")

    AlertDialog(
      onDismissRequest = { editingChecklistItem = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Edit, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            if (language == "bn") "টাস্ক সম্পাদনা করুন" else "Edit Checklist Task",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            label = { Text(if (language == "bn") "টাস্ক বিবরণ" else "Task description") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_checklist_title_input")
          )

          // Category Chips Selector
          Column {
            Text(
              text = if (language == "bn") "ক্যাটাগরি:" else "Category:",
              style = MaterialTheme.typography.labelMedium,
              color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(dialogCategories) { cat ->
                val isSelected = editCategory == cat
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { editCategory = cat },
                  color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = BorderStroke(1.dp, if (isSelected) DeepPlum else BorderSubtle)
                ) {
                  Text(
                    text = when (cat) {
                      "Venue" -> if (language == "bn") "🏢 ভেন্যু" else "🏢 Venue"
                      "Catering" -> if (language == "bn") "🍽️ খাবার" else "🍽️ Catering"
                      "Decoration" -> if (language == "bn") "🎨 ডেকোরেশন" else "🎨 Decor"
                      "Attire" -> if (language == "bn") "👗 পোশাক" else "👗 Attire"
                      "Photography" -> if (language == "bn") "📸 ছবি/ভিডিও" else "📸 Photo"
                      "Guests" -> if (language == "bn") "👥 অতিথি" else "👥 Guests"
                      else -> if (language == "bn") "📝 সাধারণ" else "📝 General"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = editDueDate,
              onValueChange = { editDueDate = it },
              label = { Text(if (language == "bn") "নির্ধারিত তারিখ (Due Date)" else "Target Due Date") },
              trailingIcon = {
                IconButton(onClick = { editDatePicker.show() }) {
                  Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = DeepPlum)
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().testTag("edit_checklist_date_input")
            )
          }

          // Quick Preset Due Date buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (event != null && event.dateFormatted.isNotBlank()) {
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { editDueDate = event.dateFormatted }
                  .testTag("edit_checklist_preset_event_date"),
                color = AccentGold.copy(alpha = 0.15f)
              ) {
                Text(
                  text = if (language == "bn") "ইভেন্টের দিন" else "Event Day",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = DeepPlum,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { editDatePicker.show() }
                .testTag("edit_checklist_open_calendar"),
              color = DeepPlum.copy(alpha = 0.1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (language == "bn") "ক্যালেন্ডার" else "Calendar",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = DeepPlum
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editTitle.isNotBlank()) {
              onUpdateChecklistItem(
                itemToEdit.copy(
                  title = editTitle.trim(),
                  dueDate = editDueDate.trim(),
                  category = editCategory
                )
              )
              editingChecklistItem = null
            }
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
          modifier = Modifier.testTag("edit_checklist_save_button")
        ) {
          Text(if (language == "bn") "সংরক্ষণ করুন" else "Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingChecklistItem = null }) {
          Text(if (language == "bn") "বাতিল" else "Cancel", color = TextMuted)
        }
      }
    )
  }

  // Comprehensive Event Summary Share Bottom Sheet / Dialog
  if (showShareSummaryDialog && event != null) {
    ModalBottomSheet(
      onDismissRequest = { showShareSummaryDialog = false },
      containerColor = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp)
          .padding(bottom = 32.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (language == "bn") "ইভেন্ট সামারি শেয়ার করুন" else "Share Event Summary",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
            Text(
              text = if (language == "bn") "পরিবার বা টিম মেম্বারদের সাথে আপডেট শেয়ার করুন" else "Keep family & team members updated with 1-click",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
          IconButton(onClick = { showShareSummaryDialog = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Preview Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.background,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "📌 ${event.title}",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall,
              color = DeepPlum
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "• 📅 ${event.dateFormatted} • ${event.location.ifBlank { "Venue TBD" }}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "• 💰 ${if (language == "bn") "বাজেট" else "Budget"}: $currencySymbol ${budgetSummary.planned.toLong()} | ${if (language == "bn") "খরচ" else "Spent"}: $currencySymbol ${budgetSummary.spent.toLong()}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "• 👥 ${if (language == "bn") "মেহমান" else "Guests"}: ${eventGuests.size} ${if (language == "bn") "জন" else ""} (${guestSummary.confirmed} ${if (language == "bn") "কনফার্মড" else "Confirmed"})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "• ✅ ${if (language == "bn") "চেকলিস্ট" else "Checklist"}: ${checklist.count { it.isDone }}/${checklist.size} ${if (language == "bn") "সম্পন্ন" else "Done"}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. WhatsApp & Quick Text Share Option
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
              val textSummary = EventSummaryExporter.generateTextSummary(
                event = event,
                budgetSummary = budgetSummary,
                checklist = checklist,
                eventGuests = eventGuests,
                allContacts = allContacts,
                currencySymbol = currencySymbol,
                language = language
              )
              EventSummaryExporter.shareText(
                context = context,
                text = textSummary,
                title = if (language == "bn") "ইভেন্ট সামারি শেয়ার" else "Share Event Summary"
              )
              showShareSummaryDialog = false
            }
            .testTag("share_summary_text_button"),
          color = WhatsAppGreen.copy(alpha = 0.12f),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = WhatsAppGreen,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("💬", fontSize = 22.sp)
              }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (language == "bn") "হোয়াটসঅ্যাপ ও টেক্সট সামারি" else "WhatsApp & Text Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PlumDark
              )
              Text(
                text = if (language == "bn") "বাজেট, খরচ, ভেন্ডর নম্বর ও চেকলিস্টসহ মেসেজ পাঠান" else "Formatted text with budget, vendor contacts & tasks",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
              )
            }
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              tint = WhatsAppGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. PDF Export & Document Share Option
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
              EventSummaryExporter.generateAndSharePdf(
                context = context,
                event = event,
                budgetSummary = budgetSummary,
                checklist = checklist,
                eventGuests = eventGuests,
                allContacts = allContacts,
                currencySymbol = currencySymbol,
                language = language
              )
              showShareSummaryDialog = false
            }
            .testTag("share_summary_pdf_button"),
          color = DeepPlum.copy(alpha = 0.08f),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepPlum.copy(alpha = 0.35f)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = DeepPlum,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("📄", fontSize = 22.sp)
              }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (language == "bn") "পিডিএফ রিপোর্ট তৈরি ও শেয়ার (PDF)" else "Export & Share PDF Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum
              )
              Text(
                text = if (language == "bn") "প্রিন্ট বা ডকুমেন্টের উপযোগী এ৪ (A4) সাইজ রিপোর্ট" else "Clean, printable A4 summary document",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
              )
            }
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = DeepPlum,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChecklistTabContent(
  checklist: List<ChecklistItemEntity>,
  onToggleItem: (ChecklistItemEntity) -> Unit,
  onEditItem: (ChecklistItemEntity) -> Unit,
  onDeleteItem: (ChecklistItemEntity) -> Unit,
  onAddTaskClick: () -> Unit,
  eventName: String = "ইভেন্ট",
  language: String = "en"
) {
  var taskForReminder by remember { mutableStateOf<ChecklistItemEntity?>(null) }
  var selectedCategory by remember { mutableStateOf("All") }
  var selectedStatus by remember { mutableStateOf("All") } // "All", "Pending", "Done"
  var searchQuery by remember { mutableStateOf("") }

  val isBn = language == "bn"

  if (taskForReminder != null) {
    TaskReminderDialog(
      task = taskForReminder!!,
      eventName = eventName,
      language = language,
      onDismiss = { taskForReminder = null }
    )
  }

  // Calculate statistics
  val totalTasks = checklist.size
  val doneTasks = checklist.count { it.isDone }
  val pendingTasks = totalTasks - doneTasks
  val progressFraction = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0f
  val progressPercent = (progressFraction * 100).toInt()

  // Filter checklist
  val filteredList = checklist.filter { item ->
    val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
    val matchesStatus = when (selectedStatus) {
      "Pending" -> !item.isDone
      "Done" -> item.isDone
      else -> true
    }
    val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesStatus && matchesSearch
  }

  val categories = listOf("All", "General", "Venue", "Catering", "Decoration", "Attire", "Photography", "Guests")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 4.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Progress Overview Header Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DeepPlum.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, DeepPlum.copy(alpha = 0.15f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (isBn) "চেকলিস্ট অগ্রগতি" else "Checklist Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepPlum
              )
              Text(
                text = if (isBn) "$totalTasks টি কাজের মধ্যে $doneTasks টি সম্পন্ন" else "$doneTasks of $totalTasks tasks completed",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
              )
            }

            // Quick Add Button
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddTaskClick() }
                .testTag("checklist_header_add_btn"),
              color = AccentGold
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = PlumDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isBn) "নতুন টাস্ক" else "Add Task",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = PlumDark
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Progress bar
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            LinearProgressIndicator(
              progress = { progressFraction },
              modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (progressFraction == 1f) SuccessGreen else AccentGold,
              trackColor = DeepPlum.copy(alpha = 0.12f)
            )
            Text(
              text = "$progressPercent%",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.ExtraBold,
              color = DeepPlum
            )
          }
        }
      }
    }

    // 2. Search & Category Filter Section
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Search Input Box
        if (totalTasks > 3) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isBn) "টাস্ক খুঁজুন..." else "Search checklist tasks...", fontSize = 13.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotBlank()) {
                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                  Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = BorderSubtle,
              focusedBorderColor = DeepPlum
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp)
          )
        }

        // Status Filter Chips (All, Pending, Done)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            "All" to if (isBn) "সব ($totalTasks)" else "All ($totalTasks)",
            "Pending" to if (isBn) "বাকি ($pendingTasks)" else "Pending ($pendingTasks)",
            "Done" to if (isBn) "সম্পন্ন ($doneTasks)" else "Completed ($doneTasks)"
          ).forEach { (statusKey, labelText) ->
            val isSelected = selectedStatus == statusKey
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { selectedStatus = statusKey },
              color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, if (isSelected) DeepPlum else BorderSubtle)
            ) {
              Text(
                text = labelText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        // Category Scrollable Filter Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(categories) { cat ->
            val isSelected = selectedCategory == cat
            val catText = when (cat) {
              "Venue" -> if (isBn) "🏢 ভেন্যু" else "🏢 Venue"
              "Catering" -> if (isBn) "🍽️ খাবার" else "🍽️ Catering"
              "Decoration" -> if (isBn) "🎨 ডেকোরেশন" else "🎨 Decor"
              "Attire" -> if (isBn) "👗 পোশাক" else "👗 Attire"
              "Photography" -> if (isBn) "📸 ছবি/ভিডিও" else "📸 Photo"
              "Guests" -> if (isBn) "👥 অতিথি" else "👥 Guests"
              "General" -> if (isBn) "📝 সাধারণ" else "📝 General"
              else -> if (isBn) "সব ক্যাটাগরি" else "All Categories"
            }

            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { selectedCategory = cat },
              color = if (isSelected) AccentGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = BorderStroke(1.dp, if (isSelected) AccentGold else Color.Transparent)
            ) {
              Text(
                text = catText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }
      }
    }

    // 3. Checklist Items List
    if (filteredList.isEmpty()) {
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, BorderSubtle)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = DeepPlum.copy(alpha = 0.4f),
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = if (checklist.isEmpty()) {
                if (isBn) "কোন টাস্ক বা কাজ যুক্ত করা হয়নি" else "No checklist tasks yet"
              } else {
                if (isBn) "এই ফিল্টারে কোন কাজ পাওয়া যায়নি" else "No tasks found for this filter"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (checklist.isEmpty()) {
                if (isBn) "'নতুন টাস্ক' বাটনে ট্যাপ করে আপনার প্রথম কাজ যুক্ত করুন" else "Tap 'Add Task' above to add your first event task"
              } else {
                if (isBn) "ফিল্টার পরির্বতন করুন অথবা নতুন টাস্ক যুক্ত করুন" else "Try clearing search or filters to see more tasks"
              },
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                selectedCategory = "All"
                selectedStatus = "All"
                searchQuery = ""
                if (checklist.isEmpty()) onAddTaskClick()
              },
              colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                if (checklist.isEmpty()) (if (isBn) "+ প্রথম টাস্ক যুক্ত করুন" else "+ Add First Task")
                else (if (isBn) "ফিল্টার রিসেট করুন" else "Reset Filters")
              )
            }
          }
        }
      }
    } else {
      items(filteredList, key = { it.id }) { item ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
              1.dp,
              if (item.isDone) SuccessGreen.copy(alpha = 0.3f) else DeepPlum.copy(alpha = 0.15f),
              RoundedCornerShape(14.dp)
            ),
          color = if (item.isDone) SuccessGreen.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface,
          shadowElevation = if (item.isDone) 0.dp else 1.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 8.dp)
          ) {
            // Row 1: Checkbox + 1-Line Task Title + Top-Right Action Icons
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = item.isDone,
                onCheckedChange = { onToggleItem(item) },
                colors = CheckboxDefaults.colors(
                  checkedColor = SuccessGreen,
                  uncheckedColor = TextMuted
                ),
                modifier = Modifier
                  .size(24.dp)
                  .testTag("checklist_checkbox_${item.id}")
              )

              Spacer(modifier = Modifier.width(8.dp))

              // 1-Line Task Title (Max 1 Line with Ellipsis for space saving)
              Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (item.isDone) FontWeight.Normal else FontWeight.SemiBold,
                color = if (item.isDone) TextMuted else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .clickable { onEditItem(item) }
                  .testTag("checklist_item_title_${item.id}")
              )

              Spacer(modifier = Modifier.width(4.dp))

              // Top Right Corner Action Icons Row (Reminder, Edit, Delete)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
              ) {
                // Reminder Bell
                IconButton(
                  onClick = { taskForReminder = item },
                  modifier = Modifier
                    .size(28.dp)
                    .testTag("checklist_reminder_button_${item.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = if (isBn) "রিমাইন্ডার" else "Reminder",
                    tint = WarningAmber,
                    modifier = Modifier.size(16.dp)
                  )
                }

                // Edit Pencil
                IconButton(
                  onClick = { onEditItem(item) },
                  modifier = Modifier
                    .size(28.dp)
                    .testTag("checklist_edit_button_${item.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isBn) "এডিট" else "Edit",
                    tint = DeepPlum,
                    modifier = Modifier.size(16.dp)
                  )
                }

                // Delete Trash
                IconButton(
                  onClick = { onDeleteItem(item) },
                  modifier = Modifier
                    .size(28.dp)
                    .testTag("checklist_delete_button_${item.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (isBn) "মুছুন" else "Delete",
                    tint = DangerRed.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Metadata Pills in LazyRow (Aligned past Checkbox, No truncation)
            LazyRow(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Category Pill
              item {
                val catIcon = when (item.category.lowercase()) {
                  "venue" -> Icons.Default.LocationOn
                  "catering" -> Icons.Default.Restaurant
                  "decoration" -> Icons.Default.Palette
                  "attire" -> Icons.Default.Checkroom
                  "photography" -> Icons.Default.PhotoCamera
                  "guests" -> Icons.Default.People
                  else -> Icons.Default.Assignment
                }

                val catName = when (item.category.lowercase()) {
                  "venue" -> if (isBn) "ভেন্যু" else "Venue"
                  "catering" -> if (isBn) "খাবার" else "Catering"
                  "decoration" -> if (isBn) "ডেকোরেশন" else "Decor"
                  "attire" -> if (isBn) "পোশাক" else "Attire"
                  "photography" -> if (isBn) "ছবি/ভিডিও" else "Photo"
                  "guests" -> if (isBn) "অতিথি" else "Guests"
                  else -> if (isBn) "সাধারণ" else "General"
                }

                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditItem(item) },
                  color = DeepPlum.copy(alpha = 0.08f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(catIcon, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = catName,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = DeepPlum,
                      maxLines = 1,
                      softWrap = false
                    )
                  }
                }
              }

              // Due Date Pill
              item {
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditItem(item) }
                    .testTag("checklist_item_date_pill_${item.id}"),
                  color = if (item.isDone) TextMuted.copy(alpha = 0.1f) else WarningAmber.copy(alpha = 0.14f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.CalendarToday,
                      contentDescription = null,
                      tint = if (item.isDone) TextMuted else WarningAmber,
                      modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = item.dueDate.ifBlank { if (isBn) "তারিখ সেট" else "Set date" },
                      color = if (item.isDone) TextMuted else WarningAmber,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      softWrap = false
                    )
                  }
                }
              }

              // Status Pill (Clear, Full Text Without Truncation)
              item {
                Surface(
                  modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                  color = if (item.isDone) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.12f)
                ) {
                  Text(
                    text = if (item.isDone) (if (isBn) "✓ সম্পন্ন" else "✓ Done") else (if (isBn) "⏳ বাকি" else "⏳ Pending"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isDone) SuccessGreen else WarningAmber,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Bottom Spacing & Add Button Footer
    item {
      Spacer(modifier = Modifier.height(12.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, AccentGold, RoundedCornerShape(14.dp))
          .clickable { onAddTaskClick() }
          .testTag("checklist_add_task_row"),
        color = AccentGold.copy(alpha = 0.08f)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = DeepPlum,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isBn) "+ নতুন টাস্ক যোগ করুন" else "+ Add New Checklist Task",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )
        }
      }
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
fun GuestsTabContent(
  eventGuests: List<EventContactCrossRef>,
  allContacts: List<ContactEntity>,
  onUpdateStatus: (contactId: Long, newStatus: String) -> Unit,
  onMarkInviteSent: (contactId: Long) -> Unit = {},
  onUpdateContact: (ContactEntity) -> Unit = {},
  onAddGuestClick: () -> Unit,
  cateringEstimate: CateringEstimate = CateringEstimate(),
  currencySymbol: String = "৳",
  onGoToBudget: () -> Unit = {},
  eventName: String = "Event",
  eventDate: String = "",
  eventLocation: String = "",
  language: String = "en"
) {
  val contactMap = remember(allContacts) { allContacts.associateBy { it.id } }

  // Exclude Vendors from Guests tab so vendors stay exclusively in the Vendors tab
  val nonVendorGuests = remember(eventGuests, allContacts) {
    eventGuests.filter { guestRef ->
      val contact = contactMap[guestRef.contactId]
      contact == null || !contact.relation.equals("Vendor", ignoreCase = true)
    }
  }

  // Guest Status Filter state: "ALL", "CONFIRMED", "PENDING_CALL", "CALLED", "DECLINED"
  var selectedStatusFilter by remember { mutableStateOf("ALL") }

  // Compute counts for status chips
  val allCount = nonVendorGuests.size
  val confirmedCount = remember(nonVendorGuests) { nonVendorGuests.count { it.status.equals("Confirmed", ignoreCase = true) } }
  val pendingCallCount = remember(nonVendorGuests) {
    nonVendorGuests.count {
      it.status.equals("Not Called", ignoreCase = true) ||
      it.status.equals("Pending Call", ignoreCase = true) ||
      it.status.equals("Pending", ignoreCase = true) ||
      it.status.isBlank()
    }
  }
  val calledCount = remember(nonVendorGuests) { nonVendorGuests.count { it.status.equals("Called", ignoreCase = true) } }
  val declinedCount = remember(nonVendorGuests) { nonVendorGuests.count { it.status.equals("Declined", ignoreCase = true) } }

  val filteredGuests = remember(nonVendorGuests, selectedStatusFilter) {
    when (selectedStatusFilter) {
      "CONFIRMED" -> nonVendorGuests.filter { it.status.equals("Confirmed", ignoreCase = true) }
      "PENDING_CALL" -> nonVendorGuests.filter {
        it.status.equals("Not Called", ignoreCase = true) ||
        it.status.equals("Pending Call", ignoreCase = true) ||
        it.status.equals("Pending", ignoreCase = true) ||
        it.status.isBlank()
      }
      "CALLED" -> nonVendorGuests.filter { it.status.equals("Called", ignoreCase = true) }
      "DECLINED" -> nonVendorGuests.filter { it.status.equals("Declined", ignoreCase = true) }
      else -> nonVendorGuests
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (language == "bn") "${nonVendorGuests.size} জন মেহমান" else "${nonVendorGuests.size} Guests",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f, fill = false)
      )

      Button(
        onClick = onAddGuestClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AccentGold,
          contentColor = PlumDark
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier
          .height(36.dp)
          .testTag("guests_tab_add_button")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          if (language == "bn") "মেহমান যোগ" else "Add Guests",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Filter by Status Chips Row
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val filterOptions = listOf(
        Triple("ALL", if (language == "bn") "সবাই" else "All", allCount),
        Triple("CONFIRMED", if (language == "bn") "কনফার্মড" else "Confirmed", confirmedCount),
        Triple("PENDING_CALL", if (language == "bn") "কল করা হয়নি" else "Pending Call", pendingCallCount),
        Triple("CALLED", if (language == "bn") "কল করা হয়েছে" else "Called", calledCount),
        Triple("DECLINED", if (language == "bn") "আসবে না" else "Declined", declinedCount)
      )

      items(filterOptions) { (key, label, count) ->
        val isSelected = selectedStatusFilter == key
        val chipBg = if (isSelected) {
          when (key) {
            "CONFIRMED" -> SuccessGreen
            "PENDING_CALL" -> DangerRed
            "CALLED" -> WarningAmber
            "DECLINED" -> TextMuted
            else -> DeepPlum
          }
        } else {
          MaterialTheme.colorScheme.surface
        }
        val chipTextColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        val borderColor = if (isSelected) Color.Transparent else BorderSubtle

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = chipBg,
          border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { selectedStatusFilter = key }
            .testTag("filter_chip_$key")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = chipTextColor
            )
            // Counter badge
            Surface(
              shape = CircleShape,
              color = if (isSelected) Color.White.copy(alpha = 0.25f) else DeepPlum.copy(alpha = 0.08f),
              modifier = Modifier.size(20.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "$count",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = chipTextColor
                )
              }
            }
          }
        }
      }
    }

    if (filteredGuests.isEmpty()) {
      // Catering connection banner
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
          .clickable { onGoToBudget() },
        color = DeepPlum.copy(alpha = 0.05f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f, fill = false)
          ) {
            Icon(
              imageVector = Icons.Default.Restaurant,
              contentDescription = "Catering",
              tint = Emerald,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (cateringEstimate.perPlateCost > 0.0) {
                "Catering: ${cateringEstimate.recommendedPlates} plates (${cateringEstimate.confirmedGuestCount} confirmed)"
              } else {
                "Catering: ${cateringEstimate.confirmedGuestCount} confirmed guests → Calculate plates"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = DeepPlum,
              maxLines = 1
            )
          }
          Text(
            text = "Budget →",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            fontSize = 11.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "🔍",
            fontSize = 32.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (language == "bn") "এই ক্যাটাগরিতে কোনো মেহমান নেই" else "No guests in this category",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        // Compact Catering Banner as first scrollable item
        item {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp)
              .clickable { onGoToBudget() }
              .testTag("guests_catering_banner"),
            color = DeepPlum.copy(alpha = 0.05f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
              ) {
                Icon(
                  imageVector = Icons.Default.Restaurant,
                  contentDescription = "Catering",
                  tint = Emerald,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = if (cateringEstimate.perPlateCost > 0.0) {
                    "Catering: ${cateringEstimate.recommendedPlates} plates (${cateringEstimate.confirmedGuestCount} confirmed)"
                  } else {
                    "Catering: ${cateringEstimate.confirmedGuestCount} confirmed guests → Calculate plates"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = DeepPlum,
                  maxLines = 1
                )
              }
              Text(
                text = "Budget →",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                fontSize = 11.sp
              )
            }
          }
        }

        items(filteredGuests, key = { it.contactId }) { guestRef ->
          val contact = contactMap[guestRef.contactId]
          if (contact != null) {
            GuestRowItem(
              contact = contact,
              status = guestRef.status,
              isInviteSent = guestRef.isInviteSent,
              onStatusChange = { newStatus ->
                onUpdateStatus(contact.id, newStatus)
              },
              onMarkInviteSent = {
                onMarkInviteSent(contact.id)
              },
              onUpdateContact = onUpdateContact,
              eventName = eventName,
              eventDate = eventDate,
              eventLocation = eventLocation,
              language = language
            )
          }
        }
        item { Spacer(modifier = Modifier.height(70.dp)) }
      }
    }
  }
}

@Composable
fun GuestRowItem(
  contact: ContactEntity,
  status: String,
  isInviteSent: Boolean = false,
  onStatusChange: (String) -> Unit,
  onMarkInviteSent: () -> Unit = {},
  onUpdateContact: (ContactEntity) -> Unit = {},
  eventName: String = "Event",
  eventDate: String = "",
  eventLocation: String = "",
  language: String = "en"
) {
  val context = LocalContext.current
  var menuExpanded by remember { mutableStateOf(false) }
  var showConvertDialog by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      InitialsAvatar(name = contact.name, backgroundColorHex = contact.avatarColorHex, size = 40.dp)

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = contact.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
          if (isInviteSent) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = WhatsAppGreen.copy(alpha = 0.15f),
              modifier = Modifier.padding(start = 2.dp)
            ) {
              Text(
                text = if (language == "bn") "আমন্ত্রিত" else "Invited",
                style = MaterialTheme.typography.labelSmall,
                color = WhatsAppGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }
        Text(
          text = "${contact.relation} • ${contact.phone}",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted
        )
      }

      // Status Pill (Clickable to switch status)
      Box {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { menuExpanded = true }
            .testTag("guest_status_${contact.id}")
        ) {
          StatusBadge(status = status)
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          listOf("Confirmed", "Called", "Not Called", "Declined").forEach { opt ->
            DropdownMenuItem(
              text = { Text(opt) },
              onClick = {
                onStatusChange(opt)
                menuExpanded = false
              }
            )
          }
          HorizontalDivider()
          DropdownMenuItem(
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.SwapHoriz,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = DeepPlum
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (language == "bn") "রোল রূপান্তর..." else "Convert Role...",
                  color = DeepPlum,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.5.sp
                )
              }
            },
            onClick = {
              menuExpanded = false
              showConvertDialog = true
            }
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // 1-Click WhatsApp Direct Button
      WhatsAppIconButton(
        onClick = {
          if (!isInviteSent) {
            // First time: Send formal invitation message
            val inviteMsg = if (language == "bn") {
              "আসসালামু আলাইকুম ${contact.name}! আপনাকে আমাদের \"$eventName\" অনুষ্ঠানে সাদর আমন্ত্রণ জানাচ্ছি।${if (eventDate.isNotBlank()) "\n📅 তারিখ: $eventDate" else ""}${if (eventLocation.isNotBlank()) "\n📍 স্থান: $eventLocation" else ""}\nআপনার উপস্থিতি আমাদের আনন্দিত করবে।"
            } else {
              "Hello ${contact.name}! You are cordially invited to our \"$eventName\" event.${if (eventDate.isNotBlank()) "\n📅 Date: $eventDate" else ""}${if (eventLocation.isNotBlank()) "\n📍 Venue: $eventLocation" else ""}\nLooking forward to having you with us!"
            }
            openWhatsAppMessage(context, contact.phone, inviteMsg)
            onMarkInviteSent()
          } else {
            // Second time onwards: Send regular/normal message
            val normalMsg = if (language == "bn") {
              "আসসালামু আলাইকুম ${contact.name}! আশা করি ভালো আছেন। \"$eventName\" ইভেন্ট সংক্রান্ত কোনো তথ্যের জন্য মেসেজ পাঠাচ্ছি।"
            } else {
              "Hello ${contact.name}! Hope you are doing well. Reaching out regarding our \"$eventName\" event."
            }
            openWhatsAppMessage(context, contact.phone, normalMsg)
          }
        },
        size = 32.dp
      )

      Spacer(modifier = Modifier.width(4.dp))

      // Call Icon
      IconButton(
        onClick = {
          val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
          context.startActivity(dialIntent)
        },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(Icons.Default.Call, contentDescription = "Call", tint = DeepPlum, modifier = Modifier.size(17.dp))
      }
    }
  }

  if (showConvertDialog) {
    var selectedCategory by remember(contact) { mutableStateOf(contact.relation) }
    var vendorNote by remember(contact) { mutableStateOf(contact.note) }
    val categories = listOf("Family", "Friend", "Vendor", "Colleague", "Other")

    AlertDialog(
      onDismissRequest = { showConvertDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (language == "bn") "মেম্বার রোল রূপান্তর" else "Convert Member Role",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = if (language == "bn") "${contact.name}-এর নতুন রোল সিলেক্ট করুন:" else "Select new role for ${contact.name}:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))
          categories.forEach { cat ->
            val isSel = selectedCategory.equals(cat, ignoreCase = true)
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedCategory = cat },
              color = if (isSel) DeepPlum.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              border = androidx.compose.foundation.BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) DeepPlum else BorderSubtle)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(cat, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                if (isSel) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(16.dp))
              }
            }
          }
          if (selectedCategory.equals("Vendor", ignoreCase = true)) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = vendorNote,
              onValueChange = { vendorNote = it },
              label = { Text("Vendor Service / Note") },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateContact(
              contact.copy(
                relation = selectedCategory.trim(),
                note = if (selectedCategory.equals("Vendor", ignoreCase = true) && vendorNote.isBlank()) "Vendor Service" else vendorNote.trim()
              )
            )
            val msg = if (language == "bn") "${contact.name}-কে $selectedCategory বানানো হয়েছে" else "Converted ${contact.name} to $selectedCategory"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            showConvertDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text(if (language == "bn") "কনভার্ট করুন" else "Convert")
        }
      },
      dismissButton = {
        TextButton(onClick = { showConvertDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsTabContent(
  eventGuests: List<EventContactCrossRef>,
  allContacts: List<ContactEntity>,
  onAddVendorClick: () -> Unit = {},
  externalOpenAddDialog: Boolean = false,
  onResetExternalOpenAddDialog: () -> Unit = {},
  onUpdateContact: (ContactEntity) -> Unit = {},
  onAddNewVendor: (name: String, phone: String, note: String) -> Unit = { _, _, _ -> },
  onRemoveVendor: (contactId: Long) -> Unit = {},
  onLinkExistingContactAsVendor: (contactId: Long) -> Unit = {},
  onImportPhoneVendors: (List<Triple<String, String, String>>) -> Unit = {},
  eventName: String = "Event",
  eventDate: String = "",
  eventLocation: String = "",
  language: String = "en"
) {
  val context = LocalContext.current
  var showAddVendorDialog by remember { mutableStateOf(false) }
  var vendorToEdit by remember { mutableStateOf<ContactEntity?>(null) }
  var showPhoneVendorsSheet by remember { mutableStateOf(false) }

  LaunchedEffect(externalOpenAddDialog) {
    if (externalOpenAddDialog) {
      showAddVendorDialog = true
      onResetExternalOpenAddDialog()
    }
  }

  // Phone contacts state for importing vendors
  var phoneContactsList by remember { mutableStateOf<List<PhoneContactItem>>(emptyList()) }
  var phoneSearchQuery by remember { mutableStateOf("") }
  var selectedPhoneVendors by remember { mutableStateOf<Set<PhoneContactItem>>(emptySet()) }
  var selectedVendorRoleForImport by remember { mutableStateOf("Catering") }

  // Helper to load real phone contacts if permission is available
  fun loadDeviceContacts(ctx: Context): List<PhoneContactItem> {
    val list = mutableListOf<PhoneContactItem>()
    try {
      if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        val cursor: Cursor? = ctx.contentResolver.query(
          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
          arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
          ),
          null,
          null,
          ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
          val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
          val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
          val seenPhones = mutableSetOf<String>()
          while (it.moveToNext()) {
            val name = it.getString(nameIdx) ?: "Unknown"
            val phone = it.getString(phoneIdx)?.replace(" ", "") ?: ""
            if (phone.isNotBlank() && seenPhones.add(phone)) {
              list.add(PhoneContactItem(name = name, phone = phone, category = "Vendor"))
            }
          }
        }
      }
    } catch (e: Exception) {
      // Graceful fallback
    }
    return list
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    phoneContactsList = loadDeviceContacts(context)
    showPhoneVendorsSheet = true
  }

  fun triggerPhoneImport() {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
      phoneContactsList = loadDeviceContacts(context)
      showPhoneVendorsSheet = true
    } else {
      permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }
  }

  val guestContactIds = remember(eventGuests) {
    eventGuests.map { it.contactId }.toSet()
  }

  // Vendors linked to this event
  val vendorContacts = remember(allContacts, guestContactIds) {
    allContacts.filter { it.relation.equals("Vendor", ignoreCase = true) && guestContactIds.contains(it.id) }
  }

  // Other contacts with relation == "Vendor" not yet added to this event
  val unlinkedVendors = remember(allContacts, guestContactIds) {
    allContacts.filter { !guestContactIds.contains(it.id) }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (language == "bn") "${vendorContacts.size} জন ভেন্ডর" else "${vendorContacts.size} Vendors",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f, fill = false)
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Import from Phone Button
        Surface(
          modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { triggerPhoneImport() }
            .testTag("event_import_vendor_phone_button"),
          color = AccentGold.copy(alpha = 0.18f),
          border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.PhoneAndroid,
              contentDescription = "Import from Phone",
              tint = DeepPlum,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (language == "bn") "ফোন থেকে" else "Import",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
          }
        }

        Button(
          onClick = { showAddVendorDialog = true },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
          modifier = Modifier
            .height(36.dp)
            .testTag("event_add_vendor_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = if (language == "bn") "ভেন্ডর যোগ" else "Add Vendor",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    if (vendorContacts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            tint = AccentGold.copy(alpha = 0.8f),
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = if (language == "bn") "কোন ভেন্ডর যুক্ত করা হয়নি" else "No vendors added yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (language == "bn") "ক্যাটারিং, স্টেজ, ফটোগ্রাফি ইত্যাদি ভেন্ডর যুক্ত করতে উপরের বাটনগুলো ব্যবহার করুন" else "Use the buttons above to add caterers, photographers, decorators or import from phone.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(vendorContacts, key = { it.id }) { vendor ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
            color = MaterialTheme.colorScheme.surface
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              InitialsAvatar(name = vendor.name, backgroundColorHex = "#1F6E52", size = 44.dp)

              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = vendor.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Surface(
                    color = AccentGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = vendor.note.ifBlank { "Vendor" },
                      style = MaterialTheme.typography.labelSmall,
                      color = PlumDark,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 10.sp,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Text(
                    text = vendor.phone,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.width(6.dp))

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                // 1-Click WhatsApp Direct Message
                WhatsAppIconButton(
                  onClick = {
                    val vendorRole = vendor.note.ifBlank { "Service Provider" }
                    val vendorMsg = if (language == "bn") {
                      "আসসালামু আলাইকুম ${vendor.name}! আমাদের \"$eventName\" ইভেন্টের ($vendorRole) সংক্রান্ত বিষয়ে যোগাযোগ করছি।${if (eventDate.isNotBlank()) "\n📅 ইভেন্ট তারিখ: $eventDate" else ""}${if (eventLocation.isNotBlank()) "\n📍 লোকেশন: $eventLocation" else ""}\nঅনুগ্রহ করে আপডেট জানাবেন।"
                    } else {
                      "Hello ${vendor.name}! Following up regarding $vendorRole for our upcoming event \"$eventName\".${if (eventDate.isNotBlank()) "\n📅 Date: $eventDate" else ""}${if (eventLocation.isNotBlank()) "\n📍 Location: $eventLocation" else ""}\nPlease let us know the latest updates."
                    }
                    openWhatsAppMessage(context, vendor.phone, vendorMsg)
                  },
                  size = 32.dp
                )

                // Call button
                IconButton(
                  onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vendor.phone}"))
                    try {
                      context.startActivity(dialIntent)
                    } catch (e: Exception) {
                      // Handled
                    }
                  },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Default.Call, contentDescription = "Call Vendor", tint = Emerald, modifier = Modifier.size(18.dp))
                }

                // Edit button
                IconButton(
                  onClick = { vendorToEdit = vendor },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit Vendor", tint = DeepPlum, modifier = Modifier.size(16.dp))
                }

                // Remove from event button
                IconButton(
                  onClick = { onRemoveVendor(vendor.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Remove Vendor", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
      }
    }
  }

  // Dialog to Add Vendor (New Vendor OR Pick Existing)
  if (showAddVendorDialog) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var newVendorName by remember { mutableStateOf("") }
    var newVendorPhone by remember { mutableStateOf("") }
    var newVendorService by remember { mutableStateOf("Catering") }
    var newVendorNote by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val vendorServices = listOf(
      "Catering", "Photography", "Decoration", "Stage",
      "Sound & Music", "Venue", "Makeup", "Transport", "Other"
    )

    AlertDialog(
      onDismissRequest = { showAddVendorDialog = false },
      title = {
        Column {
          Text(
            text = if (language == "bn") "ভেন্ডর যুক্ত করুন" else "Add Vendor to Event",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              label = { Text("New Vendor", fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DeepPlum,
                selectedLabelColor = Color.White
              )
            )
            FilterChip(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              label = { Text("From Contacts (${unlinkedVendors.size})", fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DeepPlum,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      },
      text = {
        if (selectedTab == 0) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
          ) {
            OutlinedTextField(
              value = newVendorName,
              onValueChange = {
                newVendorName = it
                if (nameError) nameError = false
              },
              label = { Text("Vendor Name *") },
              placeholder = { Text("e.g. Master Chef Catering") },
              singleLine = true,
              isError = nameError,
              supportingText = if (nameError) {
                { Text("Name is required", color = MaterialTheme.colorScheme.error) }
              } else null,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("add_vendor_name_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = newVendorPhone,
              onValueChange = {
                newVendorPhone = it
                if (phoneError) phoneError = false
              },
              label = { Text("Phone Number *") },
              placeholder = { Text("+880 1712-345678") },
              singleLine = true,
              isError = phoneError,
              supportingText = if (phoneError) {
                { Text("Phone is required", color = MaterialTheme.colorScheme.error) }
              } else null,
              keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("add_vendor_phone_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Service Type",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(vendorServices) { srv ->
                val isSrvSel = newVendorService == srv
                FilterChip(
                  selected = isSrvSel,
                  onClick = { newVendorService = srv },
                  label = { Text(srv, fontSize = 11.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGold,
                    selectedLabelColor = PlumDark
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = newVendorNote,
              onValueChange = { newVendorNote = it },
              label = { Text("Additional Notes (optional)") },
              placeholder = { Text("e.g. advance 5000 paid, 300 guests menu") },
              maxLines = 2,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("add_vendor_note_input")
            )
          }
        } else {
          // Tab 1: Pick from existing contacts
          Column(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search contact...") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredList = remember(unlinkedVendors, searchQuery) {
              if (searchQuery.isBlank()) unlinkedVendors
              else unlinkedVendors.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                  it.phone.contains(searchQuery, ignoreCase = true) ||
                  it.note.contains(searchQuery, ignoreCase = true)
              }
            }

            if (filteredList.isEmpty()) {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                  text = if (searchQuery.isBlank()) "No available contacts. Switch to 'New Vendor' tab to add." else "No contacts match search.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            } else {
              LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList, key = { it.id }) { c ->
                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 3.dp)
                      .clickable {
                        // If not already a vendor, update relation to Vendor
                        if (!c.relation.equals("Vendor", ignoreCase = true)) {
                          onUpdateContact(c.copy(relation = "Vendor"))
                        }
                        onLinkExistingContactAsVendor(c.id)
                        showAddVendorDialog = false
                      },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                  ) {
                    Row(
                      modifier = Modifier.padding(8.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(c.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${c.relation} • ${c.phone}", fontSize = 11.sp, color = TextMuted)
                      }
                      Text(
                        text = "+ Add",
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        if (selectedTab == 0) {
          Button(
            onClick = {
              if (newVendorName.isBlank()) {
                nameError = true
                return@Button
              }
              if (newVendorPhone.isBlank()) {
                phoneError = true
                return@Button
              }
              val fullNote = if (newVendorNote.isBlank()) newVendorService else "$newVendorService - $newVendorNote"
              onAddNewVendor(newVendorName.trim(), newVendorPhone.trim(), fullNote.trim())
              showAddVendorDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            modifier = Modifier.testTag("confirm_add_vendor_button")
          ) {
            Text("Save Vendor")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddVendorDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }

  // Dialog to Edit Vendor details
  if (vendorToEdit != null) {
    val targetVendor = vendorToEdit!!
    var editName by remember(targetVendor) { mutableStateOf(targetVendor.name) }
    var editPhone by remember(targetVendor) { mutableStateOf(targetVendor.phone) }
    var editNote by remember(targetVendor) { mutableStateOf(targetVendor.note) }
    var nameErr by remember { mutableStateOf(false) }
    var phoneErr by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { vendorToEdit = null },
      title = {
        Text(
          text = if (language == "bn") "ভেন্ডর তথ্য আপডেট" else "Edit Vendor Details",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = editName,
            onValueChange = {
              editName = it
              if (nameErr) nameErr = false
            },
            label = { Text("Vendor Name *") },
            singleLine = true,
            isError = nameErr,
            supportingText = if (nameErr) {
              { Text("Name is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_vendor_name_input")
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = editPhone,
            onValueChange = {
              editPhone = it
              if (phoneErr) phoneErr = false
            },
            label = { Text("Phone Number *") },
            singleLine = true,
            isError = phoneErr,
            supportingText = if (phoneErr) {
              { Text("Phone is required", color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
              keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_vendor_phone_input")
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = editNote,
            onValueChange = { editNote = it },
            label = { Text("Service / Role Note") },
            placeholder = { Text("e.g. Photography, Catering menu details") },
            maxLines = 2,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_vendor_note_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editName.isBlank()) {
              nameErr = true
              return@Button
            }
            if (editPhone.isBlank()) {
              phoneErr = true
              return@Button
            }
            onUpdateContact(
              targetVendor.copy(
                name = editName.trim(),
                phone = editPhone.trim(),
                relation = "Vendor",
                note = editNote.trim()
              )
            )
            vendorToEdit = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
          modifier = Modifier.testTag("confirm_edit_vendor_button")
        ) {
          Text("Update Vendor")
        }
      },
      dismissButton = {
        TextButton(onClick = { vendorToEdit = null }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }

  // BottomSheet for importing phone contacts directly as event vendors
  if (showPhoneVendorsSheet) {
    val importRoles = listOf("Catering", "Photography", "Decoration", "Stage", "Music/DJ", "Venue", "Makeup", "Transport", "Vendor")
    androidx.compose.material3.ModalBottomSheet(
      onDismissRequest = { showPhoneVendorsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (language == "bn") "ফোন থেকে ভেন্ডর যুক্ত করুন" else "Import Phone Contacts as Vendors",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
            Text(
              text = if (language == "bn") "ফোনবুক থেকে কন্ট্যাক্ট নির্বাচন করে সার্ভিস সিলেক্ট করুন" else "Select contacts and assign their service role",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }

          IconButton(onClick = { showPhoneVendorsSheet = false }) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Role Selector for imported contacts
        Text(
          text = if (language == "bn") "সার্ভিসের ধরণ:" else "Assign Service Role:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.foundation.lazy.LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(importRoles) { role ->
            val isRoleSelected = selectedVendorRoleForImport == role
            FilterChip(
              selected = isRoleSelected,
              onClick = { selectedVendorRoleForImport = role },
              label = { Text(role, fontSize = 11.5.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DeepPlum,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phoneSearchQuery,
          onValueChange = { phoneSearchQuery = it },
          placeholder = { Text(if (language == "bn") "কন্ট্যাক্ট খুঁজুন..." else "Search phone contacts...", fontSize = 14.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
          },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        val displayedPhoneContacts = remember(phoneContactsList, phoneSearchQuery) {
          if (phoneSearchQuery.isBlank()) phoneContactsList
          else phoneContactsList.filter {
            it.name.contains(phoneSearchQuery, ignoreCase = true) ||
            it.phone.contains(phoneSearchQuery)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${selectedPhoneVendors.size} selected",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = DeepPlum
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
              selectedPhoneVendors = displayedPhoneContacts.toSet()
            }) {
              Text("Select All", fontSize = 12.sp, color = AccentGold, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { selectedPhoneVendors = emptySet() }) {
              Text("Clear", fontSize = 12.sp, color = TextMuted)
            }
          }
        }

        HorizontalDivider(color = BorderSubtle)

        if (displayedPhoneContacts.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (phoneSearchQuery.isBlank()) "No contacts found on device." else "No matching contacts.",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
          ) {
            items(displayedPhoneContacts, key = { it.phone }) { contact ->
              val isSelected = selectedPhoneVendors.contains(contact)

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    selectedPhoneVendors = if (isSelected) {
                      selectedPhoneVendors - contact
                    } else {
                      selectedPhoneVendors + contact
                    }
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                InitialsAvatar(name = contact.name, size = 38.dp)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }

                Checkbox(
                  checked = isSelected,
                  onCheckedChange = { checked ->
                    selectedPhoneVendors = if (checked) {
                      selectedPhoneVendors + contact
                    } else {
                      selectedPhoneVendors - contact
                    }
                  },
                  colors = CheckboxDefaults.colors(checkedColor = DeepPlum)
                )
              }
              HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            val listToImport = selectedPhoneVendors.map {
              Triple(it.name, it.phone, selectedVendorRoleForImport)
            }
            onImportPhoneVendors(listToImport)
            selectedPhoneVendors = emptySet()
            showPhoneVendorsSheet = false
          },
          enabled = selectedPhoneVendors.isNotEmpty(),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("confirm_import_phone_vendors_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PlumDark
          )
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (language == "bn") "ইভেন্টে ভেন্ডর হিসেবে যুক্ত করুন (${selectedPhoneVendors.size})" else "Add as Event Vendors (${selectedPhoneVendors.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}

@Composable
fun BudgetTabContent(
  budgetSummary: EventBudgetSummary,
  currencySymbol: String,
  cateringPlan: CateringPlanEntity?,
  cateringEstimate: CateringEstimate,
  onSaveCateringPlan: (perPlateCost: Double, bufferPercent: Int) -> Unit,
  onAddCateringToBudget: (estimatedCost: Double, recommendedPlates: Int, perPlateCost: Double, bufferPercent: Int) -> Unit,
  onOpenFullBudget: () -> Unit,
  onUpdatePlannedBudget: (Double) -> Unit = {},
  language: String = "en"
) {
  var showEditPlannedBudgetDialog by remember { mutableStateOf(false) }
  var editedBudgetText by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    val isOverBudget = budgetSummary.spent > budgetSummary.planned && budgetSummary.planned > 0
    val overspentAmount = if (isOverBudget) budgetSummary.spent - budgetSummary.planned else 0.0

    // Budget Overspend Warning Banner (যদি খরচ বাজেট অতিক্রম করে)
    if (isOverBudget) {
      val formattedOverspent = formatCurrency(overspentAmount, currencySymbol)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(1.5.dp, DangerRed.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
          .testTag("event_detail_budget_overspend_warning"),
        color = DangerRed.copy(alpha = 0.10f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = DangerRed,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (language == "bn") "বাজেট সতর্কতা!" else "Budget Overspend Alert!",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = DangerRed
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (language == "bn")
                "আপনি নির্ধারিত বাজেটের চেয়ে $formattedOverspent বেশি খরচ করেছেন!"
              else
                "You have exceeded your planned budget by $formattedOverspent!",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .border(1.dp, if (isOverBudget) DangerRed.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(18.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (language == "bn") "বাজেট ওভারভিউ" else "Budget Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${budgetSummary.percentageSpent.toInt()}% Used",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isOverBudget) DangerRed else AccentGold
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Planned (Clickable with Edit pencil)
          Column(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                editedBudgetText = if (budgetSummary.planned > 0) budgetSummary.planned.toLong().toString() else ""
                showEditPlannedBudgetDialog = true
              }
              .padding(vertical = 2.dp)
              .testTag("event_detail_edit_planned_budget_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                if (language == "bn") "পরিকল্পিত বাজেট" else "Planned",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
              )
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Planned Budget",
                tint = AccentGold,
                modifier = Modifier.size(12.dp)
              )
            }
            Text(
              formatCurrency(budgetSummary.planned, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Column {
            Text(
              if (language == "bn") "মোট খরচ" else "Spent",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted
            )
            Text(
              formatCurrency(budgetSummary.spent, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isOverBudget) DangerRed else DeepPlum
            )
          }

          Column {
            Text(
              text = if (isOverBudget) {
                if (language == "bn") "অতিরিক্ত খরচ" else "Overspent"
              } else {
                if (language == "bn") "অবশিষ্ট" else "Remaining"
              },
              style = MaterialTheme.typography.labelSmall,
              color = if (isOverBudget) DangerRed else TextMuted
            )
            Text(
              text = if (isOverBudget) "- ${formatCurrency(overspentAmount, currencySymbol)}"
              else formatCurrency(budgetSummary.remaining, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isOverBudget) DangerRed else SuccessGreen
            )
          }
        }
      }
    }

    // Edit Planned Budget Dialog for EventDetailScreen
    if (showEditPlannedBudgetDialog) {
      var errorMsg by remember { mutableStateOf(false) }

      AlertDialog(
        onDismissRequest = { showEditPlannedBudgetDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Payment, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (language == "bn") "পরিকল্পিত বাজেট পরিবর্তন" else "Edit Planned Budget",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = if (language == "bn")
                "এই ইভেন্টের জন্য আপনার মোট নির্ধারিত বাজেট লিখুন:"
              else
                "Set the total planned budget for this event:",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )

            OutlinedTextField(
              value = editedBudgetText,
              onValueChange = {
                editedBudgetText = it
                errorMsg = false
              },
              label = { Text("${if (language == "bn") "বাজেটের পরিমাণ" else "Planned Budget"} ($currencySymbol)") },
              placeholder = { Text("e.g. 50000") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              isError = errorMsg,
              supportingText = {
                if (errorMsg) {
                  Text(
                    text = if (language == "bn") "সঠিক বাজেটের পরিমাণ লিখুন" else "Please enter a valid positive number",
                    color = MaterialTheme.colorScheme.error
                  )
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepPlum,
                focusedLabelColor = DeepPlum
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_event_detail_edit_planned_budget")
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val amount = editedBudgetText.toDoubleOrNull()
              if (amount != null && amount >= 0) {
                onUpdatePlannedBudget(amount)
                showEditPlannedBudgetDialog = false
              } else {
                errorMsg = true
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
            modifier = Modifier.testTag("confirm_event_detail_save_planned_budget_button")
          ) {
            Text(if (language == "bn") "সংরক্ষণ করুন" else "Save Budget")
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditPlannedBudgetDialog = false }) {
            Text(if (language == "bn") "বাতিল" else "Cancel", color = TextMuted)
          }
        }
      )
    }

    // Category Breakdown Pie Chart
    if (budgetSummary.categoryBreakdown.isNotEmpty() && budgetSummary.spent > 0) {
      CategoryExpensePieChart(
        categoryBreakdown = budgetSummary.categoryBreakdown,
        totalSpent = budgetSummary.spent,
        currencySymbol = currencySymbol,
        language = language
      )
    }

    // Catering Estimate Card (Guest Headcount -> Catering Quantity Auto-calculation)
    CateringEstimateCard(
      cateringPlan = cateringPlan,
      cateringEstimate = cateringEstimate,
      currencySymbol = currencySymbol,
      onSaveCateringPlan = onSaveCateringPlan,
      onAddCateringToBudget = onAddCateringToBudget,
      language = language,
      modifier = Modifier.testTag("catering_estimate_card")
    )

    // Full budget button
    Button(
      onClick = onOpenFullBudget,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("event_detail_open_full_budget_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = DeepPlum,
        contentColor = Color.White
      )
    ) {
      Text("Manage Budget & Expenses", fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun CateringEstimateCard(
  cateringPlan: CateringPlanEntity?,
  cateringEstimate: CateringEstimate,
  currencySymbol: String,
  onSaveCateringPlan: (perPlateCost: Double, bufferPercent: Int) -> Unit,
  onAddCateringToBudget: (estimatedCost: Double, recommendedPlates: Int, perPlateCost: Double, bufferPercent: Int) -> Unit,
  language: String = "en",
  modifier: Modifier = Modifier
) {
  var perPlateInput by remember(cateringPlan?.perPlateCost) {
    mutableStateOf(
      if (cateringPlan != null && cateringPlan.perPlateCost > 0.0) {
        if (cateringPlan.perPlateCost % 1.0 == 0.0) {
          cateringPlan.perPlateCost.toInt().toString()
        } else {
          cateringPlan.perPlateCost.toString()
        }
      } else ""
    )
  }

  var bufferInput by remember(cateringPlan?.bufferPercent) {
    mutableStateOf((cateringPlan?.bufferPercent ?: 10).toString())
  }

  var justAddedToBudget by remember { mutableStateOf(false) }

  val confirmedCount = cateringEstimate.confirmedGuestCount
  val perPlateCost = perPlateInput.toDoubleOrNull() ?: 0.0
  val bufferPercent = bufferInput.toIntOrNull()?.coerceIn(0, 100) ?: 10

  val bufferPlates = if (confirmedCount > 0) {
    ceil(confirmedCount * bufferPercent / 100.0).toInt()
  } else 0
  val recommendedPlates = confirmedCount + bufferPlates
  val estimatedCost = recommendedPlates * perPlateCost

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Compact Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(DeepPlum.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Restaurant,
              contentDescription = "Catering",
              tint = DeepPlum,
              modifier = Modifier.size(16.dp)
            )
          }

          Column {
            Text(
              text = if (language == "bn") "খাবার ও প্লেট হিসাব" else "Catering Quantity & Cost",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (language == "bn") "মেহমান সংখ্যা অনুযায়ী স্বয়ংক্রিয় হিসাব" else "Guest Headcount → Plate Calculator",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = TextMuted
            )
          }
        }

        Surface(
          color = Emerald.copy(alpha = 0.12f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Emerald,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = "RSVP Sync",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = Emerald
            )
          }
        }
      }

      // Compact Confirmed Headcount Strip
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepPlum.copy(alpha = 0.04f),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = DeepPlum,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = if (language == "bn") "নিশ্চিত মেহমান:" else "Confirmed Guests:",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp,
              color = TextMuted
            )
            Text(
              text = "$confirmedCount",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = DeepPlum
            )
          }

          Text(
            text = if (confirmedCount == 0) {
              if (language == "bn") "কোনো মেহমান নিশ্চিত হয়নি" else "0 RSVP confirmed"
            } else {
              if (language == "bn") "লাইভ সিঙ্ক" else "Live from Guests"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (confirmedCount == 0) AccentGold else Emerald,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Compact Input controls: Cost / plate and Buffer %
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = perPlateInput,
          onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
              perPlateInput = newValue
              val cost = newValue.toDoubleOrNull() ?: 0.0
              val buffer = bufferInput.toIntOrNull()?.coerceIn(0, 100) ?: 10
              onSaveCateringPlan(cost, buffer)
              justAddedToBudget = false
            }
          },
          label = { Text(if (language == "bn") "প্রতি প্লেট খরচ" else "Cost / plate", fontSize = 11.sp) },
          placeholder = { Text("0", fontSize = 12.sp) },
          prefix = {
            Text(
              text = "$currencySymbol ",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = DeepPlum
            )
          },
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .weight(1.3f)
            .testTag("catering_cost_per_plate_input"),
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = bufferInput,
          onValueChange = { newValue ->
            if (newValue.isEmpty() || (newValue.matches(Regex("^\\d+$")) && newValue.length <= 3)) {
              bufferInput = newValue
              val cost = perPlateInput.toDoubleOrNull() ?: 0.0
              val buffer = newValue.toIntOrNull()?.coerceIn(0, 100) ?: 10
              onSaveCateringPlan(cost, buffer)
              justAddedToBudget = false
            }
          },
          label = { Text(if (language == "bn") "বাফার %" else "Buffer %", fontSize = 11.sp) },
          placeholder = { Text("10", fontSize = 12.sp) },
          suffix = {
            Text(
              text = "%",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = DeepPlum
            )
          },
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .weight(0.9f)
            .testTag("catering_buffer_percent_input"),
          shape = RoundedCornerShape(10.dp)
        )
      }

      // Compact Result Section
      if (perPlateCost <= 0.0) {
        Text(
          text = if (language == "bn") "💡 মোট খাবার বাজেট হিসেব করতে প্রতি প্লেটের খরচ লিখুন (ডিফল্ট ১০% বাফার)।"
          else "💡 Enter cost per plate above to calculate food budget (10% buffer recommended).",
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          fontSize = 10.sp
        )
      } else {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = DeepPlum.copy(alpha = 0.04f),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DeepPlum.copy(alpha = 0.12f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (language == "bn") "প্রস্তাবিত প্লেট" else "RECOMMENDED",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "$recommendedPlates Plates",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DeepPlum
              )
              Text(
                text = if (bufferPlates > 0) "$confirmedCount + $bufferPlates extra" else "$confirmedCount confirmed",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
              )
            }

            Box(
              modifier = Modifier
                .height(30.dp)
                .width(1.dp)
                .background(BorderSubtle)
            )

            Column(
              modifier = Modifier
                .weight(1.1f)
                .padding(start = 10.dp),
              horizontalAlignment = Alignment.End
            ) {
              Text(
                text = if (language == "bn") "মোট আনুমানিক খরচ" else "ESTIMATED TOTAL",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = formatCurrency(estimatedCost, currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Emerald
              )
              Text(
                text = "$recommendedPlates × ${formatCurrency(perPlateCost, currencySymbol)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
              )
            }
          }
        }

        Button(
          onClick = {
            onAddCateringToBudget(estimatedCost, recommendedPlates, perPlateCost, bufferPercent)
            justAddedToBudget = true
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .testTag("catering_add_to_budget_button"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (justAddedToBudget) SuccessGreen else Emerald,
            contentColor = Color.White
          ),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = if (justAddedToBudget) Icons.Default.Check else Icons.Default.ReceiptLong,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (justAddedToBudget) {
                if (language == "bn") "বাজেটে আপডেট করা হয়েছে ✓" else "Updated in Budget Line Items ✓"
              } else {
                if (language == "bn") "বাজেটে খাবার যোগ করুন" else "Add Catering to Budget"
              },
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun LiveEventCountdownCard(
  event: EventEntity,
  language: String = "en",
  modifier: Modifier = Modifier
) {
  var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

  LaunchedEffect(event.dateTimeMillis) {
    while (true) {
      currentTime = System.currentTimeMillis()
      delay(1000L)
    }
  }

  val diffMillis = event.dateTimeMillis - currentTime
  val isPast = diffMillis <= 0

  val totalSeconds = (diffMillis / 1000).coerceAtLeast(0)
  val days = totalSeconds / (24 * 3600)
  val hours = (totalSeconds % (24 * 3600)) / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 2.dp),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Timer,
          contentDescription = null,
          tint = DeepPlum,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isPast) {
            if (language == "bn") "🎉 আজই আপনার ইভেন্টের দিন!" else "🎉 Event Day is Today!"
          } else {
            if (language == "bn") "⏳ ইভেন্ট কাউন্টডাউন" else "⏳ Event Countdown"
          },
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = DeepPlum
        )
      }

      if (isPast) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (language == "bn") "আপনার শুভ অনুষ্ঠান শুরু হয়েছে বা সম্পন্ন হয়েছে!" else "Your special event is here!",
          style = MaterialTheme.typography.bodySmall,
          color = SuccessGreen,
          fontWeight = FontWeight.Bold
        )
      } else {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          CountdownUnitBox(value = days.toInt(), label = if (language == "bn") "দিন" else "Days")
          Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DeepPlum)
          CountdownUnitBox(value = hours.toInt(), label = if (language == "bn") "ঘণ্টা" else "Hours")
          Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DeepPlum)
          CountdownUnitBox(value = minutes.toInt(), label = if (language == "bn") "মিনিট" else "Mins")
          Text(":", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DeepPlum)
          CountdownUnitBox(value = seconds.toInt(), label = if (language == "bn") "সেকেন্ড" else "Secs")
        }
      }
    }
  }
}

@Composable
fun CountdownUnitBox(value: Int, label: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = DeepPlum,
      shadowElevation = 1.dp,
      modifier = Modifier.widthIn(min = 44.dp)
    ) {
      Box(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = String.format("%02d", value),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = AccentGoldLight
        )
      }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun TaskReminderDialog(
  task: ChecklistItemEntity,
  eventName: String = "ইভেন্ট",
  language: String = "en",
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val isBn = language == "bn"

  val selectedCalendar = remember {
    mutableStateOf(
      Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 2)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= Calendar.getInstance().timeInMillis) {
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, 9)
        }
      }
    )
  }

  val dateFormat = remember(isBn) { SimpleDateFormat(if (isBn) "dd MMM, yyyy" else "MMM dd, yyyy", if (isBn) Locale("bn") else Locale.getDefault()) }
  val timeFormat = remember(isBn) { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

  val formattedDate = remember(selectedCalendar.value.timeInMillis) {
    dateFormat.format(selectedCalendar.value.time)
  }
  val formattedTime = remember(selectedCalendar.value.timeInMillis) {
    timeFormat.format(selectedCalendar.value.time)
  }

  val datePickerDialog = remember(context) {
    DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        val updated = (selectedCalendar.value.clone() as Calendar).apply {
          set(Calendar.YEAR, year)
          set(Calendar.MONTH, month)
          set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        selectedCalendar.value = updated
      },
      selectedCalendar.value.get(Calendar.YEAR),
      selectedCalendar.value.get(Calendar.MONTH),
      selectedCalendar.value.get(Calendar.DAY_OF_MONTH)
    )
  }

  val timePickerDialog = remember(context) {
    TimePickerDialog(
      context,
      { _, hourOfDay, minute ->
        val updated = (selectedCalendar.value.clone() as Calendar).apply {
          set(Calendar.HOUR_OF_DAY, hourOfDay)
          set(Calendar.MINUTE, minute)
          set(Calendar.SECOND, 0)
        }
        selectedCalendar.value = updated
      },
      selectedCalendar.value.get(Calendar.HOUR_OF_DAY),
      selectedCalendar.value.get(Calendar.MINUTE),
      false
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isBn) "রিমাইন্ডার সেট করুন" else "Set Reminder",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = task.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // Date & Time pickers side by side in 1 Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Date Selector
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier
              .weight(1f)
              .clickable { datePickerDialog.show() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Column {
                Text(
                  text = if (isBn) "তারিখ" else "Date",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                )
                Text(
                  text = formattedDate,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          // Time Selector
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier
              .weight(1f)
              .clickable { timePickerDialog.show() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Column {
                Text(
                  text = if (isBn) "সময়" else "Time",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                )
                Text(
                  text = formattedTime,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        // Quick Preset Chips + Test badge
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Quick preset: +1 hour
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
              modifier = Modifier.clickable {
                val updated = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
                selectedCalendar.value = updated
              }
            ) {
              Text(
                text = if (isBn) "+১ ঘণ্টা" else "+1 Hour",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            // Quick preset: Tomorrow 9 AM
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
              modifier = Modifier.clickable {
                val updated = Calendar.getInstance().apply {
                  add(Calendar.DAY_OF_YEAR, 1)
                  set(Calendar.HOUR_OF_DAY, 9)
                  set(Calendar.MINUTE, 0)
                }
                selectedCalendar.value = updated
              }
            ) {
              Text(
                text = if (isBn) "আগামীকাল ৯টা" else "Tomorrow 9am",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          // Test notification icon button
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.clickable {
              val trigger = System.currentTimeMillis() + 10_000L
              LocalEventReminderReceiver.scheduleTaskReminder(
                context, task.id, task.title, eventName, trigger
              )
              Toast.makeText(
                context,
                if (isBn) "⚡ ১০ সে. পর টেস্ট নোটিফিকেশন আসবে!" else "⚡ Test alert in 10s!",
                Toast.LENGTH_SHORT
              ).show()
              onDismiss()
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Test Notification",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = if (isBn) "১০ সে. টেস্ট" else "10s Test",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val triggerMillis = selectedCalendar.value.timeInMillis
          if (triggerMillis <= System.currentTimeMillis()) {
            Toast.makeText(
              context,
              if (isBn) "অনুগ্রহ করে ভবিষ্যতের সময় বাছুন!" else "Please pick a future time!",
              Toast.LENGTH_SHORT
            ).show()
            return@Button
          }

          LocalEventReminderReceiver.scheduleTaskReminder(
            context = context,
            taskId = task.id,
            taskTitle = task.title,
            eventTitle = eventName,
            triggerTimeMillis = triggerMillis
          )

          val msg = if (isBn) "'${task.title}' কাজের রিমাইন্ডার সেট হয়েছে!" else "Reminder set for '${task.title}'!"
          Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          onDismiss()
        },
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isBn) "রিমাইন্ডার সেট করুন" else "Set Reminder")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text(if (isBn) "বাতিল" else "Cancel")
      }
    }
  )
}
