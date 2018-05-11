package com.keliya.chickson.ifinder

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_user_selection.*

class UserSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)
        admin_User.setOnClickListener { v->
            startActivity(Intent(this,MainActivity::class.java))
        }
        button.setOnClickListener { v->
            startActivity(Intent(this,UserMapsActivity::class.java))
        }
    }
}
