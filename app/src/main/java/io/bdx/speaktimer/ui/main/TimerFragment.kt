package io.bdx.speaktimer.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.bdx.speaktimer.R
import io.bdx.speaktimer.model.Talk
import kotlinx.android.synthetic.main.content_countdown.*
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TimerFragment : Fragment() {

    companion object {

        private val TALK_DATA = "TALK_DATA"

        fun newInstance(talk: Talk): TimerFragment {
            val timerFragment = TimerFragment()
            val bundle = Bundle()
            bundle.putSerializable(TALK_DATA, talk)
            timerFragment.arguments = bundle
            return timerFragment
        }
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.content_countdown, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val talk = arguments?.getSerializable(TALK_DATA) as Talk
        val from = LocalDateTime.parse(talk.from, DateTimeFormatter.ISO_DATE_TIME)
        val to = LocalDateTime.parse(talk.to, DateTimeFormatter.ISO_DATE_TIME)

        val duration = Duration.between(from, to)
        progressBarCircle.max = (duration.toMillis() / 1000).toInt()
        progressBarCircle.progress = (duration.toMillis() / 1000).toInt()
        textViewTime.text = DurationFormatUtils.formatDuration(duration.toMillis(), "mm:ss")

        val now = LocalDateTime.now()
        val isCurrentEvent = now.isAfter(from) && now.isBefore(to)

        val countdown = object : CountDownTimer(duration.toMillis(), 1000) {
            override fun onFinish() {
                MediaPlayer.create(context, R.raw.sound2).start()
            }

            override fun onTick(millisUntilFinished: Long) {

                progressBarCircle.progress = (millisUntilFinished / 1000).toInt()

                val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(millisUntilFinished), ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)
                textViewTime.text = from.format(formatter)
            }

        }

        btn_start_anyway.setOnClickListener { countdown.start(); btn_start_anyway.isEnabled = false }

    }


}
