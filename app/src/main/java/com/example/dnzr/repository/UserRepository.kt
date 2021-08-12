package com.example.dnzfind.repository

import android.util.Log
import com.example.dnzfind.data.Dnzr
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val _db = Firebase.firestore
    private val _fireAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authUser = _fireAuth.currentUser?.uid.toString()

    private var _dnzr : Dnzr? = null


    suspend fun getUser(): Dnzr? {
        if (_dnzr == null) {
            val result =
                _db.collection("dancers").document(_authUser).get()
                    .await()

            result?.let {
                Log.d(
                    "UserRepository",
                    "${it["userName"]} of role ${it["role"]} and ${it["styles"]} number of styles"
                )
                _dnzr = Dnzr(
                    it["userName"].toString(),
                    it["role"].toString(),
                    it["styles"] as List<String>,
                    it["status"].toString()
                )
            }
        }

        return _dnzr
    }


}