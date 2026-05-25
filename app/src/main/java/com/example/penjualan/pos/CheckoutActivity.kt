package com.example.penjualan.pos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.LocaleHelper
import com.example.penjualan.R
import com.example.penjualan.model.ItemTransaksi
import com.example.penjualan.model.ModelTransaksi
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    @Parcelize
    data class CartItemParcel(
        val idProduk: String,
        val namaProduk: String,
        val kategori: String,
        val hargaSatuan: Double,
        val jumlah: Int,
        val subtotal: Double
    ) : Parcelable

    private val transaksiRef = FirebaseUtils.getRef("transaksi")
    private val produkRef = FirebaseUtils.getRef("produk")
    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    private lateinit var tvTotalCheckout: TextView
    private lateinit var tvSubtotalCO: TextView
    private lateinit var tvDiskonCO: TextView
    private lateinit var tvPajakCO: TextView
    private lateinit var tvTotalFinalCO: TextView
    private lateinit var rvOrderSummary: RecyclerView
    private lateinit var etNamaPelangganCO: TextInputEditText
    private lateinit var etNoHpPelangganCO: TextInputEditText
    private lateinit var etNamaKasir: TextInputEditText
    private lateinit var rgMetodePembayaran: RadioGroup
    private lateinit var tilUangTunai: TextInputLayout
    private lateinit var etUangTunai: TextInputEditText
    private lateinit var cardKembalian: View
    private lateinit var tvKembalian: TextView
    private lateinit var btnProsesBayar: MaterialButton

    private var cartItems = listOf<CartItemParcel>()
    private var totalHarga = 0.0

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val prefs = getSharedPreferences("PenjualanPrefs", Context.MODE_PRIVATE)
        val namaUser = prefs.getString("USER_NAME", "Admin") ?: "Admin"

        cartItems = intent.getParcelableArrayListExtra("CART_ITEMS") ?: emptyList()
        totalHarga = cartItems.sumOf { it.subtotal }

        tvTotalCheckout = findViewById(R.id.tvTotalCheckout)
        tvSubtotalCO = findViewById(R.id.tvSubtotalCO)
        tvDiskonCO = findViewById(R.id.tvDiskonCO)
        tvPajakCO = findViewById(R.id.tvPajakCO)
        tvTotalFinalCO = findViewById(R.id.tvTotalFinalCO)
        rvOrderSummary = findViewById(R.id.rvOrderSummary)
        etNamaPelangganCO = findViewById(R.id.etNamaPelangganCO)
        etNoHpPelangganCO = findViewById(R.id.etNoHpPelangganCO)
        etNamaKasir = findViewById(R.id.etNamaKasir)
        rgMetodePembayaran = findViewById(R.id.rgMetodePembayaran)
        tilUangTunai = findViewById(R.id.tilUangTunai)
        etUangTunai = findViewById(R.id.etUangTunai)
        cardKembalian = findViewById(R.id.cardKembalian)
        tvKembalian = findViewById(R.id.tvKembalian)
        btnProsesBayar = findViewById(R.id.btnProsesBayar)

        etNamaKasir.setText(namaUser)

        // Order summary adapter (reuse keranjang adapter view)
        rvOrderSummary.layoutManager = LinearLayoutManager(this)
        rvOrderSummary.adapter = OrderSummaryAdapter(cartItems, fmt)

        updateTotals()

        // Back button
        findViewById<ImageView>(R.id.btnBackCheckout).setOnClickListener { finish() }

        // Payment method listener
        rgMetodePembayaran.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbTunai) {
                tilUangTunai.visibility = View.VISIBLE
                updateKembalian()
            } else {
                tilUangTunai.visibility = View.GONE
                cardKembalian.visibility = View.GONE
            }
        }

        // Cash input listener
        etUangTunai.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateKembalian()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnProsesBayar.setOnClickListener { processPayment() }
    }

    private fun updateTotals() {
        val subtotal = totalHarga
        val diskon = 0.0
        val pajak = 0.0
        val total = subtotal - diskon + pajak

        tvTotalCheckout.text = fmt.format(total)
        tvSubtotalCO.text = fmt.format(subtotal)
        tvDiskonCO.text = "- ${fmt.format(diskon)}"
        tvPajakCO.text = fmt.format(pajak)
        tvTotalFinalCO.text = fmt.format(total)
    }

    private fun updateKembalian() {
        val tunai = etUangTunai.text.toString().toDoubleOrNull() ?: 0.0
        if (tunai > 0) {
            val kembalian = tunai - totalHarga
            cardKembalian.visibility = View.VISIBLE
            if (kembalian >= 0) {
                tvKembalian.text = fmt.format(kembalian)
                tvKembalian.setTextColor(0xFF2E7D32.toInt())
            } else {
                tvKembalian.text = "Kurang ${fmt.format(-kembalian)}"
                tvKembalian.setTextColor(0xFFC62828.toInt())
            }
        } else {
            cardKembalian.visibility = View.GONE
        }
    }

    private fun getSelectedPaymentMethod(): String {
        return when (rgMetodePembayaran.checkedRadioButtonId) {
            R.id.rbTunai -> "Tunai"
            R.id.rbTransfer -> "Transfer Bank"
            R.id.rbQris -> "QRIS / E-Wallet"
            R.id.rbDebit -> "Kartu Debit/Kredit"
            else -> "Tunai"
        }
    }

    private fun processPayment() {
        val metode = getSelectedPaymentMethod()
        val kasir = etNamaKasir.text.toString().trim().ifBlank { "Admin" }
        val namaPelanggan = etNamaPelangganCO.text.toString().trim()
        val noHpPelanggan = etNoHpPelangganCO.text.toString().trim()

        if (metode == "Tunai") {
            val tunai = etUangTunai.text.toString().toDoubleOrNull() ?: 0.0
            if (tunai < totalHarga) {
                Toast.makeText(this, getString(R.string.uang_tunai_tidak_cukup), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val key = transaksiRef.push().key ?: return
        val now = Date()
        val tanggal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
        val waktu = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
        val nomorNota = "INV-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(now)}"
        val tunaiDibayar = if (metode == "Tunai") etUangTunai.text.toString().toDoubleOrNull() ?: totalHarga else totalHarga
        val kembalian = if (metode == "Tunai") maxOf(0.0, tunaiDibayar - totalHarga) else 0.0

        // Build items map
        val itemsMap = mutableMapOf<String, ItemTransaksi>()
        cartItems.forEachIndexed { i, ci ->
            itemsMap["item_$i"] = ItemTransaksi(
                idProduk = ci.idProduk,
                namaProduk = ci.namaProduk,
                kategori = ci.kategori,
                hargaSatuan = ci.hargaSatuan,
                jumlah = ci.jumlah,
                subtotal = ci.subtotal
            )
        }

        val transaksi = ModelTransaksi(
            idTransaksi = key,
            nomorNota = nomorNota,
            namaProduk = cartItems.firstOrNull()?.namaProduk,
            jumlah = cartItems.sumOf { it.jumlah },
            items = itemsMap,
            subtotal = totalHarga,
            diskon = 0.0,
            pajak = 0.0,
            totalHarga = totalHarga,
            metodePembayaran = metode,
            jumlahBayar = tunaiDibayar,
            kembalian = kembalian,
            namaPelanggan = namaPelanggan.ifBlank { "Umum" },
            kasir = kasir,
            tanggal = tanggal,
            waktu = waktu,
            timestamp = now.time,
            status = "Selesai"
        )

        btnProsesBayar.isEnabled = false
        btnProsesBayar.text = getString(R.string.memproses)

        transaksiRef.child(key).setValue(transaksi)
            .addOnSuccessListener {
                // Kurangi stok produk
                cartItems.forEach { ci ->
                    produkRef.child(ci.idProduk).child("stock")
                        .get().addOnSuccessListener { snap ->
                            val currentStock = snap.getValue(Int::class.java) ?: 0
                            val sisaStok = maxOf(0, currentStock - ci.jumlah)
                            produkRef.child(ci.idProduk).child("stock").setValue(sisaStok)
                        }
                }

                // Go to Receipt
                val intent = Intent(this, ReceiptActivity::class.java)
                intent.putExtra("TRANSAKSI_ID", key)
                intent.putExtra("NOMOR_NOTA", nomorNota)
                intent.putExtra("TANGGAL", tanggal)
                intent.putExtra("WAKTU", waktu)
                intent.putExtra("NAMA_KASIR", kasir)
                intent.putExtra("NAMA_PELANGGAN", namaPelanggan.ifBlank { "Umum" })
                intent.putExtra("NO_HP_PELANGGAN", noHpPelanggan)
                intent.putExtra("METODE", metode)
                intent.putExtra("SUBTOTAL", totalHarga)
                intent.putExtra("DISKON", 0.0)
                intent.putExtra("PAJAK", 0.0)
                intent.putExtra("TOTAL", totalHarga)
                intent.putExtra("DIBAYAR", tunaiDibayar)
                intent.putExtra("KEMBALIAN", kembalian)
                intent.putParcelableArrayListExtra("ITEMS", ArrayList(cartItems))
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "${getString(R.string.gagal_menyimpan)} ${it.message}", Toast.LENGTH_SHORT).show()
                btnProsesBayar.isEnabled = true
                btnProsesBayar.text = "✅ ${getString(R.string.proses_pembayaran)}"
            }
    }

    // Inner adapter for order summary in checkout
    class OrderSummaryAdapter(
        private val items: List<CartItemParcel>,
        private val fmt: NumberFormat
    ) : RecyclerView.Adapter<OrderSummaryAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvNama: TextView = v.findViewById(R.id.tvNamaItem)
            val tvHarga: TextView = v.findViewById(R.id.tvHargaItem)
            val tvQty: TextView = v.findViewById(R.id.tvQty)
            val tvSubtotal: TextView = v.findViewById(R.id.tvSubtotalItem)
            val btnPlus: View = v.findViewById(R.id.btnPlus)
            val btnMinus: View = v.findViewById(R.id.btnMinus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang, parent, false)
        )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvNama.text = item.namaProduk
            holder.tvHarga.text = fmt.format(item.hargaSatuan)
            holder.tvQty.text = "×${item.jumlah}"
            holder.tvSubtotal.text = fmt.format(item.subtotal)
            holder.btnPlus.visibility = View.GONE
            holder.btnMinus.visibility = View.GONE
        }
    }
}
