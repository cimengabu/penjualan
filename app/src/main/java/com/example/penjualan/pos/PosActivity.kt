package com.example.penjualan.pos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.LocaleHelper
import com.example.penjualan.R
import com.example.penjualan.model.CartItem
import com.example.penjualan.model.Product
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class PosActivity : AppCompatActivity() {

    private val produkRef = FirebaseUtils.getRef("produk")
    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    private lateinit var tvTotalBelanja: TextView
    private lateinit var chipItemCount: com.google.android.material.chip.Chip
    private lateinit var btnCheckout: MaterialButton
    private lateinit var btnClearCart: ImageView
    private lateinit var btnBackPOS: ImageView
    private lateinit var etSearchPOS: EditText
    private lateinit var chipGroupKategori: ChipGroup
    private lateinit var rvProdukPOS: RecyclerView
    private lateinit var rvKeranjang: RecyclerView
    private lateinit var tvKeranjangCount: TextView

    private val allProducts = mutableListOf<Product>()
    private val cartItems = mutableListOf<CartItem>()

    private lateinit var produkAdapter: ProdukPosAdapter
    private lateinit var keranjangAdapter: KeranjangAdapter

    private var selectedKategori = "Semua"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)

        tvTotalBelanja = findViewById(R.id.tvTotalBelanja)
        chipItemCount = findViewById(R.id.chipItemCount)
        btnCheckout = findViewById(R.id.btnCheckout)
        btnClearCart = findViewById(R.id.btnClearCart)
        btnBackPOS = findViewById(R.id.btnBackPOS)
        etSearchPOS = findViewById(R.id.etSearchPOS)
        chipGroupKategori = findViewById(R.id.chipGroupKategori)
        rvProdukPOS = findViewById(R.id.rvProdukPOS)
        rvKeranjang = findViewById(R.id.rvKeranjang)
        tvKeranjangCount = findViewById(R.id.tvKeranjangCount)

        btnBackPOS.setOnClickListener { finish() }

        // Product adapter (2-column grid)
        produkAdapter = ProdukPosAdapter(allProducts) { product ->
            addToCart(product)
        }
        rvProdukPOS.layoutManager = GridLayoutManager(this, 2)
        rvProdukPOS.adapter = produkAdapter

        // Cart adapter
        keranjangAdapter = KeranjangAdapter(cartItems) { updateTotal() }
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = keranjangAdapter

        // Search
        etSearchPOS.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Checkout
        btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("CART_ITEMS", ArrayList(cartItems.map { item ->
                CheckoutActivity.CartItemParcel(
                    idProduk = item.product.id ?: "",
                    namaProduk = item.product.name ?: "",
                    kategori = item.product.category ?: "",
                    hargaSatuan = item.product.sellPrice,
                    jumlah = item.quantity,
                    subtotal = item.subtotal
                )
            }))
            startActivity(intent)
        }

        // Clear cart
        btnClearCart.setOnClickListener {
            cartItems.clear()
            keranjangAdapter.notifyDataSetChanged()
            updateTotal()
        }

        fetchProduk()
    }

    private fun fetchProduk() {
        produkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProducts.clear()
                val categories = mutableSetOf("Semua")
                for (data in snapshot.children) {
                    val p = data.getValue(Product::class.java)
                    p?.let {
                        allProducts.add(it)
                        it.category?.let { cat -> categories.add(cat) }
                    }
                }
                setupCategoryChips(categories.toList())
                applyFilter(etSearchPOS.text.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PosActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCategoryChips(categories: List<String>) {
        chipGroupKategori.removeAllViews()
        categories.forEach { cat ->
            val chip = Chip(this).apply {
                text = cat
                isCheckable = true
                isChecked = cat == selectedKategori
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        selectedKategori = cat
                        applyFilter(etSearchPOS.text.toString())
                    }
                }
            }
            chipGroupKategori.addView(chip)
        }
    }

    private fun applyFilter(query: String) {
        val keyword = query.trim().lowercase()
        val filtered = allProducts.filter { p ->
            val matchesKat = selectedKategori == "Semua" || p.category == selectedKategori
            val matchesQuery = keyword.isEmpty() ||
                p.name?.lowercase()?.contains(keyword) == true ||
                p.category?.lowercase()?.contains(keyword) == true
            matchesKat && matchesQuery
        }
        produkAdapter.updateData(filtered)
    }

    private fun addToCart(product: Product) {
        val existing = cartItems.find { it.product.id == product.id }
        if (existing != null) {
            if (existing.quantity < product.stock) {
                existing.quantity++
                keranjangAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Stok tidak cukup!", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            cartItems.add(CartItem(product, 1))
            keranjangAdapter.notifyItemInserted(cartItems.size - 1)
        }
        updateTotal()
        Toast.makeText(this, "${product.name} ditambahkan ✓", Toast.LENGTH_SHORT).show()
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.subtotal }
        val totalItem = cartItems.sumOf { it.quantity }
        tvTotalBelanja.text = fmt.format(total)
        chipItemCount.text = "$totalItem item"
        tvKeranjangCount.text = "(${cartItems.size} jenis, $totalItem item)"
    }
}
