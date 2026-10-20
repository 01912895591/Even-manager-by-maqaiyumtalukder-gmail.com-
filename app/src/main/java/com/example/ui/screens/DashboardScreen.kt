package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EventCard
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.PlumDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.util.AppStrings
import java.util.Calendar
import java.util.concurrent.TimeUnit

private fun formatCompactAmount(amount: Double, currency: String = "৳"): String {
  return when {
    amount >= 1_000_000 -> "$currency ${(amount / 1_000_000).let { if (it % 1 == 0.0) it.toLong().toString() else "%.1f".format(it) }}M"
    amount >= 1_000 -> "$currency ${(amount / 1_000).let { if (it % 1 == 0.0) it.toLong().toString() else "%.1f".format(it) }}k"
    else -> "$currency ${amount.toLong()}"
  }
}

@Composable
fun DashboardScreen(
  currentUser: UserEntity?,
  events: List<EventEntity>,
  expenses: List<ExpenseEntity>,
  allEventContacts: List<EventContactCrossRef>,
  language: String,
  onEventClick: (Long) -> Unit,
  onNewEventClick: () -> Unit,
  onAddContactClick: () -> Unit,
  onAddExpenseClick: (Long?) -> Unit,
  onAddGuestClick: (Long) -> Unit,
  onChecklistClick: (Long) -> Unit,
  onSeeAllEventsClick: () -> Unit,
  onProfileClick: () -> Unit,
  onDeleteEvent: (EventEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  // Confirmation state for deleting an event
  var eventToDelete by remember { mutableStateOf<EventEntity?>(null) }

  // Selected spotlight event for countdown (defaults to earliest upcoming event)
  var spotlightEventId by remember(events) {
    mutableStateOf(events.minByOrNull { it.dateTimeMillis }?.id)
  }

  val spotlightEvent = events.find { it.id == spotlightEventId } ?: events.firstOrNull()

  // Compute countdown
  val now = System.currentTimeMillis()
  val diff = if (spotlightEvent != null) spotlightEvent.dateTimeMillis - now else 0L
  val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)
  val isPast = diff < 0L
  val isToday = !isPast && (daysLeft == 0L || TimeUnit.MILLISECONDS.toHours(diff) < 24L)

  // Guests calculation for spotlight event
  val spotlightGuests = if (spotlightEvent != null) {
    allEventContacts.filter { it.eventId == spotlightEvent.id }
  } else emptyList()
  val confirmedCount = spotlightGuests.count { it.status == "Confirmed" }
  val totalGuestCount = spotlightGuests.size

  // Category filter state
  var selectedCategoryFilter by remember { mutableStateOf("All") }
  val filteredEvents = remember(events, selectedCategoryFilter) {
    if (selectedCategoryFilter == "All") events
    else events.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
  }

  // Toggle for showing Expense Overview breakdown on dashboard
  var showExpenseOverview by remember { mutableStateOf(false) }

  // Global metrics & pre-grouped map for smooth scrolling performance
  val expensesByEventId = remember(expenses) { expenses.groupBy { it.eventId } }
  val totalPlannedBudget = events.sumOf { it.plannedBudget }
  val totalSpentAllEvents = expenses.sumOf { it.amount }
  val totalConfirmedRSVPs = allEventContacts.count { it.status == "Confirmed" }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val contentMaxWidth = 600.dp

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
      contentAlignment = Alignment.TopCenter
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = contentMaxWidth)
      ) {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
        ) {
        // 1. Dark Plum Hero Header
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(PlumDark)
              .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              // Greeting & Avatar Header
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = AppStrings.get("welcome_back", language),
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGoldLight.copy(alpha = 0.85f)
                  )
                  Text(
                    text = currentUser?.name?.ifBlank { "Event Planner" } ?: "Event Planner",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User Profile Initial Avatar
                Box(
                  modifier = Modifier
                    .clickable { onProfileClick() }
                    .testTag("dashboard_profile_avatar")
                ) {
                  InitialsAvatar(
                    name = currentUser?.name?.ifBlank { "Event Planner" } ?: "Event Planner",
                    backgroundColorHex = "#D4AF6A",
                    size = 38.dp
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Sleek, Ultra-Compact Glassmorphism Metric Strip
              val budgetPercentage = if (totalPlannedBudget > 0) {
                ((totalSpentAllEvents / totalPlannedBudget) * 100).coerceAtMost(100.0).toFloat()
              } else 0f

              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                color = Color.White.copy(alpha = 0.08f)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Celebration,
                      contentDescription = null,
                      tint = AccentGold,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${events.size} ${if (language == "bn") "ইভেন্ট" else "Events"}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }

                  Box(
                    modifier = Modifier
                      .width(1.dp)
                      .height(16.dp)
                      .background(Color.White.copy(alpha = 0.2f))
                  )

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.People,
                      contentDescription = null,
                      tint = AccentGold,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "$totalConfirmedRSVPs ${if (language == "bn") "অতিথি" else "RSVPs"}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }

                  Box(
                    modifier = Modifier
                      .width(1.dp)
                      .height(16.dp)
                      .background(Color.White.copy(alpha = 0.2f))
                  )

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.AccountBalanceWallet,
                      contentDescription = null,
                      tint = AccentGold,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${formatCompactAmount(totalSpentAllEvents)} / ${formatCompactAmount(totalPlannedBudget)}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (budgetPercentage > 90f) Color(0xFFFF8A80) else AccentGoldLight
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Compact Spotlight Event Card or Ultra-Sleek Empty Banner
              if (spotlightEvent != null) {
                // Multi-event switcher pills if more than 1 event exists
                if (events.size > 1) {
                  LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    items(events, key = { it.id }) { ev ->
                      val isSelected = ev.id == spotlightEvent.id
                      Surface(
                        modifier = Modifier
                          .clip(RoundedCornerShape(16.dp))
                          .clickable { spotlightEventId = ev.id },
                        color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = ev.title,
                          color = if (isSelected) PlumDark else Color.White,
                          fontSize = 11.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                          maxLines = 1,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                    }
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                }

                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                      1.5.dp,
                      if (!spotlightEvent.coverPhotoUri.isNullOrBlank()) AccentGold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                      RoundedCornerShape(16.dp)
                    )
                    .clickable { onEventClick(spotlightEvent.id) }
                    .testTag("dashboard_next_event_card"),
                  color = Color.White.copy(alpha = 0.12f)
                ) {
                  Box(modifier = Modifier.fillMaxWidth()) {
                    if (!spotlightEvent.coverPhotoUri.isNullOrBlank()) {
                      AsyncImage(
                        model = spotlightEvent.coverPhotoUri,
                        contentDescription = spotlightEvent.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                      )
                      Box(
                        modifier = Modifier
                          .matchParentSize()
                          .background(
                            brush = Brush.verticalGradient(
                              colors = listOf(
                                Color.Black.copy(alpha = 0.50f),
                                Color.Black.copy(alpha = 0.85f)
                              )
                            )
                          )
                      )
                    }

                    Column(modifier = Modifier.padding(12.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                              shape = RoundedCornerShape(4.dp),
                              color = AccentGold.copy(alpha = 0.25f)
                            ) {
                              Text(
                                text = spotlightEvent.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                              )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                              text = "${spotlightEvent.location} • ${spotlightEvent.dateFormatted}",
                              style = MaterialTheme.typography.labelSmall,
                              color = Color.White.copy(alpha = 0.90f),
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis
                            )
                          }
                          Spacer(modifier = Modifier.height(3.dp))
                          Text(
                            text = spotlightEvent.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Compact Gold Countdown Badge
                        Surface(
                          shape = RoundedCornerShape(10.dp),
                          color = AccentGold
                        ) {
                          Box(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                          ) {
                            Text(
                              text = if (isToday) (if (language == "bn") "আজ" else "TODAY")
                                     else if (isPast) AppStrings.get("passed", language)
                                     else "$daysLeft ${if (language == "bn") "দিন" else "Days"}",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Bold,
                              color = PlumDark,
                              fontSize = 11.sp
                            )
                          }
                        }
                      }

                      Spacer(modifier = Modifier.height(8.dp))

                      // Quick action row
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Surface(
                          modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAddGuestClick(spotlightEvent.id) }
                            .testTag("spotlight_add_guest_btn"),
                          color = Color.White.copy(alpha = 0.22f)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                          ) {
                            Icon(
                              imageVector = Icons.Default.GroupAdd,
                              contentDescription = "Guest",
                              tint = Color.White,
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = if (language == "bn") "অতিথি" else "Guest",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Bold,
                              color = Color.White
                            )
                          }
                        }

                        Surface(
                          modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onChecklistClick(spotlightEvent.id) }
                            .testTag("spotlight_checklist_btn"),
                          color = AccentGold.copy(alpha = 0.30f)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                          ) {
                            Icon(
                              imageVector = Icons.Default.Checklist,
                              contentDescription = "Checklist",
                              tint = AccentGoldLight,
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = if (language == "bn") "চেকলিস্ট" else "Checklist",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Bold,
                              color = Color.White
                            )
                          }
                        }

                        Surface(
                          modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, DangerRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable { eventToDelete = spotlightEvent }
                            .testTag("spotlight_delete_event_btn"),
                          color = DangerRed.copy(alpha = 0.30f)
                        ) {
                          Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                          ) {
                            Icon(
                              imageVector = Icons.Default.Delete,
                              contentDescription = "Delete Event",
                              tint = Color.White,
                              modifier = Modifier.size(15.dp)
                            )
                          }
                        }
                      }
                    }
                  }
                }
              } else {
                // Premium, Inviting Empty State Hero Card ("Start Planning Your Event")
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                      brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                          Color.White.copy(alpha = 0.16f),
                          Color.White.copy(alpha = 0.08f)
                        )
                      )
                    )
                    .border(1.5.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clickable { onNewEventClick() }
                    .testTag("dashboard_empty_create_event_hero"),
                  color = Color.Transparent
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(
                      modifier = Modifier.weight(1f),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(40.dp)
                          .clip(CircleShape)
                          .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                              colors = listOf(AccentGold.copy(alpha = 0.35f), AccentGold.copy(alpha = 0.15f))
                            )
                          )
                          .border(1.dp, AccentGold.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Celebration,
                          contentDescription = null,
                          tint = AccentGold,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(
                          text = if (language == "bn") "আপনার ইভেন্ট পরিকল্পনা শুরু করুন" else "Start Planning Your Event",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = if (language == "bn") "বিয়ে, জন্মদিন বা অনুষ্ঠান সহজ প্ল্যানিং 🚀" else "Wedding, birthday or party in 1 min 🚀",
                          style = MaterialTheme.typography.bodySmall,
                          color = Color.White.copy(alpha = 0.9f),
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                      onClick = onNewEventClick,
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = PlumDark
                      ),
                      contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                      modifier = Modifier
                        .height(36.dp)
                        .testTag("dashboard_empty_create_event_cta")
                    ) {
                      Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                      Spacer(modifier = Modifier.width(3.dp))
                      Text(
                        text = if (language == "bn") "নতুন" else "Create",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Three Compact Quick-Action Tiles
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                QuickActionTile(
                  title = AppStrings.get("new_event", language),
                  icon = Icons.Default.EventNote,
                  onClick = onNewEventClick,
                  modifier = Modifier.weight(1f),
                  testTag = "dashboard_quick_new_event"
                )

                QuickActionTile(
                  title = AppStrings.get("add_contact", language),
                  icon = Icons.Default.GroupAdd,
                  onClick = onAddContactClick,
                  modifier = Modifier.weight(1f),
                  testTag = "dashboard_quick_add_contact"
                )

                QuickActionTile(
                  title = AppStrings.get("expense_overview", language),
                  icon = Icons.Default.AccountBalanceWallet,
                  onClick = { onAddExpenseClick(spotlightEvent?.id) },
                  modifier = Modifier.weight(1f),
                  testTag = "dashboard_quick_add_expense"
                )
              }
            }
          }
        }

        // Payments Due Summary Card (Feature 1)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        val weekEnd = todayStart + (7L * 24 * 60 * 60 * 1000)

        val dueExpenses = expenses.filter { exp ->
          val remDue = if (exp.dueAmount > 0) exp.dueAmount else (exp.amount - exp.advancePaid).coerceAtLeast(0.0)
          !exp.paymentStatus.equals("Paid", ignoreCase = true) && remDue > 0.0 && exp.dueDate != null
        }
        val overdueCount = dueExpenses.count { (it.dueDate ?: 0L) < todayStart }
        val dueThisWeekCount = dueExpenses.count { (it.dueDate ?: 0L) in todayStart..weekEnd }
        val totalDueAmount = dueExpenses.sumOf { if (it.dueAmount > 0) it.dueAmount else (it.amount - it.advancePaid).coerceAtLeast(0.0) }

        if (overdueCount > 0 || dueThisWeekCount > 0 || dueExpenses.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                  1.dp,
                  if (overdueCount > 0) DangerRed.copy(alpha = 0.5f) else AccentGold.copy(alpha = 0.5f),
                  RoundedCornerShape(16.dp)
                )
                .clickable { onAddExpenseClick(spotlightEvent?.id) }
                .testTag("dashboard_payments_due_card"),
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 2.dp
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  modifier = Modifier.weight(1f),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = CircleShape,
                    color = if (overdueCount > 0) DangerRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = if (overdueCount > 0) Icons.Default.Paid else Icons.Default.AccountBalanceWallet,
                        contentDescription = "Payments Due",
                        tint = if (overdueCount > 0) DangerRed else WarningAmber,
                        modifier = Modifier.size(22.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = if (language == "bn") "বকেয়া ভেন্ডর পেমেন্ট" else "Vendor Payments Due",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      if (overdueCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = DangerRed
                        ) {
                          Text(
                            text = if (language == "bn") "$overdueCount মেয়াদোত্তীর্ণ" else "$overdueCount Overdue",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val statusSubtitle = buildString {
                      if (overdueCount > 0) {
                        append(if (language == "bn") "$overdueCount মেয়াদোত্তীর্ণ" else "$overdueCount overdue")
                      }
                      if (dueThisWeekCount > 0) {
                        if (isNotEmpty()) append(" • ")
                        append(if (language == "bn") "$dueThisWeekCount এই সপ্তাহে" else "$dueThisWeekCount due this week")
                      }
                      if (isEmpty()) {
                        append(if (language == "bn") "${dueExpenses.size}টি পেমেন্ট বাকি" else "${dueExpenses.size} payments scheduled")
                      }
                    }

                    Text(
                      text = statusSubtitle,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (overdueCount > 0) DangerRed else TextMuted
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = formatCompactAmount(totalDueAmount),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (overdueCount > 0) DangerRed else WarningAmber
                    )
                    Text(
                      text = if (language == "bn") "সময়সূচী দেখুন" else "View schedule",
                      fontSize = 11.sp,
                      color = DeepPlum,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = DeepPlum,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }

        // 2. Expense & Budget Overview Section (Only shown if user clicks "Expense Overview")
        if (showExpenseOverview) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 2.dp
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DeepPlum.copy(alpha = 0.1f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        tint = DeepPlum,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = AppStrings.get("expense_overview", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = if (events.isEmpty()) {
                          if (language == "bn") "কোনো ইভেন্ট নেই" else "No events planned"
                        } else {
                          if (language == "bn") "${events.size}টি ইভেন্টের মোট ব্যয় ও বাজেট" else "Across ${events.size} event${if (events.size > 1) "s" else ""}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                      onClick = { onAddExpenseClick(spotlightEvent?.id) },
                      modifier = Modifier.testTag("dashboard_view_budget_btn")
                    ) {
                      Text(
                        text = if (language == "bn") "বিস্তারিত" else "View All",
                        color = DeepPlum,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = DeepPlum,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                      onClick = { showExpenseOverview = false },
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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If there are events, show a breakdown of each event's budget status
                if (events.isNotEmpty()) {
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    events.forEach { ev ->
                      val evExpenses = expenses.filter { it.eventId == ev.id }
                      val evSpent = evExpenses.sumOf { it.amount }
                      val evPlanned = ev.plannedBudget
                      val evProgress = if (evPlanned > 0) (evSpent / evPlanned).coerceIn(0.0, 1.0).toFloat() else 0f
                      val catColor = getCategoryColor(ev.category)

                      Surface(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(RoundedCornerShape(12.dp))
                          .border(1.dp, BorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                          .clickable { onAddExpenseClick(ev.id) },
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                      ) {
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                        ) {
                          Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                          ) {
                            Row(
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier.weight(1f)
                            ) {
                              Box(
                                modifier = Modifier
                                  .size(10.dp)
                                  .clip(CircleShape)
                                  .background(catColor)
                              )
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(
                                text = ev.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                              )
                            }

                            Text(
                              text = "${formatCompactAmount(evSpent)} / ${formatCompactAmount(evPlanned)}",
                              style = MaterialTheme.typography.labelMedium,
                              fontWeight = FontWeight.Bold,
                              color = if (evSpent > evPlanned && evPlanned > 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                            )
                          }

                          Spacer(modifier = Modifier.height(6.dp))

                          LinearProgressIndicator(
                            progress = { evProgress },
                            modifier = Modifier
                              .fillMaxWidth()
                              .height(5.dp)
                              .clip(RoundedCornerShape(2.5.dp)),
                            color = if (evSpent > evPlanned && evPlanned > 0) Color(0xFFD32F2F) else catColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                          )
                        }
                      }
                    }
                  }
                } else {
                  Text(
                    text = if (language == "bn") "কোনো ইভেন্ট নেই। নতুন ইভেন্ট তৈরি করে ব্যয় ট্র্যাক করুন।" else "No events yet. Create an event to begin tracking expenses and budget.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                  )
                }
              }
            }
          }
        }

        // 2. Category Filter & Section Header
        item {
          Spacer(modifier = Modifier.height(20.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = AppStrings.get("your_events", language),
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )

            Text(
              text = AppStrings.get("see_all", language),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = DeepPlum,
              modifier = Modifier
                .clickable { onSeeAllEventsClick() }
                .padding(4.dp)
                .testTag("dashboard_see_all_events")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Category filter pills
          val categoryChips = listOf("All", "Wedding", "Eid", "Birthday", "Aqiqah")
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(categoryChips) { cat ->
              val isSelected = selectedCategoryFilter == cat
              FilterChip(
                selected = isSelected,
                onClick = { selectedCategoryFilter = cat },
                label = { Text(cat, fontSize = 12.sp) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = DeepPlum,
                  selectedLabelColor = Color.White,
                  containerColor = MaterialTheme.colorScheme.surface,
                  labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                  borderColor = if (isSelected) DeepPlum else BorderSubtle,
                  enabled = true,
                  selected = isSelected
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Scrollable List of Event Cards
        if (filteredEvents.isEmpty()) {
          item {
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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
                  modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "No events in this category",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                  onClick = onNewEventClick,
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
                ) {
                  Text("Add New Event", fontSize = 13.sp)
                }
              }
            }
          }
        } else {
          items(filteredEvents, key = { it.id }) { event ->
            val eventExpenses = expensesByEventId[event.id] ?: emptyList()
            val totalSpent = eventExpenses.sumOf { it.amount }

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
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

        // Space for bottom nav and FAB
        item {
          Spacer(modifier = Modifier.height(96.dp))
        }
      }

      // Floating Action Button
      FloatingActionButton(
        onClick = onNewEventClick,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 20.dp, bottom = 20.dp)
          .testTag("dashboard_fab_new_event"),
        containerColor = AccentGold,
        contentColor = PlumDark,
        shape = CircleShape
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "New Event",
          modifier = Modifier.size(28.dp)
        )
      }
    }
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
            modifier = Modifier.testTag("confirm_delete_event_button")
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
            modifier = Modifier.testTag("cancel_delete_event_button")
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

@Composable
fun MetricSummaryItem(
  label: String,
  value: String,
  icon: ImageVector
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AccentGold,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White.copy(alpha = 0.7f),
      fontSize = 10.sp
    )
  }
}

@Composable
fun QuickActionTile(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  testTag: String = ""
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .border(
        width = if (isSelected) 1.5.dp else 1.dp,
        color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .testTag(testTag),
    color = if (isSelected) AccentGold.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f)
  ) {
    Row(
      modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(26.dp)
          .clip(CircleShape)
          .background(if (isSelected) AccentGold else AccentGold.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (isSelected) PlumDark else AccentGold,
          modifier = Modifier.size(14.dp)
        )
      }
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
        color = if (isSelected) AccentGold else Color.White,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
