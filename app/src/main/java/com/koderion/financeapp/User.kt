package com.koderion.financeapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_user")
data class User(

    var name: String,
    @PrimaryKey
    val email: String,
    val password: String
)