package com.keliya.chickson.ifinder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
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
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressPie
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
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
        TODO("not implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented")
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        val latLng = LatLng(location.latitude, location.longitude)
        if(first) {
            updateMap("test", true,LatLng(mLastLocation!!.latitude,mLastLocation!!.longitude))
            first=false
        }
        if(!isCameraFocused){
            mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            isCameraFocused=true
        }
    }

    private lateinit var mGoogleMap: GoogleMap
    var isCameraFocused:Boolean=false
    var ref = FirebaseDatabase.getInstance().getReference("geofire")
    var uref = FirebaseDatabase.getInstance().getReference("services")
    var geoFire = GeoFire(ref)
    lateinit var btn:Button
    var first=true

    internal var mLocationRequest: LocationRequest?=null
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var mLastLocation: Location?=null
    internal var mCurrLocationMarker: Marker? = null
    var serviceSpinner:Spinner?=null
    lateinit var showAll:Button
    var backButtonCount = 0
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    lateinit var  dialogs:ACProgressPie
    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
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
            val title: String? = marker.title
            val titleUi = view.findViewById<TextView>(R.id.title)
            if (title != null) {
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
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val btnEffect=ButtonEffects()
        Thread().run{
            btnEffect.buttonEffect(btn_select_service)
            btnEffect.buttonEffect(add_service)
            btnEffect.buttonEffect(search_location)
        }
        btn_select_service.setOnClickListener { v->
            dialodServiceShow()
        }
        add_service.setOnClickListener { v->
            val mPrefs = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
            if(mPrefs.getBoolean("is_first_click_add_services", false)==false){
                val dialogView=LayoutInflater.from(this).inflate(R.layout.dialog_add_services,null)

                val builder=AlertDialog.Builder(this)
                        .setView(dialogView)

                        .setPositiveButton("Ok",null)
                val dialog=builder.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                    dialog.dismiss()
                    val editor = mPrefs.edit()
                    editor.putBoolean("is_first_click_add_services",true)
                    editor.commit()
                    startActivity(Intent(this,AddServicesActivity::class.java))
                })

            }else{
                startActivity(Intent(this,AddServicesActivity::class.java))
            }
        }
        search_location.setOnClickListener({
            autoComplete()
        })
        my_location.setOnClickListener { v->
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(mLastLocation!!.latitude,mLastLocation!!.longitude)))
        }
        admin_login.setOnClickListener { v->
            dialogAdminLogin()
        }
        dialogs = ACProgressPie.Builder(this)
                .ringColor(Color.WHITE)
                .pieColor(Color.WHITE)
                .updateType(ACProgressConstant.PIE_AUTO_UPDATE)
                .build()
        dialogs.show()
    }
    fun autoComplete(){
        try {

            val typeFilter = AutocompleteFilter.Builder()
                    .setCountry("LK")
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(this)


            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {
            // TODO: Handle the error.
        } catch (e: GooglePlayServicesNotAvailableException) {
            // TODO: Handle the error.
            val message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    override  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                val queriedLocation = place.getLatLng()
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(queriedLocation))
                updateMap("",true,queriedLocation)

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Toast.makeText(this, ""+status+" error", Toast.LENGTH_SHORT).show()

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onBackPressed() {
        if (backButtonCount >= 1) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Press the back button once again to close the application.", Toast.LENGTH_LONG).show()
            backButtonCount++
        }

    }
    fun dialodServiceShow(){
        val dialogView=LayoutInflater.from(this).inflate(R.layout.dialod_service_select,null)
        serviceSpinner=dialogView.findViewById(R.id.spinner)
        val aa = ArrayAdapter.createFromResource(this, R.array.category_list, android.R.layout.simple_spinner_item)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serviceSpinner!!.setAdapter(aa)

        if(Build.VERSION.SDK_INT>23)
            serviceSpinner!!.setBackgroundResource(R.drawable.my_spinner)

        showAll=dialogView.findViewById(R.id.button3)



        var title="SELECT A SERVICE"
        val builder=AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton("Search",null)
                .setNegativeButton("Cancel",null)
        val dialog=builder.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
            dialog.dismiss()
            updateMap(serviceSpinner!!.selectedItem.toString(),false,LatLng(mLastLocation!!.latitude,mLastLocation!!.longitude))

        })
        val btnEf=ButtonEffects()
        Thread().run(){
            btnEf.buttonEffect(showAll)
        }
        showAll.setOnClickListener { v->
            updateMap("",true, LatLng(mLastLocation!!.longitude,mLastLocation!!.longitude))
            dialog.dismiss()
        }
    }
    fun dialogAdminLogin(){
        val dialogView=LayoutInflater.from(this).inflate(R.layout.dialog_admin_login,null)
        val tv=dialogView.findViewById<TextInputEditText>(R.id.password)
        var title="Login"
        val builder=AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton("Login",null)
                .setNegativeButton("Cancel",null)
        val dialog=builder.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({

            if(tv.text.toString()=="admin"){
                tv.error=null
                dialog.dismiss()
                startActivity(Intent(this,MainActivity::class.java))
            }else{
                tv.error="Wrong Password.\n(Developing purpose enter password as \"admin\".)"
            }
        })

    }

    fun updateMap(selectedIndex:String,firstTime:Boolean,locationToSearch:LatLng){
        mGoogleMap.clear()
        if (firstTime) {

            val geoQuery: GeoQuery = geoFire!!.queryAtLocation(GeoLocation(locationToSearch.latitude, locationToSearch.longitude), 7.0)
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {

                    uref.addValueEventListener( object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            for (dsp in dataSnapshot.getChildren()) {
                                if(key==dsp.key.toString()) {

                                    val latlong= LatLng(location.latitude, location.longitude)
                                    if(Build.VERSION.SDK_INT>23){
                                        mGoogleMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet(dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!)
                                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                    }else{
                                        mGoogleMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet("(CLICK HERE TO MORE)")
                                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@UserMapsActivity, "not read", Toast.LENGTH_LONG).show()
                        }
                    })


                }

                override fun onKeyExited(key: String) {


                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                    Log.i("TAG", String.format("Provider %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))

                }

                override fun onGeoQueryReady() {
                    Log.i("TAG", "onGeoQueryReady")

                }

                override fun onGeoQueryError(error: DatabaseError) {
                    Log.e("TAG", "error: " + error)

                }

            })

            if(dialogs.isShowing)
                dialogs.hide()
        }else{
            val geoQuery: GeoQuery = geoFire!!.queryAtLocation(GeoLocation(locationToSearch.latitude, locationToSearch.longitude), 7.0)
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
                                        val latlong = LatLng(location.latitude, location.longitude)
                                        if(Build.VERSION.SDK_INT>23){
                                            mGoogleMap!!.addMarker(MarkerOptions()
                                                    .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                    .position(latlong)
                                                    .snippet(dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!)
                                                    .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                        }else{
                                            mGoogleMap!!.addMarker(MarkerOptions()
                                                    .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                    .position(latlong)
                                                    .snippet("(CLICK HERE TO MORE)")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                        }

                                    }

                                }
                            }
                            if(!found){
                                Toast.makeText(this@UserMapsActivity, "No Services Found.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
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
        }

    }
    

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.uiSettings.isMyLocationButtonEnabled=false
        mGoogleMap.setOnInfoWindowClickListener(object  : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker?) {

                marker!!.hideInfoWindow()
                uref.addValueEventListener( object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (dsp in dataSnapshot.getChildren()) {
                            if(marker.title==dsp.key.toString()){
                                val dialogView=LayoutInflater.from(this@UserMapsActivity).inflate(R.layout.dialod_infowindow_clicked,null)
                                val serviceType=dialogView.findViewById<TextView>(R.id.textView)
                                val telephoneNo=dialogView.findViewById<TextView>(R.id.textView2)
                                val addressTV=dialogView.findViewById<TextView>(R.id.address)
                                addressTV.text="Address: "+dataSnapshot.child(dsp.key.toString()).child("address").getValue(String::class.java)!!
                                telephoneNo.text="Tele No: "+dataSnapshot.child(dsp.key.toString()).child("telephone").getValue(String::class.java)!!
                                serviceType.text=dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!
                                var title=marker.title
                                val builder=AlertDialog.Builder(this@UserMapsActivity)
                                        .setView(dialogView)
                                        .setTitle(title)
                                        .setPositiveButton("Contact",null)
                                        .setNegativeButton("Cancel",null)
                                val dialog=builder.show()
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                                    dialog.dismiss()
                                    val number=dataSnapshot.child(dsp.key.toString()).child("telephone").getValue(String::class.java)!!
                                    val intent=Intent(Intent.ACTION_DIAL)
                                    intent.data= Uri.parse("tel:$number")
                                    startActivity(intent)
                                    Toast.makeText(this@UserMapsActivity, "calling", Toast.LENGTH_SHORT).show()
                                })
                            }
                        }



                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserMapsActivity, "not read", Toast.LENGTH_LONG).show()
                    }
                })

            }

        })


        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style))

        } catch (e: Resources.NotFoundException) {

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
        if(Build.VERSION.SDK_INT>23) {
            mGoogleMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        }

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
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                            ActivityCompat.requestPermissions(this@UserMapsActivity,
                                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mGoogleMap!!.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
}
