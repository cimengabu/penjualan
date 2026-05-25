package com.example.penjualan.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.CartItem
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class KeranjangAdapter(
    private val items: MutableList<CartItem>,
    private val onQtyChanged: () -> Unit
) : RecyclerView.Adapter<KeranjangAdapter.VH>() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaItem)
        val tvHarga: TextView = v.findViewById(R.id.tvHargaItem)
        val tvQty: TextView = v.findViewById(R.id.tvQty)
        val tvSubtotal: TextView = v.findViewById(R.id.tvSubtotalItem)
        val btnPlus: MaterialButton = v.findViewById(R.id.btnPlus)
        val btnMinus: MaterialButton = v.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNama.text = item.product.name ?: "-"
        holder.tvHarga.text = fmt.format(item.product.sellPrice)
        holder.tvQty.text = item.quantity.toString()
        holder.tvSubtotal.text = fmt.format(item.subtotal)

        holder.btnPlus.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            val it = items[pos]
            if (it.quantity < it.product.stock) {
                it.quantity++
                notifyItemChanged(pos)
                onQtyChanged()
            }
        }
        holder.btnMinus.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
            val it = items[pos]
            if (it.quantity > 1) {
                it.quantity--
                notifyItemChanged(pos)
                onQtyChanged()
            } else {
                items.removeAt(pos)
                notifyItemRemoved(pos)
                onQtyChanged()
            }
        }
    }
}
