
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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    var uref = FirebaseDatabase.getInstance().getReference("services")
    var backButtonCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        val aa = ArrayAdapter.createFromResource(this, R.array.category_list, android.R.layout.simple_spinner_item)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cattegory_spinner!!.setAdapter(aa)
        if(Build.VERSION.SDK_INT>23)
            cattegory_spinner!!.setBackgroundResource(R.drawable.my_spinner_3)
        val btnEf=ButtonEffects()
        Thread().run{
            btnEf.buttonEffect(pick_location)
            btnEf.buttonEffect(setting_btn)
            btnEf. buttonEffect(confirm_btn)
            btnEf. buttonEffect(delete_btn)
        }
        confirm_btn.setOnClickListener { v->
            startActivity(Intent(this,ConfirmMapsActivity::class.java))
        }
        pick_location.setOnClickListener { v->
            if(validateForm()) {
                autoComplete()
            }
        }
        setting_btn.setOnClickListener{v->
            startActivity(Intent(this,UserMapsActivity::class.java))
        }
        delete_btn.setOnClickListener { v->

            startActivity(Intent(this,EditServicesActivity::class.java))
        }
    }


    override fun onResume() {
        super.onResume()
        service_name.setText("")
        address_service.setText("")
        telephone_service.setText("")
        pick_location.text="CHOOSE A LOCATION"

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
                    setLocationInDatabase(service_name.text.toString(), queriedLocation, place.name.toString())
                    pick_location.text = place.name
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
    fun validateForm():Boolean{
        var valid =true
        val serviceName=service_name.text.toString().trim()
        if (TextUtils.isEmpty(serviceName)) {
            service_name.error = "Required."
            valid = false
        } else {
            service_name.error = null
        }
        val serviceAddress=address_service.text.toString().trim()
        if (TextUtils.isEmpty(serviceAddress)) {
            address_service.error = "Required."
            valid = false
        } else {
            address_service.error = null
        }
        val serviceTelephone=telephone_service.text.toString().trim()
        if (TextUtils.isEmpty(serviceTelephone)) {
            telephone_service.error = "Required."
            valid = false
        } else {
            telephone_service.error = null
        }
        val regexStr="""^[0-9]{10}$""".toRegex()
        if(telephone_service.text.toString().matches(regexStr)==false){
            telephone_service.error = "Mobile number should contain 10 digits."
            valid = false
        }else{
            telephone_service.error = null
            val regPhone="""^07[0-9]{8}$""".toRegex()
            if(telephone_service.text.toString().matches(regPhone)==false){
                telephone_service.error = "Mobile Number Should start from 07"
                valid = false
            }else{
                telephone_service.error = null
            }
        }

        return valid
    }
    private fun setLocationInDatabase(key : String, latLng: LatLng, place:String) {
                val moreDataProvider: HashMap<String, Any> = hashMapOf("servicename" to key,
                        "location" to place,
                        "category" to cattegory_spinner.selectedItem.toString(),
                        "telephone" to telephone_service.text.toString(),
                        "address" to address_service.text.toString(),
                        "latitude" to latLng.latitude.toString(),
                        "longitude" to latLng.longitude.toString())

                uref!!.child(key).setValue(moreDataProvider)
                val intent=Intent(this,AdminMapsActivity::class.java)
                intent.putExtra("key",key)
                startActivity(intent)

    }

}
