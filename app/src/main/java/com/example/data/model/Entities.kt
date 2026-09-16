package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val email: String,
  val passwordHash: String = "",
  val passwordSalt: String = "",
  val authProvider: String = "email",
  val isLoggedIn: Boolean = false,
  val profilePictureUrl: String? = null
) {
  val password: String get() = passwordHash
}

@Entity(tableName = "events")
data class EventEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long = 1,
  val title: String,
  val category: String, // Wedding, Eid, Birthday, Aqiqah, Funeral, Custom
  val coverPhotoColorHex: String = "#1F6E52", // Emerald by default or category color
  val coverPhotoUri: String? = null,
  val dateTimeMillis: Long,
  val dateFormatted: String,
  val timeFormatted: String,
  val location: String,
  val description: String = "",
  val plannedBudget: Double,
  val currency: String = "৳",
  val status: String = "Active" // Active, Completed, Cancelled
)

@Entity(
  tableName = "checklist_items",
  foreignKeys = [
    ForeignKey(
      entity = EventEntity::class,
      parentColumns = ["id"],
      childColumns = ["eventId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["eventId"])]
)
data class ChecklistItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val eventId: Long,
  val title: String,
  val dueDate: String = "",
  val isDone: Boolean = false
)

@Entity(
  tableName = "expenses",
  foreignKeys = [
    ForeignKey(
      entity = EventEntity::class,
      parentColumns = ["id"],
      childColumns = ["eventId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["eventId"])]
)
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val eventId: Long,
  val name: String,
  val category: String, // Catering, Venue, Decoration, Photography, Attire, Gifts, Other
  val amount: Double,
  val paymentStatus: String = "Paid", // Paid or Due
  val dueAmount: Double = 0.0,
  val note: String = "",
  val date: String = ""
)

@Entity(tableName = "contacts")
data class ContactEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long = 1,
  val name: String,
  val phone: String,
  val relation: String, // Family, Friend, Vendor, Colleague, Other
  val note: String = "",
  val avatarColorHex: String = "#4A1030"
)

@Entity(
  tableName = "event_contacts",
  foreignKeys = [
    ForeignKey(
      entity = EventEntity::class,
      parentColumns = ["id"],
      childColumns = ["eventId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = ContactEntity::class,
      parentColumns = ["id"],
      childColumns = ["contactId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["eventId"]), Index(value = ["contactId"])]
)
data class EventContactCrossRef(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val eventId: Long,
  val contactId: Long,
  val status: String = "Not Called" // Not Called, Called, Confirmed, Declined
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val language: String = "en", // "en" or "bn"
  val isDarkMode: Boolean = false,
  val accentColorHex: String = "#D4AF6A",
  val defaultCurrency: String = "৳",
  val notificationsEnabled: Boolean = true,
  val googleWebClientId: String = "755771767239-uhtf0nttaosh42kcs3g90hmq9enqj0q1.apps.googleusercontent.com",
  val autoLoginWithGoogleEnabled: Boolean = true
)
