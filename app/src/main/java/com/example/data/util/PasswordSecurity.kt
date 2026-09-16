package com.example.data.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility for cryptographically secure password hashing and verification using SHA-256 with per-user salt.
 * Ensures raw passwords are never stored in the database or included in backups.
 */
object PasswordSecurity {
  private const val SALT_BYTES = 16

  /**
   * Generates a random cryptographic salt.
   */
  fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(SALT_BYTES)
    random.nextBytes(salt)
    return bytesToHex(salt)
  }

  /**
   * Computes SHA-256(salt + password).
   */
  fun hashPassword(password: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val saltBytes = hexToBytes(salt)
    digest.update(saltBytes)
    val hashedBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
    return bytesToHex(hashedBytes)
  }

  /**
   * Verifies an entered password against the stored salt and hash in constant time.
   */
  fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
    if (expectedHash.isEmpty()) return false
    val calculatedHash = hashPassword(password, salt)
    return MessageDigest.isEqual(
      calculatedHash.toByteArray(Charsets.UTF_8),
      expectedHash.toByteArray(Charsets.UTF_8)
    )
  }

  private fun bytesToHex(bytes: ByteArray): String {
    return bytes.joinToString("") { "%02x".format(it) }
  }

  private fun hexToBytes(hex: String): ByteArray {
    if (hex.length % 2 != 0) {
      return hex.toByteArray(Charsets.UTF_8)
    }
    return ByteArray(hex.length / 2) { i ->
      hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
  }
}
