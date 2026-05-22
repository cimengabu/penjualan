package com.example.penjualan.kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.penjualan.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://penjualan-indah-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val myRef = database.getReference("kategori")

    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var spStatusKategori: AutoCompleteTextView
    private lateinit var btnSimpan: Button

    private var editId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modkategori)

        // Adjust for system status bars
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Bind Views
        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNama)
        spStatusKategori = findViewById(R.id.actStatus)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Setup dropdown statuses
        val statusList = resources.getStringArray(R.array.status_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        spStatusKategori.setAdapter(adapter)
        if (statusList.isNotEmpty()) {
            spStatusKategori.setText(statusList[0], false)
        }

        // Check if editing or adding
        editId = intent.getStringExtra("EXTRA_ID")
        if (editId != null) {
            tvJudul.text = "Ubah Kategori"
            etNamaKategori.setText(intent.getStringExtra("EXTRA_NAMA"))
            spStatusKategori.setText(intent.getStringExtra("EXTRA_STATUS"), false)
            btnSimpan.text = "Perbarui"
        } else {
            tvJudul.text = "Tambah Kategori"
            btnSimpan.text = "Simpan"
        }

        // Handle click save
        btnSimpan.setOnClickListener {
            val nama = etNamaKategori.text.toString().trim()
            val status = spStatusKategori.text.toString()

            if (nama.isEmpty()) {
                etNamaKategori.error = "Nama kategori wajib diisi"
                return@setOnClickListener
            }

            val key = editId ?: myRef.push().key
            if (key != null) {
                val kategoriData = mapOf(
                    "idKategori" to key,
                    "namaKategori" to nama,
                    "statusKategori" to status
                )

                myRef.child(key).setValue(kategoriData)
                    .addOnSuccessListener {
                        val message = if (editId != null) "Kategori berhasil diperbarui" else "Kategori berhasil disimpan"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}
