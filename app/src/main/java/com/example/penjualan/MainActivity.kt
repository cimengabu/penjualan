package com.example.penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.penjualan.kategori.DatakategoriActivity
import com.example.penjualan.produk.ProdukActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

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
    }

    private fun setGreetingMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        val greetingRes = when (hour) {
            in 4..11 -> R.string.Selamat_Pagi
            in 12..14 -> R.string.Selamat_Siang
            in 15..17 -> R.string.Selamat_Sore
            else -> R.string.Selamat_Malam
        }

        val userName = "Admin Cimeng"
        val greetingText = getString(greetingRes, userName)
        tvGreeting.text = greetingText
    }

    private fun showPlaceholderDialog(menuName: String) {
        AlertDialog.Builder(this)
            .setTitle(menuName)
            .setMessage("Menu \"$menuName\" masih dalam pengembangan dan akan segera hadir dalam pembaruan berikutnya.")
            .setPositiveButton("Mengerti", null)
            .show()
    }
}
