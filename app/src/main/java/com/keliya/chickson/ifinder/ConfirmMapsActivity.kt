package com.keliya.chickson.ifinder

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ConfirmMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    var uTref = FirebaseDatabase.getInstance().getReference("temporyServices")
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
        setContentView(R.layout.activity_confirm_maps)
        supportActionBar!!.hide()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,7f))
        uTref.addValueEventListener( object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dsp in dataSnapshot.getChildren()) {


                    val latlong = LatLng(dataSnapshot.child(dsp.key.toString()).child("latitude").getValue(String::class.java)!!.toDouble(),
                            dataSnapshot.child(dsp.key.toString()).child("longitude").getValue(String::class.java)!!.toDouble())
                        mMap!!.addMarker(MarkerOptions()
                                .title(dataSnapshot.child(dsp.key.toString()).child("servicename").getValue(String::class.java)!!)
                                .position(latlong)
                                .snippet("(CLICK HERE TO MORE)")
                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))

                }
            }
        })
        mMap.setOnInfoWindowClickListener(object  : GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker?) {

                marker!!.hideInfoWindow()
                uTref.addValueEventListener( object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (dsp in dataSnapshot.getChildren()) {
                            if(marker.title==dsp.key.toString()){
                                val dialogView= LayoutInflater.from(this@ConfirmMapsActivity).inflate(R.layout.dialod_infowindow_clicked,null)
                                val serviceType=dialogView.findViewById<TextView>(R.id.textView)
                                val telephoneNo=dialogView.findViewById<TextView>(R.id.textView2)
                                val addressTV=dialogView.findViewById<TextView>(R.id.address)
                                addressTV.text="Address: "+dataSnapshot.child(dsp.key.toString()).child("address").getValue(String::class.java)!!
                                telephoneNo.text="Tele No: "+dataSnapshot.child(dsp.key.toString()).child("telephone").getValue(String::class.java)!!
                                serviceType.text=dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!
                                var title=marker.title
                                val builder= AlertDialog.Builder(this@ConfirmMapsActivity)
                                        .setView(dialogView)
                                        .setTitle(title)
                                        .setPositiveButton("Confirm",null)
                                        .setNegativeButton("Ignore",null)
                                val dialog=builder.show()
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                                    dialog.dismiss()
                                    geoFire!!.setLocation(title.toString(), GeoLocation(dataSnapshot.child(dsp.key.toString()).child("latitude").getValue(String::class.java)!!.toDouble(),dataSnapshot.child(dsp.key.toString()).child("longitude").getValue(String::class.java)!!.toDouble()), GeoFire.CompletionListener { key, error ->
                                        if (error == null) {

                                            val moreDataProvider: HashMap<String, Any> = hashMapOf(
                                                    "servicename" to title.toString(),
                                                    "location" to dataSnapshot.child(dsp.key.toString()).child("location").getValue(String::class.java)!!,
                                                    "category" to dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!,
                                                    "telephone" to dataSnapshot.child(dsp.key.toString()).child("telephone").getValue(String::class.java)!!,
                                                    "address" to dataSnapshot.child(dsp.key.toString()).child("address").getValue(String::class.java)!!,
                                                    "latitude" to dataSnapshot.child(dsp.key.toString()).child("latitude").getValue(String::class.java)!!,
                                                    "longitude" to dataSnapshot.child(dsp.key.toString()).child("longitude").getValue(String::class.java)!!)
                                            uref!!.child(title.toString()).setValue(moreDataProvider)
                                            uTref.child(marker.title.toString()).removeValue()
                                            Toast.makeText(this@ConfirmMapsActivity, "Service added Successfully.", Toast.LENGTH_SHORT).show()


                                            onBackPressed()

                                        }else {
                                            Toast.makeText(this@ConfirmMapsActivity, "Service not added.", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener({
                                        uTref.child(marker.title.toString()).removeValue()
                                    })

                                    Toast.makeText(this@ConfirmMapsActivity, "calling", Toast.LENGTH_SHORT).show()
                                })
                            }
                        }



                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ConfirmMapsActivity, "not read", Toast.LENGTH_LONG).show()
                    }
                })

            }

        })
    }
}
