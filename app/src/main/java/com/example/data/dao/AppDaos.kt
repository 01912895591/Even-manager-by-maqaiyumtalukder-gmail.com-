package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventDayEntity
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
  @Query("SELECT * FROM events ORDER BY dateTimeMillis ASC")
  fun getAllEvents(): Flow<List<EventEntity>>

  @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
  fun getEventById(id: Long): Flow<EventEntity?>

  @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
  suspend fun getEventByIdOnce(id: Long): EventEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvent(event: EventEntity): Long

  @Update
  suspend fun updateEvent(event: EventEntity)

  @Delete
  suspend fun deleteEvent(event: EventEntity)

  @Query("DELETE FROM events WHERE id = :id")
  suspend fun deleteEventById(id: Long)
}

@Dao
interface ChecklistDao {
  @Query("SELECT * FROM checklist_items WHERE eventId = :eventId ORDER BY id ASC")
  fun getItemsForEvent(eventId: Long): Flow<List<ChecklistItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ChecklistItemEntity): Long

  @Update
  suspend fun updateItem(item: ChecklistItemEntity)

  @Delete
  suspend fun deleteItem(item: ChecklistItemEntity)

  @Query("DELETE FROM checklist_items WHERE id = :id")
  suspend fun deleteItemById(id: Long)
}

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses WHERE eventId = :eventId ORDER BY id DESC")
  fun getExpensesForEvent(eventId: Long): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses ORDER BY id DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Update
  suspend fun updateExpense(expense: ExpenseEntity)

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpenseById(id: Long)
}

@Dao
interface ContactDao {
  @Query("SELECT * FROM contacts ORDER BY name ASC")
  fun getAllContacts(): Flow<List<ContactEntity>>

  @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
  fun getContactById(id: Long): Flow<ContactEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContact(contact: ContactEntity): Long

  @Update
  suspend fun updateContact(contact: ContactEntity)

  @Delete
  suspend fun deleteContact(contact: ContactEntity)

  @Query("DELETE FROM contacts WHERE id = :id")
  suspend fun deleteContactById(id: Long)
}

@Dao
interface EventContactDao {
  @Query("SELECT * FROM event_contacts WHERE eventId = :eventId")
  fun getEventContactsForEvent(eventId: Long): Flow<List<EventContactCrossRef>>

  @Query("SELECT * FROM event_contacts")
  fun getAllEventContacts(): Flow<List<EventContactCrossRef>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEventContact(ref: EventContactCrossRef): Long

  @Update
  suspend fun updateEventContact(ref: EventContactCrossRef)

  @Query("UPDATE event_contacts SET status = :status WHERE eventId = :eventId AND contactId = :contactId")
  suspend fun updateGuestStatus(eventId: Long, contactId: Long, status: String)

  @Query("DELETE FROM event_contacts WHERE eventId = :eventId AND contactId = :contactId")
  suspend fun deleteByEventAndContact(eventId: Long, contactId: Long)

  @Delete
  suspend fun deleteEventContact(ref: EventContactCrossRef)
}

@Dao
interface AppSettingsDao {
  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<AppSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveSettings(settings: AppSettingsEntity)
}

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
  fun getCurrentUser(): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users")
  suspend fun getAllUsers(): List<UserEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveUser(user: UserEntity): Long

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET isLoggedIn = 0")
  suspend fun logOutAll()

  @Query("DELETE FROM users")
  suspend fun clearUsers()
}

@Dao
interface EventDayDao {
  @Query("SELECT * FROM event_days WHERE eventId = :eventId ORDER BY dateTimeMillis ASC, id ASC")
  fun getDaysForEvent(eventId: Long): Flow<List<EventDayEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEventDay(day: EventDayEntity): Long

  @Update
  suspend fun updateEventDay(day: EventDayEntity)

  @Delete
  suspend fun deleteEventDay(day: EventDayEntity)

  @Query("DELETE FROM event_days WHERE id = :id")
  suspend fun deleteEventDayById(id: Long)
}
