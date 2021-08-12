package com.example.dnzfind.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.dnzfind.R
import com.example.dnzfind.adapters.DnzrAdapter
import com.example.dnzfind.data.Dnzr
import com.example.dnzfind.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.InternalCoroutinesApi


class HomeFragment : Fragment(), OnMapReadyCallback {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var _map: GoogleMap
    private var isMapReady = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val map get() = _map


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

        mapFragment?.getMapAsync(this)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DnzrAdapter()
        binding.recyclerView.adapter = adapter

        homeViewModel.currentLocation.observe(viewLifecycleOwner) {
            updateMyLocation(it)
        }

        homeViewModel.nearbyDancers.asLiveData().observe(viewLifecycleOwner){
            val dnzrList : List<Dnzr> = it.keys.toList()

            adapter.submitList(dnzrList)
            updateNearbyDancers(it)
        }
    }

    @InternalCoroutinesApi
    override fun onMapReady(googleMap: GoogleMap) {
        _map = googleMap
        isMapReady = true
    }

    private fun updateMyLocation(geoPt: GeoPoint) {
        if (isMapReady) {
            Log.d(TAG, "Updating Map to current Location")
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(geoPt.latitude, geoPt.longitude))
                    .title("You are here")
            )
        }
    }

    private fun updateNearbyDancers(dancers: HashMap<Dnzr, GeoPoint>) {

        //if the HashMap is not null, add the markers
        dancers?.let {
            for (key in dancers.keys) {
                dancers[key]?.let { ky ->
                    map.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                ky.latitude,
                                ky.longitude
                            )
                        ).title(key.userName.split("@")[0])
                    )
                    Log.d(TAG, "Added marker for ${key.userName} at ${ky.latitude} , ${ky.longitude}")
                }
            }
        }

        // Move the camera to the marker, the zoom ranges from 2(zoomed out) and 21(zoomed in).
        homeViewModel.currentLocation.value?.let {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                 LatLng(it.latitude, it.longitude) ,
                DEFAULT_ZOOM))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        private const val TAG = "Maps Fragment" // for debugging
        private const val DEFAULT_ZOOM = 13.toFloat()
    }

}