package com.kulaan.app.utils

import java.util.Calendar
import java.util.TimeZone

object StoreUtils {

    fun isStoreOpen(operatingHours: String?): Boolean {
        if (operatingHours.isNullOrBlank()) return true

        try {
            // Normalize spaces and dashes
            val normalized = operatingHours.replace(Regex("\\s+"), " ")
                .replace("–", "-")
                .replace("—", "-")
                .trim()

            // 1. Parse time range using Regex
            val timeRegex = Regex("(\\d{1,2})[:.](\\d{2})")
            val matches = timeRegex.findAll(normalized).toList()
            if (matches.size < 2) {
                return true // default to open if times cannot be parsed
            }

            val openHour = matches[0].groupValues[1].toInt()
            val openMinute = matches[0].groupValues[2].toInt()
            val closeHour = matches[1].groupValues[1].toInt()
            val closeMinute = matches[1].groupValues[2].toInt()

            // 2. Parse day range / day list
            val parts = normalized.split(Regex("[,:]"), 2)
            val daysPart = if (parts.isNotEmpty()) parts[0].trim().lowercase() else normalized.lowercase()

            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val weekdayMapping = mapOf(
                "senin" to "sen", "sen" to "sen",
                "selasa" to "sel", "sel" to "sel",
                "rabu" to "rab", "rab" to "rab",
                "kamis" to "kam", "kam" to "kam",
                "jumat" to "jum", "jum'at" to "jum", "jum" to "jum",
                "sabtu" to "sab", "sab" to "sab",
                "minggu" to "min", "min" to "min", "ming" to "min"
            )

            val calendarDayMap = mapOf(
                Calendar.MONDAY to "sen",
                Calendar.TUESDAY to "sel",
                Calendar.WEDNESDAY to "rab",
                Calendar.THURSDAY to "kam",
                Calendar.FRIDAY to "jum",
                Calendar.SATURDAY to "sab",
                Calendar.SUNDAY to "min"
            )

            val currentDayCode = calendarDayMap[currentDayOfWeek] ?: ""

            val weekdayOrder = listOf("sen", "sel", "rab", "kam", "jum", "sab", "min")

            var openEveryDay = false
            val openDays = mutableListOf<String>()

            if (daysPart.contains("setiap hari") || daysPart.contains("setiap-hari")) {
                openEveryDay = true
            } else {
                // Match range pattern like "senin - sabtu" or "senin-sabtu"
                val rangeRegex = Regex("(senin|selasa|rabu|kamis|jumat|sabtu|minggu|sen|sel|rab|kam|jum|sab|min)\\s*-\\s*(senin|selasa|rabu|kamis|jumat|sabtu|minggu|sen|sel|rab|kam|jum|sab|min)")
                val rangeMatch = rangeRegex.find(daysPart)
                if (rangeMatch != null) {
                    val startDayName = rangeMatch.groupValues[1]
                    val endDayName = rangeMatch.groupValues[2]
                    val startKey = weekdayMapping[startDayName]
                    val endKey = weekdayMapping[endDayName]
                    if (startKey != null && endKey != null) {
                        val startIndex = weekdayOrder.indexOf(startKey)
                        val endIndex = weekdayOrder.indexOf(endKey)
                        if (startIndex != -1 && endIndex != -1) {
                            var i = startIndex
                            while (true) {
                                openDays.add(weekdayOrder[i])
                                if (i == endIndex) break
                                i = (i + 1) % 7
                            }
                        }
                    }
                } else {
                    // Parse individual days separated by space, comma, slash
                    val words = daysPart.split(Regex("[\\s,/;]+"))
                    for (w in words) {
                        val cleanWord = w.replace(Regex("[^a-z']"), "")
                        val dayVal = weekdayMapping[cleanWord]
                        if (dayVal != null) {
                            if (!openDays.contains(dayVal)) {
                                openDays.add(dayVal)
                            }
                        }
                    }
                }
            }

            if (!openEveryDay) {
                if (!openDays.contains(currentDayCode)) {
                    return false
                }
            }

            // 3. Time comparison
            val currentMinutesTotal = currentHour * 60 + currentMinute
            val openMinutesTotal = openHour * 60 + openMinute
            val closeMinutesTotal = closeHour * 60 + closeMinute

            if (openMinutesTotal == closeMinutesTotal) {
                return true // 00:00 - 00:00 or same times means open 24 hours
            }

            return if (openMinutesTotal <= closeMinutesTotal) {
                currentMinutesTotal in openMinutesTotal..closeMinutesTotal
            } else {
                currentMinutesTotal >= openMinutesTotal || currentMinutesTotal <= closeMinutesTotal
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return true // Fallback to open
        }
    }
}
