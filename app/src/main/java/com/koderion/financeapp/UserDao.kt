package com.koderion.financeapp

import androidx.room.*

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User):Long

    @Query("SELECT * FROM table_user")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM table_user WHERE email = :email")
    fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM table_user WHERE email = :email AND password = :password")
    fun getUserByEmailAndPassword(email: String, password: String): User?

    @Update
    suspend fun updateUser(user: User)
}