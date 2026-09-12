package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.ChecklistDao
import com.example.data.dao.ContactDao
import com.example.data.dao.EventContactDao
import com.example.data.dao.EventDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.UserDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.ContactEntity
import com.example.data.model.EventContactCrossRef
import com.example.data.model.EventEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    EventEntity::class,
    ChecklistItemEntity::class,
    ExpenseEntity::class,
    ContactEntity::class,
    EventContactCrossRef::class,
    AppSettingsEntity::class
  ],
  version = 6,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun eventDao(): EventDao
  abstract fun checklistDao(): ChecklistDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun contactDao(): ContactDao
  abstract fun eventContactDao(): EventContactDao
  abstract fun appSettingsDao(): AppSettingsDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "event_manager_database"
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            try {
              populateInitialData(database)
            } catch (e: Exception) {
              // Graceful failure prevention
            }
          }
        }
      }
    }

    suspend fun populateInitialData(db: AppDatabase) {
      // Clean start: NO demo events, contacts, checklists, or expenses.
      // Only default application settings are initialized if not existing.
      try {
        val current = db.appSettingsDao().getSettings().firstOrNull()
        if (current == null) {
          db.appSettingsDao().saveSettings(
            AppSettingsEntity(
              id = 1,
              language = "en",
              isDarkMode = false,
              accentColorHex = "#D4AF6A",
              defaultCurrency = "৳",
              notificationsEnabled = true
            )
          )
        }
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }
}
