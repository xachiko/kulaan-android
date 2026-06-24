package com.kulaan.app.utils

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.NumberFormat
import java.util.Locale

fun Double.toRupiah(): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return fmt.format(this).replace(",00", "")
}

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }

fun Fragment.toast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
}

fun String.formatWhatsApp(): String {
    var cleaned = this.replace("[^0-9]".toRegex(), "")
    if (cleaned.startsWith("0")) {
        cleaned = "62" + cleaned.substring(1)
    }
    return cleaned
}

fun String?.toFullImageUrl(): String? {
    if (this == null || this.isBlank()) return null
    var url = this.trim()
    if (url.startsWith("http://") || url.startsWith("https://")) {
        url = url.replace("127.0.0.1:8000", "10.0.2.2:8000")
        url = url.replace("localhost:8000", "10.0.2.2:8000")
        url = url.replace("localhost", "10.0.2.2")
        return url
    }
    val base = com.kulaan.app.BuildConfig.BASE_URL
    val host = try {
        val uri = java.net.URI(base)
        "${uri.scheme}://${uri.authority}"
    } catch (e: Exception) {
        "http://10.0.2.2:8000"
    }
    val pathWithStorage = if (url.startsWith("storage/")) {
        url
    } else if (url.startsWith("/storage/")) {
        url.substring(1)
    } else {
        "storage/${url.removePrefix("/")}"
    }
    return "$host/$pathWithStorage"
}

fun getProductPlaceholderEmoji(categoryName: String?, productId: Int): String {
    val cleanCategory = categoryName?.lowercase() ?: ""
    val emojis = when {
        cleanCategory.contains("makanan") || cleanCategory.contains("minuman") || cleanCategory.contains("katering") || cleanCategory.contains("sembako") -> {
            listOf("🍱", "🍛", "🧆", "🍲", "🥙", "🍗", "🥟", "🌿", "🍚", "🥘", "🍜", "🥗", "🍣")
        }
        cleanCategory.contains("kerajinan") -> {
            listOf("🧶", "🎨", "🏺", "🪁", "🧵", "🪡", "👜", "🪵", "🧱", "🏮")
        }
        cleanCategory.contains("fashion") || cleanCategory.contains("batik") || cleanCategory.contains("pakaian") || cleanCategory.contains("aksesoris") -> {
            listOf("👕", "👖", "👔", "👗", "👘", "👟", "👜", "🧢", "🕶️", "🧣")
        }
        cleanCategory.contains("kecantikan") || cleanCategory.contains("perawatan") -> {
            listOf("💄", "💅", "💆", "🧖", "🧴", "🧼", "💈", "💋", "🌸", "🎐")
        }
        cleanCategory.contains("pertanian") || cleanCategory.contains("tani") || cleanCategory.contains("pupuk") -> {
            listOf("🌱", "🌾", "🥕", "🍅", "🥦", "🌽", "🍎", "🌻", "🌿", "🚜")
        }
        else -> {
            listOf("📦", "🏷️", "✨", "🎁", "🛍️")
        }
    }
    return emojis[productId % emojis.size]
}

fun formatCategoryName(name: String?): String {
    if (name == null) return "Produk"
    val clean = name.lowercase()
    return when {
        clean == "makanan_minuman" || clean == "makanan & minuman" -> "Makanan & Minuman"
        clean == "fashion_batik" || clean == "fashion & batik" -> "Fashion & Batik"
        clean == "kerajinan" || clean == "kerajinan tangan" -> "Kerajinan Tangan"
        clean == "kecantikan" || clean == "perawatan & kecantikan" -> "Kecantikan"
        clean == "pertanian" -> "Pertanian"
        else -> name.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}


