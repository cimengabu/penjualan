// ProdukAdapter.kt
package com.example.inventoryapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inventoryapp.databinding.ItemProdukBinding
import com.example.inventoryapp.models.Produk
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private val items: List<Produk>,
    private val onEditClick: (Produk) -> Unit,
    private val onDeleteClick: (Produk) -> Unit
) : RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProdukBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemProdukBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(produk: Produk) {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            binding.tvNamaProduk.text = produk.nama
            binding.tvKategori.text = "Kategori: ${produk.kategori}"
            binding.tvStok.text = "Stok: ${produk.stok}"
            binding.tvHarga.text = format.format(produk.hargaJual)

            binding.btnEdit.setOnClickListener { onEditClick(produk) }
            binding.btnDelete.setOnClickListener { onDeleteClick(produk) }
        }
    }
}