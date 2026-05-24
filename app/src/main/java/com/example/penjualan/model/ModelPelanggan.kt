package com.example.penjualan.model

data class ModelPelanggan(
    var idPelanggan: String? = null,
    var namaPelanggan: String? = null,
    var noHp: String? = null,
    var alamat: String? = null,
    var email: String? = null,
    var totalTransaksi: Int = 0,
    var totalBelanja: Double = 0.0
)
