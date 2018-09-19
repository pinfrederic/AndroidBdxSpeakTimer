package io.bdx.speaktimer

import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {


    var prefs: SharedPreferences? = null
    var myService: CountdownService? = null
    var isBound = false
    val TAG = "MainActivity"
    val COUNT_IS_RUNNING = "RUNNING"
    val UNTIL_MILLIS = "UNTIL_MILLIS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        prefs = getSharedPreferences("pref", 0)

        button.isEnabled = false

        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(CountdownReceiver(), IntentFilter(BROADCAST_ACTION))
        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(FinishedReceiver(), IntentFilter(FINISHED_BROADCAST_ACTION))


        val intent = Intent(this, CountdownService::class.java)
        startService(intent)
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)



        button.setOnClickListener {
            Log.i(TAG, "Button clicked")
//            myService?.startCountdown()
            prefs!!.edit().putLong(UNTIL_MILLIS, LocalDateTime.now().plusSeconds(30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).apply()
            prefs!!.edit().putBoolean(COUNT_IS_RUNNING, true).apply()

            object : CountDownTimer(prefs!!.getLong(UNTIL_MILLIS, 0) - LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 1000) {
                override fun onFinish() = MediaPlayer.create(applicationContext, R.raw.sound2).start()

                override fun onTick(millisUntilFinished: Long) {
                    val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(millisUntilFinished), ZoneId.systemDefault())
                    val formatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)
                    countdownTV2.text = from.format(formatter)
                }
            }.start()

            button.isEnabled = false
        }

        val timeBeforeEnd = prefs!!.getLong(UNTIL_MILLIS, 0) - LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (prefs!!.getBoolean(COUNT_IS_RUNNING, false) && timeBeforeEnd > LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) {

            object : CountDownTimer(timeBeforeEnd, 1000) {
                override fun onFinish() = MediaPlayer.create(applicationContext, R.raw.sound2).start()

                override fun onTick(millisUntilFinished: Long) {
                    val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(millisUntilFinished), ZoneId.systemDefault())
                    val formatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)
                    countdownTV2.text = from.format(formatter)
                }
            }.start()

        }

        buttonSecond.setOnClickListener {
            startActivity(TalksActivity.newIntent(this))
        }

        Log.i(TAG, "Activity Started")
    }

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as CountdownService.MyLocalBinder
            myService = binder.getService()
            isBound = true

            button.isEnabled = !(myService?.isCountdownRunning() ?: false)
            label.text = (myService?.isCountdownRunning() ?: false).toString()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }


    inner class CountdownReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val long = intent.extras.getLong(EXTENDED_DATA_TIME)


            val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(long), ZoneId.systemDefault())

            val formatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)

            countdownTV.text = from.format(formatter)

        }

    }

    inner class FinishedReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            button.isEnabled = true
            prefs!!.edit().putBoolean("running", false).apply()
            sound()
        }

    }

    override fun onDestroy() {
        unbindService(myConnection)
        super.onDestroy()
    }

    fun sound() {
        MediaPlayer.create(this, R.raw.sound).start()
    }
}
