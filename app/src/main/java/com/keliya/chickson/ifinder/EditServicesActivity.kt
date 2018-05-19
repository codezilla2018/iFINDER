package com.keliya.chickson.ifinder

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
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
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
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
import kotlinx.android.synthetic.main.activity_edit_services.*

class EditServicesActivity : AppCompatActivity(), OnMapReadyCallback {
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var mMap: GoogleMap
    var ref = FirebaseDatabase.getInstance().getReference("geofire")
    var uref = FirebaseDatabase.getInstance().getReference("services")
    var geoFire = GeoFire(ref)
    lateinit var btn:Button

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
        setContentView(R.layout.activity_edit_services)
        supportActionBar!!.hide()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val btnEf=ButtonEffects()
        Thread().run{
            btnEf.buttonEffect(btn_select_location)
        }
        btn_select_location.setOnClickListener { v->
            autoComplete()
        }
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
                updateMap(queriedLocation,false)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(queriedLocation,15f))


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                PlaceAutocomplete.getStatus(this, data)
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style))

        } catch (e: Resources.NotFoundException) {
            // Log.e(FragmentActivity.TAG, "Can't find style. Error: ", e)
        }
        val latlng= LatLng(7.2906, 80.6337)
        updateMap(latlng,true)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,7f))
        mMap.setOnInfoWindowClickListener(object  : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker?) {

                marker!!.hideInfoWindow()
                uref.addValueEventListener( object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (dsp in dataSnapshot.getChildren()) {
                            if(marker.title==dsp.key.toString()){
                                var title=marker.title
                                val builder= AlertDialog.Builder(this@EditServicesActivity)
                                        .setTitle(title)
                                        .setPositiveButton("Edit",null)
                                        .setNegativeButton("Delete",null)
                                val dialog=builder.show()
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                                    dialog.dismiss()
                                    var latlng:LatLng?=null
                                    uref.addValueEventListener( object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            for (dsp in dataSnapshot.getChildren()) {
                                                if(marker.title.toString()==dsp.key.toString()) {
                                                     latlng=LatLng(dataSnapshot.child(dsp.key.toString()).child("latitude").getValue(String::class.java)!!.toDouble(),
                                                            dataSnapshot.child(dsp.key.toString()).child("longitude").getValue(String::class.java)!!.toDouble())

                                                }
                                            }

                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(this@EditServicesActivity, "not read", Toast.LENGTH_LONG).show()
                                        }
                                    })
                                    uref.child(marker.title.toString()).removeValue()
                                    geoFire.removeLocation(marker.title.toString(),object :GeoFire.CompletionListener{
                                        override fun onComplete(key: String?, error: DatabaseError?) {
                                            dialodServiceShow(marker.title.toString(),latlng!!)
                                        }

                                    })

                                })
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
                                    dialog.dismiss()
                                    uref.child(marker.title.toString()).removeValue()
                                    geoFire.removeLocation(marker.title.toString(),object :GeoFire.CompletionListener{
                                        override fun onComplete(key: String?, error: DatabaseError?) {
                                            updateMap(latlng,true)
                                            Toast.makeText(this@EditServicesActivity, "Service is deleted Successfully.", Toast.LENGTH_SHORT).show()
                                        }

                                    })


                                })
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@EditServicesActivity, "not read", Toast.LENGTH_LONG).show()
                    }
                })

            }

        })

        if(Build.VERSION.SDK_INT>23) {
            mMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
        }
    }
    fun updateMap(latLng: LatLng,firstTime:Boolean){
        mMap.clear()
        if (firstTime==true) {
            val geoQuery: GeoQuery = geoFire!!.queryAtLocation(GeoLocation(latLng.latitude, latLng.longitude), 300.0)
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    uref.addValueEventListener( object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            for (dsp in dataSnapshot.getChildren()) {
                                if(key==dsp.key.toString()) {

                                    val latlong = LatLng(location.latitude, location.longitude)
                                    if(Build.VERSION.SDK_INT>23){
                                        mMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet(dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!)
                                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                    }else{
                                        mMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet("(CLICK HERE TO MORE)")
                                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                    }
                                }
                            }



                        }
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@EditServicesActivity, "not read", Toast.LENGTH_LONG).show()
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
            val geoQuery: GeoQuery = geoFire!!.queryAtLocation(GeoLocation(latLng!!.latitude, latLng!!.longitude), 7.0)
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {

                override fun onKeyEntered(key: String, location: GeoLocation) {
                    uref.addValueEventListener( object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            for (dsp in dataSnapshot.getChildren()) {
                                if(key==dsp.key.toString()){
                                        val latlong = LatLng(location.latitude, location.longitude)
                                    if(Build.VERSION.SDK_INT>23){
                                        mMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet(dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!)
                                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                    }else{
                                        mMap!!.addMarker(MarkerOptions()
                                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                                .position(latlong)
                                                .snippet("(CLICK HERE TO MORE)")
                                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                                    }

                                }
                            }



                        }
                        override fun onCancelled(error: DatabaseError) {

                            Toast.makeText(this@EditServicesActivity, "not read", Toast.LENGTH_LONG).show()
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
    fun dialodServiceShow(key:String,latlng:LatLng){
        val dialogView=LayoutInflater.from(this).inflate(R.layout.edit_service_dialog,null)
        val serviceSpinner=dialogView.findViewById<Spinner>(R.id.cattegory_spinner_dialog)
        if(Build.VERSION.SDK_INT>23)
            serviceSpinner.setBackgroundResource(R.drawable.my_spinner_3)
        val serviceName=dialogView.findViewById<TextInputEditText>(R.id.service_name_dialog)
        serviceName.setText(key)
        val address=dialogView.findViewById<TextInputEditText>(R.id.address_service_dialog)
        val telephone=dialogView.findViewById<TextInputEditText>(R.id.telephone_service_dialog)
        val PickLocBtn=dialogView.findViewById<Button>(R.id.pick_location_dialog)
        val btnef=ButtonEffects()
        Thread().run(){
            btnef.buttonEffect(PickLocBtn)
        }
        val aa = ArrayAdapter.createFromResource(this, R.array.category_list, android.R.layout.simple_spinner_item)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serviceSpinner!!.setAdapter(aa)

        val builder=AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(title)

        val dialog=builder.show()

        PickLocBtn.setOnClickListener { v->
            dialog.dismiss()
            setLocationInDatabase(serviceName.text.toString(),latlng,"",address.text.toString(),telephone.text.toString(),serviceSpinner.selectedItem.toString())
        }


    }
    override fun onBackPressed() {
        startActivity(Intent(this,MainActivity::class.java))

    }


    private fun setLocationInDatabase(key : String, latLng: LatLng, place:String,addres:String,telephone:String,category:String) {
        val moreDataProvider: HashMap<String, Any> = hashMapOf("servicename" to key,
                "location" to place,
                "category" to category,
                "telephone" to telephone,
                "address" to addres,
                "latitude" to latLng.latitude.toString(),
                "longitude" to latLng.longitude.toString())

        uref!!.child(key).setValue(moreDataProvider)
        val intent=Intent(this,MapsActivity::class.java)
        intent.putExtra("key",key)
        startActivity(intent)

    }

}
