package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.components.EventCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.TextMuted
import com.example.ui.util.AppStrings

@Composable
fun EventsListScreen(
  events: List<EventEntity>,
  expenses: List<ExpenseEntity>,
  language: String,
  onEventClick: (Long) -> Unit,
  onNewEventClick: () -> Unit,
  onAddGuestClick: (Long) -> Unit,
  onChecklistClick: (Long) -> Unit,
  onDeleteEvent: (EventEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var eventToDelete by remember { mutableStateOf<EventEntity?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategoryFilter by remember { mutableStateOf("All") }

  val categories = listOf("All", "Wedding", "Eid", "Birthday", "Aqiqah", "Custom")

  val filteredEvents = remember(events, searchQuery, selectedCategoryFilter) {
    events.filter { ev ->
      val matchesSearch = searchQuery.isBlank() ||
        ev.title.contains(searchQuery, ignoreCase = true) ||
        ev.location.contains(searchQuery, ignoreCase = true)

      val matchesCategory = selectedCategoryFilter == "All" ||
        ev.category.equals(selectedCategoryFilter, ignoreCase = true)

      matchesSearch && matchesCategory
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Top Bar: "Your Events"
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
        ) {
          Text(
            text = AppStrings.get("your_events", language),
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search events by title, venue...", fontSize = 14.sp) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
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
              .testTag("events_search_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Filter chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            categories.take(4).forEach { cat ->
              val isSelected = cat.equals(selectedCategoryFilter, ignoreCase = true)
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .border(1.dp, if (isSelected) DeepPlum else BorderSubtle, RoundedCornerShape(20.dp))
                  .clickable { selectedCategoryFilter = cat }
                  .testTag("events_filter_$cat"),
                color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
              }
            }
          }
        }
      }

      // List of Events
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 8.dp)
      ) {
        if (filteredEvents.isEmpty()) {
          item {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(16.dp)),
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 1.dp
            ) {
              Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.EventNote,
                  contentDescription = null,
                  tint = TextMuted,
                  modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = if (searchQuery.isNotBlank()) "No events matching '$searchQuery'" else "No events in this category",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = onNewEventClick,
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
                ) {
                  Text("Create New Event", fontSize = 13.sp)
                }
              }
            }
          }
        } else {
          items(filteredEvents, key = { it.id }) { event ->
            val eventExpenses = expenses.filter { it.eventId == event.id }
            val totalSpent = eventExpenses.sumOf { it.amount }

            Box(modifier = Modifier.padding(vertical = 6.dp)) {
              EventCard(
                event = event,
                spentAmount = totalSpent,
                onCardClick = { onEventClick(event.id) },
                onAddGuestClick = { onAddGuestClick(event.id) },
                onChecklistClick = { onChecklistClick(event.id) },
                onDeleteClick = { eventToDelete = event }
              )
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(88.dp))
        }
      }
    }

    // Floating Button
    FloatingActionButton(
      onClick = onNewEventClick,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 24.dp, bottom = 24.dp)
        .testTag("events_fab_new"),
      containerColor = AccentGold,
      contentColor = PlumDark,
      shape = CircleShape
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Event", modifier = Modifier.size(28.dp))
    }

    // Confirmation Dialog before deleting an event
    if (eventToDelete != null) {
      val target = eventToDelete!!
      AlertDialog(
        onDismissRequest = { eventToDelete = null },
        icon = {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(DangerRed.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = null,
              tint = DangerRed,
              modifier = Modifier.size(24.dp)
            )
          }
        },
        title = {
          Text(
            text = if (language == "bn") "ইভেন্ট মুছে ফেলবেন?" else "Delete Event?",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        text = {
          Text(
            text = if (language == "bn") {
              "আপনি কি নিশ্চিতভাবে \"${target.title}\" ইভেন্টটি মুছে ফেলতে চান? এর সাথে যুক্ত সকল খরচ, অতিথি তালিকা ও চেকলিস্টও স্থায়ীভাবে মুছে যাবে।"
            } else {
              "Are you sure you want to delete \"${target.title}\"? All associated expenses, guest lists, and checklist items will also be permanently removed."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        confirmButton = {
          Button(
            onClick = {
              onDeleteEvent(target)
              eventToDelete = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("confirm_delete_event_list_button")
          ) {
            Text(
              text = if (language == "bn") "মুছে ফেলুন" else "Delete",
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        },
        dismissButton = {
          OutlinedButton(
            onClick = { eventToDelete = null },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.testTag("cancel_delete_event_list_button")
          ) {
            Text(
              text = if (language == "bn") "বাতিল" else "Cancel",
              fontWeight = FontWeight.SemiBold
            )
          }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
      )
    }
  }
}
