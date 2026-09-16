package com.example.ui.screens

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventDayEntity
import com.example.data.model.EventEntity
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.StatMiniCard
import com.example.ui.components.StatusBadge
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
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.EventBudgetSummary
import com.example.ui.viewmodel.EventGuestSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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
  onAddChecklistItem: (title: String, dueDate: String) -> Unit,
  onDeleteChecklistItem: (ChecklistItemEntity) -> Unit,
  onUpdateGuestStatus: (contactId: Long, newStatus: String) -> Unit,
  onNavigateToAddGuests: () -> Unit,
  onNavigateToBudget: () -> Unit,
  onEditEvent: () -> Unit = {},
  onUpdateContact: (ContactEntity) -> Unit = {},
  onAddVendorToEvent: (name: String, phone: String, note: String) -> Unit = { _, _, _ -> },
  onRemoveVendorFromEvent: (contactId: Long) -> Unit = {},
  onLinkContactToEvent: (contactId: Long) -> Unit = {},
  eventDays: List<EventDayEntity> = emptyList(),
  onAddEventDay: (dayTitle: String, dateFormatted: String, timeFormatted: String, dateTimeMillis: Long, location: String, notes: String) -> Unit = { _, _, _, _, _, _ -> },
  onUpdateEventDay: (EventDayEntity) -> Unit = {},
  onDeleteEventDay: (EventDayEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf(
    if (language == "bn") "সময়সূচী" else "Schedule",
    AppStrings.get("checklist", language),
    AppStrings.get("guests", language),
    AppStrings.get("vendors", language),
    AppStrings.get("budget", language)
  )

  var showAddTaskDialog by remember { mutableStateOf(false) }
  var showAddDayDialog by remember { mutableStateOf(false) }
  var dayToEdit by remember { mutableStateOf<EventDayEntity?>(null) }

  // Days left calculation
  val daysLeft = if (event != null) {
    val diff = event.dateTimeMillis - System.currentTimeMillis()
    TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
  } else 0

  val coverBg = if (event != null) getCategoryColor(event.category) else Emerald

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
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
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
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
                  val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "You're invited to ${event.title} on ${event.dateFormatted} at ${event.location}!")
                    type = "text/plain"
                  }
                  val shareIntent = Intent.createChooser(sendIntent, null)
                  context.startActivity(shareIntent)
                }
              },
              modifier = Modifier.testTag("event_detail_share_button")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Label
        Text(
          text = (event?.category ?: "EVENT").uppercase(),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = AccentGoldLight,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Title in Serif
        Text(
          text = event?.title ?: "Event Details",
          style = MaterialTheme.typography.headlineMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Date + Location
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${event?.dateFormatted ?: ""} • ${event?.timeFormatted ?: ""}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
          )
        }

        if (!event?.location.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
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

    // 2. Three Stat Mini-Cards Side by Side
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
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
      TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DeepPlum,
        indicator = { tabPositions ->
          if (selectedTabIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
              color = DeepPlum
            )
          }
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
                color = if (isSelected) DeepPlum else TextMuted
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
        0 -> ScheduleTabContent(
          event = event,
          eventDays = eventDays,
          language = language,
          onAddFunctionClick = { showAddDayDialog = true },
          onEditDay = { dayToEdit = it },
          onDeleteDay = onDeleteEventDay
        )

        1 -> ChecklistTabContent(
          checklist = checklist,
          onToggleItem = onToggleChecklistItem,
          onDeleteItem = onDeleteChecklistItem,
          onAddTaskClick = { showAddTaskDialog = true }
        )

        2 -> GuestsTabContent(
          eventGuests = eventGuests,
          allContacts = allContacts,
          onUpdateStatus = onUpdateGuestStatus,
          onAddGuestClick = onNavigateToAddGuests
        )

        3 -> VendorsTabContent(
          eventGuests = eventGuests,
          allContacts = allContacts,
          onAddVendorClick = onNavigateToAddGuests,
          onUpdateContact = onUpdateContact,
          onAddNewVendor = onAddVendorToEvent,
          onRemoveVendor = onRemoveVendorFromEvent,
          onLinkExistingContactAsVendor = onLinkContactToEvent,
          language = language
        )

        4 -> BudgetTabContent(
          budgetSummary = budgetSummary,
          currencySymbol = currencySymbol,
          onOpenFullBudget = onNavigateToBudget
        )
      }
    }
  }

  // Dialog to Add/Edit Day / Function in Schedule
  if (showAddDayDialog || dayToEdit != null) {
    AddEditEventDayDialog(
      initialDay = dayToEdit,
      defaultLocation = event?.location ?: "",
      defaultDate = event?.dateFormatted ?: "",
      defaultTime = event?.timeFormatted ?: "18:00",
      language = language,
      onDismiss = {
        showAddDayDialog = false
        dayToEdit = null
      },
      onSave = { title, date, time, location, description, dateTimeMillis ->
        val currentEdit = dayToEdit
        if (currentEdit != null) {
          onUpdateEventDay(
            currentEdit.copy(
              dayTitle = title,
              dateFormatted = date,
              timeFormatted = time,
              location = location,
              notes = description,
              dateTimeMillis = dateTimeMillis
            )
          )
        } else {
          onAddEventDay(title, date, time, dateTimeMillis, location, description)
        }
        showAddDayDialog = false
        dayToEdit = null
      }
    )
  }

  // Dialog to Add Task to Checklist
  if (showAddTaskDialog) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDueDate by remember { mutableStateOf("Oct 20") }

    AlertDialog(
      onDismissRequest = { showAddTaskDialog = false },
      title = {
        Text("Add Checklist Item", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Task description") },
            placeholder = { Text("e.g. Order wedding stage flowers") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = taskDueDate,
            onValueChange = { taskDueDate = it },
            label = { Text("Target Due Date") },
            placeholder = { Text("e.g. Oct 22") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (taskTitle.isNotBlank()) {
              onAddChecklistItem(taskTitle, taskDueDate)
              showAddTaskDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("Add Task")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddTaskDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}

@Composable
fun ChecklistTabContent(
  checklist: List<ChecklistItemEntity>,
  onToggleItem: (ChecklistItemEntity) -> Unit,
  onDeleteItem: (ChecklistItemEntity) -> Unit,
  onAddTaskClick: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 14.dp)
  ) {
    items(checklist, key = { it.id }) { item ->
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
          // Checkbox with Green Check Icon when done
          Checkbox(
            checked = item.isDone,
            onCheckedChange = { onToggleItem(item) },
            colors = CheckboxDefaults.colors(
              checkedColor = SuccessGreen,
              uncheckedColor = TextMuted
            ),
            modifier = Modifier.testTag("checklist_checkbox_${item.id}")
          )

          Spacer(modifier = Modifier.width(6.dp))

          // Task Title (with strikethrough if done)
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (item.isDone) FontWeight.Normal else FontWeight.Medium,
              color = if (item.isDone) TextMuted else MaterialTheme.colorScheme.onSurface,
              textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
          }

          // Amber Due Date Pill for tasks not yet done
          if (!item.isDone && item.dueDate.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(WarningAmber.copy(alpha = 0.16f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = item.dueDate,
                color = WarningAmber,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          IconButton(
            onClick = { onDeleteItem(item) },
            modifier = Modifier.size(30.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete task",
              tint = TextMuted.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // "+ Add task" row at the bottom
    item {
      Spacer(modifier = Modifier.height(10.dp))
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
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add task",
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
  onAddGuestClick: () -> Unit
) {
  val contactMap = remember(allContacts) { allContacts.associateBy { it.id } }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${eventGuests.size} Invited Guests",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Button(
        onClick = onAddGuestClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AccentGold,
          contentColor = PlumDark
        ),
        modifier = Modifier.testTag("guests_tab_add_button")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Guests", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
      }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(eventGuests, key = { it.contactId }) { guestRef ->
        val contact = contactMap[guestRef.contactId]
        if (contact != null) {
          GuestRowItem(
            contact = contact,
            status = guestRef.status,
            onStatusChange = { newStatus ->
              onUpdateStatus(contact.id, newStatus)
            }
          )
        }
      }
      item { Spacer(modifier = Modifier.height(30.dp)) }
    }
  }
}

@Composable
fun GuestRowItem(
  contact: ContactEntity,
  status: String,
  onStatusChange: (String) -> Unit
) {
  val context = LocalContext.current
  var menuExpanded by remember { mutableStateOf(false) }

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
        Text(
          text = contact.name,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
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
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Call Icon
      IconButton(
        onClick = {
          val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
          context.startActivity(dialIntent)
        },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.Call, contentDescription = "Call", tint = DeepPlum, modifier = Modifier.size(18.dp))
      }
    }
  }
}

@Composable
fun VendorsTabContent(
  eventGuests: List<EventContactCrossRef>,
  allContacts: List<ContactEntity>,
  onAddVendorClick: () -> Unit,
  onUpdateContact: (ContactEntity) -> Unit = {},
  onAddNewVendor: (name: String, phone: String, note: String) -> Unit = { _, _, _ -> },
  onRemoveVendor: (contactId: Long) -> Unit = {},
  onLinkExistingContactAsVendor: (contactId: Long) -> Unit = {},
  language: String = "en"
) {
  val context = LocalContext.current
  var showAddVendorDialog by remember { mutableStateOf(false) }
  var vendorToEdit by remember { mutableStateOf<ContactEntity?>(null) }

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
        text = if (language == "bn") "${vendorContacts.size} জন ভেন্ডর" else "${vendorContacts.size} Event Vendors",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Button(
        onClick = { showAddVendorDialog = true },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AccentGold,
          contentColor = PlumDark
        ),
        modifier = Modifier.testTag("event_add_vendor_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (language == "bn") "ভেন্ডর যোগ করুন" else "Add Vendor",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
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
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (language == "bn") "ক্যাটারিং, স্টেজ, ফটোগ্রাফি ইত্যাদি ভেন্ডর যুক্ত করুন" else "Add caterers, photographers, decorators, sound systems, and more.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(18.dp))
          Button(
            onClick = { showAddVendorDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepPlum, contentColor = Color.White)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (language == "bn") "প্রথম ভেন্ডর যোগ করুন" else "Add Your First Vendor", fontWeight = FontWeight.SemiBold)
          }
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
              InitialsAvatar(name = vendor.name, backgroundColorHex = "#1F6E52", size = 42.dp)

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = vendor.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = vendor.note.ifBlank { "Service Provider" },
                  style = MaterialTheme.typography.bodySmall,
                  color = AccentGold,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = vendor.phone,
                  style = MaterialTheme.typography.labelSmall,
                  color = DeepPlum,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
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
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(Icons.Default.Call, contentDescription = "Call Vendor", tint = Emerald, modifier = Modifier.size(20.dp))
                }

                // Edit button
                IconButton(
                  onClick = { vendorToEdit = vendor },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit Vendor", tint = DeepPlum, modifier = Modifier.size(18.dp))
                }

                // Remove from event button
                IconButton(
                  onClick = { onRemoveVendor(vendor.id) },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Remove Vendor", tint = TextMuted, modifier = Modifier.size(18.dp))
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
          Spacer(modifier = Modifier.height(8.dp))
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
          Column(modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
fun BudgetTabContent(
  budgetSummary: EventBudgetSummary,
  currencySymbol: String,
  onOpenFullBudget: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
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
            text = "Budget Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${budgetSummary.percentageSpent.toInt()}% Used",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AccentGold
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Planned", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
              formatCurrency(budgetSummary.planned, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Column {
            Text("Spent", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
              formatCurrency(budgetSummary.spent, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = DeepPlum
            )
          }

          Column {
            Text("Remaining", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
              formatCurrency(budgetSummary.remaining, currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = SuccessGreen
            )
          }
        }
      }
    }

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
fun ScheduleTabContent(
  event: EventEntity?,
  eventDays: List<EventDayEntity>,
  language: String,
  onAddFunctionClick: () -> Unit,
  onEditDay: (EventDayEntity) -> Unit,
  onDeleteDay: (EventDayEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val sortedDays = remember(eventDays) {
    eventDays.sortedBy { it.dateTimeMillis }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 80.dp)
  ) {
    // Header with action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (language == "bn") "সময়সূচী ও অনুষ্ঠানসমূহ" else "Event Functions & Schedule",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = if (sortedDays.isNotEmpty()) {
              if (language == "bn") "${sortedDays.size}টি অনুষ্ঠান নির্ধারিত" else "${sortedDays.size} function${if (sortedDays.size > 1) "s" else ""} scheduled"
            } else {
              if (language == "bn") "১টি মূল অনুষ্ঠান (একক দিন)" else "1 main function (Single-day default)"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
          )
        }

        Button(
          onClick = onAddFunctionClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
          modifier = Modifier.testTag("add_event_function_button")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (language == "bn") "অনুষ্ঠান যোগ" else "Add Function",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Backward compatibility: If no EventDayEntity rows exist yet, show the event's top-level date/time/location
    if (sortedDays.isEmpty()) {
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 2.dp
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = DeepPlum.copy(alpha = 0.12f),
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = "1",
                      color = DeepPlum,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }
                }

                Column {
                  Text(
                    text = event?.title ?: "Main Event",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = if (language == "bn") "মূল অনুষ্ঠান (ডিফল্ট)" else "Main Function (Default)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentGold,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = SuccessGreen.copy(alpha = 0.15f)
              ) {
                Text(
                  text = if (language == "bn") "সক্রিয়" else "Active",
                  color = SuccessGreen,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date & Time
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = DeepPlum,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "${event?.dateFormatted ?: ""} • ${event?.timeFormatted ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            if (!event?.location.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = TextMuted,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = event.location,
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextMuted
                )
              }
            }

            if (!event?.description.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextDark
              )
            }
          }
        }
      }

      // Explanatory Banner for Multi-day features
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
          color = AccentGold.copy(alpha = 0.08f)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = if (language == "bn") "বহুদিনের অনুষ্ঠান পরিকল্পনা?" else "Planning a Multi-Day Event?",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (language == "bn")
                "গায়ে হলুদ, মেহেদী নাইট, সঙ্গীত, বিয়ে এবং ওয়ালিমা/রিসেপশনের মতো প্রতিটি দিনের জন্য আলাদা সময়সূচী ও ভেন্যু যোগ করুন।"
              else
                "Add distinct days and functions for Haldi/Mehendi, Sangeet, Wedding Ceremony, and Walima/Reception with custom dates, timings, and venues.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
              onClick = onAddFunctionClick,
              modifier = Modifier.align(Alignment.End)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DeepPlum, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (language == "bn") "+ প্রথম ফাংশন যোগ করুন" else "+ Add First Function",
                color = DeepPlum,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    } else {
      // Show the list of functions in chronological/order sequence
      items(sortedDays, key = { it.id }) { day ->
        val index = sortedDays.indexOf(day) + 1
        ScheduleTimelineItemCard(
          dayNumber = index,
          day = day,
          language = language,
          onEdit = { onEditDay(day) },
          onDelete = { onDeleteDay(day) }
        )
      }
    }
  }
}

@Composable
fun ScheduleTimelineItemCard(
  dayNumber: Int,
  day: EventDayEntity,
  language: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDeleteConfirm by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = DeepPlum.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "$dayNumber",
                color = DeepPlum,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }

          Column {
            Text(
              text = day.dayTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (language == "bn") "দিন $dayNumber" else "Day $dayNumber",
              style = MaterialTheme.typography.labelSmall,
              color = AccentGold,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Function",
              tint = DeepPlum,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Function",
              tint = DangerRed,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Date & Time
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CalendarToday,
          contentDescription = null,
          tint = DeepPlum,
          modifier = Modifier.size(15.dp)
        )
        Text(
          text = if (day.timeFormatted.isNotBlank()) "${day.dateFormatted} • ${day.timeFormatted}" else day.dateFormatted,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      // Location
      if (day.location.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = day.location,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
          )
        }
      }

      // Description / Note
      if (day.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = day.notes,
            style = MaterialTheme.typography.bodySmall,
            color = TextDark,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = {
        Text(
          text = if (language == "bn") "অনুষ্ঠান মুছে ফেলবেন?" else "Delete Function?",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = if (language == "bn")
            "আপনি কি নিশ্চিত যে '${day.dayTitle}' অনুষ্ঠানটি মুছে ফেলতে চান?"
          else
            "Are you sure you want to remove '${day.dayTitle}' from the schedule?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDelete()
            showDeleteConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
          Text(if (language == "bn") "মুছুন" else "Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text(if (language == "bn") "বাতিল" else "Cancel", color = TextMuted)
        }
      }
    )
  }
}

@Composable
fun AddEditEventDayDialog(
  initialDay: EventDayEntity?,
  defaultLocation: String,
  defaultDate: String,
  defaultTime: String,
  language: String,
  onDismiss: () -> Unit,
  onSave: (title: String, date: String, time: String, location: String, description: String, dateTimeMillis: Long) -> Unit
) {
  val context = LocalContext.current
  val isEditing = initialDay != null

  var title by remember { mutableStateOf(initialDay?.dayTitle ?: "") }
  var dateFormatted by remember { mutableStateOf(initialDay?.dateFormatted ?: if (defaultDate.isNotBlank()) defaultDate else "Oct 24, 2026") }
  var timeFormatted by remember { mutableStateOf(initialDay?.timeFormatted ?: if (defaultTime.isNotBlank()) defaultTime else "19:00") }
  var location by remember { mutableStateOf(initialDay?.location ?: defaultLocation) }
  var description by remember { mutableStateOf(initialDay?.notes ?: "") }
  var dateTimeMillis by remember { mutableStateOf(initialDay?.dateTimeMillis ?: System.currentTimeMillis()) }

  val calendar = remember {
    Calendar.getInstance().apply {
      if (initialDay?.dateTimeMillis != null && initialDay.dateTimeMillis > 0) {
        timeInMillis = initialDay.dateTimeMillis
      }
    }
  }

  val datePicker = remember {
    DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dateFormatted = sdf.format(calendar.time)
        dateTimeMillis = calendar.timeInMillis
      },
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH)
    )
  }

  val timePicker = remember {
    TimePickerDialog(
      context,
      { _, hourOfDay, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val amPm = if (hourOfDay >= 12) "PM" else "AM"
        val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
        timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
        dateTimeMillis = calendar.timeInMillis
      },
      calendar.get(Calendar.HOUR_OF_DAY),
      calendar.get(Calendar.MINUTE),
      false
    )
  }

  val suggestedTitles = listOf("Gaye Holud", "Mehendi Night", "Sangeet", "Wedding Ceremony", "Walima / Reception", "Post-Wedding Brunch")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isEditing) {
          if (language == "bn") "অনুষ্ঠান সম্পাদনা" else "Edit Function"
        } else {
          if (language == "bn") "নতুন দিন/অনুষ্ঠান যোগ করুন" else "Add Day / Function"
        },
        style = MaterialTheme.typography.titleLarge,
        fontFamily = FontFamily.Serif
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Suggested function tags
        Text(
          text = if (language == "bn") "জনপ্রিয় অনুষ্ঠান নির্বাচন করুন:" else "Quick Suggestions:",
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(suggestedTitles) { chip ->
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (title == chip) DeepPlum.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (title == chip) androidx.compose.foundation.BorderStroke(1.dp, DeepPlum) else null,
              modifier = Modifier.clickable { title = chip }
            ) {
              Text(
                text = chip,
                style = MaterialTheme.typography.labelSmall,
                color = if (title == chip) DeepPlum else TextDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(if (language == "bn") "অনুষ্ঠানের নাম" else "Function / Day Title") },
          placeholder = { Text("e.g. Gaye Holud / Sangeet") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("function_title_input")
        )

        // Date and Time Pickers
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = dateFormatted,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (language == "bn") "তারিখ" else "Date") },
            trailingIcon = {
              IconButton(onClick = { datePicker.show() }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = DeepPlum)
              }
            },
            modifier = Modifier
              .weight(1f)
              .clickable { datePicker.show() }
          )

          OutlinedTextField(
            value = timeFormatted,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (language == "bn") "সময়" else "Time") },
            trailingIcon = {
              IconButton(onClick = { timePicker.show() }) {
                Icon(Icons.Default.AccessTime, contentDescription = "Pick Time", tint = DeepPlum)
              }
            },
            modifier = Modifier
              .weight(1f)
              .clickable { timePicker.show() }
          )
        }

        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text(if (language == "bn") "ভেন্যু বা স্থান" else "Venue / Location") },
          placeholder = { Text("e.g. Community Center or Hotel") },
          leadingIcon = {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text(if (language == "bn") "বিবরণ বা নোট" else "Notes / Dress Code / Description") },
          placeholder = { Text("e.g. Traditional Yellow dress, starts with snacks") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSave(title.trim(), dateFormatted, timeFormatted, location.trim(), description.trim(), dateTimeMillis)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
        enabled = title.isNotBlank(),
        modifier = Modifier.testTag("save_event_function_button")
      ) {
        Text(
          if (isEditing) {
            if (language == "bn") "আপডেট করুন" else "Save Changes"
          } else {
            if (language == "bn") "যোগ করুন" else "Add Function"
          }
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (language == "bn") "বাতিল" else "Cancel", color = TextMuted)
      }
    }
  )
}
