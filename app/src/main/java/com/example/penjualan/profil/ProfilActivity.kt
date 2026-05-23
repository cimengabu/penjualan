package com.example.penjualan.profil

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.penjualan.LoginActivity
import com.example.penjualan.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfilActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://penjualan-indah-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val profilRef = database.getReference("profil")
    private lateinit var auth: FirebaseAuth

    private lateinit var etNamaToko: TextInputEditText
    private lateinit var etAlamatToko: TextInputEditText
    private lateinit var etNoTelpToko: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        auth = FirebaseAuth.getInstance()

        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        etNamaToko = findViewById(R.id.etNamaToko)
        etAlamatToko = findViewById(R.id.etAlamatToko)
        etNoTelpToko = findViewById(R.id.etNoTelpToko)

        val btnSimpanProfil: MaterialButton = findViewById(R.id.btnSimpanProfil)
        btnSimpanProfil.setOnClickListener {
            simpanProfil()
        }

        val btnLogout: MaterialButton = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            konfirmasiLogout()
        }

        loadProfil()
    }

    private fun konfirmasiLogout() {
        AlertDialog.Builder(this)
            .setTitle("🐻 Keluar Akun?")
            .setMessage("Yakin ingin keluar dari akun ini?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadProfil() {
        profilRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    etNamaToko.setText(snapshot.child("namaToko").getValue(String::class.java) ?: "")
                    etAlamatToko.setText(snapshot.child("alamatToko").getValue(String::class.java) ?: "")
                    etNoTelpToko.setText(snapshot.child("noTelpToko").getValue(String::class.java) ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfilActivity, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun simpanProfil() {
        val nama = etNamaToko.text.toString().trim()
        val alamat = etAlamatToko.text.toString().trim()
        val telp = etNoTelpToko.text.toString().trim()

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama toko wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val profilData = mapOf(
            "namaToko" to nama,
            "alamatToko" to alamat,
            "noTelpToko" to telp
        )

        profilRef.setValue(profilData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
