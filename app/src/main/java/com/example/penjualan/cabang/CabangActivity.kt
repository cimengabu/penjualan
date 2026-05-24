package com.example.penjualan.cabang

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.R
import com.example.penjualan.model.ModelCabang
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.example.penjualan.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CabangActivity : AppCompatActivity() {

    private val cabangRef = FirebaseUtils.getRef("cabang")

    private lateinit var btnBack: ImageView
    private lateinit var rvCabang: RecyclerView
    private lateinit var fabTambahCabang: FloatingActionButton
    private lateinit var searchView: SearchView

    private lateinit var adapter: CabangAdapter
    private val cabangList = ArrayList<ModelCabang>()
    private val originalList = ArrayList<ModelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cabang)

        btnBack = findViewById(R.id.btnBack)
        rvCabang = findViewById(R.id.rvCabang)
        fabTambahCabang = findViewById(R.id.fabTambahCabang)
        searchView = findViewById(R.id.searchView)

        btnBack.setOnClickListener { finish() }

        adapter = CabangAdapter(
            cabangList,
            onEditClick = { showAddOrEditDialog(it) },
            onDeleteClick = { showDeleteDialog(it) }
        )
        rvCabang.layoutManager = LinearLayoutManager(this)
        rvCabang.adapter = adapter

        fetchCabang()

        fabTambahCabang.setOnClickListener { showAddOrEditDialog(null) }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { filter(query); return true }
            override fun onQueryTextChange(newText: String?): Boolean { filter(newText); return true }
        })
    }

    private fun fetchCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangList.clear()
                originalList.clear()
                for (data in snapshot.children) {
                    val c = data.getValue(ModelCabang::class.java)
                    c?.let { cabangList.add(it); originalList.add(it) }
                }
                adapter.updateData(cabangList)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CabangActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrBlank()) originalList
        else originalList.filter { it.namaCabang?.contains(query, ignoreCase = true) == true }
        adapter.updateData(ArrayList(filtered))
    }

    private fun showAddOrEditDialog(cabang: ModelCabang?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_cabang, null)
        val etNama = dialogView.findViewById<TextInputEditText>(R.id.etNamaCabang)
        val etAlamat = dialogView.findViewById<TextInputEditText>(R.id.etAlamat)
        val etNoTelp = dialogView.findViewById<TextInputEditText>(R.id.etNoTelp)
        val actStatus = dialogView.findViewById<AutoCompleteTextView>(R.id.actStatusCabang)

        val statusOptions = arrayOf("Aktif", "Nonaktif")
        actStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions))

        if (cabang != null) {
            etNama.setText(cabang.namaCabang)
            etAlamat.setText(cabang.alamat)
            etNoTelp.setText(cabang.noTelp)
            actStatus.setText(cabang.statusCabang, false)
        } else {
            actStatus.setText(statusOptions[0], false)
        }

        AlertDialog.Builder(this)
            .setTitle(if (cabang != null) "Ubah Cabang" else "Tambah Cabang")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama cabang wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val key = cabang?.idCabang ?: cabangRef.push().key ?: return@setPositiveButton
                val data = ModelCabang(
                    idCabang = key,
                    namaCabang = nama,
                    alamat = etAlamat.text.toString().trim(),
                    noTelp = etNoTelp.text.toString().trim(),
                    statusCabang = actStatus.text.toString()
                )
                cabangRef.child(key).setValue(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, if (cabang != null) "Cabang diperbarui" else "Cabang ditambahkan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteDialog(cabang: ModelCabang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Cabang")
            .setMessage("Hapus cabang \"${cabang.namaCabang}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                val id = cabang.idCabang ?: return@setPositiveButton
                cabangRef.child(id).removeValue()
                    .addOnSuccessListener { Toast.makeText(this, "Cabang dihapus", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
