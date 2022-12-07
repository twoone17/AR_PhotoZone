package com.google.ar.core.examples.java.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.ar.core.examples.java.geospatial.R
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.Button
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.*
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.retrofit_rest.MapActivity
import de.hdodenhof.circleimageview.CircleImageView


class Fragment3 : Fragment(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private lateinit var mView: MapView
    private lateinit var mGMap: GoogleMap

    private lateinit var currentPosition : LatLng

    private lateinit var marker_root_view : View
    private lateinit var imageView_marker : CircleImageView

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var REQEST_CODE = 101
    var mLocationManager: LocationManager? = null

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

        checkPermission();

        return rootView
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {

        setCusomMarkerView()

        var collectionReference : CollectionReference = db.collection("photoZone")

        collectionReference.get().addOnSuccessListener { result ->
            for (documentSnapshot in result) {
                var imgURL = documentSnapshot.get("imgURL") as String
                var lat = documentSnapshot.get("latitude") as Double
                var lng = documentSnapshot.get("longitude") as Double
                var position = LatLng(lat,lng)

                // 커스텀 마커의 이미지 뷰에 먼저 이미지를 넣어준 후 .icon 파트에서 xml 통째로 불러옴
                Glide.with(this.requireContext()).asBitmap().load(imgURL).fitCenter()
                    .into(object : CustomTarget<Bitmap>(200,200) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            googleMap.addMarker(MarkerOptions()
                                .position(position)
                                .icon(BitmapDescriptorFactory.fromBitmap(
                                    createDrawableFromView(requireContext(), marker_root_view, resource))))
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            googleMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                                .position(position))
                        }
                    })
            }
        }

        // 마커 클릭은 여기서 처리합니다.
        googleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {
                // TODO 포토존에 속하는 게시글들의 사진 띄우기
                val customDialog = Dialog(requireContext())
                customDialog.setContentView(R.layout.custom_dialog)

                val navButton = customDialog.findViewById<Button>(R.id.navigateToPhotozoneButton)
                navButton.setOnClickListener {

                    // 도착지 정보 받아오는건 완료
                    Log.e("TAG", " " + p0.position.latitude + p0.position.longitude )
                    Log.e("TAG", " " + currentPosition.latitude + currentPosition.longitude )

                    Intent(requireContext(), MapActivity::class.java).apply {
                        putExtra("startLatitude", currentPosition.latitude)
                        putExtra("startLongitude", currentPosition.longitude)
                        putExtra("endLatitude", p0.position.latitude)
                        putExtra("endLongitude", p0.position.longitude)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.run { startActivity(this) }
                }

                    // Custom Dialog 크기 설정
                    customDialog.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    // Custom Dialog 위치 조절
                    customDialog.window?.setGravity(Gravity.BOTTOM)
                    // Custom Dialog 배경 설정 (다음과 같이 진행해야 좌우 여백 없이 그려짐)
                    customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    // Custom Dialog 표시
                    customDialog.show()
                return false
            }
        })

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(20f))

        mGMap = googleMap
    }

    private fun setCusomMarkerView() {
        val inflater: LayoutInflater = getLayoutInflater()
        marker_root_view = inflater.inflate(R.layout.custom_marker, null)
        imageView_marker = marker_root_view.findViewById(R.id.marker_circle_img)
    }

    private fun createDrawableFromView(context: Context, view: View, resource: Bitmap): Bitmap {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.findViewById<CircleImageView>(R.id.marker_circle_img).setImageBitmap(resource)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
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
                    currentPosition = LatLng(location.latitude, location.longitude)
                    val now_loc = LatLng(lat, lon)
                    Log.d("curLoc",now_loc.toString())

                    mGMap.moveCamera(CameraUpdateFactory.newLatLng(now_loc))

                }

            }
        //fusedLocationProviderClient?.lastLocation은 실패
    }

    private fun checkPermission() {
        if(ActivityCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity!!.baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQEST_CODE)
            //ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), REQEST_CODE)
            //return
        }
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