package com.example.inventoryapp.ui.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapp.adapters.ProdukAdapter
import com.example.inventoryapp.databinding.ActivityProdukBinding
import com.example.inventoryapp.models.Produk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProdukBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: ProdukAdapter
    private val produkList = mutableListOf<Produk>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCategories()
        loadProducts()

        binding.btnTambahProduk.setOnClickListener {
            showAddProductDialog()
        }

        binding.btnRefresh.setOnClickListener {
            loadProducts()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProdukAdapter(produkList,
            onEditClick = { produk -> showEditProductDialog(produk) },
            onDeleteClick = { produk -> deleteProduct(produk) }
        )
        binding.rvProduk.layoutManager = LinearLayoutManager(this)
        binding.rvProduk.adapter = adapter
    }

    private fun loadProducts() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("produk")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                produkList.clear()
                produkList.addAll(snapshot.toObjects(Produk::class.java))
                adapter.notifyDataSetChanged()

                binding.tvTotalProduk.text = "Total Produk: ${produkList.size}"
            } catch (e: Exception) {
                Toast.makeText(this@ProdukActivity, "Gagal load produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("kategori")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val categories = snapshot.documents.mapNotNull { it.getString("nama") }
                if (categories.isNotEmpty()) {
                    // Use categories for spinner
                }
            } catch (e: Exception) {
                // Use default categories
            }
        }
    }

    private fun showAddProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_produk, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaProduk)
        val spinnerKategori = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerKategori)
        val etStok = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStok)
        val etHargaBeli = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHargaBeli)
        val etHargaJual = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHargaJual)

        // Setup kategori spinner
        val categories = arrayOf("Elektronik", "Fashion", "Makanan", "Minuman", "Alat Rumah Tangga", "Lainnya")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerKategori.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Tambah Produk Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val kategori = spinnerKategori.selectedItem.toString()
                val stok = etStok.text.toString().toIntOrNull() ?: 0
                val hargaBeli = etHargaBeli.text.toString().toDoubleOrNull() ?: 0.0
                val hargaJual = etHargaJual.text.toString().toDoubleOrNull() ?: 0.0

                if (nama.isNotEmpty() && stok > 0 && hargaJual > 0) {
                    saveProduct(nama, kategori, stok, hargaBeli, hargaJual)
                } else {
                    Toast.makeText(this, "Isi semua field dengan benar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveProduct(nama: String, kategori: String, stok: Int, hargaBeli: Double, hargaJual: Double) {
        val userId = auth.currentUser?.uid ?: return
        val produk = Produk(
            id = firestore.collection("produk").document().id,
            userId = userId,
            nama = nama,
            kategori = kategori,
            stok = stok,
            hargaBeli = hargaBeli,
            hargaJual = hargaJual,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("produk").document(produk.id).set(produk)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                loadProducts()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan produk", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditProductDialog(produk: Produk) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_produk, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaProduk)
        val etStok = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStok)
        val etHargaBeli = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHargaBeli)
        val etHargaJual = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHargaJual)

        etNama.setText(produk.nama)
        etStok.setText(produk.stok.toString())
        etHargaBeli.setText(produk.hargaBeli.toString())
        etHargaJual.setText(produk.hargaJual.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Produk")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val namaBaru = etNama.text.toString()
                val stokBaru = etStok.text.toString().toIntOrNull() ?: produk.stok
                val hargaBeliBaru = etHargaBeli.text.toString().toDoubleOrNull() ?: produk.hargaBeli
                val hargaJualBaru = etHargaJual.text.toString().toDoubleOrNull() ?: produk.hargaJual

                updateProduct(produk.id, namaBaru, stokBaru, hargaBeliBaru, hargaJualBaru)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateProduct(id: String, nama: String, stok: Int, hargaBeli: Double, hargaJual: Double) {
        val updates = mapOf(
            "nama" to nama,
            "stok" to stok,
            "hargaBeli" to hargaBeli,
            "hargaJual" to hargaJual
        )

        firestore.collection("produk").document(id).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil diupdate", Toast.LENGTH_SHORT).show()
                loadProducts()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update produk", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(produk: Produk) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus ${produk.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                firestore.collection("produk").document(produk.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadProducts()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}