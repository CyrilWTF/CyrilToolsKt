package com.cyrilw.cyriltools.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.preference.PreferenceManager
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.ui.WakeActivity

class ActiveDisplayService : NotificationListenerService() {

    private val mSharedPreference: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val mSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "exclusions") {
                val new = HashSet<String>(sharedPreferences?.getStringSet(key, null))
                with(mExclusions) {
                    clear()
                    addAll(new)
                }
            }
        }

    private val mNotifying: HashSet<String> = HashSet()
    private val mExclusions: HashSet<String> = HashSet()

    override fun onListenerConnected() {
        val exclusions = mSharedPreference.getStringSet(Constant.NOTIFICATION_EXCLUSIONS_KEY, null)
        exclusions?.let { mExclusions.addAll(it) }
        mSharedPreference.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener)
    }

    override fun onListenerDisconnected() {
        mNotifying.clear()
        mSharedPreference.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener)

        requestRebind(ComponentName(applicationContext, ActiveDisplayService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val name = sbn?.packageName
        if (name.equals(packageName) || mExclusions.contains(name)) {
            return
        }
        val id = sbn?.notification?.channelId
        if (mNotifying.add("$name:$id")) {
            wake()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val name = sbn?.packageName
        if (name.equals(packageName)) {
            return
        }
        val id = sbn?.notification?.channelId
        mNotifying.remove("$name:$id")
    }

    private fun wake() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) {
            val intent = Intent(this, WakeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

}
