package com.example.penjualan.model

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val subtotal: Double get() = product.sellPrice * quantity
}
