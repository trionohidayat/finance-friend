package com.koderion.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactionList: List<List<Transaction>>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount() = transactionList.size

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transactions = transactionList[position]
        holder.textDate.text = transactions[0].date

        // Calculate total income and expenses
        var totalIncome = 0
        var totalExpenses = 0
        for (transaction in transactions) {
            if (transaction.type == "Income") {
                totalIncome += transaction.amount
            } else {
                totalExpenses += transaction.amount
            }
        }

        // Show total income and expenses
        val total = totalIncome - totalExpenses
        if (total >= 0) {
            holder.textAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            holder.textAmount.text = "+${formatRupiah(total)}"
        } else {
            holder.textAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            holder.textAmount.text = "-${formatRupiah(total)}"
        }

        // set recyclerview kedua
        holder.recyclerDetail.apply {
            adapter = TransactionDetailAdapter(transactions)
            layoutManager = LinearLayoutManager(context)
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDate: TextView = itemView.findViewById(R.id.text_date)
        val textAmount: TextView = itemView.findViewById(R.id.text_amount)
        val recyclerDetail: RecyclerView = itemView.findViewById(R.id.recycler_detail)
    }
}