package com.example.data.auth

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GoogleUserInfo(
  val idToken: String?,
  val email: String,
  val displayName: String,
  val photoUrl: String? = null
)

object GoogleAuthService {
  private const val TAG = "GoogleAuthService"
  private const val PLACEHOLDER_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID"
  const val DEFAULT_WEB_CLIENT_ID = "755771767239-uhtf0nttaosh42kcs3g90hmq9enqj0q1.apps.googleusercontent.com"

  /**
   * Resolves the active Google OAuth Web Client ID.
   * Checks custom user setting first, then BuildConfig (from .env), or the configured default.
   */
  fun getActiveClientId(customClientId: String?): String? {
    val trimmedCustom = customClientId?.trim()
    if (!trimmedCustom.isNullOrBlank() && trimmedCustom != PLACEHOLDER_CLIENT_ID) {
      return trimmedCustom
    }
    val buildConfigId = try {
      BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
    } catch (e: Throwable) {
      ""
    }
    if (buildConfigId.isNotBlank() && buildConfigId != PLACEHOLDER_CLIENT_ID) {
      return buildConfigId
    }
    return DEFAULT_WEB_CLIENT_ID
  }

  /**
   * Attempts silent auto-login via modern Android Credential Manager.
   * If the user previously authorized an account or has a single Google Account available with auto-select,
   * Google Credential Manager logs them in automatically without prompting.
   */
  suspend fun attemptAutoLogin(
    activity: Activity,
    clientId: String?
  ): GoogleUserInfo? = withContext(Dispatchers.IO) {
    val activeId = getActiveClientId(clientId)
    if (activeId.isNullOrBlank()) {
      Log.d(TAG, "Auto-login skipped: No Google Web Client ID configured.")
      return@withContext null
    }

    try {
      val credentialManager = CredentialManager.create(activity)
      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(activeId)
        .setAutoSelectEnabled(true)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val response = credentialManager.getCredential(
        request = request,
        context = activity
      )

      val credential = response.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val email = googleIdTokenCredential.id
        val name = googleIdTokenCredential.displayName ?: email.substringBefore("@")
          .replace(".", " ")
          .split(" ")
          .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        val photo = googleIdTokenCredential.profilePictureUri?.toString()
        val token = googleIdTokenCredential.idToken

        Log.i(TAG, "Auto-login successful for $email")
        return@withContext GoogleUserInfo(
          idToken = token,
          email = email,
          displayName = name,
          photoUrl = photo
        )
      }
    } catch (e: NoCredentialException) {
      Log.d(TAG, "No auto-login credential available: ${e.message}")
    } catch (e: GetCredentialCancellationException) {
      Log.d(TAG, "Auto-login cancelled by user.")
    } catch (e: Exception) {
      Log.w(TAG, "Auto-login attempt encountered: ${e.message}")
    }
    return@withContext null
  }

  /**
   * Executes interactive Google Sign-In.
   * AutoSelect MUST be disabled during interactive sign-in so Google's Credential Manager
   * does not prematurely fail with "Something went wrong / Sign in another way" when credentials
   * require user confirmation.
   */
  suspend fun signInWithGoogleCredentialManager(
    activity: Activity,
    clientId: String
  ): Result<GoogleUserInfo> = withContext(Dispatchers.IO) {
    val pkgName = activity.packageName
    Log.d(TAG, "Initiating Google Sign-In for $pkgName")

    try {
      val credentialManager = CredentialManager.create(activity)
      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(false) // Must be false for interactive button clicks
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val response = credentialManager.getCredential(
        request = request,
        context = activity
      )

      val credential = response.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val email = googleIdTokenCredential.id
        val name = googleIdTokenCredential.displayName ?: email.substringBefore("@")
          .replace(".", " ")
          .split(" ")
          .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        val photo = googleIdTokenCredential.profilePictureUri?.toString()
        val token = googleIdTokenCredential.idToken

        Log.i(TAG, "Google Sign-In successful for $email")

        return@withContext Result.success(
          GoogleUserInfo(
            idToken = token,
            email = email,
            displayName = name,
            photoUrl = photo
          )
        )
      } else {
        val receivedType = (credential as? CustomCredential)?.type ?: credential.javaClass.simpleName
        Log.w(TAG, "Received unsupported credential type: $receivedType")
        return@withContext Result.failure(Exception("Unsupported credential type received from Google ($receivedType)."))
      }
    } catch (e: GetCredentialCancellationException) {
      Log.d(TAG, "User cancelled Google Sign-In")
      return@withContext Result.failure(Exception("Sign-in cancelled by user."))
    } catch (e: NoCredentialException) {
      Log.w(TAG, "No Google Account found on device: ${e.message}")
      return@withContext Result.failure(e)
    } catch (e: GetCredentialException) {
      Log.w(TAG, "CredentialManager error: ${e.type} - ${e.message}")
      return@withContext Result.failure(e)
    } catch (e: Exception) {
      Log.w(TAG, "CredentialManager unexpected exception: ${e.message}")
      return@withContext Result.failure(e)
    }
  }
}
