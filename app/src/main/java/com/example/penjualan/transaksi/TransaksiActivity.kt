package com.example.penjualan.transaksi

import com.example.penjualan.BaseActivity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelTransaksi
import com.example.penjualan.model.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.example.penjualan.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiActivity : BaseActivity() {

    private val transaksiRef = FirebaseUtils.getRef("transaksi")
    private val produkRef = FirebaseUtils.getRef(getString(R.string.produk))

    private lateinit var btnBack: ImageView
    private lateinit var rvTransaksi: RecyclerView
    private lateinit var fabTambahTransaksi: FloatingActionButton
    private lateinit var tvTotalPendapatan: TextView

    private lateinit var adapter: TransaksiAdapter
    private val transaksiList = ArrayList<ModelTransaksi>()
    private val produkList = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        btnBack = findViewById(R.id.btnBack)
        rvTransaksi = findViewById(R.id.rvTransaksi)
        fabTambahTransaksi = findViewById(R.id.fabTambahTransaksi)
        tvTotalPendapatan = findViewById(R.id.tvTotalPendapatan)

        btnBack.setOnClickListener { finish() }

        adapter = TransaksiAdapter(transaksiList, onDeleteClick = { showDeleteDialog(it) })
        rvTransaksi.layoutManager = LinearLayoutManager(this)
        rvTransaksi.adapter = adapter

        fetchTransaksi()
        fetchProduk()

        fabTambahTransaksi.setOnClickListener { showTambahTransaksiDialog() }
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
                Toast.makeText(this@TransaksiActivity, "Gagal memuat transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchProduk() {
        produkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                produkList.clear()
                for (data in snapshot.children) {
                    val p = data.getValue(Product::class.java)
                    p?.let { produkList.add(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateTotalPendapatan() {
        val total = transaksiList.sumOf { it.totalHarga }
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalPendapatan.text = "Total: ${fmt.format(total)}"
    }

    private fun showTambahTransaksiDialog() {
        if (produkList.isEmpty()) {
            Toast.makeText(this, "Belum ada produk tersedia. Tambahkan produk dulu.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_transaksi, null)
        val spinnerProduk = dialogView.findViewById<Spinner>(R.id.spinnerProduk)
        val etJumlah = dialogView.findViewById<TextInputEditText>(R.id.etJumlahTransaksi)

        val produkNames = produkList.map { it.name ?: getString(R.string.produk) }.toTypedArray()
        spinnerProduk.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, produkNames)

        AlertDialog.Builder(this)
            .setTitle("Tambah Transaksi")
            .setView(dialogView)
            .setPositiveButton(getString(R.string.simpan)) { _, _ ->
                val selectedIndex = spinnerProduk.selectedItemPosition
                val produk = produkList.getOrNull(selectedIndex) ?: return@setPositiveButton
                val jumlah = etJumlah.text.toString().trim().toIntOrNull() ?: 1

                if (jumlah <= 0) {
                    Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (jumlah > produk.stock) {
                    Toast.makeText(this, "Stok tidak cukup! Stok tersedia: ${produk.stock}", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val key = transaksiRef.push().key ?: return@setPositiveButton
                val tanggal = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val totalHarga = produk.sellPrice * jumlah

                val transaksi = ModelTransaksi(
                    idTransaksi = key,
                    namaProduk = produk.name,
                    kategori = produk.category,
                    jumlah = jumlah,
                    hargaSatuan = produk.sellPrice,
                    totalHarga = totalHarga,
                    tanggal = tanggal,
                    kasir = "Admin"
                )

                transaksiRef.child(key).setValue(transaksi)
                    .addOnSuccessListener {
                        // Kurangi stok produk
                        val sisaStok = produk.stock - jumlah
                        produk.id?.let { prodId ->
                            produkRef.child(prodId).child("stock").setValue(sisaStok)
                        }
                        Toast.makeText(this, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan transaksi: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }

    private fun showDeleteDialog(transaksi: ModelTransaksi) {
        val title = if (!transaksi.nomorNota.isNullOrBlank()) transaksi.nomorNota else transaksi.namaProduk
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Hapus transaksi \"$title\"?")
            .setPositiveButton(getString(R.string.hapus)) { _, _ ->
                val id = transaksi.idTransaksi ?: return@setPositiveButton
                transaksiRef.child(id).removeValue()
                    .addOnSuccessListener { Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }
}
