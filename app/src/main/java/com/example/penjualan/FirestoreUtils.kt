package com.example.penjualan

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference

object FirestoreUtils {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCollection(name: String): CollectionReference = db.collection(name)
}
