package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.Rust
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.CategoryExpenseSummary

val DefaultCategoryColors = listOf(
  Color(0xFFE53935), // Food & Catering (Red/Crimson)
  Color(0xFF8E24AA), // Decoration & Flowers (Purple)
  Color(0xFF1E88E5), // Venue & Hall (Blue)
  Color(0xFF43A047), // Photography & Media (Green)
  Color(0xFFFB8C00), // Attire & Jewelry (Orange)
  Color(0xFF00ACC1), // Gifts & Favors (Teal)
  Color(0xFF6D4C41), // Transport & Logistics (Brown)
  Color(0xFFD81B60), // Music & Entertainment (Pink)
  Color(0xFF757575)  // Others (Grey)
)

/**
 * Clean & beautiful Material 3 Category Expense Pie / Donut Chart
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryExpensePieChart(
  categoryBreakdown: List<CategoryExpenseSummary>,
  totalSpent: Double,
  currencySymbol: String,
  language: String = "bn",
  modifier: Modifier = Modifier,
  chartSize: Dp = 160.dp
) {
  if (categoryBreakdown.isEmpty() || totalSpent <= 0.0) {
    Surface(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
      color = MaterialTheme.colorScheme.surface
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (language == "bn") "কোনো খরচের হিসাব এখনো যোগ করা হয়নি" else "No expense records yet to display breakdown",
          style = MaterialTheme.typography.bodyMedium,
          color = TextMuted
        )
      }
    }
    return
  }

  // Animated sweep progress
  val animationProgress = remember { Animatable(0f) }
  LaunchedEffect(categoryBreakdown) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800)
    )
  }

  var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
      .testTag("category_pie_chart_card"),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Text(
        text = if (language == "bn") "বাজেট খরচ ক্যাটাগরি চার্ট" else "Expense Category Breakdown",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = if (language == "bn") "কোন খাতে কত শতাংশ টাকা খরচ হলো তার পাই-চার্ট" else "Visual percentage split of your spent budget",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Center Donut Pie Chart & Center summary
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(chartSize)
        ) {
          Canvas(modifier = Modifier.size(chartSize)) {
            val strokeWidth = 26.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val topLeft = Offset(
              (size.width - diameter) / 2f,
              (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f

            categoryBreakdown.forEachIndexed { index, item ->
              val sweepAngle = (item.percentageOfTotalSpent / 100f) * 360f * animationProgress.value
              val color = DefaultCategoryColors[index % DefaultCategoryColors.size]
              val isSelected = selectedCategoryIndex == index

              drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = (sweepAngle - 2f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                  width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                  cap = StrokeCap.Round
                )
              )

              startAngle += sweepAngle
            }
          }

          // Center Label inside Donut
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            val activeItem = selectedCategoryIndex?.let { categoryBreakdown.getOrNull(it) }
            if (activeItem != null) {
              Text(
                text = "${activeItem.percentageOfTotalSpent.toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = activeItem.category,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1
              )
            } else {
              Text(
                text = if (language == "bn") "মোট খরচ" else "Total Spent",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
              )
              Text(
                text = "$currencySymbol ${totalSpent.toLong()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepPlum
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Category Legend Chips / Legend Items
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categoryBreakdown.forEachIndexed { index, item ->
          val color = DefaultCategoryColors[index % DefaultCategoryColors.size]
          val isSelected = selectedCategoryIndex == index

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) color else BorderSubtle
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                selectedCategoryIndex = if (selectedCategoryIndex == index) null else index
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(color)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${item.percentageOfTotalSpent.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
              )
            }
          }
        }
      }
    }
  }
}
