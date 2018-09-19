package io.bdx.speaktimer

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import io.bdx.speaktimer.model.Talk
import kotlinx.android.synthetic.main.activity_countdown.*
import kotlinx.android.synthetic.main.content_countdown.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class CountdownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)
        setSupportActionBar(toolbar)

        val talk = intent.extras?.get(TALK_DATA) as Talk
        val from = LocalDateTime.parse(talk.from, DateTimeFormatter.ISO_DATE_TIME)
        val to = LocalDateTime.parse(talk.to, DateTimeFormatter.ISO_DATE_TIME)

        val duration = Duration.between(from, to)
        progressBarCircle.max = (duration.toMillis() / 1000).toInt()

        val countdown = object : CountDownTimer(duration.toMillis(), 1000) {
            override fun onFinish() {
                MediaPlayer.create(applicationContext, R.raw.sound2).start()
            }

            override fun onTick(millisUntilFinished: Long) {

                progressBarCircle.progress = (millisUntilFinished / 1000).toInt()

                val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(millisUntilFinished), ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)
                textViewTime.text = from.format(formatter)

            }

        }
        countdown.start()

    }


    companion object {

        private val TALK_DATA = "TALK_DATA"

        fun newIntent(context: Context, talk: Talk): Intent {
            val intent = Intent(context, CountdownActivity::class.java)
            intent.putExtra(TALK_DATA, talk)
            return intent
        }

    }


}
