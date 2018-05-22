package com.keliya.chickson.ifinder

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mPrefs = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        if (haveNetworkConnection()) {
            if (!locationEnabled()) {
                val intent = LocationEnabledCheckActivity.newIntent(this)
                startActivity(intent)
            } else {
                if (mPrefs.getBoolean("is_logged_before", false)==false)
                   startActivity(Intent(this,LoginActivity::class.java))
                else
                    startActivity(Intent(this,UserMapsActivity::class.java))
            }
        } else {
            val intent = NetworkEnableActivity.newIntent(this)
            startActivity(intent)
        }
        finish()

    }



    public fun haveNetworkConnection(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo  = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    public fun locationEnabled():Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    }
}
