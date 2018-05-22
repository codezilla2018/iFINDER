package com.keliya.chickson.ifinder

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_temp_map.*

class TempMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var uTref = FirebaseDatabase.getInstance().getReference("temporyServices")
    lateinit var latLng:LatLng
    lateinit var location:String
    lateinit var category:String
    lateinit var telephone:String
    lateinit var address:String
    lateinit var gKey:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp_map)
        supportActionBar!!.hide()
        gKey=intent.getStringExtra("key")

        Toast.makeText(this, "Drag the pin to exact location and confirm.", Toast.LENGTH_SHORT).show()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val btnEf=ButtonEffects()
        Thread().run(){
            btnEf.buttonEffect(btn_accept_location_confirm)
        }
        btn_accept_location_confirm.setOnClickListener { v->
            val moreDataProvider: HashMap<String, Any> = hashMapOf(
                    "servicename" to gKey,
                    "location" to location,
                    "category" to category,
                    "telephone" to telephone,
                    "address" to address,
                    "latitude" to latLng.latitude.toString(),
                    "longitude" to latLng.longitude.toString())
            uTref!!.child(gKey).setValue(moreDataProvider)

            Toast.makeText(this, "Service added Successfully.", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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
        uTref.addValueEventListener( object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dsp in dataSnapshot.getChildren()) {
                    if(gKey==dsp.key.toString()) {

                        category=dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!
                        latLng = LatLng(dataSnapshot.child(dsp.key.toString()).child("latitude").getValue(String::class.java)!!.toDouble(), dataSnapshot.child(dsp.key.toString()).child("longitude").getValue(String::class.java)!!.toDouble())
                        val marker=mMap!!.addMarker(MarkerOptions()
                                .title(gKey)
                                .position(latLng)
                                .draggable(true)
                                .snippet(category)
                                .icon(BitmapDescriptorFactory.defaultMarker(150f)))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))
                        location=dataSnapshot.child(dsp.key.toString()).child("location").getValue(String::class.java)!!
                        telephone=dataSnapshot.child(dsp.key.toString()).child("telephone").getValue(String::class.java)!!
                        address=dataSnapshot.child(dsp.key.toString()).child("address").getValue(String::class.java)!!
                        category=dataSnapshot.child(dsp.key.toString()).child("category").getValue(String::class.java)!!
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value

                Toast.makeText(this@TempMapActivity, "not read", Toast.LENGTH_LONG).show()
            }
        })
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(p0: Marker?) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(p0!!.position))
                latLng=LatLng(p0!!.position.latitude,p0!!.position.longitude)//To change body of created functions use File | Settings | File Templates.
            }

            override fun onMarkerDragStart(p0: Marker?) {
                p0!!.setIcon(BitmapDescriptorFactory.defaultMarker(50f))
            }

            override fun onMarkerDrag(p0: Marker?) {

            }

        })
    }
}
