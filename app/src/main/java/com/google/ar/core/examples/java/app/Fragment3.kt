package com.google.ar.core.examples.java.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.*
import com.google.ar.core.examples.java.app.board.BoardClickActivity
import com.google.ar.core.examples.java.app.board.BoardData
import com.google.ar.core.examples.java.app.profile.ProfileAdapter
import com.google.ar.core.examples.java.geospatial.ArLikes
import com.google.ar.core.examples.java.geospatial.R
import com.google.ar.core.examples.java.retrofit_rest.MapActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*


class Fragment3 : Fragment(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private lateinit var mView: MapView
    private lateinit var mGMap: GoogleMap
    val datas = mutableListOf<BoardData>()
    private lateinit var currentPosition : LatLng

    private lateinit var marker_root_view : View
    private lateinit var imageView_marker : CircleImageView
    private var markerPerth: Marker? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var REQEST_CODE = 101
    var mLocationManager: LocationManager? = null
    private lateinit var profileAdapter : ProfileAdapter
    var imgURL: String?=null
    var imgURL2: String?=null
    var lat: Double? =0.0
    var lng: Double? =0.0


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
                imgURL = documentSnapshot.get("imgURL") as String
                lat = documentSnapshot.get("latitude") as Double
                lng = documentSnapshot.get("longitude") as Double
                var position = LatLng(lat!!,lng!!)
                Log.e(TAG, "onMapReady: "+ documentSnapshot.id )
                // ????????? ????????? ????????? ?????? ?????? ???????????? ????????? ??? .icon ???????????? xml ????????? ?????????
                Glide.with(this.requireContext()).asBitmap().load(imgURL).fitCenter()
                    .into(object : CustomTarget<Bitmap>(200,200) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            markerPerth = googleMap.addMarker(MarkerOptions()
                                .position(position)
                                .title(documentSnapshot.id)
                                .icon(BitmapDescriptorFactory.fromBitmap(
                                    createDrawableFromView(requireContext(), marker_root_view, resource))))
                            markerPerth?.tag = imgURL
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            markerPerth = googleMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                                .position(position))
                            markerPerth?.tag = imgURL
                        }
                    })
            }
        }

        // ?????? ????????? ????????? ???????????????.
        googleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {

                val customDialog = Dialog(requireContext())
                customDialog.setContentView(R.layout.custom_dialog)

                //????????? ?????? ????????????
                val photozoneName : TextView = customDialog.findViewById(R.id.photozoneName)
                photozoneName.text = p0.title

                //????????? ???????????? ????????? ?????? ????????????
                db.collection("photoZone").document(p0.title!!).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                            imgURL= document.data?.get("imgURL")?.toString()
                            val circle_img : CircleImageView = customDialog.findViewById(R.id.circle_img)
                            Glide.with(requireContext()).load(imgURL.toString()).error(R.drawable.ic_baseline_error_outline_24).centerCrop().into(circle_img)

//                            likes = document.data?.get("likes")?.toString(),
                        } else {
                            Log.d(TAG, "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
                Log.e(TAG, "onMarkerClick: tag"+ tag.toString() )

                //????????? ?????? ????????????
//                val circle_img : CircleImageView = customDialog.findViewById(R.id.circle_img)
//                Glide.with(requireContext()).load(imgURL.toString()).error(R.drawable.ic_baseline_error_outline_24).centerCrop().into(circle_img)

                //????????? ?????? ????????????
                val photozoneDetail : TextView = customDialog.findViewById(R.id.photozoneDetail)
                //?????? ?????? ????????? ?????????
                photozoneDetail.text = "?????? : " + p0.position.latitude.toString().substring(0 until 6) + "    " +
                         "?????? : " + p0.position.longitude.toString().substring(0 until 6)

                val photozoneLikes : TextView = customDialog.findViewById(R.id.photozoneLikes)

                db.collection("photoZone").document(p0.title!!).collection("userLikes").get().addOnSuccessListener { result ->
                    photozoneLikes.text = result.size().toString() + " likes"
                }.addOnFailureListener { error ->
                    Log.e(TAG, "onMarkerClick: " + error )
                    photozoneLikes.text = "Unknown"
                }

                profileAdapter = ProfileAdapter(requireContext())
                profileAdapter.notifyDataSetChanged()
                val recyclerView: RecyclerView = customDialog.recycler_mypost

                profileAdapter.setOnItemClickListener(object : ProfileAdapter.OnItemClickListener{
                    override fun onItemClick(view: View, boardData: BoardData, position: Int) {
                        Intent(requireContext(), BoardClickActivity::class.java).apply {
                            putExtra("boardData", boardData)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.run { startActivity(this) }
                    }
                })
                profileAdapter.notifyDataSetChanged()
                recyclerView.adapter = profileAdapter
                val gm = GridLayoutManager(requireContext(), 2)
                recyclerView.layoutManager = gm
                profileAdapter.datas.clear()
                profileAdapter.notifyDataSetChanged()
                recyclerView.adapter = profileAdapter

                db.collection("photoZone").document(p0.title!!).collection("boardList").get()
                    .addOnSuccessListener { result ->
                        profileAdapter.datas.clear()
                        datas.clear()
                        for (post in result) {
                            datas.apply {
                                add(
                                    BoardData(
                                        imgURL = post.get("imgURL").toString(),
                                        description = post?.get("description").toString(),
//                                        likes = post.get("likes") as Long,
                                        publisher = post.get("publisher").toString(),
                                        userId = post.get("userId").toString(),
                                        documentId = post.id
                                    )
                                )
                            }
                        }
                        profileAdapter.datas = datas
                        profileAdapter.notifyDataSetChanged()

                    }



                val navButton = customDialog.findViewById<ImageButton>(R.id.navigateToPhotozoneButton)
                navButton.setOnClickListener {

                    // ????????? ?????? ??????????????? ??????
                    Log.e("TAG", " " + p0.position.latitude + p0.position.longitude )
                    Log.e("TAG", " " + currentPosition.latitude + currentPosition.longitude )

                    Intent(requireContext(), MapActivity::class.java).apply {
                        putExtra("startLatitude", currentPosition.latitude)
                        putExtra("startLongitude", currentPosition.longitude)
                        putExtra("endLatitude", p0.position.latitude)
                        putExtra("endLongitude", p0.position.longitude)
                        putExtra("photoZoneName", p0.title)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.run { startActivity(this) }
                }

                val likesButton = customDialog.findViewById<ImageButton>(R.id.likesARButton)
                likesButton.setOnClickListener {
                    Intent(context, ArLikes::class.java).apply {
                        putExtra("photoZoneName", p0.title)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.run { startActivity(this) }
                }

                    // Custom Dialog ?????? ??????
                    customDialog.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )



                    // Custom Dialog ?????? ??????
                    customDialog.window?.setGravity(Gravity.TOP)
                    // Custom Dialog ?????? ?????? (????????? ?????? ???????????? ?????? ?????? ?????? ?????????)
                    customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    // Custom Dialog ??????
                    customDialog.show()
                return false
            }
        })
        //?????? ?????? ??? ??????
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(16f))

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
        //permission check -> ????????? home fragment ???????????? ?????? ?????? : permission check ??? ?????? ?????? ?????? ?????? ??? ??????
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
        //fusedLocationProviderClient?.lastLocation??? ??????
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
        locationRequest.interval = 10 // ms?????? , set as 1min


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