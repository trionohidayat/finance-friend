package com.koderion.financeapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_transaction")
data class Transaction(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val savings: String,
    val type: String,
    val date: String,
    val amount: Int,
    val note: String,
)