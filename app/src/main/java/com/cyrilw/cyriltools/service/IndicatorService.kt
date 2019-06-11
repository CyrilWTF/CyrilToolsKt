package com.cyrilw.cyriltools.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.IBinder
import com.cyrilw.cyriltools.component.NetSpeedNotification

class IndicatorService : Service() {

    private val mManager: ConnectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val mNotification: NetSpeedNotification by lazy { NetSpeedNotification(this) }

    private val mHandler: Handler = Handler()

    private var isNotificationCreated: Boolean = false
    private var isNetworkAvailable: Boolean = false

    private val mRunnable: Runnable = object : Runnable {
        override fun run() {
            mNotification.update()
            mHandler.postDelayed(this, 1000)
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    pauseNotifying()
                    mNotification.doze()
                }
                Intent.ACTION_USER_PRESENT -> {
                    if (isNetworkAvailable) {
                        restartNotifying()
                    }
                }
            }
        }

    }

    private val mNetworkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network?) {
                super.onAvailable(network)
                if (!isNetworkAvailable) {
                    isNetworkAvailable = true
                    restartNotifying()
                }
            }

            override fun onLosing(network: Network?, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                isNetworkAvailable = false
                pauseNotifying()
                mNotification.doze()
            }

            override fun onLost(network: Network?) {
                super.onLost(network)
                isNetworkAvailable = false
                pauseNotifying()
                mNotification.doze()
            }

        }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(mReceiver, filter)

        mManager.registerDefaultNetworkCallback(mNetworkCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isNotificationCreated) {
            mNotification.start(this)
            isNotificationCreated = true
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseNotifying()

        if (isNotificationCreated) {
            mNotification.stop(this)
            isNotificationCreated = false
        }

        mManager.unregisterNetworkCallback(mNetworkCallback)
        unregisterReceiver(mReceiver)
    }

    private fun pauseNotifying() {
        mHandler.removeCallbacks(mRunnable)
    }

    private fun restartNotifying() {
        mHandler.removeCallbacks(mRunnable)
        mHandler.post(mRunnable)
    }

}
