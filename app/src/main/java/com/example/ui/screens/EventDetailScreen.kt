package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.EventEntity
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.StatMiniCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
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
        0 -> ChecklistTabContent(
          checklist = checklist,
          onToggleItem = onToggleChecklistItem,
          onDeleteItem = onDeleteChecklistItem,
          onAddTaskClick = { showAddTaskDialog = true }
        )

        1 -> GuestsTabContent(
          eventGuests = eventGuests,
          allContacts = allContacts,
          onUpdateStatus = onUpdateGuestStatus,
          onAddGuestClick = onNavigateToAddGuests
        )

        2 -> VendorsTabContent(
          eventGuests = eventGuests,
          allContacts = allContacts,
          onAddVendorClick = onNavigateToAddGuests
        )

        3 -> BudgetTabContent(
          budgetSummary = budgetSummary,
          currencySymbol = currencySymbol,
          onOpenFullBudget = onNavigateToBudget
        )
      }
    }
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
  onAddVendorClick: () -> Unit
) {
  val context = LocalContext.current
  val vendorContacts = remember(allContacts, eventGuests) {
    val guestContactIds = eventGuests.map { it.contactId }.toSet()
    allContacts.filter { it.relation.equals("Vendor", ignoreCase = true) && guestContactIds.contains(it.id) }
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
        text = "${vendorContacts.size} Event Vendors",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Button(
        onClick = onAddVendorClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AccentGold,
          contentColor = PlumDark
        )
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Vendor", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
      }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(vendorContacts, key = { it.id }) { vendor ->
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
                color = TextMuted
              )
              Text(
                text = vendor.phone,
                style = MaterialTheme.typography.labelSmall,
                color = DeepPlum,
                fontWeight = FontWeight.SemiBold
              )
            }

            IconButton(
              onClick = {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vendor.phone}"))
                context.startActivity(dialIntent)
              }
            ) {
              Icon(Icons.Default.Call, contentDescription = "Call Vendor", tint = Emerald)
            }
          }
        }
      }
      item { Spacer(modifier = Modifier.height(30.dp)) }
    }
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
