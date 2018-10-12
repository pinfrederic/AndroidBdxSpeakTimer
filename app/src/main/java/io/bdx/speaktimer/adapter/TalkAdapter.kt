package io.bdx.speaktimer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import io.bdx.speaktimer.R
import io.bdx.speaktimer.model.Talk

import kotlinx.android.synthetic.main.adapter_talk.view.*

class TalkAdapter(var dataList: ArrayList<Talk>, private val listener: Listener) : RecyclerView.Adapter<TalkAdapter.ViewHolder>() {

    interface Listener {

        fun onItemClick(talk: Talk)
    }

    private val colors: Array<String> = arrayOf("#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0", "#42A5F5")

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position], listener, colors, position)
    }

    override fun getItemCount(): Int = dataList.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_talk, parent, false)

        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(talk: Talk, listener: Listener, colors: Array<String>, position: Int) {

            itemView.title.text = talk.title
            itemView.recap.text = talk.summary
            itemView.eventId.text = talk.eventId + " - " + talk.location?.name
            itemView.setBackgroundColor(Color.parseColor(colors[position % 6]))

            itemView.setOnClickListener { listener.onItemClick(talk) }
        }
    }
}