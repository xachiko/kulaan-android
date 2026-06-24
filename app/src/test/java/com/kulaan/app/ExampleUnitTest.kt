package com.kulaan.app

import com.kulaan.app.utils.StoreUtils
import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar
import java.util.TimeZone
import com.kulaan.app.data.network.RetrofitInstance
import kotlinx.coroutines.runBlocking


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

    @Test
    fun testApiCategoriesAndProducts() {
        try {
            val categoriesUrl = java.net.URL("http://127.0.0.1:8000/api/categories")
            val categoriesJson = categoriesUrl.readText()
            println("CATEGORIES RESPONSE: $categoriesJson")

            val productsUrl = java.net.URL("http://127.0.0.1:8000/api/products")
            val productsJson = productsUrl.readText()
            println("PRODUCTS RESPONSE: $productsJson")
        } catch (e: Exception) {
            println("API TEST FAILED: ${e.message}")
            e.printStackTrace()
        }
    }

    @Test
    fun testRetrofitGetProductsCategory2() {
        kotlinx.coroutines.runBlocking {
            try {
                // Let's call the api directly
                val response = RetrofitInstance.api.getProducts(category = 2)
                println("RETROFIT RESPONSE SUCCESS: ${response.isSuccessful}")
                println("RETROFIT RESPONSE CODE: ${response.code()}")
                val body = response.body()
                println("RETROFIT RESPONSE BODY: $body")
                if (body != null) {
                    println("RETROFIT PRODUCTS COUNT: ${body.data.size}")
                    for (p in body.data) {
                        println("  Product: ${p.name} | Category: ${p.category?.nameCategory}")
                    }
                }
            } catch (e: Exception) {
                println("RETROFIT TEST FAILED: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
