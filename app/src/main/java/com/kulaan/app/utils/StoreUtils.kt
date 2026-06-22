package com.kulaan.app.utils

import java.util.Calendar
import java.util.TimeZone

object StoreUtils {

    fun isStoreOpen(operatingHours: String?): Boolean {
        if (operatingHours.isNullOrBlank()) return false

        // Example formats: "Senin - Sabtu, 08:00 - 17:00" or "Setiap hari, 09:00 - 21:00"
        try {
            val parts = operatingHours.split(",")
            if (parts.size < 2) return true // Cannot parse format, assume open to be safe or false depending on business logic

            val daysPart = parts[0].trim().lowercase()
            val timesPart = parts[1].trim()

            val timeRange = timesPart.split("-").map { it.trim() }
            if (timeRange.size != 2) return true

            val openTimeStr = timeRange[0]
            val closeTimeStr = timeRange[1]

            val openTimeParts = openTimeStr.split(":")
            val closeTimeParts = closeTimeStr.split(":")

            if (openTimeParts.size != 2 || closeTimeParts.size != 2) return true

            val openHour = openTimeParts[0].toIntOrNull() ?: 0
            val openMinute = openTimeParts[1].toIntOrNull() ?: 0
            val closeHour = closeTimeParts[0].toIntOrNull() ?: 0
            val closeMinute = closeTimeParts[1].toIntOrNull() ?: 0

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ...
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            // Map current day to Indonesian string or check ranges
            val dayMap = mapOf(
                Calendar.MONDAY to "senin",
                Calendar.TUESDAY to "selasa",
                Calendar.WEDNESDAY to "rabu",
                Calendar.THURSDAY to "kamis",
                Calendar.FRIDAY to "jumat",
                Calendar.SATURDAY to "sabtu",
                Calendar.SUNDAY to "minggu"
            )
            
            val currentDayString = dayMap[currentDayOfWeek] ?: ""

            val isOpenDay = if (daysPart.contains("setiap hari")) {
                true
            } else if (daysPart.contains("-")) {
                val dayRange = daysPart.split("-").map { it.trim() }
                if (dayRange.size == 2) {
                    val startDayStr = dayRange[0]
                    val endDayStr = dayRange[1]
                    
                    val daysList = listOf("senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "minggu")
                    val startIndex = daysList.indexOf(startDayStr)
                    val endIndex = daysList.indexOf(endDayStr)
                    val currentIndex = daysList.indexOf(currentDayString)
                    
                    if (startIndex != -1 && endIndex != -1 && currentIndex != -1) {
                        currentIndex in startIndex..endIndex
                    } else {
                        true // fallback
                    }
                } else {
                    true
                }
            } else {
                daysPart.contains(currentDayString)
            }

            if (!isOpenDay) return false

            val currentMinutesTotal = currentHour * 60 + currentMinute
            val openMinutesTotal = openHour * 60 + openMinute
            val closeMinutesTotal = closeHour * 60 + closeMinute

            return currentMinutesTotal in openMinutesTotal..closeMinutesTotal

        } catch (e: Exception) {
            e.printStackTrace()
            return true // Fallback to true if parsing fails
        }
    }
}
