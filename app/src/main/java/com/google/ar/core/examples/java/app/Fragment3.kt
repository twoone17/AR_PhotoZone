package com.google.ar.core.examples.java.app

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.tasks.Task
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.*
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class Fragment3 : Fragment(), OnMapReadyCallback {

    //refer link : https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering
    inner class MyItem(
        lat: Double,
        lng: Double,
        title: String,
        snippet: String
    ) : ClusterItem {

        private val position: LatLng
        private val title: String
        private val snippet: String

        override fun getPosition(): LatLng {
            return position
        }

        override fun getTitle(): String? {
            return title
        }

        override fun getSnippet(): String? {
            return snippet
        }

        init {
            position = LatLng(lat, lng)
            this.title = title
            this.snippet = snippet
        }
    }

    // Declare a variable for the cluster manager.
    private lateinit var clusterManager: ClusterManager<MyItem>

    private fun setUpClusterer() {
        // Position the map.
        mGMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.503186, -0.126446), 10f))

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(context, mGMap)

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mGMap.setOnCameraIdleListener(clusterManager)
        mGMap.setOnMarkerClickListener(clusterManager)

        // Add cluster items (markers) to the cluster manager.
        addItems()
    }

    private fun addItems() {

        // Set some lat/lng coordinates to start with.
        var lat = 51.5145160
        var lng = -0.1270060

        // Add ten cluster items in close proximity, for purposes of this example.
        for (i in 0..9) {
            val offset = i / 60.0
            lat += offset
            lng += offset
            val offsetItem =
                MyItem(lat, lng, "Title $i", "Snippet $i")
            clusterManager.addItem(offsetItem)
        }
    }


    private lateinit var db: FirebaseFirestore
    private lateinit var mView: MapView
    private lateinit var mGMap: GoogleMap

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var REQEST_CODE = 101
    var mLocationManager: LocationManager? = null

    var bmp: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = Firebase.firestore

        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_third, container, false)
        mView = rootView.findViewById(R.id.mapView)
        mView.onCreate(savedInstanceState)
        mView.getMapAsync(this)


        mLocationManager = activity!!.baseContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //initial
        /*val initial_loc = LatLng(37.568291, 126.997780)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(initial_loc))*/

        //val marker = LatLng(37.39989, 126.9555049)
        //googleMap.addMarker(MarkerOptions().position(marker).title("initial_marker"))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(20f))

        mGMap = googleMap

        /*
        db.collection("app_board")
            .get()
            .addOnSuccessListener { result->

                var str_url:String = result.documents[0].data?.get("imgURL")?.toString()!!
                val url = URL(str_url)

                val exceptionHandler = CoroutineExceptionHandler{_, exception ->
                    when(exception){
                        is IllegalAccessException -> println("More Argument Needed To PRocess Job")
                        is InterruptedException -> println("Job Interrupted")
                    }
                }

                val deferred = CoroutineScope(Dispatchers.IO).async {
                    throw IllegalArgumentException()
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                }

                CoroutineScope(Dispatchers.IO).launch(exceptionHandler){
                    deferred.await()
                    Log.d("bmp_exception_handler",deferred.toString())
                }
                Log.d("bmp_main",deferred.toString())

                /*val a = GlobalScope.launch (Dispatchers.IO){
                    val bmp = async {  BitmapFactory.decodeStream(url.openConnection().getInputStream()) }
                    Log.d("bmp_scope",bmp.await().toString())
                    bmp
                }*/

                //Log.d("bmp_main",a.toString())

                /*mGMap.addMarker(
                    MarkerOptions().icon(BitmapFactory.bmp).position(LatLng(37.3991309, 126.9376358))
                )*/

                //for(document in result){
                //Log.d("link", document.data["imgURL"].toString())
                //var link = document.data["imgURL"].concat(".png")
                //var url: URL = URL(document.data["imgURL"] as String?)
                //val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                //}
            }*/
        //setUpClusterer()
    }

    private fun url_to_bmp(url: URL): Bitmap? {
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            Log.d("bmp_in_func",bmp.toString())
        return bmp
    }

    override fun onStart() {
        super.onStart()
        getGPSon()
        getUserLocation()
        mView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mView.onStop()
    }

    override fun onResume() {
        super.onResume()
        //updateUserLocation()
        mView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mView.onLowMemory()
    }

    override fun onDestroy() {
        mView.onDestroy()
        super.onDestroy()
    }

    private fun getGPSon() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client:SettingsClient = LocationServices.getSettingsClient(activity!!)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener{ exception ->
            if(exception is ResolvableApiException){
                Log.d("error","GPSONFAILURE")
                try{
                    exception.startResolutionForResult(activity!!, 100)
                } catch (sendEx:IntentSender.SendIntentException){
                    Log.d("error",sendEx.message.toString())
                }
            }
        }
    }

    private fun getUserLocation(){
        Log.d("curLoc","getUserLocation func start")
        //permission check -> 나중에 home fragment 실행으로 변경 필요 : permission check 후 바로 위치 정보 받는 게 안됨
        if(ActivityCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQEST_CODE)
            //ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), REQEST_CODE)

            //return
        }

        //val locationProvider = LocationManager.GPS_PROVIDER
        fusedLocationProviderClient?.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            ?.addOnSuccessListener { location: Location? ->
                if (location == null)
                    Log.d("curLoc","location is null")
                //Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    val lat = location.latitude
                    val lon = location.longitude
                    val now_loc = LatLng(lat, lon)
                    Log.d("curLoc",now_loc.toString())

                    mGMap.moveCamera(CameraUpdateFactory.newLatLng(now_loc))

                }

            }
        //fusedLocationProviderClient?.lastLocation은 실패
    }

    private fun updateUserLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10 // ms단위 , set as 1min


        if(ActivityCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQEST_CODE)
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQEST_CODE)
        }


        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.equals(null)) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val new_loc = LatLng(latitude, latitude)
                        Log.d("Test", "GPS Location changed, Latitude: $latitude" +
                                ", Longitude: $longitude")
                        mGMap.moveCamera(CameraUpdateFactory.newLatLng(new_loc))
                    }
                }
            }
        }

        fusedLocationProviderClient?.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper());
    }
}