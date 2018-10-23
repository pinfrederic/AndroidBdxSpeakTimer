package io.bdx.speaktimer.ui

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.bdx.speaktimer.R
import io.bdx.speaktimer.domain.Talk
import kotlinx.android.synthetic.main.adapter_talk.view.*

class TalkAdapter(var talks: ArrayList<Talk>) : RecyclerView.Adapter<TalkAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
        holder.bind(talks[index])
    }

    override fun getItemCount(): Int = talks.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_talk, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(talk: Talk) {
            itemView.title.text = talk.title
            itemView.recap.text = Html.fromHtml(talk.summary, Html.FROM_HTML_MODE_LEGACY)
            itemView.eventId.text = talk.eventId + " - " + talk.location?.name
        }
    }
}