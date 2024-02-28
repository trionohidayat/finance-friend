package com.koderion.financeapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat

@Entity(tableName = "table_savings")
data class Savings(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val type: String,
    var amount: Int,
)