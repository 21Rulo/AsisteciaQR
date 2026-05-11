package com.sssl.asisteciaqr.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PasswordManager {

    private const val PREFS_NAME = "profesor_credentials"
    private const val KEY_PASSWORD = "password"
    private const val KEY_PIN = "recovery_pin"
    private const val DEFAULT_PASSWORD = "profesor123"

    private fun getPreferences(context: Context): SharedPreferences {
        return try {
            // Usar EncryptedSharedPreferences para mayor seguridad
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback a SharedPreferences normal si falla EncryptedSharedPreferences
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Obtiene la contraseña actual
     */
    fun getPassword(context: Context): String {
        return getPreferences(context).getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    /**
     * Guarda una nueva contraseña
     */
    fun setPassword(context: Context, newPassword: String): Boolean {
        return try {
            getPreferences(context).edit().apply {
                putString(KEY_PASSWORD, newPassword)
                apply()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si la contraseña es correcta
     */
    fun verifyPassword(context: Context, password: String): Boolean {
        return password == getPassword(context)
    }

    /**
     * Obtiene el PIN de recuperación
     */
    fun getRecoveryPin(context: Context): String? {
        return getPreferences(context).getString(KEY_PIN, null)
    }

    /**
     * Guarda el PIN de recuperación (4 dígitos)
     */
    fun setRecoveryPin(context: Context, pin: String): Boolean {
        return try {
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                return false
            }
            getPreferences(context).edit().apply {
                putString(KEY_PIN, pin)
                apply()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si el PIN de recuperación es correcto
     */
    fun verifyRecoveryPin(context: Context, pin: String): Boolean {
        val savedPin = getRecoveryPin(context)
        return savedPin != null && savedPin == pin
    }

    /**
     * Verifica si tiene PIN de recuperación configurado
     */
    fun hasRecoveryPin(context: Context): Boolean {
        return getRecoveryPin(context) != null
    }

    /**
     * Resetea la contraseña a la predeterminada (requiere confirmación)
     */
    fun resetToDefault(context: Context): Boolean {
        return try {
            getPreferences(context).edit().apply {
                putString(KEY_PASSWORD, DEFAULT_PASSWORD)
                remove(KEY_PIN) // También elimina el PIN
                apply()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si está usando la contraseña predeterminada
     */
    fun isUsingDefaultPassword(context: Context): Boolean {
        return getPassword(context) == DEFAULT_PASSWORD
    }
}