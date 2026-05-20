package com.example.inventoryapp.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapp.adapters.KategoriAdapter
import com.example.inventoryapp.databinding.ActivityKategoriBinding
import com.example.inventoryapp.models.Kategori
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class KategoriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKategoriBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: KategoriAdapter
    private val kategoriList = mutableListOf<Kategori>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCategories()

        binding.btnTambahKategori.setOnClickListener {
            showAddCategoryDialog()
        }

        binding.btnRefresh.setOnClickListener {
            loadCategories()
        }
    }

    private fun setupRecyclerView() {
        adapter = KategoriAdapter(kategoriList,
            onEditClick = { kategori -> showEditCategoryDialog(kategori) },
            onDeleteClick = { kategori -> deleteCategory(kategori) }
        )
        binding.rvKategori.layoutManager = LinearLayoutManager(this)
        binding.rvKategori.adapter = adapter
    }

    private fun loadCategories() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("kategori")
                    .whereEqualTo("userId", userId)
                    .orderBy("nama")
                    .get()
                    .await()

                kategoriList.clear()
                val categories = snapshot.toObjects(Kategori::class.java)
                kategoriList.addAll(categories)
                adapter.notifyDataSetChanged()

                // Hitung jumlah produk per kategori
                countProductsPerCategory()

                if (kategoriList.isEmpty()) {
                    // Load default categories
                    loadDefaultCategories()
                }
            } catch (e: Exception) {
                Toast.makeText(this@KategoriActivity, "Gagal load kategori", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun countProductsPerCategory() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val produkSnapshot = firestore.collection("produk")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val produkMap = produkSnapshot.documents.groupBy { it.getString("kategori") ?: "" }

                kategoriList.forEach { kategori ->
                    kategori.jumlahProduk = produkMap[kategori.nama]?.size ?: 0
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadDefaultCategories() {
        val defaultCategories = listOf(
            "Elektronik", "Fashion", "Makanan", "Minuman",
            "Alat Rumah Tangga", "Olahraga", "Kesehatan", "Lainnya"
        )

        defaultCategories.forEach { categoryName ->
            val kategori = Kategori(
                id = firestore.collection("kategori").document().id,
                userId = auth.currentUser?.uid ?: "",
                nama = categoryName,
                deskripsi = "Kategori $categoryName",
                timestamp = System.currentTimeMillis()
            )

            firestore.collection("kategori").document(kategori.id).set(kategori)
        }

        loadCategories()
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_kategori, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaKategori)
        val etDeskripsi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDeskripsi)

        AlertDialog.Builder(this)
            .setTitle("Tambah Kategori Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val deskripsi = etDeskripsi.text.toString()

                if (nama.isNotEmpty()) {
                    saveCategory(nama, deskripsi)
                } else {
                    Toast.makeText(this, "Nama kategori harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveCategory(nama: String, deskripsi: String) {
        val userId = auth.currentUser?.uid ?: return
        val kategori = Kategori(
            id = firestore.collection("kategori").document().id,
            userId = userId,
            nama = nama,
            deskripsi = deskripsi,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("kategori").document(kategori.id).set(kategori)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                loadCategories()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan kategori", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditCategoryDialog(kategori: Kategori) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_kategori, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaKategori)
        val etDeskripsi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDeskripsi)

        etNama.setText(kategori.nama)
        etDeskripsi.setText(kategori.deskripsi)

        AlertDialog.Builder(this)
            .setTitle("Edit Kategori")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val namaBaru = etNama.text.toString()
                val deskripsiBaru = etDeskripsi.text.toString()

                if (namaBaru.isNotEmpty()) {
                    updateCategory(kategori.id, namaBaru, deskripsiBaru)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateCategory(id: String, nama: String, deskripsi: String) {
        val updates = mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi
        )

        firestore.collection("kategori").document(id).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
                loadCategories()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update kategori", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCategory(kategori: Kategori) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori ${kategori.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                firestore.collection("kategori").document(kategori.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus kategori", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}