package com.example.penjualan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.penjualan.kategori.DatakategoriActivity
import com.example.penjualan.produk.ProdukActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance("https://penjualan-indah-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val productsRef = database.getReference("produk")
    private val transaksiRef = database.getReference("transaksi")

    private lateinit var tvGreeting: TextView
    private lateinit var cardProfil: CardView
    private lateinit var cardProduk: CardView
    private lateinit var cardKategori: CardView
    private lateinit var cardPegawai: CardView
    private lateinit var cardCabang: CardView
    private lateinit var cardMencetak: CardView

    private lateinit var ivTransaksi: ImageView
    private lateinit var tvTransaksi: TextView
    private lateinit var ivLaporan: ImageView
    private lateinit var tvLaporan: TextView
    private lateinit var ivSettings: ImageView

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // Bind Views
        tvGreeting = findViewById(R.id.SelamatPagi)
        cardProfil = findViewById(R.id.card2)
        cardProduk = findViewById(R.id.card3)
        cardKategori = findViewById(R.id.card4)
        cardPegawai = findViewById(R.id.card5)
        cardCabang = findViewById(R.id.card6)
        cardMencetak = findViewById(R.id.card7)

        ivTransaksi = findViewById(R.id.ivTransaksi)
        tvTransaksi = findViewById(R.id.tvTransaksi)
        ivLaporan = findViewById(R.id.ivLaporan)
        tvLaporan = findViewById(R.id.tvLaporan)
        ivSettings = findViewById(R.id.ivSettings)

        // Set Dynamic Greeting
        setGreetingMessage()

        // Setup Main Actions
        cardKategori.setOnClickListener {
            val intent = Intent(this, DatakategoriActivity::class.java)
            startActivity(intent)
        }

        cardProduk.setOnClickListener {
            val intent = Intent(this, ProdukActivity::class.java)
            startActivity(intent)
        }
        
        ivSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Setup Placeholder Actions
        cardProfil.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.profil.ProfilActivity::class.java)) 
        }
        cardPegawai.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.pegawai.PegawaiActivity::class.java)) 
        }
        cardCabang.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.cabang.CabangActivity::class.java)) 
        }
        cardMencetak.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.cetak.CetakActivity::class.java)) 
        }

        ivTransaksi.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.transaksi.TransaksiActivity::class.java)) 
        }
        tvTransaksi.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.transaksi.TransaksiActivity::class.java)) 
        }
        ivLaporan.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.laporan.LaporanActivity::class.java)) 
        }
        tvLaporan.setOnClickListener { 
            startActivity(Intent(this, com.example.penjualan.laporan.LaporanActivity::class.java)) 
        }

        // Fetch Dashboard Analytics
        fetchAnalyticsData()
    }

    private fun fetchAnalyticsData() {
        // Fetch Total Products
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                tvLaporan.text = "$count Produk"
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Total Transactions & Income
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalIncome = 0.0
                val count = snapshot.childrenCount
                for (data in snapshot.children) {
                    val totalStr = data.child("totalHarga").value?.toString() ?: "0"
                    val totalHarga = totalStr.toDoubleOrNull() ?: 0.0
                    totalIncome += totalHarga
                }
                
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                tvTransaksi.text = "$count Trx\n${formatRupiah.format(totalIncome)}"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setGreetingMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        val greetingRes = when (hour) {
            in 4..11 -> R.string.Selamat_Pagi
            in 12..14 -> R.string.Selamat_Siang
            in 15..17 -> R.string.Selamat_Sore
            else -> R.string.Selamat_Malam
        }

        val userName = "Admin"
        val greetingText = getString(greetingRes, userName)
        tvGreeting.text = greetingText
    }

    private fun showPlaceholderDialog(menuName: String) {
        AlertDialog.Builder(this)
            .setTitle(menuName)
            .setMessage("Menu \"$menuName\" masih dalam pengembangan.")
            .setPositiveButton("Mengerti", null)
            .show()
    }
}
