package com.example.penjualan

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {
    private val database = FirebaseDatabase.getInstance(
        "https://penjualan-indah-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    /**
     * Returns a DatabaseReference scoped to the currently logged-in user.
     * Path: users/{uid}/{path}
     * This ensures data is isolated between different user accounts.
     */
    fun getRef(path: String): DatabaseReference {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: "guest"
        return database.getReference("users").child(uid).child(path)
    }

    fun getRootRef(): DatabaseReference = database.reference
}
