package com.example.penjualan.produk

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.example.penjualan.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ProdukActivity : AppCompatActivity() {

    private val productsRef = FirebaseUtils.getRef("produk")
    private val categoriesRef = FirebaseUtils.getRef("kategori")

    private lateinit var btnBack: ImageView
    private lateinit var recyclerViewProducts: RecyclerView
    private lateinit var fabTambahProduk: FloatingActionButton

    private lateinit var adapter: ProductAdapter
    private val productList = ArrayList<Product>()
    private val categoryList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind Views
        btnBack = findViewById(R.id.btnBack)
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts)
        fabTambahProduk = findViewById(R.id.fabTambahProduk)
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)

        // Handle Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = ProductAdapter(productList,
            onEditClick = { product -> showAddOrEditProductDialog(product) },
            onDeleteClick = { product -> showDeleteConfirmationDialog(product) }
        )
        recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        recyclerViewProducts.adapter = adapter

        // Search logic
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim().lowercase()
                if (keyword.isEmpty()) {
                    adapter.updateData(productList)
                } else {
                    val filtered = productList.filter { 
                        it.name?.lowercase()?.contains(keyword) == true || 
                        it.category?.lowercase()?.contains(keyword) == true 
                    }
                    adapter.updateData(filtered)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Fetch Data from Firebase
        fetchProducts()
        fetchCategories()

        // Handle Add Product
        fabTambahProduk.setOnClickListener {
            showAddOrEditProductDialog(null)
        }
    }

    private fun fetchProducts() {
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (data in snapshot.children) {
                    val product = data.getValue(Product::class.java)
                    product?.let { productList.add(it) }
                }
                adapter.updateData(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProdukActivity, "Gagal memuat produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCategories() {
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (data in snapshot.children) {
                    val categoryName = data.child("namaKategori").getValue(String::class.java)
                    categoryName?.let { categoryList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore, will fallback to default list
            }
        })
    }

    private fun showAddOrEditProductDialog(product: Product?) {
        val dialogView = layoutInflater.inflate(R.layout.tambahproduk, null)
        val etNamaProduk = dialogView.findViewById<TextInputEditText>(R.id.etNamaProduk)
        val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)
        val etStok = dialogView.findViewById<TextInputEditText>(R.id.etStok)
        val etHargaBeli = dialogView.findViewById<TextInputEditText>(R.id.etHargaBeli)
        val etHargaJual = dialogView.findViewById<TextInputEditText>(R.id.etHargaJual)

        // Setup Kategori spinner options
        val defaultCategories = arrayOf("Elektronik", "Makanan", "Minuman", "Pakaian", "Alat Rumah Tangga", "Lainnya")
        val spinnerOptions = if (categoryList.isNotEmpty()) categoryList.toTypedArray() else defaultCategories
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = spinnerAdapter

        // Prepopulate fields if editing
        if (product != null) {
            etNamaProduk.setText(product.name)
            etStok.setText(product.stock.toString())
            etHargaBeli.setText(product.buyPrice.toString())
            etHargaJual.setText(product.sellPrice.toString())

            val selectedIndex = spinnerOptions.indexOf(product.category)
            if (selectedIndex >= 0) {
                spinnerKategori.setSelection(selectedIndex)
            }
        }

        val dialogTitle = if (product != null) "Ubah Produk" else "Tambah Produk Baru"

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val name = etNamaProduk.text.toString().trim()
                val category = spinnerKategori.selectedItem?.toString() ?: "Lainnya"
                val stock = etStok.text.toString().trim().toIntOrNull() ?: 0
                val buyPrice = etHargaBeli.text.toString().trim().toDoubleOrNull() ?: 0.0
                val sellPrice = etHargaJual.text.toString().trim().toDoubleOrNull() ?: 0.0

                if (name.isEmpty()) {
                    Toast.makeText(this, "Nama produk wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val key = product?.id ?: productsRef.push().key
                if (key != null) {
                    val newProduct = Product(
                        id = key,
                        name = name,
                        category = category,
                        stock = stock,
                        buyPrice = buyPrice,
                        sellPrice = sellPrice
                    )

                    productsRef.child(key).setValue(newProduct)
                        .addOnSuccessListener {
                            val msg = if (product != null) "Produk berhasil diperbarui" else "Produk berhasil ditambahkan"
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus produk \"${product.name}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                val id = product.id ?: return@setPositiveButton
                productsRef.child(id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus produk: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
