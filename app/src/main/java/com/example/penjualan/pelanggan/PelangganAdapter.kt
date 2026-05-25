package com.example.penjualan.pelanggan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelPelanggan
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class PelangganAdapter(
    private var list: List<ModelPelanggan>,
    private val onEdit: (ModelPelanggan) -> Unit,
    private val onDelete: (ModelPelanggan) -> Unit
) : RecyclerView.Adapter<PelangganAdapter.VH>() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvAvatar: TextView      = v.findViewById(R.id.tvAvatarPelanggan)
        val tvNama: TextView        = v.findViewById(R.id.tvNamaPelangganItem)
        val tvNoHp: TextView        = v.findViewById(R.id.tvNoHpPelangganItem)
        val tvAlamat: TextView      = v.findViewById(R.id.tvAlamatPelangganItem)
        val chipTransaksi: Chip     = v.findViewById(R.id.chipTransaksiPelanggan)
        val btnEdit: ImageView      = v.findViewById(R.id.btnEditPelanggan)
        val btnDelete: ImageView    = v.findViewById(R.id.btnDeletePelanggan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_pelanggan, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = list[position]
        val nama = p.namaPelanggan ?: "-"
        holder.tvAvatar.text = nama.firstOrNull()?.uppercase() ?: "?"
        holder.tvNama.text = nama
        holder.tvNoHp.text  = "📞 ${p.noHp?.ifBlank { "-" } ?: "-"}"
        holder.tvAlamat.text = "📍 ${p.alamat?.ifBlank { "-" } ?: "-"}"
        holder.chipTransaksi.text = "${p.totalTransaksi} transaksi  •  ${fmt.format(p.totalBelanja)}"
        holder.btnEdit.setOnClickListener   { onEdit(p) }
        holder.btnDelete.setOnClickListener { onDelete(p) }
    }

    fun updateData(newList: List<ModelPelanggan>) {
        list = newList
        notifyDataSetChanged()
    }
}
