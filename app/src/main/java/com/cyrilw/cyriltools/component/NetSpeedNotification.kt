package com.cyrilw.cyriltools.component

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Icon
import android.net.TrafficStats
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.ui.MainActivity

class NetSpeedNotification(context: Context) {

    private val mContext: Context = context
    private val mManager: NotificationManager
    private val mBuilder: Notification.Builder

    private val mPaint: Paint
    private val mIconBitmap: Bitmap
    private val mCanvas: Canvas

    private val mSpeed: NetSpeed = NetSpeed()

    init {
        mManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mBuilder = Notification.Builder(mContext, Constant.NET_SPEED_CHANNEL_ID)

        mPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 36f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        mIconBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ALPHA_8)
        mCanvas = Canvas(mIconBitmap)

        notificationGenerator()
    }

    fun start(service: Service) {
        service.startForeground(Constant.NET_SPEED_NOTIFICATION_ID, mBuilder.build())
    }

    fun stop(service: Service) {
        service.stopForeground(true)
    }

    fun doze() {
        mManager.notify(
            Constant.NET_SPEED_NOTIFICATION_ID,
            mBuilder.setSmallIcon(R.drawable.ic_notification_doze)
                .setContentText("Network unavailable")
                .build()
        )
    }

    fun update() {
        mSpeed.update()
        val iconRx = mSpeed.rxSpeed + mSpeed.rxUnit.substring(0, 1)
        val iconTx = mSpeed.txSpeed + mSpeed.txUnit.substring(0, 1)
        val content = "${mSpeed.txSpeed} ${mSpeed.txUnit} ↑  ${mSpeed.rxSpeed} ${mSpeed.rxUnit} ↓"
        mManager.notify(
            Constant.NET_SPEED_NOTIFICATION_ID,
            mBuilder.setSmallIcon(createIcon(iconRx, iconTx)).setContentText(content).build()
        )
    }

    private fun notificationGenerator() {
        val channel = NotificationChannel(
            Constant.NET_SPEED_CHANNEL_ID,
            Constant.NET_SPEED_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        mManager.createNotificationChannel(channel)
        val intent =
            PendingIntent.getActivity(mContext, 0, Intent(mContext, MainActivity::class.java), 0)

        mBuilder.setVisibility(Notification.VISIBILITY_SECRET)
            .setContentTitle("Net speed indicator")
            .setContentIntent(intent)
    }

    private fun createIcon(rxSpeed: String, txSpeed: String): Icon {
        with(mCanvas) {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawText(txSpeed, 48f, 36f, mPaint)
            drawText(rxSpeed, 48f, 84f, mPaint)
        }
        return Icon.createWithBitmap(mIconBitmap)
    }

    private class NetSpeed {

        companion object {
            private val UNIT: Array<String> = arrayOf("B/s", "KB/s", "MB/s")
        }

        lateinit var rxSpeed: String
        lateinit var txSpeed: String
        lateinit var rxUnit: String
        lateinit var txUnit: String

        private var preRx: Long = TrafficStats.getTotalRxBytes()
        private var preTx: Long = TrafficStats.getTotalTxBytes()
        private var preTime: Long = System.currentTimeMillis()

        fun update() {
            val curRx = TrafficStats.getTotalRxBytes()
            val curTx = TrafficStats.getTotalTxBytes()
            val curTime = System.currentTimeMillis()

            val usedRx = curRx - preRx
            val usedTx = curTx - preTx
            val duration = curTime - preTime

            preRx = curRx
            preTx = curTx
            preTime = curTime

            val rxSpeed = usedRx * 1000 / duration
            val txSpeed = usedTx * 1000 / duration

            formatSpeed(rxSpeed, 'r')
            formatSpeed(txSpeed, 't')
        }

        private fun formatSpeed(speed: Long, type: Char) {
            val temp: String
            val unit: Int
            when {
                speed < 1000 -> {
                    temp = "$speed"
                    unit = 0
                }
                speed < 100000 -> {
                    temp = String.format("%.1f", speed / 1000f)
                    unit = 1
                }
                speed < 1000000 -> {
                    temp = "${speed / 1000}"
                    unit = 1
                }
                else -> {
                    temp = String.format("%.1f", speed / 1000000f)
                    unit = 2
                }
            }

            when (type) {
                'r' -> {
                    rxSpeed = temp
                    rxUnit = UNIT[unit]
                }
                't' -> {
                    txSpeed = temp
                    txUnit = UNIT[unit]
                }
            }

        }

    }

}