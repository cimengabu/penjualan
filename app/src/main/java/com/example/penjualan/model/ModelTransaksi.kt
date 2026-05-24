package com.example.penjualan.model

data class ModelTransaksi(
    var idTransaksi: String? = null,
    var nomorNota: String? = null,
    // Backward compat fields (single item)
    var namaProduk: String? = null,
    var kategori: String? = null,
    var jumlah: Int = 0,
    var hargaSatuan: Double = 0.0,
    // Multi-item list (serialized as Map)
    var items: Map<String, ItemTransaksi> = emptyMap(),
    // Totals
    var subtotal: Double = 0.0,
    var diskon: Double = 0.0,
    var pajak: Double = 0.0,
    var totalHarga: Double = 0.0,
    // Payment
    var metodePembayaran: String? = null,
    var jumlahBayar: Double = 0.0,
    var kembalian: Double = 0.0,
    // People
    var idPelanggan: String? = null,
    var namaPelanggan: String? = null,
    var kasir: String? = null,
    // Timestamps
    var tanggal: String? = null,
    var waktu: String? = null,
    var timestamp: Long = 0L,
    // Status
    var status: String? = "Selesai"
)

data class ItemTransaksi(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var kategori: String? = null,
    var hargaSatuan: Double = 0.0,
    var jumlah: Int = 0,
    var subtotal: Double = 0.0
)

