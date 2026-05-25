package com.example.penjualan.pegawai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelPegawai

class PegawaiAdapter(
    private var pegawaiList: List<ModelPegawai>,
    private val onEditClick: (ModelPegawai) -> Unit,
    private val onDeleteClick: (ModelPegawai) -> Unit
) : RecyclerView.Adapter<PegawaiAdapter.PegawaiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pegawai, parent, false)
        return PegawaiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        holder.bind(pegawaiList[position])
    }

    override fun getItemCount(): Int = pegawaiList.size

    fun updateData(newList: List<ModelPegawai>) {
        pegawaiList = newList
        notifyDataSetChanged()
    }

    inner class PegawaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        private val tvJabatan: TextView = itemView.findViewById(R.id.tvJabatan)
        private val tvNoHp: TextView = itemView.findViewById(R.id.tvNoHp)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatusPegawai)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(p: ModelPegawai) {
            tvNama.text = p.namaPegawai ?: "N/A"
            tvJabatan.text = p.jabatan?.takeIf { it.isNotEmpty() }?.let { "Jabatan: $it" } ?: "Jabatan: -"
            tvNoHp.text = p.noHp?.takeIf { it.isNotEmpty() }?.let { "HP: $it" } ?: "HP: -"
            tvStatus.text = p.statusPegawai ?: itemView.context.getString(R.string.aktif)

            val statusColor = if (p.statusPegawai.equals(itemView.context.getString(R.string.nonaktif), ignoreCase = true))
                android.R.color.holo_red_dark else android.R.color.holo_green_dark
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

            btnEdit.setOnClickListener { onEditClick(p) }
            btnDelete.setOnClickListener { onDeleteClick(p) }
        }
    }
}
