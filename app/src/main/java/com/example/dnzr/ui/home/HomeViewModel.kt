package com.example.dnzfind.ui.home

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener
import com.example.dnzfind.LoginActivity
import com.example.dnzfind.data.Dnzr
import com.example.dnzfind.data.Role

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application

    //New database references and GeoFireStore references
    private val _db = Firebase.firestore
    private val _fireAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authUser = _fireAuth.currentUser?.uid.toString()
    private val _documentRef = _db.collection("dancers")
    private val geoFirestore = GeoFirestore(_documentRef)

    private val _dnzr = MutableLiveData<Dnzr>()

    val userName: String get() = _fireAuth.currentUser?.email.toString().split("@")[0]
    val dnzr: LiveData<Dnzr> get() = _dnzr

    //dancers is used as a local HashMap that will be sent to the livedata steam
    private var dancers: HashMap<Dnzr, GeoPoint> = hashMapOf<Dnzr, GeoPoint>()


    //LOCATION specific APIs
    private var fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app.applicationContext)

    private val _currentLocation = MutableLiveData<GeoPoint>()

    private val geoQuery = geoFirestore.queryAtLocation(
        _currentLocation.value ?: GeoPoint(
            DEFAULT_LAT, DEFAULT_LONG
        ), DEFAULT_GEOFENCE
    )

    val currentLocation: LiveData<GeoPoint> get() = _currentLocation

    init {
        viewModelScope.launch {
            _dnzr.value = getUser()
            _currentLocation.value = getLastKnownLocationSync(false)
            _currentLocation.value?.let { geoQuery.center = it }
            Log.d(
                TAG,
                "Last known location is gathered, result : " + currentLocation.value.toString()
            )
        }
    }

    /**
     * This method uses the play services library to make any routine that returns a callback
     * synchronous by calling the .await functionality. This is a much better way than creating my own suspend coroutine.
     * implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.1.1'
     */
    private suspend fun getLastKnownLocationSync(overrideFlag: Boolean): GeoPoint? {

        var geoPt: GeoPoint? = null

        if (_currentLocation.value == null || overrideFlag) {
            try {
                val data: Location? = fusedLocationProviderClient.lastLocation.await()
                Log.d(TAG, "Location is back...")
                if (data != null) {
                    geoPt = GeoPoint(data.latitude, data.longitude)
                    geoFirestore.setLocation(_authUser, GeoPoint(data.latitude, data.longitude))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Location could not be loaded: " + e.message.toString())
            }
        }
        return geoPt
    }

    /**
     * This method uses the callbackFlow library to create a flow for GeoLocations
     * awaitClose is required to implement this callbackFlow and offer is used to send to any listeners.
     */
    @ExperimentalCoroutinesApi
    val nearbyDancers = callbackFlow<HashMap<Dnzr, GeoPoint>> {
        //write GeoFire Location to get the users Location

        Log.d(
            TAG,
            "Getting nearby dancers to ${(_currentLocation.value?.latitude)}, ${(_currentLocation.value?.longitude)}"
        )

        geoQuery.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {
            override fun onDocumentChanged(documentSnapshot: DocumentSnapshot, location: GeoPoint) {
                Log.d(TAG, "The key ${documentSnapshot["userName"]} has data changed /n")
            }

            override fun onDocumentEntered(documentSnapshot: DocumentSnapshot, location: GeoPoint) {

                _dnzr.value?.let {

                    if (isDnzrCompatible(documentSnapshot) ) {
                        Log.d(TAG, "The key ${documentSnapshot["userName"]} has entered the query ")
                        location?.let { loc ->
                            dancers.put(snapshotToDnzr(documentSnapshot),loc)
                        }

                    }
                }
            }

            override fun onDocumentExited(documentSnapshot: DocumentSnapshot) {
                Log.d(TAG, "The key ${documentSnapshot["userName"]} has exited the query \n")
                dancers.remove(snapshotToDnzr(documentSnapshot))
            }

            override fun onDocumentMoved(documentSnapshot: DocumentSnapshot, location: GeoPoint) {
                Log.d(TAG, "The key ${documentSnapshot["userName"]} has moved \n")

                if (isDnzrCompatible(documentSnapshot) ) {
                    location?.let { loc ->
                        dancers.put(
                            snapshotToDnzr(documentSnapshot),
                            loc
                        )
                    }
                }
            }

            override fun onGeoQueryError(exception: Exception) {
                Log.d(TAG, "GeoQuery Error : ${exception.message}")
            }

            override fun onGeoQueryReady() {
                Log.d(TAG, "All initial data has been loaded and events have been fired!")
                offer(dancers)
            }

        })

        awaitClose { geoQuery.removeAllListeners() }
    }

    private fun isDnzrCompatible(partnerDnzr : DocumentSnapshot) : Boolean {

        var compatible = false

        _dnzr.value?.let {
            val intersect = it.styles.intersect(partnerDnzr["styles"] as List<String>)
            compatible = ((partnerDnzr["role"] != it.role || partnerDnzr["role"] == Role.BOTH.toString() || it.role == Role.BOTH.toString())
                    && (partnerDnzr.id != _authUser)
                    && intersect.isNotEmpty())
        }

        return compatible

    }

    private fun snapshotToDnzr(dnzr : DocumentSnapshot) : Dnzr{
        return Dnzr(
            dnzr["userName"].toString(),
            dnzr["role"].toString(),
            dnzr["styles"] as List<String>,
            dnzr["status"].toString()
        )
    }

    private suspend fun getUser(): Dnzr? {
        if (_dnzr.value == null) {
            val result =
                _db.collection("dancers").document(_authUser).get()
                    .await()

            result?.let {
                Log.d(
                    TAG,
                    "${it["userName"]} of role ${it["role"]} and ${it["styles"]} number of styles"
                )
                _dnzr.value = snapshotToDnzr(it)
            }
        }

        return _dnzr.value
    }

    fun updateUserRole(role: String) {
        if (role != dnzr.value?.role.toString()) {
            Log.d(TAG, "Updating role to $role")

            _db.collection("dancers").document("$_authUser").update("role", role)
            _dnzr.value?.let { _dnzr.value = Dnzr(it.userName, role, it.styles, it.status) }

        }
    }

    fun updateUserStyles(styles: List<String>) {
        _dnzr.value?.let {
            if (styles == it.styles) {
                Log.d(TAG, "No Changes to Styles")
            } else {
                Log.d(TAG, "Updating styles to $styles")
                _db.collection("dancers").document("$_authUser").update("styles", styles)
                _dnzr.value?.let { _dnzr.value = Dnzr(it.userName, it.role, styles, it.status) }
            }
        }
    }

    fun updateUserStatus(status: String) {
        if (status != _dnzr.value?.status) {
            Log.d(TAG, "Updating status to $status")
            _db.collection("dancers").document("$_authUser").update("status", status)
            _dnzr.value?.let { _dnzr.value = Dnzr(it.userName, it.role, it.styles, status) }
        }
    }


    fun logOut(activity: Activity) {
        _fireAuth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.finish()
    }


    companion object {
        private const val TAG = "Maps View Model" // for debugging
        private const val DEFAULT_LAT = -33.852
        private const val DEFAULT_LONG = 151.211
        private const val DEFAULT_GEOFENCE = 10.0
    }

}

