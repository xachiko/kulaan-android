package com.kulaan.app

import com.kulaan.app.utils.StoreUtils
import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {
    @Test
    fun testStoreUtils_isStoreOpen() {
        // Test 1: Standard format with en-dash: "Senin–Sabtu, 07:00 – 20:00"
        val hours1 = "Senin–Sabtu, 07:00 – 20:00"
        
        // We cannot easily mock Calendar.getInstance() time without a library,
        // but we can manually verify the parsing logic by printing test outputs or
        // writing custom check function if we parameterize isStoreOpen with time,
        // or we can test if isStoreOpen returns true when operatingHours is null.
        assertTrue(StoreUtils.isStoreOpen(null))
        assertTrue(StoreUtils.isStoreOpen(""))
        
        // Let's do a basic check.
        // Today is Tuesday (Selasa). If current time on host machine is between 7:00 and 20:00,
        // the store "Senin–Sabtu, 07:00 – 20:00" should return true, otherwise false.
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        
        val expectedOpen = (currentDay in Calendar.MONDAY..Calendar.SATURDAY) && (currentHour in 7..19)
        val actualOpen = StoreUtils.isStoreOpen(hours1)
        
        println("Testing hours: $hours1 | Current Hour in Jakarta: $currentHour | Expected open: $expectedOpen | Actual open: $actualOpen")
        assertEquals(expectedOpen, actualOpen)
    }
}