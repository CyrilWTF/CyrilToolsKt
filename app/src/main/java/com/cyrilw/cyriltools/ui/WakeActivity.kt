package com.cyrilw.cyriltools.ui

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.WindowManager

class WakeActivity : Activity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val params = WindowManager.LayoutParams().apply {
            alpha = 0f
            screenBrightness = 0f
        }

        with(window) {
            attributes = params
            setGravity(Gravity.START or Gravity.TOP)
            addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Handler().post { finish() }
    }

}
