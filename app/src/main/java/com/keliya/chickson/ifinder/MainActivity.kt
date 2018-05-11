
package com.keliya.chickson.ifinder

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var ref = FirebaseDatabase.getInstance().getReference("geofire")
    var uref = FirebaseDatabase.getInstance().getReference("services")
    var geoFire = GeoFire(ref)
    var list_of_items = arrayOf("Cleaning Service", "Day Care", "Labour")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, list_of_items)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cattegory_spinner!!.setAdapter(aa)
        pick_location.setOnClickListener { v->
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

        // Check that the result was from the autocomplete widget.
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                val place = PlaceAutocomplete.getPlace(this, data)


                // Format the place's details and display them in the TextView.
                // place_tv.text=place.name
                val queriedLocation = place.getLatLng()
                setLocationInDatabase(service_name.text.toString(),queriedLocation,place.name.toString())
                //Toast.makeText(this, place.name, Toast.LENGTH_SHORT).show()
                pick_location.text=place.name
                // Display attributions if required.

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()

            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setLocationInDatabase(key : String, latLng: LatLng, place:String) {
        geoFire!!.setLocation(key, GeoLocation(latLng.latitude, latLng.longitude), GeoFire.CompletionListener { key, error ->
            if (error == null) {

                //Save detail
                val moreDataProvider: HashMap<String, Any> = hashMapOf("servicename" to key, "location" to place, "category" to cattegory_spinner.selectedItem.toString())
                uref!!.child(key).setValue(moreDataProvider)
                Toast.makeText(this, "Service added Successfully, near "+place+".", Toast.LENGTH_SHORT).show()
                onBackPressed()

            }else {
                Toast.makeText(this, "Service not added.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
