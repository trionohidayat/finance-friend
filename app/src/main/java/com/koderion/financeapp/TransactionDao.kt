package com.koderion.financeapp

import androidx.room.*

@Dao
interface TransactionDao {

    @Query("SELECT * FROM table_transaction")
    fun getAll(): List<Transaction>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

}