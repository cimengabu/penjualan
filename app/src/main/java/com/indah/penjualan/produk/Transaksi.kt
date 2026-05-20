package com.example.inventoryapp.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapp.adapters.TransaksiAdapter
import com.example.inventoryapp.databinding.ActivityTransaksiLaporanBinding
import com.example.inventoryapp.models.Produk
import com.example.inventoryapp.models.Transaksi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaksiLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransaksiLaporanBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: TransaksiAdapter
    private val transaksiList = mutableListOf<Transaksi>()
    private val produkList = mutableListOf<Produk>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadProducts()
        loadTransaksi()

        binding.btnTambahTransaksi.setOnClickListener {
            showAddTransactionDialog()
        }

        binding.btnFilterTanggal.setOnClickListener {
            showDateFilterDialog()
        }

        binding.btnRefresh.setOnClickListener {
            loadTransaksi()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransaksiAdapter(transaksiList)
        binding.rvTransaksi.layoutManager = LinearLayoutManager(this)
        binding.rvTransaksi.adapter = adapter
    }

    private fun loadProducts() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("produk")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                produkList.clear()
                produkList.addAll(snapshot.toObjects(Produk::class.java))
            } catch (e: Exception) {
                Toast.makeText(this@TransaksiLaporanActivity, "Gagal load produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTransaksi() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("transaksi")
                    .whereEqualTo("userId", userId)
                    .orderBy("tanggal", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                transaksiList.clear()
                transaksiList.addAll(snapshot.toObjects(Transaksi::class.java))
                adapter.notifyDataSetChanged()

                calculateTotal()
            } catch (e: Exception) {
                Toast.makeText(this@TransaksiLaporanActivity, "Gagal load transaksi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateTotal() {
        val total = transaksiList.sumOf { it.total }
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalPendapatan.text = format.format(total)
        binding.tvJumlahTransaksi.text = "Total Transaksi: ${transaksiList.size}"
    }

    private fun showAddTransactionDialog() {
        if (produkList.isEmpty()) {
            Toast.makeText(this, "Tidak ada produk. Silakan tambah produk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_transaksi, null)
        val spinnerProduk = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerProduk)
        val etJumlah = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etJumlah)

        val produkNames = produkList.map { "${it.nama} - Rp ${it.hargaJual}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, produkNames)
        spinnerProduk.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Tambah Transaksi Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val position = spinnerProduk.selectedItemPosition
                val produk = produkList[position]
                val jumlah = etJumlah.text.toString().toIntOrNull() ?: 1

                if (jumlah > 0 && jumlah <= produk.stok) {
                    saveTransaction(produk, jumlah)
                } else {
                    Toast.makeText(this, "Jumlah tidak valid atau melebihi stok", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveTransaction(produk: Produk, jumlah: Int) {
        val userId = auth.currentUser?.uid ?: return
        val total = produk.hargaJual * jumlah
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val transaksi = Transaksi(
            id = firestore.collection("transaksi").document().id,
            userId = userId,
            produkId = produk.id,
            namaProduk = produk.nama,
            jumlah = jumlah,
            hargaSatuan = produk.hargaJual,
            total = total,
            tanggal = today,
            timestamp = System.currentTimeMillis()
        )

        // Update stok produk
        val stokBaru = produk.stok - jumlah

        firestore.runTransaction { transaction ->
            val produkRef = firestore.collection("produk").document(produk.id)
            transaction.update(produkRef, "stok", stokBaru)
            transaction.set(firestore.collection("transaksi").document(transaksi.id), transaksi)
            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Transaksi berhasil", Toast.LENGTH_SHORT).show()
            loadProducts()
            loadTransaksi()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateFilterDialog() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = "$year-${month + 1}-$day"
                filterByDate(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun filterByDate(date: String) {
        val filteredList = transaksiList.filter { it.tanggal == date }
        adapter.updateList(filteredList)
        val total = filteredList.sumOf { it.total }
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalPendapatan.text = format.format(total)

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Tidak ada transaksi pada tanggal tersebut", Toast.LENGTH_SHORT).show()
        }
    }
}