package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.CateringPlanEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventDayEntity
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.UserEntity
import com.example.data.repository.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class PaymentDueGroup {
  OVERDUE,
  DUE_THIS_WEEK,
  UPCOMING
}

data class UpcomingPaymentItem(
  val expense: ExpenseEntity,
  val eventTitle: String,
  val eventCategory: String,
  val remainingDue: Double,
  val daysDiff: Long,
  val group: PaymentDueGroup,
  val formattedDueDate: String
)

data class CategoryExpenseSummary(
  val category: String,
  val spent: Double,
  val percentageOfTotalSpent: Float
)

data class EventBudgetSummary(
  val planned: Double,
  val spent: Double,
  val remaining: Double,
  val percentageSpent: Float,
  val categoryBreakdown: List<CategoryExpenseSummary>
)

data class EventGuestSummary(
  val totalInvited: Int,
  val confirmed: Int,
  val called: Int,
  val notCalled: Int,
  val declined: Int
)

data class CateringEstimate(
  val confirmedGuestCount: Int = 0,
  val recommendedPlates: Int = 0,
  val estimatedCost: Double = 0.0,
  val perPlateCost: Double = 0.0,
  val bufferPercent: Int = 10
)

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: EventRepository

  init {
    val db = AppDatabase.getDatabase(application, viewModelScope)
    repository = EventRepository(db)
    viewModelScope.launch {
      repository.ensureDataInitialized()
    }
  }

  val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allEventContacts: StateFlow<List<EventContactCrossRef>> = repository.allEventContacts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val settings: StateFlow<AppSettingsEntity> = repository.settings
    .combine(flowOf(Unit)) { s, _ ->
      s ?: AppSettingsEntity()
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

  val currentUser: StateFlow<UserEntity?> = repository.currentUser
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _selectedEventId = MutableStateFlow<Long?>(null)
  val selectedEventId: StateFlow<Long?> = _selectedEventId.asStateFlow()

  val selectedEvent: StateFlow<EventEntity?> = combine(allEvents, _selectedEventId) { events, id ->
    if (id != null) (events.find { it.id == id } ?: events.firstOrNull()) else events.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val selectedEventChecklist: StateFlow<List<ChecklistItemEntity>> = _selectedEventId
    .flatMapLatest { id ->
      if (id != null) repository.getChecklistForEvent(id) else flowOf(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val selectedEventExpenses: StateFlow<List<ExpenseEntity>> = _selectedEventId
    .flatMapLatest { id ->
      if (id != null) repository.getExpensesForEvent(id) else flowOf(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val selectedEventGuests: StateFlow<List<EventContactCrossRef>> = _selectedEventId
    .flatMapLatest { id ->
      if (id != null) repository.getEventContactsForEvent(id) else flowOf(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val selectedEventDays: StateFlow<List<EventDayEntity>> = _selectedEventId
    .flatMapLatest { id ->
      if (id != null) repository.getEventDays(id) else flowOf(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val upcomingPayments: StateFlow<List<UpcomingPaymentItem>> = combine(
    allEvents,
    allExpenses
  ) { eventsList, expensesList ->
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    val endOfSevenDays = startOfToday + TimeUnit.DAYS.toMillis(7)

    val eventMap = eventsList.associateBy { it.id }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    expensesList
      .filter { it.dueDate != null }
      .mapNotNull { expense ->
        val event = eventMap[expense.eventId] ?: return@mapNotNull null
        val dueMillis = expense.dueDate!!
        val remainingDue = if (expense.paymentStatus.equals("Paid", ignoreCase = true)) 0.0
        else if (expense.dueAmount > 0.0) expense.dueAmount
        else (expense.amount - expense.advancePaid).coerceAtLeast(0.0)

        val diffDays = TimeUnit.MILLISECONDS.toDays(dueMillis - startOfToday)
        val group = when {
          dueMillis < startOfToday && remainingDue > 0.0 -> PaymentDueGroup.OVERDUE
          dueMillis in startOfToday..endOfSevenDays && remainingDue > 0.0 -> PaymentDueGroup.DUE_THIS_WEEK
          else -> PaymentDueGroup.UPCOMING
        }

        UpcomingPaymentItem(
          expense = expense,
          eventTitle = event.title,
          eventCategory = event.category,
          remainingDue = remainingDue,
          daysDiff = diffDays,
          group = group,
          formattedDueDate = dateFormat.format(java.util.Date(dueMillis))
        )
      }
      .sortedWith(compareBy<UpcomingPaymentItem> {
        when (it.group) {
          PaymentDueGroup.OVERDUE -> 0
          PaymentDueGroup.DUE_THIS_WEEK -> 1
          PaymentDueGroup.UPCOMING -> 2
        }
      }.thenBy { it.expense.dueDate ?: Long.MAX_VALUE })
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Budget calculations for selected event
  val selectedEventBudgetSummary: StateFlow<EventBudgetSummary> = combine(
    selectedEvent,
    selectedEventExpenses
  ) { event, expenses ->
    val planned = event?.plannedBudget ?: 0.0
    val spent = expenses.sumOf { it.amount }
    val remaining = (planned - spent).coerceAtLeast(0.0)
    val percentage = if (planned > 0) ((spent / planned) * 100f).coerceIn(0.0, 100.0).toFloat() else 0f

    val defaultCategories = listOf("Catering", "Venue", "Decoration", "Photography", "Attire", "Gifts", "Other")
    val allUniqueCategories = (defaultCategories + expenses.map { it.category.trim() }.filter { it.isNotBlank() })
      .distinctBy { it.lowercase() }
    val breakdown = allUniqueCategories.map { cat ->
      val catSpent = expenses.filter { it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
      val catPercent = if (spent > 0) (catSpent / spent).toFloat() else 0f
      CategoryExpenseSummary(cat, catSpent, catPercent)
    }.filter { it.spent > 0 || defaultCategories.take(3).contains(it.category) }

    EventBudgetSummary(
      planned = planned,
      spent = spent,
      remaining = remaining,
      percentageSpent = percentage,
      categoryBreakdown = breakdown
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    EventBudgetSummary(0.0, 0.0, 0.0, 0f, emptyList())
  )

  // Guest calculations for selected event
  val selectedEventGuestSummary: StateFlow<EventGuestSummary> = selectedEventGuests
    .combine(flowOf(Unit)) { guests, _ ->
      EventGuestSummary(
        totalInvited = guests.size,
        confirmed = guests.count { it.status == "Confirmed" },
        called = guests.count { it.status == "Called" },
        notCalled = guests.count { it.status == "Not Called" },
        declined = guests.count { it.status == "Declined" }
      )
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      EventGuestSummary(0, 0, 0, 0, 0)
    )

  // Catering Plan & Estimation for selected event
  val selectedCateringPlan: StateFlow<CateringPlanEntity?> = _selectedEventId
    .flatMapLatest { id ->
      if (id != null) repository.getCateringPlan(id) else flowOf(null)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val cateringEstimate: StateFlow<CateringEstimate> = combine(
    selectedEventGuests,
    selectedCateringPlan
  ) { guests, plan ->
    val confirmedCount = guests.count { it.status.equals("Confirmed", ignoreCase = true) }
    val perPlate = plan?.perPlateCost ?: 0.0
    val buffer = plan?.bufferPercent ?: 10
    val bufferPlates = if (confirmedCount > 0) {
      ceil(confirmedCount * buffer / 100.0).toInt()
    } else 0
    val recommended = confirmedCount + bufferPlates
    val cost = recommended * perPlate

    CateringEstimate(
      confirmedGuestCount = confirmedCount,
      recommendedPlates = recommended,
      estimatedCost = cost,
      perPlateCost = perPlate,
      bufferPercent = buffer
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    CateringEstimate()
  )

  // Contacts Filtering & Search
  private val _contactSearchQuery = MutableStateFlow("")
  val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

  private val _contactFilter = MutableStateFlow("All")
  val contactFilter: StateFlow<String> = _contactFilter.asStateFlow()

  val filteredContacts: StateFlow<List<ContactEntity>> = combine(
    allContacts,
    _contactSearchQuery,
    _contactFilter
  ) { list, query, filter ->
    list.filter { contact ->
      val matchesQuery = query.isBlank() ||
        contact.name.contains(query, ignoreCase = true) ||
        contact.phone.contains(query) ||
        contact.relation.contains(query, ignoreCase = true)

      val matchesFilter = filter.equals("All", ignoreCase = true) ||
        contact.relation.trim().equals(filter.trim(), ignoreCase = true)

      matchesQuery && matchesFilter
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun selectEvent(id: Long) {
    _selectedEventId.value = id
  }

  fun setContactSearchQuery(query: String) {
    _contactSearchQuery.value = query
  }

  fun setContactFilter(filter: String) {
    _contactFilter.value = filter
  }

  // Event Actions
  fun createEvent(
    title: String,
    category: String,
    colorHex: String,
    coverPhotoUri: String? = null,
    dateFormatted: String,
    timeFormatted: String,
    location: String,
    budget: Double,
    currency: String,
    description: String,
    dateTimeMillis: Long? = null,
    onSuccess: (Long) -> Unit
  ) {
    viewModelScope.launch {
      val calculatedMillis: Long = if (dateTimeMillis != null && dateTimeMillis > 0L) {
        dateTimeMillis
      } else {
        try {
          val format = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.US)
          format.parse("$dateFormatted $timeFormatted")?.time
            ?: java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).parse(dateFormatted)?.time
            ?: (System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
        } catch (e: Exception) {
          System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
        }
      }

      val newEvent = EventEntity(
        userId = currentUser.value?.id ?: 1L,
        title = title,
        category = category,
        coverPhotoColorHex = colorHex,
        coverPhotoUri = coverPhotoUri,
        dateTimeMillis = calculatedMillis,
        dateFormatted = dateFormatted,
        timeFormatted = timeFormatted,
        location = location,
        description = description,
        plannedBudget = budget,
        currency = currency,
        status = "Active"
      )
      val newId = repository.insertEvent(newEvent)
      _selectedEventId.value = newId
      onSuccess(newId)
    }
  }

  fun updateEvent(
    eventId: Long,
    title: String,
    category: String,
    colorHex: String,
    coverPhotoUri: String? = null,
    dateFormatted: String,
    timeFormatted: String,
    location: String,
    budget: Double,
    currency: String,
    description: String,
    dateTimeMillis: Long? = null,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val existing = allEvents.value.firstOrNull { it.id == eventId }
        ?: repository.getEventById(eventId).firstOrNull()
        ?: return@launch

      val calculatedMillis: Long = if (dateTimeMillis != null && dateTimeMillis > 0L) {
        dateTimeMillis
      } else {
        try {
          val format = java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.US)
          format.parse("$dateFormatted $timeFormatted")?.time
            ?: java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).parse(dateFormatted)?.time
            ?: existing.dateTimeMillis
        } catch (e: Exception) {
          existing.dateTimeMillis
        }
      }

      val updatedEvent = existing.copy(
        title = title,
        category = category,
        coverPhotoColorHex = colorHex,
        coverPhotoUri = coverPhotoUri,
        dateTimeMillis = calculatedMillis,
        dateFormatted = dateFormatted,
        timeFormatted = timeFormatted,
        location = location,
        description = description,
        plannedBudget = budget,
        currency = currency
      )
      repository.updateEvent(updatedEvent)
      _selectedEventId.value = eventId
      onSuccess()
    }
  }

  fun deleteEvent(event: EventEntity, onDeleted: () -> Unit) {
    viewModelScope.launch {
      repository.deleteEvent(event)
      val next = allEvents.value.firstOrNull { it.id != event.id }
      _selectedEventId.value = next?.id
      onDeleted()
    }
  }

  // Checklist Actions
  fun toggleChecklistItem(item: ChecklistItemEntity) {
    viewModelScope.launch {
      repository.updateChecklistItem(item.copy(isDone = !item.isDone))
    }
  }

  fun addChecklistItem(title: String, dueDate: String) {
    val currentId = _selectedEventId.value ?: return
    viewModelScope.launch {
      repository.insertChecklistItem(
        ChecklistItemEntity(
          eventId = currentId,
          title = title,
          dueDate = dueDate,
          isDone = false
        )
      )
    }
  }

  fun deleteChecklistItem(item: ChecklistItemEntity) {
    viewModelScope.launch {
      repository.deleteChecklistItem(item)
    }
  }

  // Event Day Actions (Multi-day events)
  fun addEventDay(
    dayTitle: String,
    dateFormatted: String,
    timeFormatted: String,
    dateTimeMillis: Long,
    location: String,
    notes: String = ""
  ) {
    val currentId = _selectedEventId.value ?: return
    viewModelScope.launch {
      repository.insertEventDay(
        EventDayEntity(
          eventId = currentId,
          dayTitle = dayTitle.trim().ifBlank { "Event Day" },
          dateFormatted = dateFormatted,
          timeFormatted = timeFormatted,
          dateTimeMillis = dateTimeMillis,
          location = location.trim(),
          notes = notes.trim()
        )
      )
    }
  }

  fun updateEventDay(day: EventDayEntity) {
    viewModelScope.launch {
      repository.updateEventDay(day)
    }
  }

  fun deleteEventDay(day: EventDayEntity) {
    viewModelScope.launch {
      repository.deleteEventDay(day)
    }
  }

  // Expense Actions
  fun addExpense(
    name: String,
    category: String,
    amount: Double,
    paymentStatus: String,
    dueAmount: Double = 0.0,
    advancePaid: Double = 0.0,
    dueDate: Long? = null,
    note: String = "",
    date: String = "Today",
    explicitEventId: Long? = null
  ) {
    val currentId = explicitEventId ?: _selectedEventId.value ?: allEvents.value.firstOrNull()?.id ?: return
    val computedDue = if (paymentStatus.equals("Paid", ignoreCase = true)) 0.0
    else if (dueAmount > 0.0) dueAmount
    else (amount - advancePaid).coerceAtLeast(0.0)

    viewModelScope.launch {
      repository.insertExpense(
        ExpenseEntity(
          eventId = currentId,
          name = name,
          category = category,
          amount = amount,
          paymentStatus = paymentStatus,
          dueAmount = computedDue,
          advancePaid = advancePaid,
          dueDate = dueDate,
          note = note,
          date = date
        )
      )
    }
  }

  fun updateExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.updateExpense(expense)
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
    }
  }

  // Catering Actions
  fun saveCateringPlanForSelectedEvent(
    perPlateCost: Double,
    bufferPercent: Int = 10,
    notes: String = ""
  ) {
    val eventId = _selectedEventId.value ?: return
    viewModelScope.launch {
      repository.saveCateringPlan(
        CateringPlanEntity(
          eventId = eventId,
          perPlateCost = perPlateCost,
          bufferPercent = bufferPercent,
          notes = notes
        )
      )
    }
  }

  fun addOrUpdateCateringExpense(
    eventId: Long,
    estimatedCost: Double,
    recommendedPlates: Int,
    perPlateCost: Double,
    bufferPercent: Int,
    onSuccess: () -> Unit = {}
  ) {
    val targetEventId = if (eventId > 0) eventId else (_selectedEventId.value ?: return)
    viewModelScope.launch {
      val currentExpenses = repository.getExpensesForEvent(targetEventId).firstOrNull() ?: emptyList()
      val existingCatering = currentExpenses.find { it.category.equals("Catering", ignoreCase = true) }
      val noteText = "$recommendedPlates plates @ $perPlateCost/plate ($bufferPercent% buffer)"
      if (existingCatering != null) {
        val newDue = if (existingCatering.paymentStatus.equals("Paid", ignoreCase = true)) {
          0.0
        } else {
          (estimatedCost - existingCatering.advancePaid).coerceAtLeast(0.0)
        }
        repository.updateExpense(
          existingCatering.copy(
            amount = estimatedCost,
            dueAmount = newDue,
            note = if (existingCatering.note.isBlank() || existingCatering.note.contains("plates @")) noteText else existingCatering.note
          )
        )
      } else {
        repository.insertExpense(
          ExpenseEntity(
            eventId = targetEventId,
            name = "Catering ($recommendedPlates plates)",
            category = "Catering",
            amount = estimatedCost,
            paymentStatus = "Due",
            dueAmount = estimatedCost,
            advancePaid = 0.0,
            note = noteText,
            date = "Auto-calculated"
          )
        )
      }
      onSuccess()
    }
  }

  // Contact Actions
  fun addContact(
    name: String,
    phone: String,
    relation: String,
    note: String,
    eventId: Long? = null,
    onComplete: ((Long) -> Unit)? = null
  ) {
    viewModelScope.launch {
      val colors = listOf("#4A1030", "#1F6E52", "#D4AF6A", "#285496", "#C85A32")
      val randomColor = colors.random()
      val safeCategory = when (relation.trim().lowercase()) {
        "vendor" -> "Vendor"
        "friend" -> "Friend"
        "family" -> "Family"
        "other" -> "Other"
        else -> relation.trim().ifBlank { "Other" }
      }
      val newId = repository.insertContact(
        ContactEntity(
          userId = currentUser.value?.id ?: 1L,
          name = name.trim(),
          phone = phone.trim(),
          relation = safeCategory,
          note = note.trim(),
          avatarColorHex = randomColor
        )
      )
      if (eventId != null) {
        repository.addContactToEvent(eventId, newId, "Not Called")
      }
      onComplete?.invoke(newId)
    }
  }

  fun updateContact(contact: ContactEntity, onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      repository.updateContact(contact)
      onComplete?.invoke()
    }
  }

  fun deleteContact(contact: ContactEntity) {
    viewModelScope.launch {
      repository.deleteContact(contact)
    }
  }

  fun removeGuestFromEvent(eventId: Long, contactId: Long, onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      repository.removeGuestFromEvent(eventId, contactId)
      onComplete?.invoke()
    }
  }

  fun importPhoneContacts(
    contacts: List<Pair<String, String>>,
    eventId: Long? = null,
    onComplete: (() -> Unit)? = null
  ) {
    importPhoneContacts(
      contacts.map { Triple(it.first, it.second, "Family") },
      eventId,
      onComplete
    )
  }

  @JvmName("importPhoneContactsWithCategory")
  fun importPhoneContacts(
    contacts: List<Triple<String, String, String>>,
    eventId: Long? = null,
    onComplete: (() -> Unit)? = null
  ) {
    viewModelScope.launch {
      val colors = listOf("#4A1030", "#1F6E52", "#D4AF6A", "#285496", "#C85A32")
      contacts.forEach { (name, phone, category) ->
        val safeCategory = when (category.trim().lowercase()) {
          "vendor" -> "Vendor"
          "friend" -> "Friend"
          "family" -> "Family"
          else -> category.trim().ifBlank { "Other" }
        }
        val newId = repository.insertContact(
          ContactEntity(
            userId = currentUser.value?.id ?: 1L,
            name = name.trim(),
            phone = phone.trim(),
            relation = safeCategory,
            note = "Imported contact",
            avatarColorHex = colors.random()
          )
        )
        if (eventId != null) {
          repository.addContactToEvent(eventId, newId, "Not Called")
        }
      }
      onComplete?.invoke()
    }
  }

  // Guest Management Actions
  fun toggleGuestForEvent(eventId: Long, contactId: Long, isCurrentlyAdded: Boolean) {
    viewModelScope.launch {
      if (isCurrentlyAdded) {
        repository.removeGuestFromEvent(eventId, contactId)
      } else {
        repository.addContactToEvent(eventId, contactId, "Not Called")
      }
    }
  }

  fun addGuestsToEvent(eventId: Long, contactIds: Set<Long>, onDone: () -> Unit) {
    viewModelScope.launch {
      repository.syncEventGuests(eventId, contactIds)
      onDone()
    }
  }

  fun updateGuestStatus(eventId: Long, contactId: Long, newStatus: String) {
    viewModelScope.launch {
      repository.updateGuestStatus(eventId, contactId, newStatus)
    }
  }

  // Settings Actions
  fun updateLanguage(language: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(language = language))
    }
  }

  fun toggleDarkMode(enabled: Boolean) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(isDarkMode = enabled))
    }
  }

  fun updateCurrency(currency: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(defaultCurrency = currency))
    }
  }

  fun updateAccentColor(hexColor: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(accentColorHex = hexColor))
    }
  }

  fun toggleNotifications(enabled: Boolean) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(notificationsEnabled = enabled))
    }
  }

  // Auth / User Actions
  fun signInWithCredentials(
    email: String,
    password: String,
    onSuccess: (UserEntity) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val (success, errorMsg) = repository.loginUser(email, password)
      if (success) {
        val user = repository.getUserByEmail(email)
        if (user != null) onSuccess(user) else onSuccess(UserEntity(name = "User", email = email, isLoggedIn = true))
      } else {
        onError(errorMsg ?: "Invalid email or password")
      }
    }
  }

  fun signUpWithCredentials(
    name: String,
    email: String,
    password: String,
    onSuccess: (UserEntity) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val (success, errorMsg) = repository.registerUser(name, email, password)
      if (success) {
        val user = repository.getUserByEmail(email)
        if (user != null) onSuccess(user) else onSuccess(UserEntity(name = name, email = email, isLoggedIn = true))
      } else {
        onError(errorMsg ?: "Registration failed")
      }
    }
  }

  fun signInWithGoogle(name: String, email: String, photoUrl: String? = null, onComplete: (UserEntity) -> Unit) {
    viewModelScope.launch {
      val user = repository.loginWithGoogle(name, email, photoUrl)
      onComplete(user)
    }
  }

  fun updateGoogleWebClientId(clientId: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(googleWebClientId = clientId.trim()))
    }
  }

  fun toggleAutoLoginWithGoogle(enabled: Boolean) {
    viewModelScope.launch {
      val current = settings.value
      repository.saveSettings(current.copy(autoLoginWithGoogleEnabled = enabled))
    }
  }

  fun resetPassword(
    email: String,
    newPassword: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val (success, errorMsg) = repository.resetPassword(email, newPassword)
      if (success) {
        onSuccess()
      } else {
        onError(errorMsg ?: "Failed to reset password")
      }
    }
  }

  fun verifyEmailForReset(
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val (success, errorMsg) = repository.verifyEmailForReset(email)
      if (success) {
        onSuccess()
      } else {
        onError(errorMsg ?: "No account found with this email.")
      }
    }
  }

  fun updateProfile(name: String, email: String, onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      repository.updateProfile(name, email)
      onComplete?.invoke()
    }
  }

  fun logOut(onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.logOutUser()
      onComplete()
    }
  }
}
