package com.example.penjualan.produk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_product, parent, false
        )
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    fun updateData(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        private val tvStok: TextView = itemView.findViewById(R.id.tvStok)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(product: Product) {
            tvNamaProduk.text = product.name ?: "N/A"
            tvKategori.text = "Kategori: ${product.category ?: "Lainnya"}"
            tvStok.text = "Stok: ${product.stock}"

            // Format price as IDR currency
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            tvHarga.text = formatRupiah.format(product.sellPrice)

            // Setup buttons
            btnEdit.setOnClickListener { onEditClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product) }
        }
    }
}
