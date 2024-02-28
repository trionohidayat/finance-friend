package com.koderion.financeapp

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.koderion.financeapp.databinding.ActivityMainBinding
import com.koderion.financeapp.databinding.DialogSavingsBinding
import com.koderion.financeapp.databinding.DialogTransactionBinding
import com.koderion.financeapp.databinding.ToolbarBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: ToolbarBinding

    private lateinit var prefManager: PrefManager
    private lateinit var email: String

    private var backPressedTime: Long = 0
    private val backPressedInterval = 2000

    private var transactionSelected = ""
    private var categorySelected = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this)
        email = prefManager.getEmail().toString()

        if (prefManager.isLogin() == false) {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        toolbarBinding = ToolbarBinding.bind(findViewById(R.id.toolbar))
        setSupportActionBar(toolbarBinding.toolbar)

        toolbarBinding.toolbar.title = "Halo ${getUser()?.name}"

        binding.textAmountWallet.text = formatRupiah(getSavings("Wallet")?.amount ?: 0)
        binding.textAmountDigital.text = formatRupiah(getSavings("Digital")?.amount ?: 0)
        binding.textAmountBank.text = formatRupiah(getSavings("Bank")?.amount ?: 0)

        binding.layoutWallet.setOnClickListener { showDialogSavings(1) }

        binding.layoutDigital.setOnClickListener { showDialogSavings(2) }

        binding.layoutBank.setOnClickListener { showDialogSavings(3) }

        updateRecyclerView()

        binding.fabAddTransaction.setOnClickListener {
            val dialogBinding = DialogTransactionBinding.inflate(layoutInflater)
            val dialog = Dialog(this)

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(dialogBinding.root)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT

            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()

                val savingsTypes = savingsDao.getPositiveSavings(email).map { it.type }.distinct()

                withContext(Dispatchers.Main) {
                    val savingsTypeAdapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_list_item_1,
                        savingsTypes
                    )
                    dialogBinding.autoTransaction.setAdapter(savingsTypeAdapter)
                }
            }
            dialogBinding.autoTransaction.setOnItemClickListener { parent, _, position, _ ->
                transactionSelected = parent.getItemAtPosition(position) as String

            }

            val transactionTypes = listOf("Expense", "Income")
            val transactionTypeAdapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, transactionTypes)
            dialogBinding.autoCategory.setAdapter(transactionTypeAdapter)

            dialogBinding.autoCategory.setOnItemClickListener { parent, _, position, _ ->
                categorySelected = parent.getItemAtPosition(position) as String

            }
            dialogBinding.inputDate.setOnClickListener {
                val newCalendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, monthOfYear, dayOfMonth ->
                        val newDate = Calendar.getInstance()
                        newDate.set(year, monthOfYear, dayOfMonth)

                        val dateFormatText = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val formattedDate = dateFormatText.format(newDate.time)
                        dialogBinding.inputDate.text =
                            Editable.Factory.getInstance().newEditable(formattedDate)
                    },
                    newCalendar.get(Calendar.YEAR),
                    newCalendar.get(Calendar.MONTH),
                    newCalendar.get(Calendar.DAY_OF_MONTH)
                )

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()
            }

            dialogBinding.btCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btSave.setOnClickListener {
                var errorMessage: String? = null

                when {
                    transactionSelected.isEmpty() -> errorMessage =
                        "Please select a transaction type"
                    categorySelected.isEmpty() -> errorMessage = "Please select a category type"
                    dialogBinding.inputDate.text.isNullOrEmpty() -> errorMessage =
                        "Please input date"
                    dialogBinding.inputAmount.text.isNullOrEmpty() -> errorMessage =
                        "Please input amount"
                    dialogBinding.inputNote.text.isNullOrEmpty() -> errorMessage =
                        "Please input note"
                }

                if (errorMessage != null) {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val scope = CoroutineScope(Dispatchers.IO)
                val amount = dialogBinding.inputAmount.text.toString()
                val transaction = Transaction(
                    userEmail = email,
                    savings = transactionSelected,
                    type = categorySelected,
                    date = dialogBinding.inputDate.text.toString(),
                    amount = amount.toInt(),
                    note = dialogBinding.inputNote.text.toString()
                )

                scope.launch {
                    val transactionDao =
                        AppDatabase.getInstance(applicationContext).transactionDao()
                    transactionDao.insertTransaction(transaction)
                    withContext(Dispatchers.Main) {
                        updateRecyclerView() // panggil fungsi updateRecyclerView() untuk memperbarui RecyclerView
                    }
                }

                when {
                    transactionSelected == "Wallet" && categorySelected == "Expense" -> {
                        scope.launch {
                            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                            savingsDao.updateWalletExpenseSavings(email, amount.toInt())
                            // update textAmountWallet dengan nilai terbaru
                            runOnUiThread {
                                binding.textAmountWallet.text =
                                    formatRupiah(getSavings("Wallet")?.amount ?: 0)
                            }
                        }
                    }
                    transactionSelected == "Wallet" && categorySelected == "Income" -> {
                        scope.launch {
                            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                            savingsDao.updateWalletIncomeSavings(email, amount.toInt())
                            // update textAmountWallet dengan nilai terbaru
                            runOnUiThread {
                                binding.textAmountWallet.text =
                                    formatRupiah(getSavings("Wallet")?.amount ?: 0)
                            }
                        }
                    }
                    transactionSelected == "Digital" && categorySelected == "Expense" -> {
                        scope.launch {
                            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                            savingsDao.updateDigitalExpenseSavings(email, amount.toInt())
                            // update textAmountWallet dengan nilai terbaru
                            runOnUiThread {
                                binding.textAmountDigital.text =
                                    formatRupiah(getSavings("Digital")?.amount ?: 0)
                            }
                        }
                    }
                    transactionSelected == "Digital" && categorySelected == "Income" -> {
                        scope.launch {
                            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                            savingsDao.updateDigitalIncomeSavings(email, amount.toInt())
                            // update textAmountWallet dengan nilai terbaru
                            runOnUiThread {
                                binding.textAmountDigital.text =
                                    formatRupiah(getSavings("Digital")?.amount ?: 0)
                            }
                        }
                    }
                    transactionSelected == "Bank" && categorySelected == "Expense" -> {
                        scope.launch {
                            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                            savingsDao.updateBankExpenseSavings(email, amount.toInt())
                            // update textAmountWallet dengan nilai terbaru
                            runOnUiThread {
                                binding.textAmountBank.text =
                                    formatRupiah(getSavings("Bank")?.amount ?: 0)
                            }
                        }
                    }
                    transactionSelected == "Bank" && categorySelected == "Income" -> {
                        scope.launch {
                            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                            savingsDao.updateBankIncomeSavings(email, amount.toInt())
                            // update textAmountWallet dengan nilai terbaru
                            runOnUiThread {
                                binding.textAmountBank.text =
                                    formatRupiah(getSavings("Bank")?.amount ?: 0)
                            }
                        }
                    }
                }

                dialog.dismiss()
            }

            dialog.show()
            dialog.window!!.attributes = lp
        }
    }

    private fun updateRecyclerView() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val transactionDao = AppDatabase.getInstance(applicationContext).transactionDao()
            val transactions = transactionDao.getAll().groupBy { it.date }
            val transactionList = mutableListOf<List<Transaction>>()
            transactions.forEach { (_, value) ->
                transactionList.add(value)
            }
            val transactionAdapter = TransactionAdapter(transactionList)

            withContext(Dispatchers.Main) {
                binding.recyclerTransaction.apply {
                    adapter = transactionAdapter
                    layoutManager =
                        LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, true)
                    layoutManager?.scrollToPosition(0)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                AlertDialog.Builder(this).apply {
                    setTitle(getString(R.string.app_name))
                    setMessage("Are you sure to log out?")
                    setPositiveButton("Sure") { _, _ ->
                        prefManager.logout()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                    setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    create().show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + backPressedInterval > System.currentTimeMillis()) {
            // Jika waktu antara dua kali tekan back kurang dari interval, keluar aplikasi
            onBackPressedDispatcher.onBackPressed()
            return
        } else {
            // Jika waktu antara dua kali tekan back lebih dari interval, tampilkan pesan toast
            Toast.makeText(this, "Double tap to exit", Toast.LENGTH_SHORT).show()
        }

        // Update waktu terakhir kali tekan back
        backPressedTime = System.currentTimeMillis()
    }

    private fun getUser(): User? {
        return runBlocking(Dispatchers.IO) {
            val userDao = AppDatabase.getInstance(applicationContext).userDao()
            userDao.getUserByEmail(email)
        }
    }

    private fun getSavings(type: String): Savings? {
        return runBlocking(Dispatchers.IO) {
            val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
            savingsDao.getSavingsByEmailAndType(email, type)
        }
    }

    private fun showDialogSavings(type: Int) {
        val dialogBinding = DialogSavingsBinding.inflate(layoutInflater)
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        when (type) {
            1 -> {
                dialogBinding.textSavings.text = "Wallet Account"
                dialogBinding.textInfo.text =
                    "Ini adalah jumlah uang yang saat ini kamu pegang, di dalam saku baju dan celana. Terutama yang ada di dalam dompet saat ini."

                dialogBinding.inputAmount.setText(getSavings("Wallet")?.amount.toString())

                dialogBinding.btSave.setOnClickListener {
                    val amount =
                        dialogBinding.inputAmount.text.toString().replace(Regex("[,.]"), "")
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                        savingsDao.updateAmount(email, "Wallet", amount.toInt())
                        // update
                        runOnUiThread {
                            binding.textAmountWallet.text =
                                formatRupiah(getSavings("Wallet")?.amount ?: 0)
                        }
                    }
                    dialog.dismiss()
                }
            }
            2 -> {
                dialogBinding.textSavings.text = "Digital Account"
                dialogBinding.textInfo.text =
                    "Uang yang ada di dalam aplikasi seperti ShopeePay, Gopay dan lain-lain."
                dialogBinding.inputAmount.setText(getSavings("Digital")?.amount.toString())
                dialogBinding.btSave.setOnClickListener {
                    val amount =
                        dialogBinding.inputAmount.text.toString().replace(Regex("[,.]"), "")
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                        savingsDao.updateAmount(email, "Digital", amount.toInt())
                        // update
                        runOnUiThread {
                            binding.textAmountDigital.text =
                                formatRupiah(getSavings("Digital")?.amount ?: 0)
                        }
                    }
                    dialog.dismiss()
                }
            }
            else -> {
                dialogBinding.textSavings.text = "Bank Account"
                dialogBinding.textInfo.text =
                    "Total dari seluruh uang yang ada di ATM"
                dialogBinding.inputAmount.setText(getSavings("Bank")?.amount.toString())
                dialogBinding.btSave.setOnClickListener {
                    val amount =
                        dialogBinding.inputAmount.text.toString().replace(Regex("[,.]"), "")
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val savingsDao = AppDatabase.getInstance(applicationContext).savingDao()
                        savingsDao.updateAmount(email, "Bank", amount.toInt())
                        // update
                        runOnUiThread {
                            binding.textAmountBank.text =
                                formatRupiah(getSavings("Bank")?.amount ?: 0)
                        }
                    }
                    dialog.dismiss()
                }
            }
        }

        dialogBinding.btCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

}