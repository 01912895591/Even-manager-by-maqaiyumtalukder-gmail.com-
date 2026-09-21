package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.components.BudgetProgressRing
import com.example.ui.components.CategoryProgressRow
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.PlumDark
import com.example.ui.theme.Rust
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.EventBudgetSummary
import com.example.ui.viewmodel.PaymentDueGroup
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatCurrency(amount: Double, currency: String): String {
  val formatter = NumberFormat.getNumberInstance(Locale.US)
  return "$currency ${formatter.format(amount.toLong())}"
}

@Composable
fun BudgetScreen(
  event: EventEntity?,
  allEvents: List<EventEntity> = emptyList(),
  onSelectEvent: (Long) -> Unit = {},
  budgetSummary: EventBudgetSummary,
  expenses: List<ExpenseEntity>,
  currencySymbol: String,
  language: String,
  onBack: () -> Unit,
  onAddExpense: (name: String, category: String, amount: Double, status: String, due: Double, advancePaid: Double, dueDate: Long?) -> Unit,
  onDeleteExpense: (ExpenseEntity) -> Unit,
  onUpdateExpense: (ExpenseEntity) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showAddExpenseDialog by remember { mutableStateOf(false) }

  val headerBg = if (event != null) getCategoryColor(event.category) else PlumDark

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
    // 1. Header (Category-Colored: e.g. Emerald for Wedding, Plum for Eid)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = headerBg,
      tonalElevation = 4.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("budget_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = (event?.category ?: "EVENT").uppercase(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = AccentGold,
              letterSpacing = 1.sp
            )
            Text(
              text = event?.title ?: "Event Budget",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Multi-event switcher if more than 1 event exists
        if (allEvents.size > 1) {
          Spacer(modifier = Modifier.height(12.dp))
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(allEvents, key = { it.id }) { ev ->
              val isSelected = ev.id == event?.id
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { onSelectEvent(ev.id) }
                  .testTag("budget_event_chip_${ev.id}"),
                color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.2f)
              ) {
                Text(
                  text = ev.title,
                  color = if (isSelected) PlumDark else Color.White,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }
    }

    val context = LocalContext.current
    var selectedBudgetTab by remember { mutableIntStateOf(0) }

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    val endOfSevenDays = startOfToday + TimeUnit.DAYS.toMillis(7)

    val scheduledItems = remember(expenses) {
      expenses.filter { it.dueDate != null }
        .map { exp ->
          val remainingDue = if (exp.paymentStatus.equals("Paid", ignoreCase = true)) 0.0
          else if (exp.dueAmount > 0.0) exp.dueAmount
          else (exp.amount - exp.advancePaid).coerceAtLeast(0.0)

          val dueMillis = exp.dueDate!!
          val diffDays = TimeUnit.MILLISECONDS.toDays(dueMillis - startOfToday)
          val group = when {
            dueMillis < startOfToday && remainingDue > 0.0 -> PaymentDueGroup.OVERDUE
            dueMillis in startOfToday..endOfSevenDays && remainingDue > 0.0 -> PaymentDueGroup.DUE_THIS_WEEK
            else -> PaymentDueGroup.UPCOMING
          }
          Triple(exp, remainingDue, group)
        }
        .sortedWith(compareBy<Triple<ExpenseEntity, Double, PaymentDueGroup>> {
          when (it.third) {
            PaymentDueGroup.OVERDUE -> 0
            PaymentDueGroup.DUE_THIS_WEEK -> 1
            PaymentDueGroup.UPCOMING -> 2
          }
        }.thenBy { it.first.dueDate ?: Long.MAX_VALUE })
    }

    val overdueCount = scheduledItems.count { it.third == PaymentDueGroup.OVERDUE }
    val dueThisWeekCount = scheduledItems.count { it.third == PaymentDueGroup.DUE_THIS_WEEK }
    val totalScheduleDue = scheduledItems.sumOf { it.second }
    val totalAdvancePaid = expenses.sumOf { it.advancePaid }

    // Sub-Tabs: Budget Overview | Payment Schedule
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      TabRow(
        selectedTabIndex = selectedBudgetTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = DeepPlum,
        indicator = { tabPositions ->
          if (selectedBudgetTab < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedBudgetTab]),
              color = DeepPlum
            )
          }
        }
      ) {
        Tab(
          selected = selectedBudgetTab == 0,
          onClick = { selectedBudgetTab = 0 },
          text = {
            Text(
              text = if (language == "bn") "বাজেট ওভারভিউ" else "Budget Overview",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = if (selectedBudgetTab == 0) FontWeight.Bold else FontWeight.Medium,
              color = if (selectedBudgetTab == 0) DeepPlum else TextMuted
            )
          },
          modifier = Modifier.testTag("budget_tab_overview")
        )

        val dueBadgeCount = overdueCount + dueThisWeekCount
        Tab(
          selected = selectedBudgetTab == 1,
          onClick = { selectedBudgetTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = if (language == "bn") "পেমেন্ট শিডিউল" else "Payment Schedule",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selectedBudgetTab == 1) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedBudgetTab == 1) DeepPlum else TextMuted
              )
              if (dueBadgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = CircleShape,
                  color = if (overdueCount > 0) DangerRed else WarningAmber
                ) {
                  Text(
                    text = "$dueBadgeCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          },
          modifier = Modifier.testTag("budget_tab_schedule")
        )
      }
    }

    if (selectedBudgetTab == 0) {
      // TAB 0: BUDGET OVERVIEW
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp)
      ) {
        // Due Payment Quick Banner (if any payments are due/overdue)
        if (overdueCount > 0 || dueThisWeekCount > 0) {
          item {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                  1.dp,
                  if (overdueCount > 0) DangerRed.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f),
                  RoundedCornerShape(16.dp)
                )
                .clickable { selectedBudgetTab = 1 }
                .testTag("budget_due_banner"),
              color = if (overdueCount > 0) DangerRed.copy(alpha = 0.08f) else WarningAmber.copy(alpha = 0.1f)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = if (overdueCount > 0) DangerRed else WarningAmber,
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Schedule,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (overdueCount > 0) {
                      if (language == "bn") "$overdueCount টি পেমেন্টের মেয়াদ শেষ!" else "$overdueCount vendor payment(s) overdue!"
                    } else {
                      if (language == "bn") "$dueThisWeekCount টি পেমেন্ট এই সপ্তাহে বাকি" else "$dueThisWeekCount vendor payment(s) due this week"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (overdueCount > 0) DangerRed else WarningAmber
                  )
                  Text(
                    text = if (language == "bn") "সময়সূচী দেখতে এখানে ট্যাপ করুন" else "Tap to review vendor payment schedule",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }
                Text(
                  text = "→",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (overdueCount > 0) DangerRed else WarningAmber
                )
              }
            }
          }
        }

        // 2. Overview Card with Circular Progress Ring & Stacked Rows
        item {
          Spacer(modifier = Modifier.height(16.dp))
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 2.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Large Circular Progress Ring
            BudgetProgressRing(
              percentage = budgetSummary.percentageSpent,
              size = 100.dp,
              strokeWidth = 9.dp,
              showLabel = true
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Three Stacked Rows: Planned / Spent / Remaining
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Planned
              Column {
                Text(
                  text = AppStrings.get("planned", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = formatCurrency(budgetSummary.planned, currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              // Spent
              Column {
                Text(
                  text = AppStrings.get("spent", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = formatCurrency(budgetSummary.spent, currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = DeepPlum
                )
              }

              // Remaining
              Column {
                Text(
                  text = AppStrings.get("remaining", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = formatCurrency(budgetSummary.remaining, currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = SuccessGreen
                )
              }
            }
          }
        }
      }

      // 3. "Spending by category" Section
      item {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = AppStrings.get("spending_by_category", language),
          style = MaterialTheme.typography.titleMedium,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 1.dp
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            val colors = listOf(DeepPlum, AccentGold, Emerald, Rust, Color(0xFF285496), Color(0xFF8A307F))
            budgetSummary.categoryBreakdown.forEachIndexed { index, item ->
              val color = colors[index % colors.size]
              CategoryProgressRow(
                label = item.category,
                amountFormatted = formatCurrency(item.spent, currencySymbol),
                percentage = item.percentageOfTotalSpent,
                color = color
              )
            }
          }
        }
      }

      // 4. "Recent expenses" Section Header with "+ Add" Link
      item {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = AppStrings.get("recent_expenses", language),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          Button(
            onClick = { showAddExpenseDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentGold,
              contentColor = PlumDark
            ),
            modifier = Modifier.testTag("budget_add_expense_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Add", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      // 5. Expense List Rows
      items(expenses, key = { it.id }) { expense ->
        val catColor = getCategoryColor(expense.category)
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
            // Category Icon Box
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(catColor.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = getCategoryIcon(expense.category),
                contentDescription = expense.category,
                tint = catColor,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Date + Payment Status
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = expense.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(3.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = expense.date.ifBlank { "Recently" },
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
                Text(text = " • ", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                val isPaid = expense.paymentStatus.equals("Paid", ignoreCase = true)
                Text(
                  text = if (isPaid) "Paid" else "Due ${formatCurrency(expense.dueAmount, currencySymbol)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isPaid) SuccessGreen else WarningAmber
                )
              }
            }

            // Amount Right-Aligned & Delete Button
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = formatCurrency(expense.amount, currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(4.dp))
              IconButton(
                onClick = { onDeleteExpense(expense) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete expense",
                  tint = TextMuted.copy(alpha = 0.6f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(96.dp))
      }
    }
  } else {
    PaymentScheduleTabContent(
      scheduledItems = scheduledItems,
      totalScheduleDue = totalScheduleDue,
      totalAdvancePaid = totalAdvancePaid,
      overdueCount = overdueCount,
      dueThisWeekCount = dueThisWeekCount,
      currencySymbol = currencySymbol,
      language = language,
      onAddDue = { showAddExpenseDialog = true },
      onMarkPaid = { exp ->
        onUpdateExpense(
          exp.copy(
            paymentStatus = "Paid",
            dueAmount = 0.0,
            advancePaid = exp.amount
          )
        )
      },
      onDeleteExpense = onDeleteExpense
    )
  }
}

      // Persistent Floating Action Button to Add Expense
      FloatingActionButton(
        onClick = { showAddExpenseDialog = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 20.dp, bottom = 20.dp)
          .testTag("budget_fab_add_expense"),
        containerColor = AccentGold,
        contentColor = PlumDark,
        shape = CircleShape
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Expense",
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }

  // Add Expense Dialog
  if (showAddExpenseDialog) {
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var advancePaidText by remember { mutableStateOf("") }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf("Catering") }
    var paymentStatus by remember { mutableStateOf("Paid") }
    var dueAmountText by remember { mutableStateOf("") }

    // Custom Category States
    var showCustomCategoryInput by remember { mutableStateOf(false) }
    var customCategoryNameInput by remember { mutableStateOf("") }
    var customCategoryError by remember { mutableStateOf(false) }

    val defaultCategories = remember {
      listOf("Catering", "Venue", "Decoration", "Photography", "Attire", "Gifts", "Other")
    }
    val existingExpenseCategories = remember(expenses) {
      expenses.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
    }
    var userAddedCategories by remember { mutableStateOf(listOf<String>()) }

    val allAvailableCategories = remember(defaultCategories, existingExpenseCategories, userAddedCategories) {
      (defaultCategories + existingExpenseCategories + userAddedCategories).distinctBy { it.lowercase() }
    }

    AlertDialog(
      onDismissRequest = { showAddExpenseDialog = false },
      title = {
        Text(
          text = if (language == "bn") "নতুন খরচ যোগ করুন" else "Add New Expense",
          style = MaterialTheme.typography.titleLarge,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 1. Expense Item Name
          OutlinedTextField(
            value = expenseName,
            onValueChange = { expenseName = it },
            label = { Text(if (language == "bn") "খরচের বিবরণ / ভেন্ডরের নাম" else "Expense item or vendor name") },
            placeholder = { Text(if (language == "bn") "যেমন: স্টেজ ডেকোরেশন" else "e.g. Wedding Cake & Sweets") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_expense_name_input")
          )

          // 2. Amount and Advance Paid Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = expenseAmount,
              onValueChange = { expenseAmount = it },
              label = { Text("${if (language == "bn") "মোট" else "Amount"} ($currencySymbol)") },
              placeholder = { Text("e.g. 25000") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("add_expense_amount_input")
            )

            OutlinedTextField(
              value = advancePaidText,
              onValueChange = { advancePaidText = it },
              label = { Text("${if (language == "bn") "অগ্রিম" else "Advance"} ($currencySymbol)") },
              placeholder = { Text("e.g. 5000") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("add_expense_advance_input")
            )
          }

          // Remaining Due Indicator
          val totalAmt = expenseAmount.toDoubleOrNull() ?: 0.0
          val advAmt = advancePaidText.toDoubleOrNull() ?: 0.0
          val remDue = (totalAmt - advAmt).coerceAtLeast(0.0)
          if (totalAmt > 0) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (remDue > 0) WarningAmber.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (language == "bn") "অবশিষ্ট বাকি:" else "Remaining Due:",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = formatCurrency(remDue, currencySymbol),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (remDue > 0) WarningAmber else SuccessGreen
                )
              }
            }
          }

          // Payment Due Date picker button
          val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
          val formattedDueDate = if (dueDateMillis != null) dateFormat.format(java.util.Date(dueDateMillis!!)) else null

          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = if (language == "bn") "পরিশোধের শেষ তারিখ" else "Payment Due Date",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, if (dueDateMillis != null) DeepPlum else BorderSubtle, RoundedCornerShape(10.dp))
                .clickable {
                  val c = Calendar.getInstance()
                  if (dueDateMillis != null) c.timeInMillis = dueDateMillis!!
                  DatePickerDialog(
                    context,
                    { _, y, m, d ->
                      val sel = Calendar.getInstance()
                      sel.set(y, m, d, 0, 0, 0)
                      sel.set(Calendar.MILLISECOND, 0)
                      dueDateMillis = sel.timeInMillis
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                  ).show()
                }
                .testTag("add_expense_due_date_picker"),
              color = if (dueDateMillis != null) DeepPlum.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (dueDateMillis != null) DeepPlum else TextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = formattedDueDate ?: (if (language == "bn") "তারিখ নির্ধারণ করুন..." else "Select Payment Due Date..."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dueDateMillis != null) MaterialTheme.colorScheme.onSurface else TextMuted
                  )
                }

                if (dueDateMillis != null) {
                  IconButton(
                    onClick = { dueDateMillis = null },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Clear date",
                      tint = TextMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }

          // 3. Category Section (Header + Custom Input + Chips)
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Category Header with "+ NEW" CTA Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (language == "bn") "ক্যাটাগরি" else "Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              // "+ NEW" CTA Button
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable { showCustomCategoryInput = !showCustomCategoryInput }
                  .testTag("budget_add_new_category_cta"),
                color = if (showCustomCategoryInput) AccentGold else AccentGold.copy(alpha = 0.15f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Category",
                    tint = if (showCustomCategoryInput) PlumDark else AccentGold,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = if (language == "bn") "নতুন" else "NEW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (showCustomCategoryInput) PlumDark else AccentGold
                  )
                }
              }
            }

            // Expandable Custom Category Input Field
            if (showCustomCategoryInput) {
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                color = AccentGold.copy(alpha = 0.08f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = customCategoryNameInput,
                      onValueChange = {
                        customCategoryNameInput = it
                        if (customCategoryError) customCategoryError = false
                      },
                      placeholder = {
                        Text(
                          if (language == "bn") "ক্যাটাগরির নাম..." else "Category name...",
                          fontSize = 13.sp,
                          maxLines = 1
                        )
                      },
                      singleLine = true,
                      isError = customCategoryError,
                      shape = RoundedCornerShape(8.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = BorderSubtle
                      ),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("custom_category_text_input")
                    )

                    Button(
                      onClick = {
                        val trimmed = customCategoryNameInput.trim()
                        if (trimmed.isNotBlank()) {
                          if (!allAvailableCategories.any { it.equals(trimmed, ignoreCase = true) }) {
                            userAddedCategories = userAddedCategories + trimmed
                          }
                          selectedCategory = trimmed
                          customCategoryNameInput = ""
                          showCustomCategoryInput = false
                        } else {
                          customCategoryError = true
                        }
                      },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = PlumDark
                      ),
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                      modifier = Modifier
                        .height(50.dp)
                        .testTag("save_custom_category_button")
                    ) {
                      Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = if (language == "bn") "যোগ" else "Add",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                    }

                    IconButton(
                      onClick = {
                        showCustomCategoryInput = false
                        customCategoryError = false
                      },
                      modifier = Modifier.size(36.dp)
                    ) {
                      Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }

                  if (customCategoryError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = if (language == "bn") "ক্যাটাগরির নাম লিখুন" else "Category name cannot be blank",
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.labelSmall
                    )
                  }
                }
              }
            }

            // Category Chips Row (Horizontally scrollable with "+ New" chip + all categories)
            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // First item: "+ New" chip CTA
              item {
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                      1.dp,
                      if (showCustomCategoryInput) AccentGold else BorderSubtle,
                      RoundedCornerShape(8.dp)
                    )
                    .clickable { showCustomCategoryInput = true }
                    .testTag("budget_category_new_chip"),
                  color = if (showCustomCategoryInput) AccentGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      tint = AccentGold,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (language == "bn") "নতুন" else "New",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = AccentGold
                    )
                  }
                }
              }

              // All Categories (Standard + Custom)
              items(allAvailableCategories) { cat ->
                val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                val isCustom = !defaultCategories.any { it.equals(cat, ignoreCase = true) }
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                      1.dp,
                      if (isSelected) DeepPlum else BorderSubtle,
                      RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      selectedCategory = cat
                      showCustomCategoryInput = false
                    }
                    .testTag("budget_category_chip_$cat"),
                  color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isCustom) {
                      Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isSelected) AccentGold else AccentGold.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = cat,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }

          // 4. Payment Status Section
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = if (language == "bn") "পেমেন্ট স্ট্যাটাস" else "Payment Status",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              listOf("Paid", "Due").forEach { st ->
                val isSelected = paymentStatus == st
                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, if (isSelected) DeepPlum else BorderSubtle, RoundedCornerShape(8.dp))
                    .clickable { paymentStatus = st }
                    .testTag("budget_payment_status_$st"),
                  color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface
                ) {
                  Text(
                    text = if (st == "Paid") (if (language == "bn") "পরিশোধিত (Paid)" else "Paid") else (if (language == "bn") "বাকি (Due)" else "Due"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
          }

          // 5. Due Amount Field (if Due)
          if (paymentStatus == "Due") {
            OutlinedTextField(
              value = dueAmountText,
              onValueChange = { dueAmountText = it },
              label = { Text("${if (language == "bn") "বাকির পরিমাণ" else "Due Amount"} ($currencySymbol)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("add_expense_due_input")
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = expenseAmount.toDoubleOrNull() ?: 0.0
            val advance = advancePaidText.toDoubleOrNull() ?: 0.0
            val due = dueAmountText.toDoubleOrNull() ?: 0.0
            val calculatedDue = (amount - advance).coerceAtLeast(0.0)
            val finalDue = if (paymentStatus.equals("Paid", ignoreCase = true)) 0.0
            else if (due > 0.0) due
            else calculatedDue
            val finalCategory = if (showCustomCategoryInput && customCategoryNameInput.isNotBlank()) {
              customCategoryNameInput.trim()
            } else {
              selectedCategory.ifBlank { "Other" }
            }
            if (expenseName.isNotBlank() && amount > 0) {
              onAddExpense(expenseName.trim(), finalCategory, amount, paymentStatus, finalDue, advance, dueDateMillis)
              showAddExpenseDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum),
          modifier = Modifier.testTag("confirm_add_expense_button")
        ) {
          Text(if (language == "bn") "খরচ যোগ করুন" else "Add Expense")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddExpenseDialog = false }) {
          Text(if (language == "bn") "বাতিল" else "Cancel", color = TextMuted)
        }
      }
    )
  }
}

@Composable
fun PaymentScheduleTabContent(
  scheduledItems: List<Triple<ExpenseEntity, Double, PaymentDueGroup>>,
  totalScheduleDue: Double,
  totalAdvancePaid: Double,
  overdueCount: Int,
  dueThisWeekCount: Int,
  currencySymbol: String,
  language: String,
  onAddDue: () -> Unit,
  onMarkPaid: (ExpenseEntity) -> Unit,
  onDeleteExpense: (ExpenseEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    // 1. Summary Metrics Card
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (language == "bn") "মোট বকেয়া পেমেন্ট" else "Total Due to Vendors",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
              )
              Text(
                text = formatCurrency(totalScheduleDue, currencySymbol),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = if (totalScheduleDue > 0) WarningAmber else SuccessGreen
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AccentGold.copy(alpha = 0.15f),
              modifier = Modifier
                .clickable { onAddDue() }
                .testTag("budget_add_due_cta")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PlumDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (language == "bn") "পেমেন্ট যোগ" else "+ Add Due",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = PlumDark
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Badges row: Overdue, Due this week, Advance Paid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Overdue
            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              color = if (overdueCount > 0) DangerRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (overdueCount > 0) DangerRed.copy(alpha = 0.3f) else Color.Transparent)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = if (language == "bn") "মেয়াদোত্তীর্ণ" else "Overdue",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (overdueCount > 0) DangerRed else TextMuted
                )
                Text(
                  text = "$overdueCount",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (overdueCount > 0) DangerRed else MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Due This Week
            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              color = if (dueThisWeekCount > 0) WarningAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (dueThisWeekCount > 0) WarningAmber.copy(alpha = 0.3f) else Color.Transparent)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = if (language == "bn") "এই সপ্তাহে" else "This Week",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (dueThisWeekCount > 0) WarningAmber else TextMuted
                )
                Text(
                  text = "$dueThisWeekCount",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (dueThisWeekCount > 0) WarningAmber else MaterialTheme.colorScheme.onSurface
                )
              }
            }

            // Advance Paid
            Surface(
              modifier = Modifier.weight(1.2f),
              shape = RoundedCornerShape(12.dp),
              color = Emerald.copy(alpha = 0.1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = if (language == "bn") "মোট অগ্রিম" else "Advance Paid",
                  style = MaterialTheme.typography.labelSmall,
                  color = Emerald
                )
                Text(
                  text = formatCurrency(totalAdvancePaid, currencySymbol),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Emerald,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }

    if (scheduledItems.isEmpty()) {
      item {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
          color = MaterialTheme.colorScheme.surface
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = null,
              tint = TextMuted.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (language == "bn") "কোনো পেমেন্ট শিডিউল নেই" else "No payment schedule set",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (language == "bn")
                "ভেন্ডর খরচ যোগ করার সময় শেষ তারিখ ও অগ্রিম পেমেন্ট নির্ধারণ করুন।"
              else
                "Assign due dates and advance paid amounts to track upcoming vendor deadlines.",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onAddDue,
              colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (language == "bn") "ভেন্ডর পেমেন্ট যোগ করুন" else "Add Vendor Payment")
            }
          }
        }
      }
    } else {
      val overdueItems = scheduledItems.filter { it.third == PaymentDueGroup.OVERDUE }
      val dueThisWeekItems = scheduledItems.filter { it.third == PaymentDueGroup.DUE_THIS_WEEK }
      val upcomingItems = scheduledItems.filter { it.third == PaymentDueGroup.UPCOMING }

      // Group 1: OVERDUE
      if (overdueItems.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(20.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = DangerRed, modifier = Modifier.size(8.dp)) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (language == "bn") "মেয়াদোত্তীর্ণ পেমেন্ট (${overdueItems.size})" else "Overdue Payments (${overdueItems.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = DangerRed
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        items(overdueItems, key = { it.first.id }) { item ->
          PaymentScheduleCard(
            item = item,
            currencySymbol = currencySymbol,
            language = language,
            onMarkPaid = { onMarkPaid(item.first) },
            onDelete = { onDeleteExpense(item.first) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      // Group 2: DUE THIS WEEK
      if (dueThisWeekItems.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(20.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = WarningAmber, modifier = Modifier.size(8.dp)) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (language == "bn") "এই সপ্তাহে বাকি (${dueThisWeekItems.size})" else "Due This Week (${dueThisWeekItems.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = WarningAmber
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        items(dueThisWeekItems, key = { it.first.id }) { item ->
          PaymentScheduleCard(
            item = item,
            currencySymbol = currencySymbol,
            language = language,
            onMarkPaid = { onMarkPaid(item.first) },
            onDelete = { onDeleteExpense(item.first) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      // Group 3: UPCOMING & SETTLED
      if (upcomingItems.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(20.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = SuccessGreen, modifier = Modifier.size(8.dp)) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (language == "bn") "আসন্ন ও পরিশোধিত (${upcomingItems.size})" else "Upcoming & Settled (${upcomingItems.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        items(upcomingItems, key = { it.first.id }) { item ->
          PaymentScheduleCard(
            item = item,
            currencySymbol = currencySymbol,
            language = language,
            onMarkPaid = { onMarkPaid(item.first) },
            onDelete = { onDeleteExpense(item.first) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(96.dp))
    }
  }
}

@Composable
fun PaymentScheduleCard(
  item: Triple<ExpenseEntity, Double, PaymentDueGroup>,
  currencySymbol: String,
  language: String,
  onMarkPaid: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val expense = item.first
  val remainingDue = item.second
  val group = item.third
  val isPaid = expense.paymentStatus.equals("Paid", ignoreCase = true) || remainingDue <= 0.0

  val cal = Calendar.getInstance()
  cal.set(Calendar.HOUR_OF_DAY, 0)
  cal.set(Calendar.MINUTE, 0)
  cal.set(Calendar.SECOND, 0)
  cal.set(Calendar.MILLISECOND, 0)
  val todayMillis = cal.timeInMillis

  val dueMillis = expense.dueDate ?: 0L
  val diffDays = TimeUnit.MILLISECONDS.toDays(dueMillis - todayMillis)
  val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
  val formattedDate = if (dueMillis > 0) dateFormat.format(java.util.Date(dueMillis)) else ""

  val (badgeBg, badgeText, badgeColor) = when {
    isPaid -> Triple(SuccessGreen.copy(alpha = 0.15f), if (language == "bn") "পরিশোধিত" else "Paid in Full", SuccessGreen)
    group == PaymentDueGroup.OVERDUE -> Triple(
      DangerRed.copy(alpha = 0.15f),
      if (language == "bn") "${-diffDays} দিন মেয়াদোত্তীর্ণ" else "Overdue by ${-diffDays}d",
      DangerRed
    )
    diffDays == 0L -> Triple(
      WarningAmber.copy(alpha = 0.2f),
      if (language == "bn") "আজ বাকি" else "Due Today",
      WarningAmber
    )
    diffDays in 1..7 -> Triple(
      WarningAmber.copy(alpha = 0.15f),
      if (language == "bn") "${diffDays} দিনে বাকি" else "Due in ${diffDays}d",
      WarningAmber
    )
    else -> Triple(
      DeepPlum.copy(alpha = 0.1f),
      if (language == "bn") "${diffDays} দিনে বাকি" else "Due in ${diffDays}d",
      DeepPlum
    )
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(
        1.dp,
        when {
          isPaid -> BorderSubtle
          group == PaymentDueGroup.OVERDUE -> DangerRed.copy(alpha = 0.4f)
          group == PaymentDueGroup.DUE_THIS_WEEK -> WarningAmber.copy(alpha = 0.4f)
          else -> BorderSubtle
        },
        RoundedCornerShape(16.dp)
      ),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = getCategoryColor(expense.category).copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = getCategoryIcon(expense.category),
                contentDescription = null,
                tint = getCategoryColor(expense.category),
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = expense.name,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = expense.category,
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }
        }

        // Status Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = badgeBg
        ) {
          Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Amount Row: Total | Advance Paid | Remaining Due
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = if (language == "bn") "মোট" else "Total", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(text = formatCurrency(expense.amount, currencySymbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
          }

          Column {
            Text(text = if (language == "bn") "অগ্রিম" else "Advance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(text = formatCurrency(expense.advancePaid, currencySymbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Emerald)
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(text = if (language == "bn") "বাকি" else "Due", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
              text = if (isPaid) formatCurrency(0.0, currencySymbol) else formatCurrency(remainingDue, currencySymbol),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = if (isPaid) SuccessGreen else if (group == PaymentDueGroup.OVERDUE) DangerRed else WarningAmber
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Footer: Due Date + Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (formattedDate.isNotBlank()) "${if (language == "bn") "তারিখ:" else "Due:"} $formattedDate" else "",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!isPaid) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SuccessGreen.copy(alpha = 0.15f),
              modifier = Modifier
                .clickable { onMarkPaid() }
                .testTag("mark_paid_button_${expense.id}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = if (language == "bn") "পরিশোধ" else "Mark Paid",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = SuccessGreen
                )
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = TextMuted.copy(alpha = 0.6f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
