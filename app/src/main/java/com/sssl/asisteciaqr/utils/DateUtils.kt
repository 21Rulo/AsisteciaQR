package com.sssl.asisteciaqr.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm:ss"
    private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val timeFormatter = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormatter.format(Date())
    }

    fun getCurrentTime(): String {
        return timeFormatter.format(Date())
    }

    fun getCurrentDateTime(): String {
        return dateTimeFormatter.format(Date())
    }

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormatter.format(Date(timestamp))
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = dateFormatter.parse(dateString)
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatTimeForDisplay(timeString: String): String {
        return try {
            val time = timeFormatter.parse(timeString)
            val displayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            displayFormat.format(time ?: Date())
        } catch (e: Exception) {
            timeString
        }
    }
}