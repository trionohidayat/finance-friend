package com.koderion.financeapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionDetailAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionDetailAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_transaction_detail, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.textTransaction.text = transaction.savings
        holder.textNote.text = transaction.note
        if (transaction.type == "Expense") {
            holder.textAmount.text = "-" + formatRupiah(transaction.amount)
            holder.textAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_red_dark
                )
            )
        } else {
            holder.textAmount.text = "+" + formatRupiah(transaction.amount)
            holder.textAmount.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.holo_green_dark
                )
            )
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTransaction: TextView = itemView.findViewById(R.id.text_category)
        val textNote: TextView = itemView.findViewById(R.id.text_note)
        val textAmount: TextView = itemView.findViewById(R.id.text_amount)
    }
}