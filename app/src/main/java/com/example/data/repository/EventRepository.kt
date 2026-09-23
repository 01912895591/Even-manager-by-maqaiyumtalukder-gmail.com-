package com.example.data.repository

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
import com.example.data.util.PasswordSecurity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class EventRepository(private val db: AppDatabase) {

  val allEvents: Flow<List<EventEntity>> = db.eventDao().getAllEvents()
  val allContacts: Flow<List<ContactEntity>> = db.contactDao().getAllContacts()
  val allEventContacts: Flow<List<EventContactCrossRef>> = db.eventContactDao().getAllEventContacts()
  val allExpenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
  val settings: Flow<AppSettingsEntity?> = db.appSettingsDao().getSettings()
  val currentUser: Flow<UserEntity?> = db.userDao().getCurrentUser()

  fun getEventById(id: Long): Flow<EventEntity?> = db.eventDao().getEventById(id)

  fun getChecklistForEvent(eventId: Long): Flow<List<ChecklistItemEntity>> =
    db.checklistDao().getItemsForEvent(eventId)

  fun getEventDays(eventId: Long): Flow<List<EventDayEntity>> =
    db.eventDayDao().getDaysForEvent(eventId)

  fun getExpensesForEvent(eventId: Long): Flow<List<ExpenseEntity>> =
    db.expenseDao().getExpensesForEvent(eventId)

  fun getEventContactsForEvent(eventId: Long): Flow<List<EventContactCrossRef>> =
    db.eventContactDao().getEventContactsForEvent(eventId)

  fun getCateringPlan(eventId: Long): Flow<CateringPlanEntity?> =
    db.cateringPlanDao().getCateringPlan(eventId)

  suspend fun ensureDataInitialized() {
    withContext(Dispatchers.IO) {
      try {
        val existingSettings = db.appSettingsDao().getSettings().firstOrNull()
        if (existingSettings == null) {
          AppDatabase.populateInitialData(db)
        }
      } catch (e: Exception) {
        // Handled gracefully without crash
      }
    }
  }

  // Events CRUD
  suspend fun insertEvent(event: EventEntity): Long = withContext(Dispatchers.IO) {
    db.eventDao().insertEvent(event)
  }

  suspend fun updateEvent(event: EventEntity) = withContext(Dispatchers.IO) {
    db.eventDao().updateEvent(event)
  }

  suspend fun deleteEvent(event: EventEntity) = withContext(Dispatchers.IO) {
    db.eventDao().deleteEvent(event)
  }

  suspend fun deleteEventById(id: Long) = withContext(Dispatchers.IO) {
    db.eventDao().deleteEventById(id)
  }

  // Checklist CRUD
  suspend fun insertChecklistItem(item: ChecklistItemEntity): Long = withContext(Dispatchers.IO) {
    db.checklistDao().insertItem(item)
  }

  suspend fun updateChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
    db.checklistDao().updateItem(item)
  }

  suspend fun deleteChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
    db.checklistDao().deleteItem(item)
  }

  // Event Days (Multi-day functions)
  suspend fun insertEventDay(day: EventDayEntity): Long = withContext(Dispatchers.IO) {
    db.eventDayDao().insertEventDay(day)
  }

  suspend fun updateEventDay(day: EventDayEntity) = withContext(Dispatchers.IO) {
    db.eventDayDao().updateEventDay(day)
  }

  suspend fun deleteEventDay(day: EventDayEntity) = withContext(Dispatchers.IO) {
    db.eventDayDao().deleteEventDay(day)
  }

  suspend fun deleteEventDayById(id: Long) = withContext(Dispatchers.IO) {
    db.eventDayDao().deleteEventDayById(id)
  }

  // Catering Plan CRUD
  suspend fun saveCateringPlan(plan: CateringPlanEntity): Long = withContext(Dispatchers.IO) {
    val existing = db.cateringPlanDao().getCateringPlanOnce(plan.eventId)
    if (existing != null) {
      db.cateringPlanDao().updateCateringPlan(plan.copy(id = existing.id))
      existing.id
    } else {
      db.cateringPlanDao().insertCateringPlan(plan)
    }
  }

  suspend fun deleteCateringPlanByEventId(eventId: Long) = withContext(Dispatchers.IO) {
    db.cateringPlanDao().deleteCateringPlanByEventId(eventId)
  }

  // Expense CRUD
  suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
    db.expenseDao().insertExpense(expense)
  }

  suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
    db.expenseDao().updateExpense(expense)
  }

  suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
    db.expenseDao().deleteExpense(expense)
  }

  // Contact CRUD
  suspend fun insertContact(contact: ContactEntity): Long = withContext(Dispatchers.IO) {
    db.contactDao().insertContact(contact)
  }

  suspend fun updateContact(contact: ContactEntity) = withContext(Dispatchers.IO) {
    db.contactDao().updateContact(contact)
  }

  suspend fun deleteContact(contact: ContactEntity) = withContext(Dispatchers.IO) {
    db.contactDao().deleteContact(contact)
  }

  // Event Guests
  suspend fun addContactToEvent(eventId: Long, contactId: Long, status: String = "Not Called") = withContext(Dispatchers.IO) {
    db.eventContactDao().insertEventContact(
      EventContactCrossRef(eventId = eventId, contactId = contactId, status = status)
    )
  }

  suspend fun syncEventGuests(eventId: Long, selectedContactIds: Set<Long>) = withContext(Dispatchers.IO) {
    val currentRefs = db.eventContactDao().getEventContactsForEvent(eventId).firstOrNull() ?: emptyList()
    val currentIds = currentRefs.map { it.contactId }.toSet()
    
    // Remove unselected contacts
    val toRemove = currentIds - selectedContactIds
    toRemove.forEach { contactId ->
      db.eventContactDao().deleteByEventAndContact(eventId, contactId)
    }
    
    // Add newly selected contacts
    val toAdd = selectedContactIds - currentIds
    toAdd.forEach { contactId ->
      db.eventContactDao().insertEventContact(
        EventContactCrossRef(eventId = eventId, contactId = contactId, status = "Not Called")
      )
    }
  }

  suspend fun updateGuestStatus(eventId: Long, contactId: Long, status: String) = withContext(Dispatchers.IO) {
    db.eventContactDao().updateGuestStatus(eventId, contactId, status)
  }

  suspend fun removeGuestFromEvent(eventId: Long, contactId: Long) = withContext(Dispatchers.IO) {
    db.eventContactDao().deleteByEventAndContact(eventId, contactId)
  }

  // Settings & User
  suspend fun saveSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
    db.appSettingsDao().saveSettings(settings)
  }

  suspend fun saveUser(user: UserEntity) = withContext(Dispatchers.IO) {
    db.userDao().saveUser(user)
  }

  suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
    db.userDao().getUserByEmail(email.trim().lowercase())
  }

  suspend fun registerUser(name: String, email: String, password: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val cleanName = name.trim().ifEmpty { "Event Planner" }
    val existing = db.userDao().getUserByEmail(cleanEmail)
    if (existing != null) {
      return@withContext Pair(false, "An account with this email already exists. Please Sign In.")
    }
    db.userDao().logOutAll()
    val salt = PasswordSecurity.generateSalt()
    val hash = PasswordSecurity.hashPassword(password, salt)
    val newUser = UserEntity(
      name = cleanName,
      email = cleanEmail,
      passwordHash = hash,
      passwordSalt = salt,
      authProvider = "email",
      isLoggedIn = true
    )
    db.userDao().saveUser(newUser)
    Pair(true, null)
  }

  suspend fun loginUser(email: String, password: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val user = db.userDao().getUserByEmail(cleanEmail)
    if (user == null) {
      // Auto-register and sign in seamlessly
      db.userDao().logOutAll()
      val nameFromEmail = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
      val salt = PasswordSecurity.generateSalt()
      val hash = PasswordSecurity.hashPassword(password, salt)
      val newUser = UserEntity(
        name = nameFromEmail.ifBlank { "Event Planner" },
        email = cleanEmail,
        passwordHash = hash,
        passwordSalt = salt,
        authProvider = "email",
        isLoggedIn = true
      )
      db.userDao().saveUser(newUser)
      return@withContext Pair(true, null)
    }
    if (user.passwordHash.isNotEmpty()) {
      val matches = PasswordSecurity.verifyPassword(password, user.passwordSalt, user.passwordHash)
      if (!matches) {
        return@withContext Pair(false, "Incorrect password. Please check your password and try again.")
      }
    }
    db.userDao().logOutAll()
    db.userDao().saveUser(user.copy(isLoggedIn = true))
    Pair(true, null)
  }

  suspend fun loginWithGoogle(name: String, email: String, photoUrl: String? = null): UserEntity = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val cleanName = name.trim().ifEmpty { "Google User" }
    db.userDao().logOutAll()
    val existing = db.userDao().getUserByEmail(cleanEmail)
    val user = if (existing != null) {
      existing.copy(
        name = if (existing.name.isNotBlank()) existing.name else cleanName,
        authProvider = "google",
        isLoggedIn = true,
        profilePictureUrl = photoUrl ?: existing.profilePictureUrl
      )
    } else {
      UserEntity(
        name = cleanName,
        email = cleanEmail,
        passwordHash = "",
        passwordSalt = "",
        authProvider = "google",
        isLoggedIn = true,
        profilePictureUrl = photoUrl
      )
    }
    db.userDao().saveUser(user)
    user
  }

  suspend fun resetPassword(email: String, newPassword: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val user = db.userDao().getUserByEmail(cleanEmail)
    if (user == null) {
      return@withContext Pair(false, "No account found for $cleanEmail. Please check the email address.")
    }
    val newSalt = PasswordSecurity.generateSalt()
    val newHash = PasswordSecurity.hashPassword(newPassword, newSalt)
    db.userDao().saveUser(user.copy(passwordHash = newHash, passwordSalt = newSalt))
    Pair(true, null)
  }

  suspend fun verifyEmailForReset(email: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val user = db.userDao().getUserByEmail(cleanEmail)
    if (user == null) {
      return@withContext Pair(false, "No account found for $cleanEmail.")
    }
    Pair(true, null)
  }

  suspend fun updateProfile(name: String, email: String) = withContext(Dispatchers.IO) {
    val current = db.userDao().getCurrentUser().firstOrNull()
    if (current != null) {
      db.userDao().saveUser(current.copy(name = name.trim().ifEmpty { "Event Planner" }, email = email.trim()))
    } else {
      db.userDao().saveUser(
        UserEntity(
          name = name.trim().ifEmpty { "Event Planner" },
          email = email.trim(),
          isLoggedIn = true,
          authProvider = "local"
        )
      )
    }
  }

  suspend fun logOutUser() = withContext(Dispatchers.IO) {
    db.userDao().logOutAll()
  }
}
