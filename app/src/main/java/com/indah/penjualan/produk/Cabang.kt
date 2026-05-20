package com.example.inventoryapp.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapp.adapters.CabangAdapter
import com.example.inventoryapp.databinding.ActivityCabangBinding
import com.example.inventoryapp.models.Cabang
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CabangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCabangBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: CabangAdapter
    private val cabangList = mutableListOf<Cabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCabangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadCabang()

        binding.btnTambahCabang.setOnClickListener {
            showAddBranchDialog()
        }

        binding.btnRefresh.setOnClickListener {
            loadCabang()
        }
    }

    private fun setupRecyclerView() {
        adapter = CabangAdapter(cabangList,
            onEditClick = { cabang -> showEditBranchDialog(cabang) },
            onDeleteClick = { cabang -> deleteBranch(cabang) }
        )
        binding.rvCabang.layoutManager = LinearLayoutManager(this)
        binding.rvCabang.adapter = adapter
    }

    private fun loadCabang() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("cabang")
                    .whereEqualTo("userId", userId)
                    .orderBy("nama")
                    .get()
                    .await()

                cabangList.clear()
                cabangList.addAll(snapshot.toObjects(Cabang::class.java))
                adapter.notifyDataSetChanged()

                binding.tvTotalCabang.text = "Total Cabang: ${cabangList.size}"
            } catch (e: Exception) {
                Toast.makeText(this@CabangActivity, "Gagal load cabang", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddBranchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_cabang, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaCabang)
        val etAlamat = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAlamat)
        val etTelepon = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelepon)

        AlertDialog.Builder(this)
            .setTitle("Tambah Cabang Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val alamat = etAlamat.text.toString()
                val telepon = etTelepon.text.toString()

                if (nama.isNotEmpty()) {
                    saveBranch(nama, alamat, telepon)
                } else {
                    Toast.makeText(this, "Nama cabang harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveBranch(nama: String, alamat: String, telepon: String) {
        val userId = auth.currentUser?.uid ?: return
        val cabang = Cabang(
            id = firestore.collection("cabang").document().id,
            userId = userId,
            nama = nama,
            alamat = alamat,
            telepon = telepon,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("cabang").document(cabang.id).set(cabang)
            .addOnSuccessListener {
                Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                loadCabang()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan cabang", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditBranchDialog(cabang: Cabang) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_cabang, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaCabang)
        val etAlamat = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAlamat)
        val etTelepon = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTelepon)

        etNama.setText(cabang.nama)
        etAlamat.setText(cabang.alamat)
        etTelepon.setText(cabang.telepon)

        AlertDialog.Builder(this)
            .setTitle("Edit Cabang")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val namaBaru = etNama.text.toString()
                val alamatBaru = etAlamat.text.toString()
                val teleponBaru = etTelepon.text.toString()

                updateBranch(cabang.id, namaBaru, alamatBaru, teleponBaru)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateBranch(id: String, nama: String, alamat: String, telepon: String) {
        val updates = mapOf(
            "nama" to nama,
            "alamat" to alamat,
            "telepon" to telepon
        )

        firestore.collection("cabang").document(id).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cabang berhasil diupdate", Toast.LENGTH_SHORT).show()
                loadCabang()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update cabang", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteBranch(cabang: Cabang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Cabang")
            .setMessage("Apakah Anda yakin ingin menghapus ${cabang.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                firestore.collection("cabang").document(cabang.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cabang berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadCabang()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus cabang", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}