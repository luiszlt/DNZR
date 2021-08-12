package com.example.dnzfind.ui.register

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnzfind.MainActivity
import com.example.dnzfind.data.Dnzr
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class RegisterViewModel : ViewModel() {

    private var _fireAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _db = Firebase.firestore

    private val _toast = MutableLiveData<String?>()
    private val _spinner = MutableLiveData(false)

    private var _currentUser = _fireAuth.currentUser

    val validUser: Boolean get() = (currentUser != null)
    val currentUser: FirebaseUser? get() = _currentUser
    val toast: LiveData<String?> get() = _toast
    val spinner: LiveData<Boolean> get() = _spinner


    companion object {
        const val TAG = "Login ViewModel"
    }

    fun loginUserEmailPass(email: String, password: String, activity: Activity) {
        launchDataLoad {
            try {
                val task = _fireAuth.signInWithEmailAndPassword(email, password).await()

                if (task.user != null) {
                    Log.d(TAG, "Account Login is successful for ${_currentUser?.displayName}")
                    _currentUser = _fireAuth.currentUser
                    startMainActivitiy(activity)
                }

            } catch (e: Exception) {
                Log.d(TAG, "Unable to login user with exception: " + e.message.toString())
                _toast.value = e.message.toString()
            }
        }

    }

    fun createUserEmailPass(
        email: String,
        password: String,
        role: String,
        styles: List<String>,
        activity: Activity
    ) {
        launchDataLoad {
            try {
                val task = _fireAuth.createUserWithEmailAndPassword(email, password).await()

                if (task.user != null) {
                    Log.d(TAG, "Account Creation is successful for ${_currentUser?.displayName}")
                    _currentUser = _fireAuth.currentUser
                    addNewDBUser(_fireAuth.currentUser?.uid.toString(), email, role, styles)

                    startMainActivitiy(activity)
                }

            } catch (e: Exception) {
                Log.d(TAG, "Unable to add user with exception: ${e.message.toString()}")
                _toast.value = e.message.toString()
            }
        }
    }

    fun addNewDBUser(authUser: String, userid: String, role: String, styles: List<String>) {
        try {
            val newUser = getNewUserEntry(userid, role, styles)
            Log.d(
                TAG,
                "Adding new user: $userid with values Role: $role  and this many styles ${styles.size}"
            )
            _db.collection("dancers").document("$authUser").set(newUser)
        } catch (e: Exception) {
            Log.d(TAG, "Unable to add user to the DB with exception : ${e.message.toString()}")
        }
    }

    private fun getNewUserEntry(userid: String, role: String, styles: List<String>): Dnzr {
        return Dnzr(userid, role, styles, "")
    }

    // A placeholder username validation check
    fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }


    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return this.viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _toast.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }

    fun onToastShown() {
        _toast.value = null
    }

    fun startMainActivitiy(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.finish()
    }

    //TODO: Set up styles to be downloaded directly from the database
    /**
    val styles = callbackFlow<List<String>>
    {
    launchDataLoad {
    val result = _db.collection("styles").get().await()

    Log.d(TAG, "Style download started")

    result?.let {
    for (document in it.documents) {
    Log.d(TAG, "Found style : " + document.id.toString())
    _styles.add(document.id)
    }
    }

    }
    offer(_styles)

    awaitClose { }
    }
     */
}