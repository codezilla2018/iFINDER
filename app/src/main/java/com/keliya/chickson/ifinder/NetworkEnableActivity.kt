package com.keliya.chickson.ifinder

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressPie
import kotlinx.android.synthetic.main.activity_network_enable.*

class NetworkEnableActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_enable)
        val btnEf=ButtonEffects()
        Thread().run{
            btnEf.buttonEffect(button5)
        }
        button5.setOnClickListener { v ->
            val callNetwork = Intent(
                    android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS )
            startActivity(callNetwork)

        }
    }
    override fun onRestart() {
        super.onRestart()
        val dialogs = ACProgressPie.Builder(this)
                .ringColor(Color.WHITE)
                .pieColor(Color.WHITE)
                .updateType(ACProgressConstant.PIE_AUTO_UPDATE)
                .build()
        dialogs.show()
        Handler().postDelayed({
            dialogs.hide()
            if (locationEnabled()) {
                if (!haveNetworkConnection()) {

                    val intent = NetworkEnableActivity.newIntent(this)
                    startActivity(intent)
                } else {

                    startActivity(Intent(this, SplashActivity::class.java))
                }
            } else {
                val intent = LocationEnabledCheckActivity.newIntent(this)
                startActivity(intent)

            }

        }, 1000)
    }

    private fun haveNetworkConnection(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    private fun locationEnabled():Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    }
    companion object{
        fun newIntent(context: Context): Intent {
            val intent= Intent(context,NetworkEnableActivity::class.java)
            return intent
        }
    }
}
