package com.koderion.financeapp

import androidx.room.*

@Dao
interface SavingsDao {

    @Update
    suspend fun updateSavings(savings: Savings)

    @Insert
    suspend fun insertSaving(savings: Savings)

    @Query("SELECT * FROM table_savings WHERE userEmail = :email AND type = :type")
    fun getSavingsByEmailAndType(email: String, type: String): Savings?

    @Query("UPDATE table_savings SET amount = :amount WHERE userEmail = :email AND type = :type")
    suspend fun updateAmount(email: String, type: String, amount: Int)

    @Query("SELECT * FROM table_savings WHERE userEmail = :email AND amount > 0")
    suspend fun getExpanse(email: String): List<Savings>

    @Query("SELECT * FROM table_savings WHERE userEmail = :email")
    suspend fun getIncome(email: String): List<Savings>

    @Query("UPDATE table_savings SET amount = amount - :expenseAmount WHERE userEmail = :userEmail AND type = 'Wallet'")
    suspend fun updateWalletExpenseSavings(userEmail: String, expenseAmount: Int)

    @Query("UPDATE table_savings SET amount = amount + :incomeAmount WHERE userEmail = :userEmail AND type = 'Wallet'")
    suspend fun updateWalletIncomeSavings(userEmail: String, incomeAmount: Int)

    @Query("UPDATE table_savings SET amount = amount - :expenseAmount WHERE userEmail = :userEmail AND type = 'Digital'")
    suspend fun updateDigitalExpenseSavings(userEmail: String, expenseAmount: Int)

    @Query("UPDATE table_savings SET amount = amount + :incomeAmount WHERE userEmail = :userEmail AND type = 'Digital'")
    suspend fun updateDigitalIncomeSavings(userEmail: String, incomeAmount: Int)

    @Query("UPDATE table_savings SET amount = amount - :expenseAmount WHERE userEmail = :userEmail AND type = 'Bank'")
    suspend fun updateBankExpenseSavings(userEmail: String, expenseAmount: Int)

    @Query("UPDATE table_savings SET amount = amount + :incomeAmount WHERE userEmail = :userEmail AND type = 'Bank'")
    suspend fun updateBankIncomeSavings(userEmail: String, incomeAmount: Int)
}