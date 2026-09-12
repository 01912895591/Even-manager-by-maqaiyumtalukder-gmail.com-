package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.repository.EventRepository
import com.example.ui.screens.PasswordStrengthLevel
import com.example.ui.screens.evaluatePasswordStrength
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthFlowTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: EventRepository

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = EventRepository(db)
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun testPasswordStrengthEvaluation() {
    assertEquals(PasswordStrengthLevel.NONE, evaluatePasswordStrength(""))
    assertEquals(PasswordStrengthLevel.WEAK, evaluatePasswordStrength("short"))
    assertEquals(PasswordStrengthLevel.WEAK, evaluatePasswordStrength("toolongwithoutnumbers"))
    assertEquals(PasswordStrengthLevel.MEDIUM, evaluatePasswordStrength("pass12345"))
    assertEquals(PasswordStrengthLevel.STRONG, evaluatePasswordStrength("Pass@12345!"))
  }

  @Test
  fun testFullEndToEndAuthFlow() = runBlocking {
    val testName = "Alex Morgan"
    val testEmail = "alex.morgan@test.com"
    val initialPassword = "Password123"
    val newPassword = "NewSecret2026"

    // 1. Sign Up with email/password
    val (signUpSuccess, signUpError) = repository.registerUser(testName, testEmail, initialPassword)
    assertTrue("Sign up should succeed", signUpSuccess)
    assertEquals(null, signUpError)

    val registeredUser = repository.getUserByEmail(testEmail)
    assertNotNull(registeredUser)
    assertEquals(testName, registeredUser?.name)
    assertEquals(testEmail, registeredUser?.email)
    assertEquals("email", registeredUser?.authProvider)
    assertTrue(registeredUser?.isLoggedIn == true)

    // 2. Sign Out
    repository.logOutUser()
    val afterLogoutUser = repository.getUserByEmail(testEmail)
    assertFalse(afterLogoutUser?.isLoggedIn == true)

    // 3. Sign In with wrong credentials (should fail)
    val (wrongLoginSuccess, wrongLoginError) = repository.loginUser(testEmail, "WrongPassword999")
    assertFalse("Login with wrong password must fail", wrongLoginSuccess)
    assertNotNull(wrongLoginError)

    // Sign In with correct credentials (should succeed)
    val (correctLoginSuccess, correctLoginError) = repository.loginUser(testEmail, initialPassword)
    assertTrue("Login with correct password must succeed", correctLoginSuccess)
    assertEquals(null, correctLoginError)

    // Sign Out again before testing password reset
    repository.logOutUser()

    // 4. Trigger Forgot Password flow
    // Non-existent email should fail verification
    val (verifyUnknownSuccess, _) = repository.verifyEmailForReset("unknown@test.com")
    assertFalse("Unknown email should fail verification", verifyUnknownSuccess)

    // Real email should succeed verification
    val (verifyEmailSuccess, _) = repository.verifyEmailForReset(testEmail)
    assertTrue("Existing email should be verified for reset", verifyEmailSuccess)

    // Reset password
    val (resetSuccess, resetError) = repository.resetPassword(testEmail, newPassword)
    assertTrue("Password reset should succeed", resetSuccess)
    assertEquals(null, resetError)

    // Old password should now fail
    val (oldPassLoginSuccess, _) = repository.loginUser(testEmail, initialPassword)
    assertFalse("Old password should no longer work", oldPassLoginSuccess)

    // New password should succeed
    val (newPassLoginSuccess, newPassLoginError) = repository.loginUser(testEmail, newPassword)
    assertTrue("New password should log the user in successfully", newPassLoginSuccess)
    assertEquals(null, newPassLoginError)

    // 5. Attempt Real Google Sign-In
    val googleEmail = "alex.google@gmail.com"
    val googleName = "Alex Google"
    val googleUser = repository.loginWithGoogle(googleName, googleEmail)

    assertNotNull(googleUser)
    assertEquals(googleEmail, googleUser.email)
    assertEquals(googleName, googleUser.name)
    assertEquals("google", googleUser.authProvider)
    assertTrue(googleUser.isLoggedIn)

    val dbGoogleUser = repository.getUserByEmail(googleEmail)
    assertNotNull(dbGoogleUser)
    assertEquals("google", dbGoogleUser?.authProvider)
    assertTrue(dbGoogleUser?.isLoggedIn == true)
  }

  @Test
  fun testZeroDemoDataOnInit() = runBlocking {
    AppDatabase.populateInitialData(db)

    // Verify 0 events, 0 contacts, 0 expenses
    assertEquals(0, db.eventDao().getAllEvents().first().size)
    assertEquals(0, db.contactDao().getAllContacts().first().size)
    assertEquals(0, db.expenseDao().getAllExpenses().first().size)

    // Settings should still exist
    val settings = db.appSettingsDao().getSettings().first()
    assertNotNull(settings)
    assertEquals("৳", settings?.defaultCurrency)
  }
}
