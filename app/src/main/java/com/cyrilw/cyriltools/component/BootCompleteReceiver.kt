package com.cyrilw.cyriltools.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.service.IndicatorService

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (sp.getBoolean(Constant.INDICATOR_SWITCH_KEY, false)) {
                context.startService(Intent(context, IndicatorService::class.java))
            }
        }
    }

}
