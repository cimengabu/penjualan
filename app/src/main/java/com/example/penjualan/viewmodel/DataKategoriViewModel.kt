package com.example.penjualan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.penjualan.FirebaseUtils
import com.example.penjualan.model.ModelKategori
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DataKategoriViewModel : ViewModel() {

    private val myRef = FirebaseUtils.getRef("Kategori")

    private val _kategoriList = MutableLiveData<List<ModelKategori>>()
    val kategoriList: LiveData<List<ModelKategori>> get() = _kategoriList

    private var originalKategoriList = ArrayList<ModelKategori>()

    val isLoading = MutableLiveData<Boolean>()
    val isError = MutableLiveData<String?>()

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = ArrayList<ModelKategori>()
                for (data in snapshot.children) {
                    val item = data.getValue(ModelKategori::class.java)
                    item?.let { items.add(it) }
                }

                originalKategoriList = items
                _kategoriList.value = items
                isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                isError.value = error.message
            }
        })
    }

    fun searchKategori(query: String?) {
        if (query.isNullOrBlank()) {
            _kategoriList.value = originalKategoriList
        } else {
            val filteredList = originalKategoriList.filter {
                it.namaKategori?.contains(query, ignoreCase = true) == true
            }
            _kategoriList.value = filteredList
        }
    }
}
