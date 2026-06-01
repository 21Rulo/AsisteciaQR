package com.sssl.asisteciaqr.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import com.sssl.asisteciaqr.R

object SoundManager {

    private const val PREFS_NAME = "sound_preferences"
    private const val KEY_SUCCESS_SOUND = "success_sound"
    private const val KEY_ERROR_SOUND = "error_sound"
    private const val KEY_WARNING_SOUND = "warning_sound"

    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null

    // ==================== TIPOS DE SONIDO ====================

    /**
     * Cada sonido puede ser:
     * - TONE: generado por ToneGenerator (sin archivos)
     * - RAW:  archivo en res/raw/ (personalizado)
     */
    sealed class SoundSource {
        data class Tone(val toneType: Int, val duration: Int) : SoundSource()
        data class Raw(val resId: Int) : SoundSource()
        data class Sequence(val steps: List<Pair<Int, Int>>) : SoundSource() // Lista de (tono, duración)
    }

    data class SoundOption(
        val id: String,            // Identificador único guardado en preferencias
        val displayName: String,   // Nombre visible al usuario
        val description: String,   // Descripción corta
        val emoji: String,         // Emoji decorativo
        val category: Category,    // A qué categoría aplica
        val source: SoundSource    // Cómo se reproduce
    )

    enum class Category { SUCCESS, ERROR, WARNING, ALL }

    // ==================== CATÁLOGO DE SONIDOS ====================

    /**
     * Sonidos integrados (ToneGenerator, sin archivos externos)
     */
    private val builtInSounds = listOf(

        // ---- ÉXITO ----
        SoundOption(
            id = "beep_short",
            displayName = "Beep Corto",
            description = "Un beep breve y claro",
            emoji = "🔔",
            category = Category.SUCCESS,
            source = SoundSource.Tone(ToneGenerator.TONE_PROP_BEEP, 150)
        ),
        SoundOption(
            id = "beep_long",
            displayName = "Beep Largo",
            description = "Un beep sostenido",
            emoji = "🔊",
            category = Category.SUCCESS,
            source = SoundSource.Tone(ToneGenerator.TONE_PROP_BEEP, 450)
        ),
        SoundOption(
            id = "double_beep",
            displayName = "Doble Beep",
            description = "Dos beeps seguidos",
            emoji = "🔔🔔",
            category = Category.SUCCESS,
            source = SoundSource.Sequence(
                listOf(
                    ToneGenerator.TONE_PROP_BEEP to 120,
                    -1 to 150, // -1 = pausa
                    ToneGenerator.TONE_PROP_BEEP to 120
                )
            )
        ),
        SoundOption(
            id = "chime_up",
            displayName = "Subida",
            description = "Tonos ascendentes",
            emoji = "📈",
            category = Category.SUCCESS,
            source = SoundSource.Sequence(
                listOf(
                    ToneGenerator.TONE_DTMF_3 to 100,
                    -1 to 80,
                    ToneGenerator.TONE_DTMF_6 to 100,
                    -1 to 80,
                    ToneGenerator.TONE_DTMF_9 to 200
                )
            )
        ),
        SoundOption(
            id = "soft_ding",
            displayName = "Ding Suave",
            description = "Tono suave y agradable",
            emoji = "🎵",
            category = Category.SUCCESS,
            source = SoundSource.Tone(ToneGenerator.TONE_PROP_ACK, 200)
        ),

        // ---- ERROR ----
        SoundOption(
            id = "buzz",
            displayName = "Buzz",
            description = "Zumbido de error",
            emoji = "❌",
            category = Category.ERROR,
            source = SoundSource.Tone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 350)
        ),
        SoundOption(
            id = "double_buzz",
            displayName = "Doble Buzz",
            description = "Dos zumbidos seguidos",
            emoji = "❌❌",
            category = Category.ERROR,
            source = SoundSource.Sequence(
                listOf(
                    ToneGenerator.TONE_CDMA_ABBR_ALERT to 200,
                    -1 to 200,
                    ToneGenerator.TONE_CDMA_ABBR_ALERT to 200
                )
            )
        ),
        SoundOption(
            id = "low_beep",
            displayName = "Beep Bajo",
            description = "Tono grave de error",
            emoji = "⬇️",
            category = Category.ERROR,
            source = SoundSource.Tone(ToneGenerator.TONE_PROP_NACK, 350)
        ),

        // ---- ADVERTENCIA ----
        SoundOption(
            id = "notification",
            displayName = "Notificación",
            description = "Tono de notificación",
            emoji = "🔕",
            category = Category.WARNING,
            source = SoundSource.Tone(ToneGenerator.TONE_PROP_PROMPT, 200)
        ),
        SoundOption(
            id = "soft_alert",
            displayName = "Alerta Suave",
            description = "Alerta discreta",
            emoji = "⚠️",
            category = Category.WARNING,
            source = SoundSource.Tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        ),
        SoundOption(
            id = "triple_beep",
            displayName = "Triple Beep",
            description = "Tres beeps cortos",
            emoji = "🔔🔔🔔",
            category = Category.WARNING,
            source = SoundSource.Sequence(
                listOf(
                    ToneGenerator.TONE_PROP_BEEP to 80,
                    -1 to 100,
                    ToneGenerator.TONE_PROP_BEEP to 80,
                    -1 to 100,
                    ToneGenerator.TONE_PROP_BEEP to 80
                )
            )
        )
    )

    /**
     * Genera la lista de sonidos personalizados desde res/raw/
     * Agrega aquí tus archivos de sonido descargados.
     *
     * INSTRUCCIONES:
     * 1. Coloca tu archivo en: app/src/main/res/raw/nombre_sonido.mp3
     * 2. Agrega una entrada aquí con el mismo nombre
     * 3. Sincroniza Gradle y listo
     *
     * EJEMPLO:
     * SoundOption(
     *     id = "custom_campana",
     *     displayName = "Campana",
     *     description = "Campana descargada",
     *     emoji = "🔔",
     *     category = Category.ALL,  // ALL = aparece en éxito, error y advertencia
     *     source = SoundSource.Raw(R.raw.campana)
     * )
     */
    private fun getCustomSounds(context: Context): List<SoundOption> {
        val list = mutableListOf<SoundOption>()

        // ================================================================
        // ZONA DE SONIDOS PERSONALIZADOS
        // Descomenta y modifica cada bloque según tus archivos
        // ================================================================


        list.add(
             SoundOption(
                 id = "custom_success",
                 displayName = "Tienda",
                 description = "Sonido de checador",
                 emoji = "✅",
                 category = Category.SUCCESS,
                 source = SoundSource.Raw(R.raw.success)
             )
         );


        list.add(
             SoundOption(
                 id = "custom_videojuego",
                 displayName = "Videojuego",
                 description = "Sonido de videojuego retro",
                 emoji = "🎮",
                 category = Category.WARNING,
                 source = SoundSource.Raw(R.raw.warning)
             )
         );

        // --- EJEMPLO 3 ---
        // Si tienes: res/raw/error_robot.wav
         list.add(
             SoundOption(
                 id = "custom_robot",
                 displayName = "Fail",
                 description = "Sonido de error",
                 emoji = "🤖",
                 category = Category.ERROR,
                 source = SoundSource.Raw(R.raw.fail)
             )
         )

        // ================================================================
        return list
    }

    /**
     * Retorna todos los sonidos disponibles para una categoría
     */
    fun getSoundsForCategory(context: Context, category: Category): List<SoundOption> {
        val custom = getCustomSounds(context).filter {
            it.category == category || it.category == Category.ALL
        }
        val builtin = builtInSounds.filter { it.category == category }
        return builtin + custom
    }

    // ==================== PREFERENCIAS ====================

    private fun getPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSuccessSound(context: Context): SoundOption {
        val id = getPreferences(context).getString(KEY_SUCCESS_SOUND, "beep_short")
        return findSoundById(context, id ?: "beep_short")
            ?: builtInSounds.first { it.id == "beep_short" }
    }

    fun getErrorSound(context: Context): SoundOption {
        val id = getPreferences(context).getString(KEY_ERROR_SOUND, "buzz")
        return findSoundById(context, id ?: "buzz")
            ?: builtInSounds.first { it.id == "buzz" }
    }

    fun getWarningSound(context: Context): SoundOption {
        val id = getPreferences(context).getString(KEY_WARNING_SOUND, "notification")
        return findSoundById(context, id ?: "notification")
            ?: builtInSounds.first { it.id == "notification" }
    }

    private fun findSoundById(context: Context, id: String): SoundOption? {
        return builtInSounds.firstOrNull { it.id == id }
            ?: getCustomSounds(context).firstOrNull { it.id == id }
    }

    fun setSuccessSound(context: Context, sound: SoundOption) {
        getPreferences(context).edit().putString(KEY_SUCCESS_SOUND, sound.id).apply()
    }

    fun setErrorSound(context: Context, sound: SoundOption) {
        getPreferences(context).edit().putString(KEY_ERROR_SOUND, sound.id).apply()
    }

    fun setWarningSound(context: Context, sound: SoundOption) {
        getPreferences(context).edit().putString(KEY_WARNING_SOUND, sound.id).apply()
    }

    // ==================== REPRODUCCIÓN ====================

    fun playSuccessSound(context: Context) = playSound(context, getSuccessSound(context))

    fun playErrorSound(context: Context) = playSound(context, getErrorSound(context))

    fun playWarningSound(context: Context) = playSound(context, getWarningSound(context))

    fun playSound(context: Context, sound: SoundOption) {
        try {
            release()
            when (val src = sound.source) {

                // Archivo en res/raw/
                is SoundSource.Raw -> {
                    mediaPlayer = MediaPlayer.create(context, src.resId)
                    mediaPlayer?.start()
                    mediaPlayer?.setOnCompletionListener { it.release() }
                }

                // Tono simple
                is SoundSource.Tone -> {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGenerator?.startTone(src.toneType, src.duration)
                }

                // Secuencia de tonos con pausas
                is SoundSource.Sequence -> {
                    Thread {
                        val gen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                        toneGenerator = gen
                        for ((tone, duration) in src.steps) {
                            if (tone == -1) {
                                Thread.sleep(duration.toLong())
                            } else {
                                gen.startTone(tone, duration)
                                Thread.sleep(duration.toLong())
                            }
                        }
                        gen.release()
                        toneGenerator = null
                    }.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}