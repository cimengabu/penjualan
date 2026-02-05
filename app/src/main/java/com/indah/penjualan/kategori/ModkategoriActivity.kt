package com.indah.penjualan

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class TambahKategoriActivity : AppCompatActivity() {

    // Inisialisasi Firebase Database
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("com/indah/penjualan/kategori")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modkategori)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etNamaKategori = findViewById<EditText>(R.id.etNamaKategori)
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerStatus)
        val btnSimpan = findViewById<MaterialButton>(R.id.btnSimpan)

        // Setup Spinner
        val listStatus = arrayOf("Aktif", "Tidak Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listStatus)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val namaKategori = etNamaKategori.text.toString().trim()
            val statusTerpilih = spinnerStatus.selectedItem.toString()

            if (namaKategori.isEmpty()) {
                etNamaKategori.error = "Nama kategori tidak boleh kosong"
            } else {
                simpanData(namaKategori, statusTerpilih)
            }
        }
    }

    private fun simpanData(nama: String, status: String) {
        // Membuat ID unik untuk setiap kategori baru
        val idKategori = myRef.push().key ?: return

        // Membuat objek Map untuk data
        val dataKategori = mapOf(
            "id" to idKategori,
            "nama" to nama,
            "status" to status
        )

        // Mengirim ke Firebase
        myRef.child(idKategori).setValue(dataKategori)
            .addOnSuccessListener {
                Toast.makeText(this, "Data $nama berhasil disimpan ke Cloud", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan: ${it.message}", Toast.LENGTH_LONG).show()
            }

    }
    )
}