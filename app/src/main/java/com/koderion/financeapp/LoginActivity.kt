package com.koderion.financeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.koderion.financeapp.databinding.ActivityLoginBinding
import com.koderion.financeapp.databinding.ToolbarBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var toolbarBinding: ToolbarBinding

    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this)

        if (prefManager.isLogin()!!) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        toolbarBinding = ToolbarBinding.bind(findViewById(R.id.toolbar)).apply {
            toolbar.title = "Login"
            setSupportActionBar(toolbar)
            supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        }

        binding.cardLogin.setOnClickListener {
            if (binding.inputEmail.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input your Email", Toast.LENGTH_SHORT).show()
            } else if (binding.inputPassword.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input your password", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString().trim())
                    .matches()
            ) {
                Toast.makeText(this, "Please input valid email", Toast.LENGTH_SHORT).show()
            } else {
                val scope = CoroutineScope(Dispatchers.IO)

                scope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    val user = userDao.getUserByEmail(
                        binding.inputEmail.text.toString()
                    )

                    withContext(Dispatchers.Main) {
                        if (user == null) {
                            Toast.makeText(
                                applicationContext,
                                "Email not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (user.password != binding.inputPassword.text.toString()) {
                            Toast.makeText(
                                applicationContext,
                                "Invalid password",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            prefManager.setLogin(true)
                            prefManager.setEmail(binding.inputEmail.text.toString())
                            startActivity(
                                Intent(applicationContext, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()
                        }
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