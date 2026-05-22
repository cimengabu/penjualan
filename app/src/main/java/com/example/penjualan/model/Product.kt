package com.example.penjualan.model

data class Product(
    var id: String? = null,
    var name: String? = null,
    var category: String? = null,
    var stock: Int = 0,
    var buyPrice: Double = 0.0,
    var sellPrice: Double = 0.0
)
