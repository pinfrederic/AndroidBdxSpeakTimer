package io.bdx.speaktimer

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.os.CountDownTimer
import android.os.Build


const val BROADCAST_ACTION = "BROADCAST"
const val FINISHED_BROADCAST_ACTION = "FINISHED_BROADCAST_ACTION"
const val EXTENDED_DATA_TIME = "TIME"

class CountdownService : IntentService("HelloIntentService") {

    private val TAG = "ServiceExample"
    private val myBinder = MyLocalBinder()
    private val CHANNEL_ID = "1234"
    private val CHANNEL_NAME = "MY_CHANNEL"
    private val myCountdownTimer = MyCountdownTimer(this)

    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, ">>>>>>>>>>   Intent Service Started.")
        startForeground(1337, createNotificationChannel())
    }

    fun createNotificationChannel() : Notification {
        val notificationIntent = Intent(this, CountdownService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)

            return Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText(getText(R.string.notification_message))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker(getText(R.string.ticker_text))
                    .build()
        }
        return Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, ">>>>>>>>>>   Intent Service Started 2.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, ">>>>>>>>>>   Intent Service Started 3.")
        return super.onStartCommand(intent, flags, startId)
    }


    inner class MyLocalBinder : Binder() {
        fun getService() : CountdownService {
            return this@CountdownService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, ">>>>>>>>>>   Intent Service Started 4.")
        return myBinder
    }




//    val countdown = object : CountDownTimer(30000, 1000) {
//
//        var isCountdownRunning = false
//
//        override fun onTick(millisUntilFinished: Long) {
//            isCountdownRunning = true
//            val localIntent = Intent(BROADCAST_ACTION).apply {
//                // Puts the status into the Intent
//                putExtra(EXTENDED_DATA_TIME, millisUntilFinished)
//            }
//            LocalBroadcastManager.getInstance(this@CountdownService).sendBroadcast(localIntent)
//        }
//
//        override fun onFinish() {
//            isCountdownRunning = false
//            LocalBroadcastManager.getInstance(this@CountdownService).sendBroadcast(Intent(FINISHED_BROADCAST_ACTION))
//            Log.i(TAG, "Countdown finished")
//        }
//    }

    fun isCountdownRunning() : Boolean {
        return myCountdownTimer.isCountdownRunning
    }


    fun startCountdown() {
        Log.i(TAG, ">>>>>>>>>>   startCountdown called.")
        myCountdownTimer.start()
    }


}

class MyCountdownTimer(val countdownService: CountdownService) : CountDownTimer(30000, 1000) {

    var isCountdownRunning = false

    override fun onTick(millisUntilFinished: Long) {
        isCountdownRunning = true
        val localIntent = Intent(BROADCAST_ACTION).apply {
            // Puts the status into the Intent
            putExtra(EXTENDED_DATA_TIME, millisUntilFinished)
        }
        LocalBroadcastManager.getInstance(countdownService).sendBroadcast(localIntent)
    }

    override fun onFinish() {
        isCountdownRunning = false
        LocalBroadcastManager.getInstance(countdownService).sendBroadcast(Intent(FINISHED_BROADCAST_ACTION))
        Log.i("TOTO", "Countdown finished")
    }

}