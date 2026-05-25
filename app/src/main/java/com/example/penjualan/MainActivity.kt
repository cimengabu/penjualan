package com.example.penjualan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.kategori.DatakategoriActivity
import com.example.penjualan.pelanggan.PelangganActivity
import com.example.penjualan.pos.PosActivity
import com.example.penjualan.produk.ProdukActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val productsRef  = FirebaseUtils.getRef("produk")
    private val transaksiRef = FirebaseUtils.getRef("transaksi")
    private val pelangganRef = FirebaseUtils.getRef("pelanggan")

    private lateinit var tvGreeting: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var cardProfil: CardView
    private lateinit var cardProduk: CardView
    private lateinit var cardKategori: CardView
    private lateinit var cardPegawai: CardView
    private lateinit var cardCabang: CardView
    private lateinit var cardMencetak: CardView

    private lateinit var ivLaporan: ImageView
    private lateinit var tvLaporan: TextView
    private lateinit var llPelanggan: android.widget.LinearLayout
    private lateinit var ivPelanggan: ImageView
    private lateinit var tvPelanggan: TextView
    private lateinit var ivSettings: ImageView

    private lateinit var tvTodayIncome: TextView
    private lateinit var tvTodayTrx: TextView
    private lateinit var tvTotalProduk: TextView
    private lateinit var tvTotalPelanggan: TextView
    private lateinit var btnKasirPOS: MaterialButton

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        tvGreeting       = findViewById(R.id.SelamatPagi)
        tvSubtitle       = findViewById(R.id.Teks2)
        cardProfil       = findViewById(R.id.card2)
        cardProduk       = findViewById(R.id.card3)
        cardKategori     = findViewById(R.id.card4)
        cardPegawai      = findViewById(R.id.card5)
        cardCabang       = findViewById(R.id.card6)
        cardMencetak     = findViewById(R.id.card7)

        ivLaporan        = findViewById(R.id.ivLaporan)
        tvLaporan        = findViewById(R.id.tvLaporan)
        llPelanggan      = findViewById(R.id.llPelanggan)
        ivPelanggan      = findViewById(R.id.ivPelanggan)
        tvPelanggan      = findViewById(R.id.tvPelanggan)
        ivSettings       = findViewById(R.id.ivSettings)

        tvTodayIncome    = findViewById(R.id.tvTodayIncome)
        tvTodayTrx       = findViewById(R.id.tvTodayTrx)
        tvTotalProduk    = findViewById(R.id.tvTotalProduk)
        tvTotalPelanggan = findViewById(R.id.tvTotalPelanggan)
        btnKasirPOS      = findViewById(R.id.btnKasirPOS)

        setGreetingMessage()

        // --- Navigations ---
        btnKasirPOS.setOnClickListener      { startActivity(Intent(this, PosActivity::class.java)) }
        cardKategori.setOnClickListener     { startActivity(Intent(this, DatakategoriActivity::class.java)) }
        cardProduk.setOnClickListener       { startActivity(Intent(this, ProdukActivity::class.java)) }
        llPelanggan.setOnClickListener      { startActivity(Intent(this, PelangganActivity::class.java)) }
        ivPelanggan.setOnClickListener      { startActivity(Intent(this, PelangganActivity::class.java)) }
        tvPelanggan.setOnClickListener      { startActivity(Intent(this, PelangganActivity::class.java)) }
        ivSettings.setOnClickListener       { startActivity(Intent(this, SettingsActivity::class.java)) }
        cardProfil.setOnClickListener       { startActivity(Intent(this, com.example.penjualan.profil.ProfilActivity::class.java)) }
        cardPegawai.setOnClickListener      { startActivity(Intent(this, com.example.penjualan.pegawai.PegawaiActivity::class.java)) }
        cardCabang.setOnClickListener       { startActivity(Intent(this, com.example.penjualan.cabang.CabangActivity::class.java)) }
        cardMencetak.setOnClickListener     { startActivity(Intent(this, com.example.penjualan.cetak.CetakActivity::class.java)) }
        ivLaporan.setOnClickListener        { startActivity(Intent(this, com.example.penjualan.laporan.LaporanActivity::class.java)) }
        tvLaporan.setOnClickListener        { startActivity(Intent(this, com.example.penjualan.laporan.LaporanActivity::class.java)) }

        fetchAnalyticsData()
    }

    private fun fetchAnalyticsData() {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalProduk.text = snapshot.childrenCount.toString()
                tvLaporan.text = "${snapshot.childrenCount} Produk"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        pelangganRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalPelanggan.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalIncome = 0.0
                var todayIncome = 0.0
                var todayCount  = 0
                val count = snapshot.childrenCount

                for (data in snapshot.children) {
                    val totalHarga = data.child("totalHarga").getValue(Double::class.java) ?: 0.0
                    totalIncome += totalHarga
                    val tanggal = data.child("tanggal").getValue(String::class.java) ?: ""
                    if (tanggal == todayStr) { todayIncome += totalHarga; todayCount++ }
                }

                tvTodayIncome.text = fmt.format(todayIncome)
                tvTodayTrx.text    = todayCount.toString()

            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setGreetingMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingRes = when (hour) {
            in 4..11  -> R.string.Selamat_Pagi
            in 12..14 -> R.string.Selamat_Siang
            in 15..17 -> R.string.Selamat_Sore
            else      -> R.string.Selamat_Malam
        }
        val prefs = getSharedPreferences("PenjualanPrefs", Context.MODE_PRIVATE)
        val defaultName = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: "Admin"
        val userName = prefs.getString("USER_NAME", "")?.takeIf { it.isNotBlank() } ?: defaultName
        tvGreeting.text = getString(greetingRes, userName)
    }
}
