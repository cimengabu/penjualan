package com.example.penjualan.cabang

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelCabang

class CabangAdapter(
    private var cabangList: List<ModelCabang>,
    private val onEditClick: (ModelCabang) -> Unit,
    private val onDeleteClick: (ModelCabang) -> Unit
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cabang, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) = holder.bind(cabangList[position])
    override fun getItemCount(): Int = cabangList.size

    fun updateData(newList: List<ModelCabang>) {
        cabangList = newList
        notifyDataSetChanged()
    }

    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaCabang)
        private val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamat)
        private val tvNoTelp: TextView = itemView.findViewById(R.id.tvNoTelp)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatusCabang)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(c: ModelCabang) {
            tvNama.text = c.namaCabang ?: "N/A"
            tvAlamat.text = c.alamat?.takeIf { it.isNotEmpty() }?.let { "Alamat: $it" } ?: "Alamat: -"
            tvNoTelp.text = c.noTelp?.takeIf { it.isNotEmpty() }?.let { "Telp: $it" } ?: "Telp: -"
            tvStatus.text = c.statusCabang ?: itemView.context.getString(R.string.aktif)
            val color = if (c.statusCabang.equals(itemView.context.getString(R.string.nonaktif), ignoreCase = true))
                android.R.color.holo_red_dark else android.R.color.holo_green_dark
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, color))
            btnEdit.setOnClickListener { onEditClick(c) }
            btnDelete.setOnClickListener { onDeleteClick(c) }
        }
    }
}
