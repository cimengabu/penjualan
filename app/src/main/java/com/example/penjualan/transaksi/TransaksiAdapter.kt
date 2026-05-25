package com.example.penjualan.transaksi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class TransaksiAdapter(
    private var transaksiList: List<ModelTransaksi>,
    private val onDeleteClick: (ModelTransaksi) -> Unit,
    private val onPrintClick: ((ModelTransaksi) -> Unit)? = null
) : RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder>() {

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnPrint: ImageButton? = itemView.findViewById(R.id.btnPrintTransaksi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi, parent, false)
        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val transaksi = transaksiList[position]
        if (!transaksi.nomorNota.isNullOrBlank()) {
            holder.tvNamaProduk.text = "Nota: ${transaksi.nomorNota}"
            val macam = if (transaksi.items.isNotEmpty()) "${transaksi.items.size} macam produk" else transaksi.namaProduk ?: ""
            holder.tvKategori.text = "$macam | ${transaksi.namaPelanggan ?: "Umum"}"
        } else {
            holder.tvNamaProduk.text = transaksi.namaProduk ?: "Unknown"
            holder.tvKategori.text = transaksi.kategori ?: "Unknown"
        }
        holder.tvJumlah.text = "Jumlah: ${transaksi.jumlah}"
        holder.tvTanggal.text = transaksi.tanggal ?: ""

        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.tvTotalHarga.text = "Total: ${fmt.format(transaksi.totalHarga)}"

        holder.btnDelete.setOnClickListener { onDeleteClick(transaksi) }
        
        if (onPrintClick != null && holder.btnPrint != null) {
            holder.btnPrint.visibility = View.VISIBLE
            holder.btnPrint.setOnClickListener { onPrintClick.invoke(transaksi) }
        } else {
            holder.btnPrint?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = transaksiList.size

    fun updateData(newList: List<ModelTransaksi>) {
        transaksiList = newList
        notifyDataSetChanged()
    }
}
