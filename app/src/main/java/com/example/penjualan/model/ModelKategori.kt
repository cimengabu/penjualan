package com.example.penjualan.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ModelKategori(
    var idKategori: String = "",
    var namaKategori: String = "",
    var statusKategori: String = ""
)
