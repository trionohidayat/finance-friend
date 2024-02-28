package com.koderion.financeapp

import java.text.DecimalFormat

fun formatRupiah(amount: Int): String {
    val formatter = DecimalFormat("#,###")
    return "Rp " + formatter.format(amount).replace(",", ".").replace("-","")
}
