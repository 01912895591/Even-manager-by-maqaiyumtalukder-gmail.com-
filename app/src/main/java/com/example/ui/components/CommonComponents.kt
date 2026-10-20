package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EventEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldDark
import com.example.ui.theme.AccentGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.Emerald
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.PlumDark
import com.example.ui.theme.Rust
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhiteCard
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath

fun getCategoryColor(category: String): Color {
  return when (category.lowercase()) {
    "wedding" -> Emerald
    "eid" -> DeepPlum
    "birthday" -> AccentGoldDark
    "aqiqah" -> Color(0xFF2E6B8E)
    "corporate" -> Color(0xFF1B435B)
    "funeral", "kotha" -> Color(0xFF4A4458)
    "anniversary" -> Rust
    else -> DeepPlum
  }
}

fun getCategoryIcon(category: String): ImageVector {
  return when (category.lowercase()) {
    "wedding" -> Icons.Default.Celebration
    "eid" -> Icons.Default.Mosque
    "birthday" -> Icons.Default.Cake
    "aqiqah" -> Icons.Default.Stars
    "corporate" -> Icons.Default.Event
    "catering" -> Icons.Default.Restaurant
    else -> Icons.Default.Event
  }
}

fun parseColorHex(hex: String): Color {
  return try {
    val cleanHex = hex.removePrefix("#")
    val colorLong = cleanHex.toLong(16)
    if (cleanHex.length == 6) {
      Color(0xFF000000 or colorLong)
    } else {
      Color(colorLong)
    }
  } catch (e: Exception) {
    DeepPlum
  }
}

@Composable
fun InitialsAvatar(
  name: String,
  modifier: Modifier = Modifier,
  backgroundColorHex: String = "#4A1030",
  size: Dp = 44.dp
) {
  val initials = name.split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercase() }
    .joinToString("")
    .ifEmpty { "E" }

  val bg = parseColorHex(backgroundColorHex)

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(bg),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = (size.value * 0.38f).sp,
      fontFamily = FontFamily.SansSerif
    )
  }
}

@Composable
fun BudgetProgressRing(
  percentage: Float,
  modifier: Modifier = Modifier,
  size: Dp = 80.dp,
  strokeWidth: Dp = 8.dp,
  showLabel: Boolean = true,
  trackColor: Color = Color(0x22D4AF6A),
  progressColor: Color? = null
) {
  val animatedProgress by animateFloatAsState(
    targetValue = (percentage / 100f).coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 600),
    label = "budget_progress"
  )

  val dynamicColor = progressColor ?: when {
    percentage > 95f -> DangerRed
    percentage > 80f -> WarningAmber
    else -> AccentGold
  }

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    // Track background
    CircularProgressIndicator(
      progress = { 1f },
      modifier = Modifier.size(size),
      color = trackColor,
      strokeWidth = strokeWidth,
      strokeCap = StrokeCap.Round
    )
    // Active Progress
    CircularProgressIndicator(
      progress = { animatedProgress },
      modifier = Modifier.size(size),
      color = dynamicColor,
      strokeWidth = strokeWidth,
      strokeCap = StrokeCap.Round
    )

    if (showLabel) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "${percentage.toInt()}%",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "used",
          fontSize = 10.sp,
          color = TextMuted
        )
      }
    }
  }
}

@Composable
fun StatMiniCard(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  accentColor: Color = DeepPlum
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
    }
  }
}

@Composable
fun EventCard(
  event: EventEntity,
  spentAmount: Double,
  onCardClick: () -> Unit,
  onAddGuestClick: () -> Unit,
  onChecklistClick: () -> Unit,
  onDeleteClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val catColor = getCategoryColor(event.category)
  val percentSpent = if (event.plannedBudget > 0) {
    ((spentAmount / event.plannedBudget) * 100f).coerceIn(0.0, 100.0).toFloat()
  } else 0f
  val hasCustomCover = !event.coverPhotoUri.isNullOrBlank()

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .border(
        1.dp,
        if (hasCustomCover) AccentGold.copy(alpha = 0.5f) else BorderSubtle,
        RoundedCornerShape(18.dp)
      )
      .clickable { onCardClick() }
      .testTag("event_card_${event.id}"),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      // 1. Custom Gallery Cover Photo - Full Background Image
      if (hasCustomCover) {
        AsyncImage(
          model = event.coverPhotoUri,
          contentDescription = event.title,
          modifier = Modifier.matchParentSize(),
          contentScale = ContentScale.Crop
        )

        // Dark gradient scrim overlay so all text remains crisp and highly legible
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.55f),
                  Color.Black.copy(alpha = 0.88f)
                )
              )
            )
        )
      }

      // 2. Card Contents Overlaid On Top
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Category Icon Box
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (hasCustomCover) Color.Black.copy(alpha = 0.40f) else catColor.copy(alpha = 0.14f)
              )
              .border(
                1.dp,
                if (hasCustomCover) AccentGold.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(12.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getCategoryIcon(event.category),
              contentDescription = event.category,
              tint = if (hasCustomCover) AccentGold else catColor,
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          // Title and Date
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = event.title,
              style = MaterialTheme.typography.titleMedium,
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              color = if (hasCustomCover) Color.White else MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = event.category,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (hasCustomCover) AccentGoldLight else catColor
              )
              Text(
                text = " • ${event.dateFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = if (hasCustomCover) Color.White.copy(alpha = 0.85f) else TextMuted
              )
            }
          }

          // Circular Budget Progress Ring
          BudgetProgressRing(
            percentage = percentSpent,
            size = 46.dp,
            strokeWidth = 4.5.dp,
            showLabel = true
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Pill Buttons: "Add guest", "Checklist", and "Delete"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(
                1.dp,
                if (hasCustomCover) Color.White.copy(alpha = 0.25f) else BorderSubtle,
                RoundedCornerShape(12.dp)
              )
              .clickable { onAddGuestClick() }
              .testTag("event_add_guest_button_${event.id}"),
            color = if (hasCustomCover) Color.White.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Add guest",
                tint = if (hasCustomCover) Color.White else DeepPlum,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Add guest",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (hasCustomCover) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Surface(
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(
                1.dp,
                if (hasCustomCover) AccentGold.copy(alpha = 0.4f) else BorderSubtle,
                RoundedCornerShape(12.dp)
              )
              .clickable { onChecklistClick() }
              .testTag("event_checklist_button_${event.id}"),
            color = if (hasCustomCover) AccentGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Checklist,
                contentDescription = "Checklist",
                tint = if (hasCustomCover) AccentGoldLight else AccentGoldDark,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Checklist",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (hasCustomCover) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }

          if (onDeleteClick != null) {
            Surface(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { onDeleteClick() }
                .testTag("event_delete_button_${event.id}"),
              color = DangerRed.copy(alpha = 0.2f)
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete event",
                  tint = if (hasCustomCover) Color.White else DangerRed,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CategoryProgressRow(
  label: String,
  amountFormatted: String,
  percentage: Float,
  color: Color,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = amountFormatted,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(color.copy(alpha = 0.15f))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction = percentage.coerceIn(0.01f, 1f))
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(color)
      )
    }
  }
}

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (status) {
    "Confirmed" -> SuccessGreen.copy(alpha = 0.16f) to SuccessGreen
    "Called" -> WarningAmber.copy(alpha = 0.16f) to WarningAmber
    "Declined" -> DangerRed.copy(alpha = 0.16f) to DangerRed
    else -> Color(0x18000000) to TextMuted
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = status,
      color = textColor,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold
    )
  }
}

fun openWhatsAppMessage(
  context: Context,
  phone: String,
  message: String = ""
) {
  try {
    // Sanitize phone number (strip spaces, dashes, parentheses)
    val cleanPhone = phone.replace(Regex("[^0-9+]"), "").let {
      if (it.startsWith("01")) {
        "88$it" // Default to Bangladesh +880 format if 01XXXXXXXXX
      } else if (it.startsWith("+")) {
        it.removePrefix("+")
      } else {
        it
      }
    }
    val encodedMsg = Uri.encode(message)
    val url = if (cleanPhone.isNotBlank()) {
      "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
    } else {
      "https://api.whatsapp.com/send?text=$encodedMsg"
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (e: Exception) {
    // Fallback: regular SMS or chooser
    try {
      val smsIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("sms:${phone.replace(Regex("[^0-9+]"), "")}")
        putExtra("sms_body", message)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(smsIntent)
    } catch (_: Exception) {}
  }
}

/**
 * 1-Click WhatsApp Quick Button with authentic brand styling
 */
@Composable
fun WhatsAppIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentDescription: String = "Send WhatsApp Message",
  size: Dp = 34.dp
) {
  Surface(
    shape = CircleShape,
    color = WhatsAppGreen.copy(alpha = 0.15f),
    border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.45f)),
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .clickable { onClick() }
      .testTag("whatsapp_direct_button")
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxSize()
    ) {
      Text(
        text = "💬",
        fontSize = (size.value * 0.44f).sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

