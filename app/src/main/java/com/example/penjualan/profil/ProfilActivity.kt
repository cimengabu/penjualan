package com.example.penjualan.profil

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.LoginActivity
import com.example.penjualan.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class ProfilActivity : AppCompatActivity() {

    private val profilRef = FirebaseUtils.getRef("profil")
    private lateinit var auth: FirebaseAuth

    private lateinit var ivAvatarProfil: ImageView
    private lateinit var ivHeaderBackground: ImageView
    private lateinit var tvNamaTokoDisplay: android.widget.TextView
    private lateinit var tvStatProduk: android.widget.TextView
    private lateinit var tvStatCabang: android.widget.TextView
    private lateinit var tvStatPegawai: android.widget.TextView
    private lateinit var etNamaToko: TextInputEditText
    private lateinit var etAlamatToko: TextInputEditText
    private lateinit var etNoTelpToko: TextInputEditText
    private lateinit var etEmailToko: TextInputEditText
    private lateinit var etWebsiteToko: TextInputEditText
    private lateinit var etDeskripsiToko: TextInputEditText

    // Track which image is being picked: "avatar" or "header"
    private var imagePickTarget = "avatar"

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        auth = FirebaseAuth.getInstance()

        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        ivAvatarProfil = findViewById(R.id.ivAvatarProfil)
        ivHeaderBackground = findViewById(R.id.ivHeaderBackground)
        tvNamaTokoDisplay = findViewById(R.id.tvNamaTokoDisplay)
        tvStatProduk = findViewById(R.id.tvStatProduk)
        tvStatCabang = findViewById(R.id.tvStatCabang)
        tvStatPegawai = findViewById(R.id.tvStatPegawai)

        etNamaToko = findViewById(R.id.etNamaToko)
        etAlamatToko = findViewById(R.id.etAlamatToko)
        etNoTelpToko = findViewById(R.id.etNoTelpToko)
        etEmailToko = findViewById(R.id.etEmailToko)
        etWebsiteToko = findViewById(R.id.etWebsiteToko)
        etDeskripsiToko = findViewById(R.id.etDeskripsiToko)

        val btnSimpanProfil: MaterialButton = findViewById(R.id.btnSimpanProfil)
        btnSimpanProfil.setOnClickListener { simpanProfil() }

        val btnLogout: MaterialButton = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener { konfirmasiLogout() }

        // Tap avatar to pick profile photo
        ivAvatarProfil.setOnClickListener {
            imagePickTarget = "avatar"
            imagePickerLauncher.launch("image/*")
        }

        // Tap background to pick background photo
        ivHeaderBackground.setOnClickListener {
            imagePickTarget = "header"
            imagePickerLauncher.launch("image/*")
        }

        loadProfil()
        loadStats()
    }

    private fun loadStats() {
        FirebaseUtils.getRef("produk").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) { tvStatProduk.text = s.childrenCount.toString() }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
        FirebaseUtils.getRef("cabang").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) { tvStatCabang.text = s.childrenCount.toString() }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
        FirebaseUtils.getRef("pegawai").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) { tvStatPegawai.text = s.childrenCount.toString() }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
    }

    private fun processImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Compress & resize
            val maxSize = if (imagePickTarget == "avatar") 256 else 800
            val scaled = scaleBitmap(bitmap, maxSize)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

            if (imagePickTarget == "avatar") {
                ivAvatarProfil.setImageBitmap(scaled)
                profilRef.child("fotoProfil").setValue(base64)
                    .addOnSuccessListener { Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show() }
            } else {
                ivHeaderBackground.setImageBitmap(scaled)
                profilRef.child("fotoHeader").setValue(base64)
                    .addOnSuccessListener { Toast.makeText(this, "Foto latar diperbarui", Toast.LENGTH_SHORT).show() }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val ratio = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        return if (ratio < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else bitmap
    }

    private fun konfirmasiLogout() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Akun?")
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
                    etEmailToko.setText(snapshot.child("emailToko").getValue(String::class.java) ?: "")
                    etWebsiteToko.setText(snapshot.child("websiteToko").getValue(String::class.java) ?: "")
                    etDeskripsiToko.setText(snapshot.child("deskripsiToko").getValue(String::class.java) ?: "")

                    // Update display name
                    val namaDisplay = snapshot.child("namaToko").getValue(String::class.java)
                    if (!namaDisplay.isNullOrEmpty()) tvNamaTokoDisplay.text = namaDisplay

                    // Load avatar photo
                    val fotoProfilBase64 = snapshot.child("fotoProfil").getValue(String::class.java)
                    if (!fotoProfilBase64.isNullOrEmpty()) {
                        val bytes = Base64.decode(fotoProfilBase64, Base64.NO_WRAP)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ivAvatarProfil.setImageBitmap(bmp)
                    }

                    // Load header background photo
                    val fotoHeaderBase64 = snapshot.child("fotoHeader").getValue(String::class.java)
                    if (!fotoHeaderBase64.isNullOrEmpty()) {
                        val bytes = Base64.decode(fotoHeaderBase64, Base64.NO_WRAP)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ivHeaderBackground.setImageBitmap(bmp)
                    }
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
        val email = etEmailToko.text.toString().trim()
        val website = etWebsiteToko.text.toString().trim()
        val deskripsi = etDeskripsiToko.text.toString().trim()

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama toko wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val profilData = mapOf(
            "namaToko" to nama,
            "alamatToko" to alamat,
            "noTelpToko" to telp,
            "emailToko" to email,
            "websiteToko" to website,
            "deskripsiToko" to deskripsi
        )

        profilRef.updateChildren(profilData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
