package com.indah.penjualan // Sesuaikan dengan package name anda

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi CardView berdasarkan ID di XML
        val cardProfil = findViewById<CardView>(R.id.card2)
        val cardProduk = findViewById<CardView>(R.id.card3)
        val cardKategori = findViewById<CardView>(R.id.card4)
        val cardPegawai = findViewById<CardView>(R.id.card5)
        val cardCabang = findViewById<CardView>(R.id.card6)
        val cardPrinter = findViewById<CardView>(R.id.card7)

        // Contoh Memberikan Aksi Klik
        cardProfil.setOnClickListener {
            // Mengambil teks dari strings.xml lewat kode Kotlin
            val pesan = getString(R.string.profil)
            Toast.makeText(this, "Membuka Menu $pesan", Toast.LENGTH_SHORT).show()
        }

        cardProduk.setOnClickListener {
            Toast.makeText(this, "Membuka Menu Produk", Toast.LENGTH_SHORT).show()
        }

        // Tambahkan aksi untuk card lainnya di sini...
    }
}