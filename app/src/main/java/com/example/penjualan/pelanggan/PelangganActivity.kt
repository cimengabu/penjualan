package com.example.penjualan.pelanggan

import com.example.penjualan.BaseActivity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.LocaleHelper
import com.example.penjualan.R
import com.example.penjualan.model.ModelPelanggan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PelangganActivity : BaseActivity() {

    private val pelangganRef = FirebaseUtils.getRef("pelanggan")

    private lateinit var rvPelanggan: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var tvJumlah: TextView

    private lateinit var adapter: PelangganAdapter
    private val allList = mutableListOf<ModelPelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pelanggan)

        rvPelanggan = findViewById(R.id.rvPelanggan)
        fabTambah   = findViewById(R.id.fabTambahPelanggan)
        etSearch    = findViewById(R.id.etSearchPelanggan)
        tvJumlah    = findViewById(R.id.tvJumlahPelanggan)

        findViewById<ImageView>(R.id.btnBackPelanggan).setOnClickListener { finish() }

        adapter = PelangganAdapter(allList, onEdit = { showDialog(it) }, onDelete = { showDeleteDialog(it) })
        rvPelanggan.layoutManager = LinearLayoutManager(this)
        rvPelanggan.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { applyFilter(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabTambah.setOnClickListener { showDialog(null) }
        fetchPelanggan()
    }

    private fun fetchPelanggan() {
        pelangganRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allList.clear()
                for (data in snapshot.children) {
                    val p = data.getValue(ModelPelanggan::class.java)
                    p?.let { allList.add(it) }
                }
                tvJumlah.text = "${allList.size} pelanggan terdaftar"
                applyFilter(etSearch.text.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PelangganActivity, "Gagal memuat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilter(query: String) {
        val kw = query.trim().lowercase()
        val filtered = if (kw.isEmpty()) allList
        else allList.filter {
            it.namaPelanggan?.lowercase()?.contains(kw) == true ||
            it.noHp?.contains(kw) == true ||
            it.alamat?.lowercase()?.contains(kw) == true
        }
        adapter.updateData(filtered)
    }

    private fun showDialog(pelanggan: ModelPelanggan?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pelanggan, null)
        val etNama    = dialogView.findViewById<TextInputEditText>(R.id.etNamaPelangganDialog)
        val etNoHp    = dialogView.findViewById<TextInputEditText>(R.id.etNoHpPelangganDialog)
        val etAlamat  = dialogView.findViewById<TextInputEditText>(R.id.etAlamatPelangganDialog)
        val etEmail   = dialogView.findViewById<TextInputEditText>(R.id.etEmailPelangganDialog)

        pelanggan?.let {
            etNama.setText(it.namaPelanggan)
            etNoHp.setText(it.noHp)
            etAlamat.setText(it.alamat)
            etEmail.setText(it.email)
        }

        AlertDialog.Builder(this)
            .setTitle(if (pelanggan == null) "Tambah Pelanggan" else "Ubah Pelanggan")
            .setView(dialogView)
            .setPositiveButton(getString(R.string.simpan)) { _, _ ->
                val nama = etNama.text.toString().trim()
                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama pelanggan wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val key = pelanggan?.idPelanggan ?: pelangganRef.push().key ?: return@setPositiveButton
                val data = ModelPelanggan(
                    idPelanggan = key,
                    namaPelanggan = nama,
                    noHp = etNoHp.text.toString().trim(),
                    alamat = etAlamat.text.toString().trim(),
                    email = etEmail.text.toString().trim(),
                    totalTransaksi = pelanggan?.totalTransaksi ?: 0,
                    totalBelanja = pelanggan?.totalBelanja ?: 0.0
                )
                pelangganRef.child(key).setValue(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, if (pelanggan == null) "Pelanggan ditambahkan" else "Data diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }

    private fun showDeleteDialog(pelanggan: ModelPelanggan) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pelanggan")
            .setMessage("Hapus pelanggan \"${pelanggan.namaPelanggan}\"?")
            .setPositiveButton(getString(R.string.hapus)) { _, _ ->
                val id = pelanggan.idPelanggan ?: return@setPositiveButton
                pelangganRef.child(id).removeValue()
                    .addOnSuccessListener { Toast.makeText(this, "Pelanggan dihapus", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }
}
