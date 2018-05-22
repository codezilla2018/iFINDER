package com.keliya.chickson.ifinder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_services.*
import kotlinx.android.synthetic.main.activity_main.*


class AddServicesActivity : AppCompatActivity() {
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var uref = FirebaseDatabase.getInstance().getReference("temporyServices")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_services)
        val aa = ArrayAdapter.createFromResource(this, R.array.category_list, android.R.layout.simple_spinner_item)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cattegory_spinner_add_services!!.setAdapter(aa)
        if(Build.VERSION.SDK_INT>23)
            cattegory_spinner_add_services!!.setBackgroundResource(R.drawable.my_spinner_3)
        val btnEf=ButtonEffects()
        Thread().run{
            btnEf.buttonEffect(pick_location_add_services)

        }
        pick_location_add_services.setOnClickListener { v->
            if(validateForm()) {
                autoComplete()
            }
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
                setLocationInDatabase(service_name_add_services.text.toString(), queriedLocation, place.name.toString())
                pick_location_add_services.text = place.name
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Toast.makeText(this, ""+status+" error", Toast.LENGTH_SHORT).show()

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onResume() {
        super.onResume()
        service_name_add_services.setText("")
        address_add_services.setText("")
        telephone_add_services.setText("")
        pick_location_add_services.text="CHOOSE LOCATION"

    }
    private fun setLocationInDatabase(key : String, latLng: LatLng, place:String) {
        val moreDataProvider: HashMap<String, Any> = hashMapOf("servicename" to key,
                "location" to place,
                "category" to cattegory_spinner_add_services.selectedItem.toString(),
                "telephone" to telephone_add_services.text.toString(),
                "address" to address_add_services.text.toString(),
                "latitude" to latLng.latitude.toString(),
                "longitude" to latLng.longitude.toString())

        uref!!.child(key).setValue(moreDataProvider)
        val intent=Intent(this,TempMapActivity::class.java)
        intent.putExtra("key",key)
        startActivity(intent)

    }
    fun validateForm():Boolean{
        var valid =true
        val serviceName=service_name_add_services.text.toString().trim()
        if (TextUtils.isEmpty(serviceName)) {
            text_field_boxes_add_Services.setError("Required.", false)
            valid = false
        }
        val serviceAddress=address_add_services.text.toString().trim()
        if (TextUtils.isEmpty(serviceAddress)) {
            text_field_boxes_add_Services_2.setError("Required.", false)
            valid = false
        }
        val serviceTelephone=telephone_add_services.text.toString().trim()
        if (TextUtils.isEmpty(serviceTelephone)) {
            text_field_boxes_add_Services_3.setError("Required.", false)
            valid = false
        }
        val regexStr="""^[0-9]{10}$""".toRegex()
        if(telephone_add_services.text.toString().matches(regexStr)==false){

            text_field_boxes_add_Services_3.setError("Mobile number should contain 10 digits.", false)
            valid = false
        }else{

            val regPhone="""^07[0-9]{8}$""".toRegex()
            if(telephone_add_services.text.toString().matches(regPhone)==false){


                text_field_boxes_add_Services_3.setError("Mobile Number Should start from 07", false)
                valid = false
            }
        }

        return valid
    }
}

