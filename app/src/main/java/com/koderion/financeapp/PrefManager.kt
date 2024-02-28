package com.koderion.financeapp

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {

    private val PRIVATE_MODE = 0

    private val PREF_NAME = "SharedPreferences"

    private val IS_LOGIN = "is_login"

    var pref: SharedPreferences? = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    var editor: SharedPreferences.Editor? = pref?.edit()

    fun setLogin(isLogin: Boolean) {
        editor?.putBoolean(IS_LOGIN, isLogin)
        editor?.commit()
    }

    fun isLogin(): Boolean? {
        return pref?.getBoolean(IS_LOGIN, false)
    }

    fun setEmail(email: String) {
        editor?.putString("email", email)
        editor?.commit()
    }

    fun getEmail(): String? {
        return pref?.getString("email", "")
    }

    fun logout() {
        editor?.clear()
        editor?.commit()
    }
}