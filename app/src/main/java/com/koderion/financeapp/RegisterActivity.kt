package com.koderion.financeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.koderion.financeapp.databinding.ActivityRegisterBinding
import com.koderion.financeapp.databinding.ToolbarBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var toolbarBinding: ToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbarBinding = ToolbarBinding.bind(findViewById(R.id.toolbar)).apply {
            toolbar.title = "Register"
            setSupportActionBar(toolbar)
            supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        }

        binding.cardRegister.setOnClickListener {
            if (binding.inputFullname.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input your name", Toast.LENGTH_SHORT).show()
            } else if (binding.inputEmail.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input your Email", Toast.LENGTH_SHORT).show()
            } else if (binding.inputPassword.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input your password", Toast.LENGTH_SHORT).show()
            } else if (binding.inputCheckPassword.text.toString() != binding.inputPassword.text.toString()) {
                Toast.makeText(this, "Password not match", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString().trim())
                    .matches()
            ) {
                Toast.makeText(this, "Please input valid email", Toast.LENGTH_SHORT).show()
            } else if (binding.inputPassword.text.toString().length < 8) {
                Toast.makeText(this, "Password must be minimal 8 character", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val scope = CoroutineScope(Dispatchers.IO)
                val user = User(
                    binding.inputFullname.text.toString(), binding.inputEmail.text.toString(),
                    binding.inputPassword.text.toString()
                )
                scope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()

                    if (userDao.getAllUsers().contains(user)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Email has been used, please login or try another email",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        userDao.insertUser(user)
                        arrayOf(
                            Savings(userEmail = user.email, type = "Wallet", amount = 0),
                            Savings(userEmail = user.email, type = "Digital", amount = 0),
                            Savings(userEmail = user.email, type = "Bank", amount = 0)
                        ).forEach { savingsDao.insertSaving(it) }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Create account successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        startActivity(
                            Intent(applicationContext, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}