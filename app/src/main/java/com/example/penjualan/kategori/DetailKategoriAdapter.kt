package com.example.penjualan.kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelKategori

class DetailKategoriAdapter(
    private var kategoriList: List<ModelKategori>,
    private val onItemClick: (ModelKategori) -> Unit
) : RecyclerView.Adapter<DetailKategoriAdapter.KategoriViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.activity_item_data, parent, false
        )
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val category = kategoriList[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = kategoriList.size

    fun updateData(newList: List<ModelKategori>) {
        kategoriList = newList
        notifyDataSetChanged()
    }

    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        private val chipStatus: TextView = itemView.findViewById(R.id.chipAdd)

        fun bind(kategori: ModelKategori) {
            tvNamaKategori.text = kategori.namaKategori ?: "N/A"
            chipStatus.text = kategori.statusKategori ?: itemView.context.getString(R.string.aktif)

            // Highlight status styling (gunakan ContextCompat agar tidak deprecated)
            if (kategori.statusKategori.equals(itemView.context.getString(R.string.nonaktif), ignoreCase = true)) {
                chipStatus.setTextColor(
                    ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
                )
            } else {
                chipStatus.setTextColor(
                    ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
                )
            }

            itemView.setOnClickListener {
                onItemClick(kategori)
            }
        }
    }
}
