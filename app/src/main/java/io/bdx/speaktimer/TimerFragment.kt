package io.bdx.speaktimer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        private val HOUR_MINUTE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH'h'mm")
        private val MINUTE_SECOND_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("mm:ss", Locale.FRANCE)

        private const val TALK_DATA = "TALK_DATA"
        private const val ROOM_NAME = "ROOM_NAME"

        fun newInstance(talk: Talk?, roomName: String): TimerFragment {
            val timerFragment = TimerFragment()
            val bundle = Bundle()
            bundle.putSerializable(TALK_DATA, talk)
            bundle.putSerializable(ROOM_NAME, roomName)
            timerFragment.arguments = bundle
            return timerFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.content_countdown, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tv_room_name.text = arguments?.getString(ROOM_NAME)
        val talk = arguments?.getSerializable(TALK_DATA) as Talk? ?: return

        var from = LocalDateTime.parse(talk.from, DateTimeFormatter.ISO_DATE_TIME)
        var to = LocalDateTime.parse(talk.to, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()

        /*** Set the bdx.io date to today ***/
        from = from.withDayOfYear(now.dayOfYear)
        to = to.withDayOfYear(now.dayOfYear)
        /*** **************************** ***/

        initProgressBar(Duration.between(from, to))

        val remainingDuration = Duration.between(now, to)

        countdown = object : CountDownTimer(remainingDuration.toMillis(), 1000) {
            override fun onFinish() {
                MediaPlayer.create(context, R.raw.sound2).start()
            }

            override fun onTick(millisUntilFinished: Long) {

                if (progressBarCircle != null) { //FIXME progressBar should never be null
                    progressBarCircle.progress = (millisUntilFinished / 1000).toInt()
                }

                val from = LocalDateTime.ofInstant(Instant.ofEpochMilli(millisUntilFinished), ZoneId.systemDefault())
                if (textViewTime != null) { //FIXME textViewTime should never be null
                    textViewTime.text = from.format(MINUTE_SECOND_FORMATTER)
                }
            }

        }

        tv_talk_name.text = talk.title
        tv_ending_date_value.text = to.format(HOUR_MINUTE_FORMATTER)

        if ((activity as MainActivity).isCurrent(talk)) {
            countdown.start()
        }

    }

    private fun initProgressBar(eventDuration: Duration) {
        progressBarCircle.max = (eventDuration.toMillis() / 1000).toInt()
        progressBarCircle.progress = (eventDuration.toMillis() / 1000).toInt()
        textViewTime.text = DurationFormatUtils.formatDuration(eventDuration.toMillis(), "mm:ss")
    }

}
