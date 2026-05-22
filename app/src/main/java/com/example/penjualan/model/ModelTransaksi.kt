package com.example.penjualan.model

data class ModelTransaksi(
    var idTransaksi: String? = null,
    var namaProduk: String? = null,
    var kategori: String? = null,
    var jumlah: Int = 0,
    var hargaSatuan: Double = 0.0,
    var totalHarga: Double = 0.0,
    var tanggal: String? = null,
    var kasir: String? = null
)
