package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

  // Budget calculations for selected event
  val selectedEventBudgetSummary: StateFlow<EventBudgetSummary> = combine(
    selectedEvent,
    selectedEventExpenses
  ) { event, expenses ->
    val planned = event?.plannedBudget ?: 0.0
    val spent = expenses.sumOf { it.amount }
    val remaining = (planned - spent).coerceAtLeast(0.0)
    val percentage = if (planned > 0) ((spent / planned) * 100f).coerceIn(0.0, 100.0).toFloat() else 0f

    val categories = listOf("Catering", "Venue", "Decoration", "Photography", "Attire", "Gifts", "Other")
    val breakdown = categories.map { cat ->
      val catSpent = expenses.filter { it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
      val catPercent = if (spent > 0) (catSpent / spent).toFloat() else 0f
      CategoryExpenseSummary(cat, catSpent, catPercent)
    }.filter { it.spent > 0 || categories.take(3).contains(it.category) }

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

  // Expense Actions
  fun addExpense(
    name: String,
    category: String,
    amount: Double,
    paymentStatus: String,
    dueAmount: Double = 0.0,
    note: String = "",
    date: String = "Today"
  ) {
    val currentId = _selectedEventId.value ?: return
    viewModelScope.launch {
      repository.insertExpense(
        ExpenseEntity(
          eventId = currentId,
          name = name,
          category = category,
          amount = amount,
          paymentStatus = paymentStatus,
          dueAmount = dueAmount,
          note = note,
          date = date
        )
      )
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
    }
  }

  // Contact Actions
  fun addContact(name: String, phone: String, relation: String, note: String) {
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
      repository.insertContact(
        ContactEntity(
          userId = currentUser.value?.id ?: 1L,
          name = name.trim(),
          phone = phone.trim(),
          relation = safeCategory,
          note = note.trim(),
          avatarColorHex = randomColor
        )
      )
    }
  }

  fun deleteContact(contact: ContactEntity) {
    viewModelScope.launch {
      repository.deleteContact(contact)
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
      contactIds.forEach { contactId ->
        repository.addContactToEvent(eventId, contactId, "Not Called")
      }
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

  fun signInWithGoogle(name: String, email: String, onComplete: (UserEntity) -> Unit) {
    viewModelScope.launch {
      val user = repository.loginWithGoogle(name, email)
      onComplete(user)
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

  fun logOut(onComplete: () -> Unit) {
    viewModelScope.launch {
      repository.logOutUser()
      onComplete()
    }
  }
}
