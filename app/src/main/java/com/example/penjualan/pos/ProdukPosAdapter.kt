package com.example.penjualan.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.Product
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class ProdukPosAdapter(
    private var list: List<Product>,
    private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<ProdukPosAdapter.VH>() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaProdukPOS)
        val tvKategori: TextView = v.findViewById(R.id.tvKategoriPOS)
        val tvHarga: TextView = v.findViewById(R.id.tvHargaPOS)
        val tvStok: TextView = v.findViewById(R.id.tvStokPOS)
        val btnTambah: MaterialButton = v.findViewById(R.id.btnTambahKeranjang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_produk_pos, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = list[position]
        holder.tvNama.text = p.name ?: "-"
        holder.tvKategori.text = p.category ?: "-"
        holder.tvHarga.text = fmt.format(p.sellPrice)
        holder.tvStok.text = "Stok: ${p.stock}"
        holder.btnTambah.isEnabled = p.stock > 0
        holder.btnTambah.alpha = if (p.stock > 0) 1f else 0.4f
        holder.btnTambah.setOnClickListener { if (p.stock > 0) onAddToCart(p) }
        holder.itemView.setOnClickListener { if (p.stock > 0) onAddToCart(p) }
    }

    fun updateData(newList: List<Product>) {
        list = newList
        notifyDataSetChanged()
    }
}
