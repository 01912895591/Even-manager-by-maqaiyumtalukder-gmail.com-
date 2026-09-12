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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.text.NumberFormat
import java.util.Locale

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
  onAddExpense: (name: String, category: String, amount: Double, status: String, due: Double) -> Unit,
  onDeleteExpense: (ExpenseEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddExpenseDialog by remember { mutableStateOf(false) }

  val headerBg = if (event != null) getCategoryColor(event.category) else PlumDark

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
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

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
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
        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }

  // Add Expense Dialog
  if (showAddExpenseDialog) {
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Catering") }
    var paymentStatus by remember { mutableStateOf("Paid") }
    var dueAmountText by remember { mutableStateOf("") }

    val categories = listOf("Catering", "Venue", "Decoration", "Photography", "Attire", "Gifts", "Other")

    AlertDialog(
      onDismissRequest = { showAddExpenseDialog = false },
      title = {
        Text("Add New Expense", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = expenseName,
            onValueChange = { expenseName = it },
            label = { Text("Expense item name") },
            placeholder = { Text("e.g. Wedding Cake & Sweets") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = expenseAmount,
            onValueChange = { expenseAmount = it },
            label = { Text("Amount ($currencySymbol)") },
            placeholder = { Text("e.g. 25000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            categories.take(3).forEach { cat ->
              val isSelected = cat == selectedCategory
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, if (isSelected) DeepPlum else BorderSubtle, RoundedCornerShape(8.dp))
                  .clickable { selectedCategory = cat },
                color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }

          // Payment Status
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            listOf("Paid", "Due").forEach { st ->
              val isSelected = paymentStatus == st
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, if (isSelected) DeepPlum else BorderSubtle, RoundedCornerShape(8.dp))
                  .clickable { paymentStatus = st },
                color = if (isSelected) DeepPlum else MaterialTheme.colorScheme.surface
              ) {
                Text(
                  text = st,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                )
              }
            }
          }

          if (paymentStatus == "Due") {
            OutlinedTextField(
              value = dueAmountText,
              onValueChange = { dueAmountText = it },
              label = { Text("Due Amount ($currencySymbol)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = expenseAmount.toDoubleOrNull() ?: 0.0
            val due = dueAmountText.toDoubleOrNull() ?: 0.0
            if (expenseName.isNotBlank() && amount > 0) {
              onAddExpense(expenseName, selectedCategory, amount, paymentStatus, due)
              showAddExpenseDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepPlum)
        ) {
          Text("Add Expense")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddExpenseDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }
}
