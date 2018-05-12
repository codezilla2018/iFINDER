package com.keliya.chickson.ifinder

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_maps.*


class UserMapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 0
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)
        // mDatabase!!.child("users").child(userId).setValue(latLng)
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.draggable(true)
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
       // mCurrLocationMarker = mGoogleMap!!.addMarker(markerOptions)

        if(!isCameraFocused){
            mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            isCameraFocused=true
        }
    }

    private lateinit var mGoogleMap: GoogleMap
    var isCameraFocused:Boolean=false
    //internal var mGoogleMap: GoogleMap? = null
    var ref = FirebaseDatabase.getInstance().getReference("geofire")
    var uref = FirebaseDatabase.getInstance().getReference("services")
    var geoFire = GeoFire(ref)
    lateinit var btn:Button
    var rendered=false
    internal var mapFrag: SupportMapFragment? = null
    internal var mLocationRequest: LocationRequest?=null
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var mLastLocation: Location?=null
    internal var mCurrLocationMarker: Marker? = null
    var list_of_items = arrayOf("Cleaning Service", "Day Care", "Labour")
    var serviceSpinner:Spinner?=null

    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        // These are both view groups containing an ImageView with id "badge" and two
        // TextViews with id "title" and "snippet".
        private val window: View = layoutInflater.inflate(R.layout.custom_info_window, null)
        private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)

        override fun getInfoWindow(marker: Marker): View? {

            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View? {

            render(marker, contents)
            return contents
        }

        private fun render(marker: Marker, view: View) {

            btn=view.findViewById<Button>(R.id.button2)

            // Set the title and snippet for the custom info window
            val title: String? = marker.title
            val titleUi = view.findViewById<TextView>(R.id.title)

            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                titleUi.text = SpannableString(title).apply {
                    setSpan(ForegroundColorSpan(Color.WHITE), 0, length, 0)
                }
            } else {
                titleUi.text = ""
            }

            val snippet: String? = marker.snippet
            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            if (snippet != null ) {
                snippetUi.text = SpannableString(snippet).apply {

                    setSpan(ForegroundColorSpan(Color.WHITE), 0, snippet.length, 0)
                }
            } else {
                snippetUi.text = ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_maps)
        supportActionBar!!.hide()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        Handler().postDelayed({
            // This method will be executed once the timer is over
            updateMap("",true)
        }, 2000)
        btn_select_service.setOnClickListener { v->
            dialodServiceShow()
        }


    }
    fun dialodServiceShow(){
        val dialogView=LayoutInflater.from(this).inflate(R.layout.dialod_service_select,null)
        serviceSpinner=dialogView.findViewById(R.id.spinner)
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, list_of_items)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serviceSpinner!!.setAdapter(aa)
        var title="SELECT A SERVICE"
        val builder=AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton("Search",null)
                .setNegativeButton("Cancel",null)
        val dialog=builder.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
            dialog.dismiss()
            updateMap(serviceSpinner!!.selectedItem.toString(),false)
           //Toast.makeText(this@UserMapsActivity, serviceSpinner!!.selectedItem.toString(), Toast.LENGTH_SHORT).show()
        })
    }

    fun updateMap(selectedIndex:String,firstTime:Boolean){
        mGoogleMap.clear()
        if (firstTime==true) {
            val geoQuery: GeoQuery = geoFire!!.queryAtLocation(GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude), 7.0)
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    uref.addValueEventListener( object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            for (dsp in dataSnapshot.getChildren()) {
                                if(key==dsp.key.toString()) {


                                    Toast.makeText(this@UserMapsActivity, "Services found.", Toast.LENGTH_SHORT).show()
                                    val latlong: LatLng = LatLng(location.latitude, location.longitude)
                                    val marker=mGoogleMap!!.addMarker(MarkerOptions()
                                            .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                            .position(latlong)
                                            .snippet(dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)))

                                }
                            }



                        }
                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value

                            Toast.makeText(this@UserMapsActivity, "not read", Toast.LENGTH_LONG).show()
                        }
                    })


                }

                override fun onKeyExited(key: String) {
                    Log.i("TAG", String.format("Provider %s is no longer in the search area", key))
                    // dialog.hide()
                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                    Log.i("TAG", String.format("Provider %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))
                    // dialog.hide()
                }

                override fun onGeoQueryReady() {
                    Log.i("TAG", "onGeoQueryReady")
                    ////dialog.hide()
                }

                override fun onGeoQueryError(error: DatabaseError) {
                    Log.e("TAG", "error: " + error)
                    // dialog.hide()
                }
            })
        }else{
            val geoQuery: GeoQuery = geoFire!!.queryAtLocation(GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude), 7.0)
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                var found=false
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    uref.addValueEventListener( object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            for (dsp in dataSnapshot.getChildren()) {
                                if(key==dsp.key.toString()){

                                    if(selectedIndex==dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!){
                                        found=true
                                        Toast.makeText(this@UserMapsActivity, selectedIndex+" Found", Toast.LENGTH_SHORT).show()
                                        val latlong: LatLng = LatLng(location.latitude, location.longitude)
                                        val marker=mGoogleMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet(dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)))

                                    }

                                }
                            }
                            if(!found){
                                Toast.makeText(this@UserMapsActivity, "No Services Found.", Toast.LENGTH_SHORT).show()
                            }



                        }
                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value

                            Toast.makeText(this@UserMapsActivity, "not read", Toast.LENGTH_LONG).show()
                        }
                    })

                    // Toast.makeText(this@SeekerActivity, "providers found in range", Toast.LENGTH_SHORT).show()

                }

                override fun onKeyExited(key: String) {
                    Log.i("TAG", String.format("Provider %s is no longer in the search area", key))
                    // dialog.hide()
                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                    Log.i("TAG", String.format("Provider %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))
                    // dialog.hide()
                }

                override fun onGeoQueryReady() {
                    Log.i("TAG", "onGeoQueryReady")
                    ////dialog.hide()
                }

                override fun onGeoQueryError(error: DatabaseError) {
                    Log.e("TAG", "error: " + error)
                    // dialog.hide()
                }
            })
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.setOnInfoWindowClickListener(object  : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker?) {
                //Toast.makeText(this@UserMapsActivity, marker!!.title+" marker clicked", Toast.LENGTH_SHORT).show()
                marker!!.hideInfoWindow()
                val dialogView=LayoutInflater.from(this@UserMapsActivity).inflate(R.layout.dialod_infowindow_clicked,null)
                val serviceType=dialogView.findViewById<TextView>(R.id.textView)
                serviceType.text=marker.snippet
                var title=marker.title
                val builder=AlertDialog.Builder(this@UserMapsActivity)
                        .setView(dialogView)
                        .setTitle(title)
                        .setPositiveButton("Contact",null)
                        .setNegativeButton("Cancel",null)
                val dialog=builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                    dialog.dismiss()
                    val number="0111"
                    val intent=Intent(Intent.ACTION_DIAL)
                    intent.data= Uri.parse("tel:$number")
                    startActivity(intent)
                    Toast.makeText(this@UserMapsActivity, "calling", Toast.LENGTH_SHORT).show()
                })
            }

        })


        try {
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style))
            if (!success) {
               // Log.e(FragmentActivity.TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
           // Log.e(FragmentActivity.TAG, "Can't find style. Error: ", e)
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient()
                mGoogleMap!!.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            buildGoogleApiClient()
            mGoogleMap!!.isMyLocationEnabled = true
        }
        mGoogleMap.setInfoWindowAdapter(CustomInfoWindowAdapter())

    }
    @Synchronized protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient!!.connect()
    }
    companion object{

        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
    override fun onPause() {
        super.onPause()
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@UserMapsActivity,
                                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mGoogleMap!!.isMyLocationEnabled = true
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }
}
