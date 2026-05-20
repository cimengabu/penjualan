package com.example.yourapp // Sesuaikan dengan package project Anda

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yourapp.databinding.ActivityMainBinding // Nama class binding dihasilkan otomatis dari activity_main.xml

class MainActivity : AppCompatActivity() {

    // Inisialisasi properti untuk view binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Membaca layout XML menggunakan binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- CONTOH MENANGKAP AKSI KLIK PADA MENU DI DALAM CARD ---

        // Pilihan menu Transaksi di dalam Card Besar (card1)
        binding.ivTransaksi.setOnClickListener {
            showToast("Menu Transaksi dipilih")
        }
        binding.tvTransaksi.setOnClickListener {
            showToast("Menu Transaksi dipilih")
        }

        binding.ivLaporan.setOnClickListener {
            showToast("Menu Laporan dipilih")
        }

        // --- CONTOH MENANGKAP AKSI KLIK PADA GRID MENU (CARD 2 - CARD 7) ---

        // Card Profil
        binding.card2.setOnClickListener {
            showToast("Membuka Profil")
        }

        // Card Produk
        binding.card3.setOnClickListener {
            showToast("Membuka Produk")
        }

        // Card Kategori
        binding.card4.setOnClickListener {
            showToast("Membuka Kategori")
        }

        // Card Pegawai
        binding.card5.setOnClickListener {
            showToast("Membuka Data Pegawai")
        }

        // Card Cabang
        binding.card6.setOnClickListener {
            showToast("Membuka Info Cabang")
        }

        // Card Cetak
        binding.card7.setOnClickListener {
            showToast("Membuka Menu Cetak")
        }
    }

    // Fungsi pembantu untuk memunculkan pesan singkat (Toast)
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}