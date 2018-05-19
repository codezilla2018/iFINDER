package com.keliya.chickson.ifinder

import android.graphics.PorterDuff
import android.view.MotionEvent
import android.view.View

/**
 * Created by chiCkson on 5/16/18.
 */
class ButtonEffects{
    fun buttonEffect(button: View){
        button.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN->{
                    v.background.setColorFilter(-0x1f0b8adf, PorterDuff.Mode.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            false
        }
    }
}