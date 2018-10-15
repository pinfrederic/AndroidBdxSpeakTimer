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

    private lateinit var countdown: CountDownTimer

    companion object {

        private val TALK_DATA = "TALK_DATA"
        private val ROOM_NAME = "ROOM_NAME"

        fun newInstance(talk: Talk?, roomName: String): TimerFragment {
            val timerFragment = TimerFragment()
            val bundle = Bundle()
            bundle.putSerializable(TALK_DATA, talk)
            bundle.putSerializable(ROOM_NAME, roomName)
            timerFragment.arguments = bundle
            return timerFragment
        }
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.content_countdown, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        tv_room_name.text = arguments?.getString(ROOM_NAME)
        val talk = arguments?.getSerializable(TALK_DATA) as Talk?

        if (talk == null) return

        var from = LocalDateTime.parse(talk.from, DateTimeFormatter.ISO_DATE_TIME)
        var to = LocalDateTime.parse(talk.to, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()

        from = from.withDayOfYear(now.dayOfYear)
        to = to.withDayOfYear(now.dayOfYear)

        val duration = Duration.between(from, to)
        val duration2 = Duration.between(now, to)
        progressBarCircle.max = (duration.toMillis() / 1000).toInt()
        progressBarCircle.progress = (duration.toMillis() / 1000).toInt()
        textViewTime.text = DurationFormatUtils.formatDuration(duration.toMillis(), "mm:ss")

        val isCurrentEvent = now.isAfter(from) && now.isBefore(to)

        countdown = object : CountDownTimer(duration2.toMillis(), 1000) {
            override fun onFinish() {
                MediaPlayer.create(context, R.raw.sound2).start()
            }

            override fun onTick(millisUntilFinished: Long) {

                if (progressBarCircle != null) {
                    progressBarCircle.progress = (millisUntilFinished / 1000).toInt()
                }

                val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(millisUntilFinished), ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)
                if (textViewTime != null) {
                    textViewTime.text = from.format(formatter)
                }
            }

        }

        tv_talk_name.text = talk.title

        val formatter = DateTimeFormatter.ofPattern("HH'h'mm")
        tv_ending_date_value.text = to.format(formatter)

        if (isCurrentEvent) {
            countdown.start()
        }


    }

}
