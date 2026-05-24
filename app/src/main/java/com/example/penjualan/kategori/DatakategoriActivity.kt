package com.example.penjualan.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.R
import com.example.penjualan.model.ModelKategori
import com.example.penjualan.viewmodel.DataKategoriViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase

class DatakategoriActivity : AppCompatActivity() {

    private lateinit var rvKategori: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var searchView: SearchView
    private lateinit var fabTambah: FloatingActionButton

    private lateinit var viewModel: DataKategoriViewModel
    private lateinit var adapter: DetailKategoriAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datakategoriactivity)

        // Bind Views
        rvKategori = findViewById(R.id.rvKategori)
        btnBack = findViewById(R.id.btnBack)
        searchView = findViewById(R.id.searchView)
        fabTambah = findViewById(R.id.fabTambah)

        // Setup back button
        btnBack.setOnClickListener {
            finish()
        }

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[DataKategoriViewModel::class.java]

        // Setup Adapter
        adapter = DetailKategoriAdapter(emptyList()) { category ->
            showCategoryActionDialog(category)
        }
        rvKategori.layoutManager = LinearLayoutManager(this)
        rvKategori.adapter = adapter

        // Observe Data
        viewModel.kategoriList.observe(this) { categories ->
            adapter.updateData(categories)
        }

        viewModel.isError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchKategori(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchKategori(newText)
                return true
            }
        })

        // Setup Add Category
        fabTambah.setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showCategoryActionDialog(category: ModelKategori) {
        val options = arrayOf("Ubah Kategori", "Hapus Kategori")
        AlertDialog.Builder(this)
            .setTitle(category.namaKategori ?: "Kategori")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Edit
                        val intent = Intent(this, ModKategoriActivity::class.java).apply {
                            putExtra("EXTRA_ID", category.idKategori)
                            putExtra("EXTRA_NAMA", category.namaKategori)
                            putExtra("EXTRA_STATUS", category.statusKategori)
                        }
                        startActivity(intent)
                    }
                    1 -> { // Delete
                        showDeleteConfirmationDialog(category)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(category: ModelKategori) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori \"${category.namaKategori}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                val categoryId = category.idKategori ?: return@setPositiveButton
                val categoryRef = FirebaseUtils.getRef("kategori").child(categoryId)
                categoryRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus kategori: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
