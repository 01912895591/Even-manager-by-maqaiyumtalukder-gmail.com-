package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventEntity
import com.example.ui.viewmodel.EventBudgetSummary
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object EventSummaryExporter {

  /**
   * Generates a well-structured plain text summary for WhatsApp, SMS, or Note sharing.
   */
  fun generateTextSummary(
    event: EventEntity,
    budgetSummary: EventBudgetSummary,
    checklist: List<ChecklistItemEntity>,
    eventGuests: List<EventContactCrossRef>,
    allContacts: List<ContactEntity>,
    currencySymbol: String = "৳",
    language: String = "bn"
  ): String {
    val totalBudget = if (budgetSummary.planned > 0) budgetSummary.planned else event.plannedBudget
    val totalSpent = budgetSummary.spent
    val remainingBudget = if (budgetSummary.planned > 0) budgetSummary.remaining else (event.plannedBudget - totalSpent)
    val contactMap = allContacts.associateBy { it.id }

    val vendors = eventGuests.mapNotNull { contactMap[it.contactId] }
      .filter { it.relation.equals("Vendor", ignoreCase = true) }

    val confirmedGuestCount = eventGuests.count { it.status.equals("Confirmed", ignoreCase = true) }
    val completedTasksCount = checklist.count { it.isDone }

    val nf = NumberFormat.getNumberInstance(Locale.US)

    val sb = StringBuilder()
    if (language == "bn") {
      sb.append("📋 *${event.title} - ইভেন্ট সারাংশ*\n")
      sb.append("━━━━━━━━━━━━━━━━━━━━\n")
      sb.append("📅 তারিখ: ${event.dateFormatted} (${event.timeFormatted})\n")
      if (event.location.isNotBlank()) {
        sb.append("📍 ভেন্যু: ${event.location}\n")
      }
      sb.append("🏷️ ক্যাটাগরি: ${event.category}\n\n")

      sb.append("💰 *বাজেট ও খরচ বিবরণী:*\n")
      sb.append("• মোট বাজেট: $currencySymbol ${nf.format(totalBudget.toLong())}\n")
      sb.append("• মোট খরচ: $currencySymbol ${nf.format(totalSpent.toLong())}\n")
      sb.append("• অবশিষ্ট ব্যালেন্স: $currencySymbol ${nf.format(remainingBudget.toLong())}\n\n")

      sb.append("👥 *মেহমান উপস্থিতি:*\n")
      sb.append("• মোট আমন্ত্রিত: ${eventGuests.size} জন\n")
      sb.append("• কনফার্মড মেহমান: $confirmedGuestCount জন\n\n")

      if (vendors.isNotEmpty()) {
        sb.append("🤝 *ভেন্ডর ও সার্ভিস প্রোভাইডার তালিকা:*\n")
        vendors.forEachIndexed { idx, v ->
          val roleStr = if (v.note.isNotBlank()) " (${v.note})" else ""
          sb.append("${idx + 1}. ${v.name}$roleStr: ${v.phone}\n")
        }
        sb.append("\n")
      }

      if (checklist.isNotEmpty()) {
        sb.append("✅ *চেকলিস্ট ও কাজের অগ্রগতি:*\n")
        sb.append("• সম্পন্ন: $completedTasksCount/${checklist.size} টি\n")
        checklist.take(6).forEach { task ->
          val statusIcon = if (task.isDone) "☑️" else "▫️"
          sb.append("$statusIcon ${task.title}\n")
        }
        if (checklist.size > 6) {
          sb.append("...এবং আরও ${checklist.size - 6} টি কাজ\n")
        }
        sb.append("\n")
      }

      sb.append("✨ _Generated via Event Manager App_")
    } else {
      sb.append("📋 *${event.title} - Event Summary*\n")
      sb.append("━━━━━━━━━━━━━━━━━━━━\n")
      sb.append("📅 Date: ${event.dateFormatted} (${event.timeFormatted})\n")
      if (event.location.isNotBlank()) {
        sb.append("📍 Location: ${event.location}\n")
      }
      sb.append("🏷️ Category: ${event.category}\n\n")

      sb.append("💰 *Budget & Expenses:*\n")
      sb.append("• Total Budget: $currencySymbol ${nf.format(totalBudget.toLong())}\n")
      sb.append("• Total Spent: $currencySymbol ${nf.format(totalSpent.toLong())}\n")
      sb.append("• Remaining: $currencySymbol ${nf.format(remainingBudget.toLong())}\n\n")

      sb.append("👥 *Guests Summary:*\n")
      sb.append("• Total Invited: ${eventGuests.size}\n")
      sb.append("• Confirmed: $confirmedGuestCount\n\n")

      if (vendors.isNotEmpty()) {
        sb.append("🤝 *Key Vendors & Contacts:*\n")
        vendors.forEachIndexed { idx, v ->
          val roleStr = if (v.note.isNotBlank()) " (${v.note})" else ""
          sb.append("${idx + 1}. ${v.name}$roleStr: ${v.phone}\n")
        }
        sb.append("\n")
      }

      if (checklist.isNotEmpty()) {
        sb.append("✅ *Checklist Status:*\n")
        sb.append("• Progress: $completedTasksCount/${checklist.size} Done\n")
        checklist.take(6).forEach { task ->
          val statusIcon = if (task.isDone) "☑" else "☐"
          sb.append("$statusIcon ${task.title}\n")
        }
        if (checklist.size > 6) {
          sb.append("...and ${checklist.size - 6} more tasks\n")
        }
        sb.append("\n")
      }

      sb.append("✨ _Generated via Event Manager App_")
    }

    return sb.toString()
  }

  /**
   * Shares plain text summary via WhatsApp, Telegram, Email, Messenger, SMS etc.
   */
  fun shareText(context: Context, text: String, title: String = "Share Event Summary") {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, text)
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(shareIntent)
  }

  /**
   * Generates a sleek, professional PDF document and shares it with other apps.
   */
  fun generateAndSharePdf(
    context: Context,
    event: EventEntity,
    budgetSummary: EventBudgetSummary,
    checklist: List<ChecklistItemEntity>,
    eventGuests: List<EventContactCrossRef>,
    allContacts: List<ContactEntity>,
    currencySymbol: String = "৳",
    language: String = "bn"
  ) {
    val totalBudget = if (budgetSummary.planned > 0) budgetSummary.planned else event.plannedBudget
    val totalSpent = budgetSummary.spent
    val remainingBudget = if (budgetSummary.planned > 0) budgetSummary.remaining else (event.plannedBudget - totalSpent)
    val contactMap = allContacts.associateBy { it.id }
    val vendors = eventGuests.mapNotNull { contactMap[it.contactId] }
      .filter { it.relation.equals("Vendor", ignoreCase = true) }
    val confirmedGuestCount = eventGuests.count { it.status.equals("Confirmed", ignoreCase = true) }

    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
      isAntiAlias = true
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 20f
      color = AndroidColor.parseColor("#4A0E4E") // Deep Plum
    }

    val subtitlePaint = Paint().apply {
      isAntiAlias = true
      textSize = 11f
      color = AndroidColor.parseColor("#7A7270")
    }

    val headerPaint = Paint().apply {
      isAntiAlias = true
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 12f
      color = AndroidColor.parseColor("#8A6600") // Warm Gold
    }

    val bodyPaint = Paint().apply {
      isAntiAlias = true
      textSize = 10.5f
      color = AndroidColor.parseColor("#1C1B1F")
    }

    val boldBodyPaint = Paint().apply {
      isAntiAlias = true
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textSize = 10.5f
      color = AndroidColor.parseColor("#1C1B1F")
    }

    val bgCardPaint = Paint().apply {
      color = AndroidColor.parseColor("#F7F2F8")
      style = Paint.Style.FILL
    }

    val dividerPaint = Paint().apply {
      color = AndroidColor.parseColor("#E0D6E2")
      strokeWidth = 1f
    }

    var currentY = 50f
    val marginX = 40f

    // Header Title
    canvas.drawText(event.title, marginX, currentY, titlePaint)
    currentY += 18f
    canvas.drawText("Event Summary & Coordination Report • Category: ${event.category}", marginX, currentY, subtitlePaint)
    currentY += 16f

    canvas.drawLine(marginX, currentY, 555f, currentY, dividerPaint)
    currentY += 20f

    // Event Info Card Box
    canvas.drawRoundRect(marginX, currentY, 555f, currentY + 54f, 10f, 10f, bgCardPaint)
    canvas.drawText("Date & Time: ${event.dateFormatted} at ${event.timeFormatted}", marginX + 16f, currentY + 22f, boldBodyPaint)
    canvas.drawText("Location: ${if (event.location.isNotBlank()) event.location else "Not specified"}", marginX + 16f, currentY + 42f, bodyPaint)
    currentY += 70f

    // 1. Budget Summary
    val nf = NumberFormat.getNumberInstance(Locale.US)
    canvas.drawText("1. FINANCIAL & BUDGET SUMMARY", marginX, currentY, headerPaint)
    currentY += 16f
    canvas.drawText("Planned Budget: $currencySymbol ${nf.format(totalBudget.toLong())}", marginX + 10f, currentY, bodyPaint)
    currentY += 15f
    canvas.drawText("Total Expenses Logged: $currencySymbol ${nf.format(totalSpent.toLong())}", marginX + 10f, currentY, bodyPaint)
    currentY += 15f
    canvas.drawText("Remaining Balance: $currencySymbol ${nf.format(remainingBudget.toLong())}", marginX + 10f, currentY, boldBodyPaint)
    currentY += 24f

    // 2. Guests & Attendance
    canvas.drawText("2. GUESTS & ATTENDANCE", marginX, currentY, headerPaint)
    currentY += 16f
    canvas.drawText("Total Invited: ${eventGuests.size} Guests", marginX + 10f, currentY, bodyPaint)
    currentY += 15f
    canvas.drawText("Confirmed Guests: $confirmedGuestCount", marginX + 10f, currentY, boldBodyPaint)
    currentY += 24f

    // 3. Vendors Contact List
    if (vendors.isNotEmpty()) {
      canvas.drawText("3. KEY VENDORS & SERVICE CONTACTS", marginX, currentY, headerPaint)
      currentY += 16f
      vendors.take(6).forEach { v ->
        val serviceName = if (v.note.isNotBlank()) "(${v.note})" else ""
        canvas.drawText("• ${v.name} $serviceName — Phone: ${v.phone}", marginX + 10f, currentY, bodyPaint)
        currentY += 15f
      }
      currentY += 10f
    }

    // 4. Checklist / Major Tasks
    if (checklist.isNotEmpty()) {
      canvas.drawText("4. KEY CHECKLIST & TO-DOS", marginX, currentY, headerPaint)
      currentY += 16f
      checklist.take(6).forEach { item ->
        val mark = if (item.isDone) "[DONE]" else "[PENDING]"
        canvas.drawText("$mark ${item.title}", marginX + 10f, currentY, bodyPaint)
        currentY += 15f
      }
      currentY += 10f
    }

    // Footer
    currentY = 800f
    canvas.drawLine(marginX, currentY - 10f, 555f, currentY - 10f, dividerPaint)
    canvas.drawText("Generated with Event Manager App • Keep everyone updated!", marginX, currentY + 10f, subtitlePaint)

    pdfDocument.finishPage(page)

    // Save PDF to cache directory and trigger Android Share Sheet
    val reportsDir = File(context.cacheDir, "reports")
    if (!reportsDir.exists()) {
      reportsDir.mkdirs()
    }
    val safeFileName = "Event_Summary_${event.id}_${System.currentTimeMillis()}.pdf"
    val file = File(reportsDir, safeFileName)

    try {
      val outputStream = FileOutputStream(file)
      pdfDocument.writeTo(outputStream)
      outputStream.flush()
      outputStream.close()
      pdfDocument.close()

      val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "${event.title} - Summary Report")
        putExtra(Intent.EXTRA_TEXT, "Here is the summary PDF report for ${event.title}.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Share Event PDF Summary").apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      pdfDocument.close()
      // Fallback to text share if PDF generation fails
      val txt = generateTextSummary(event, budgetSummary, checklist, eventGuests, allContacts, currencySymbol, language)
      shareText(context, txt, "Share Event Summary")
    }
  }
}
