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
