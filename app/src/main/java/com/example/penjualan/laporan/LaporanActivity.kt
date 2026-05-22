package com.example.penjualan.laporan

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelTransaksi
import com.example.penjualan.transaksi.TransaksiAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://penjualan-indah-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val transaksiRef = database.getReference("transaksi")

    private lateinit var btnBack: ImageView
    private lateinit var rvLaporan: RecyclerView
    private lateinit var tvTotalPendapatanLaporan: TextView

    private lateinit var adapter: TransaksiAdapter
    private val transaksiList = ArrayList<ModelTransaksi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        btnBack = findViewById(R.id.btnBack)
        rvLaporan = findViewById(R.id.rvLaporan)
        tvTotalPendapatanLaporan = findViewById(R.id.tvTotalPendapatanLaporan)

        btnBack.setOnClickListener { finish() }

        // Use TransaksiAdapter but do nothing on delete click for report
        adapter = TransaksiAdapter(transaksiList) { /* Disable delete in report */ }
        rvLaporan.layoutManager = LinearLayoutManager(this)
        rvLaporan.adapter = adapter

        fetchTransaksi()
    }

    private fun fetchTransaksi() {
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiList.clear()
                for (data in snapshot.children) {
                    val t = data.getValue(ModelTransaksi::class.java)
                    t?.let { transaksiList.add(it) }
                }
                adapter.updateData(transaksiList)
                updateTotalPendapatan()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LaporanActivity, "Gagal memuat laporan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotalPendapatan() {
        val total = transaksiList.sumOf { it.totalHarga }
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalPendapatanLaporan.text = "Total Pendapatan: ${fmt.format(total)}"
    }
}
