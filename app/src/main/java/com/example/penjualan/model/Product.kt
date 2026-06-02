package com.example.penjualan.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    var id: String = "",
    var name: String = "",
    var category: String = "",
    var stock: Int = 0,
    var buyPrice: Double = 0.0,
    var sellPrice: Double = 0.0
)
