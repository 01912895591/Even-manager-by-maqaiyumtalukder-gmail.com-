package com.example.data.util

import android.content.Context
import android.net.Uri
import com.example.data.AppDatabase
import com.example.data.model.CateringPlanEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventDayEntity
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Robust JSON-based Backup & Restore Manager for full offline safety
 */
object BackupRestoreManager {

  data class BackupSummary(
    val eventsCount: Int,
    val checklistCount: Int,
    val expensesCount: Int,
    val contactsCount: Int,
    val guestsCount: Int,
    val eventDaysCount: Int,
    val cateringPlansCount: Int,
    val timestamp: Long
  )

  /**
   * Exports entire local database into a structured JSON string
   */
  suspend fun exportDataToJson(db: AppDatabase): String = withContext(Dispatchers.IO) {
    val events = db.eventDao().getAllEventsList()
    val checklist = db.checklistDao().getAllChecklistItemsList()
    val expenses = db.expenseDao().getAllExpensesList()
    val contacts = db.contactDao().getAllContactsList()
    val guests = db.eventContactDao().getAllEventContactsList()
    val eventDays = db.eventDayDao().getAllEventDaysList()
    val cateringPlans = db.cateringPlanDao().getAllCateringPlansList()

    val root = JSONObject()
    root.put("version", 1)
    root.put("app", "EventManagerApp")
    root.put("exportedAtMillis", System.currentTimeMillis())
    root.put("exportedAtFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

    // Events
    val eventsArray = JSONArray()
    for (e in events) {
      val obj = JSONObject().apply {
        put("id", e.id)
        put("userId", e.userId)
        put("title", e.title)
        put("category", e.category)
        put("coverPhotoColorHex", e.coverPhotoColorHex)
        put("coverPhotoUri", e.coverPhotoUri ?: JSONObject.NULL)
        put("dateTimeMillis", e.dateTimeMillis)
        put("dateFormatted", e.dateFormatted)
        put("timeFormatted", e.timeFormatted)
        put("location", e.location)
        put("description", e.description)
        put("plannedBudget", e.plannedBudget)
        put("currency", e.currency)
        put("status", e.status)
      }
      eventsArray.put(obj)
    }
    root.put("events", eventsArray)

    // Checklist
    val checklistArray = JSONArray()
    for (c in checklist) {
      val obj = JSONObject().apply {
        put("id", c.id)
        put("eventId", c.eventId)
        put("title", c.title)
        put("dueDate", c.dueDate)
        put("isDone", c.isDone)
      }
      checklistArray.put(obj)
    }
    root.put("checklist", checklistArray)

    // Expenses
    val expensesArray = JSONArray()
    for (ex in expenses) {
      val obj = JSONObject().apply {
        put("id", ex.id)
        put("eventId", ex.eventId)
        put("name", ex.name)
        put("category", ex.category)
        put("amount", ex.amount)
        put("paymentStatus", ex.paymentStatus)
        put("dueAmount", ex.dueAmount)
        put("advancePaid", ex.advancePaid)
        put("dueDate", ex.dueDate ?: JSONObject.NULL)
        put("note", ex.note)
        put("date", ex.date)
      }
      expensesArray.put(obj)
    }
    root.put("expenses", expensesArray)

    // Contacts
    val contactsArray = JSONArray()
    for (ct in contacts) {
      val obj = JSONObject().apply {
        put("id", ct.id)
        put("userId", ct.userId)
        put("name", ct.name)
        put("phone", ct.phone)
        put("relation", ct.relation)
        put("note", ct.note)
        put("avatarColorHex", ct.avatarColorHex)
      }
      contactsArray.put(obj)
    }
    root.put("contacts", contactsArray)

    // Event Guests CrossRef
    val guestsArray = JSONArray()
    for (g in guests) {
      val obj = JSONObject().apply {
        put("id", g.id)
        put("eventId", g.eventId)
        put("contactId", g.contactId)
        put("status", g.status)
      }
      guestsArray.put(obj)
    }
    root.put("event_guests", guestsArray)

    // Event Days
    val daysArray = JSONArray()
    for (d in eventDays) {
      val obj = JSONObject().apply {
        put("id", d.id)
        put("eventId", d.eventId)
        put("dayTitle", d.dayTitle)
        put("dateFormatted", d.dateFormatted)
        put("timeFormatted", d.timeFormatted)
        put("dateTimeMillis", d.dateTimeMillis)
        put("location", d.location)
        put("notes", d.notes)
      }
      daysArray.put(obj)
    }
    root.put("event_days", daysArray)

    // Catering Plans
    val cateringArray = JSONArray()
    for (cp in cateringPlans) {
      val obj = JSONObject().apply {
        put("id", cp.id)
        put("eventId", cp.eventId)
        put("perPlateCost", cp.perPlateCost)
        put("bufferPercent", cp.bufferPercent)
        put("notes", cp.notes)
      }
      cateringArray.put(obj)
    }
    root.put("catering_plans", cateringArray)

    root.toString(2)
  }

  /**
   * Writes JSON backup string directly to a content URI (Storage Access Framework)
   */
  suspend fun writeBackupToUri(context: Context, uri: Uri, jsonContent: String): Boolean = withContext(Dispatchers.IO) {
    try {
      context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
          writer.write(jsonContent)
          writer.flush()
        }
      }
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  /**
   * Reads JSON backup content from content URI and restores data into Room database
   */
  suspend fun restoreDataFromUri(context: Context, db: AppDatabase, uri: Uri): Pair<Boolean, BackupSummary?> = withContext(Dispatchers.IO) {
    try {
      val stringBuilder = StringBuilder()
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
          var line: String? = reader.readLine()
          while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
          }
        }
      }

      val jsonStr = stringBuilder.toString()
      if (jsonStr.isBlank()) return@withContext Pair(false, null)

      val root = JSONObject(jsonStr)

      // Parse and insert events
      val events = mutableListOf<EventEntity>()
      if (root.has("events")) {
        val arr = root.getJSONArray("events")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          val coverUri = if (obj.isNull("coverPhotoUri")) null else obj.optString("coverPhotoUri")
          events.add(
            EventEntity(
              id = obj.optLong("id", 0L),
              userId = obj.optLong("userId", 1L),
              title = obj.optString("title", "Event"),
              category = obj.optString("category", "Wedding"),
              coverPhotoColorHex = obj.optString("coverPhotoColorHex", "#1F6E52"),
              coverPhotoUri = coverUri,
              dateTimeMillis = obj.optLong("dateTimeMillis", System.currentTimeMillis()),
              dateFormatted = obj.optString("dateFormatted", ""),
              timeFormatted = obj.optString("timeFormatted", ""),
              location = obj.optString("location", ""),
              description = obj.optString("description", ""),
              plannedBudget = obj.optDouble("plannedBudget", 0.0),
              currency = obj.optString("currency", "৳"),
              status = obj.optString("status", "Active")
            )
          )
        }
      }

      // Parse and insert contacts FIRST (due to foreign key relation with event_contacts)
      val contacts = mutableListOf<ContactEntity>()
      if (root.has("contacts")) {
        val arr = root.getJSONArray("contacts")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          contacts.add(
            ContactEntity(
              id = obj.optLong("id", 0L),
              userId = obj.optLong("userId", 1L),
              name = obj.optString("name", "Contact"),
              phone = obj.optString("phone", ""),
              relation = obj.optString("relation", "Friend"),
              note = obj.optString("note", ""),
              avatarColorHex = obj.optString("avatarColorHex", "#4A1030")
            )
          )
        }
      }

      // Parse and insert checklist
      val checklist = mutableListOf<ChecklistItemEntity>()
      if (root.has("checklist")) {
        val arr = root.getJSONArray("checklist")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          checklist.add(
            ChecklistItemEntity(
              id = obj.optLong("id", 0L),
              eventId = obj.optLong("eventId", 1L),
              title = obj.optString("title", ""),
              dueDate = obj.optString("dueDate", ""),
              isDone = obj.optBoolean("isDone", false)
            )
          )
        }
      }

      // Parse and insert expenses
      val expenses = mutableListOf<ExpenseEntity>()
      if (root.has("expenses")) {
        val arr = root.getJSONArray("expenses")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          val dueDate = if (obj.isNull("dueDate")) null else obj.optLong("dueDate")
          expenses.add(
            ExpenseEntity(
              id = obj.optLong("id", 0L),
              eventId = obj.optLong("eventId", 1L),
              name = obj.optString("name", "Expense"),
              category = obj.optString("category", "Other"),
              amount = obj.optDouble("amount", 0.0),
              paymentStatus = obj.optString("paymentStatus", "Paid"),
              dueAmount = obj.optDouble("dueAmount", 0.0),
              advancePaid = obj.optDouble("advancePaid", 0.0),
              dueDate = dueDate,
              note = obj.optString("note", ""),
              date = obj.optString("date", "")
            )
          )
        }
      }

      // Parse and insert event guests
      val eventGuests = mutableListOf<EventContactCrossRef>()
      if (root.has("event_guests")) {
        val arr = root.getJSONArray("event_guests")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          eventGuests.add(
            EventContactCrossRef(
              id = obj.optLong("id", 0L),
              eventId = obj.optLong("eventId", 1L),
              contactId = obj.optLong("contactId", 1L),
              status = obj.optString("status", "Not Called")
            )
          )
        }
      }

      // Parse and insert event days
      val eventDays = mutableListOf<EventDayEntity>()
      if (root.has("event_days")) {
        val arr = root.getJSONArray("event_days")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          eventDays.add(
            EventDayEntity(
              id = obj.optLong("id", 0L),
              eventId = obj.optLong("eventId", 1L),
              dayTitle = obj.optString("dayTitle", "Event Day"),
              dateFormatted = obj.optString("dateFormatted", ""),
              timeFormatted = obj.optString("timeFormatted", ""),
              dateTimeMillis = obj.optLong("dateTimeMillis", System.currentTimeMillis()),
              location = obj.optString("location", ""),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Parse and insert catering plans
      val cateringPlans = mutableListOf<CateringPlanEntity>()
      if (root.has("catering_plans")) {
        val arr = root.getJSONArray("catering_plans")
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          cateringPlans.add(
            CateringPlanEntity(
              id = obj.optLong("id", 0L),
              eventId = obj.optLong("eventId", 1L),
              perPlateCost = obj.optDouble("perPlateCost", 0.0),
              bufferPercent = obj.optInt("bufferPercent", 10),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Perform atomic database restore in correct order (parent tables first)
      if (events.isNotEmpty()) db.eventDao().insertEvents(events)
      if (contacts.isNotEmpty()) db.contactDao().insertContacts(contacts)
      if (checklist.isNotEmpty()) db.checklistDao().insertItems(checklist)
      if (expenses.isNotEmpty()) db.expenseDao().insertExpenses(expenses)
      if (eventGuests.isNotEmpty()) db.eventContactDao().insertEventContacts(eventGuests)
      if (eventDays.isNotEmpty()) db.eventDayDao().insertEventDays(eventDays)
      if (cateringPlans.isNotEmpty()) db.cateringPlanDao().insertCateringPlans(cateringPlans)

      val summary = BackupSummary(
        eventsCount = events.size,
        checklistCount = checklist.size,
        expensesCount = expenses.size,
        contactsCount = contacts.size,
        guestsCount = eventGuests.size,
        eventDaysCount = eventDays.size,
        cateringPlansCount = cateringPlans.size,
        timestamp = root.optLong("exportedAtMillis", System.currentTimeMillis())
      )

      Pair(true, summary)
    } catch (e: Exception) {
      e.printStackTrace()
      Pair(false, null)
    }
  }
}
