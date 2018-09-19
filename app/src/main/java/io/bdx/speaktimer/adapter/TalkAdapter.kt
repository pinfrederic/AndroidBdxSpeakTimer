package io.bdx.speaktimer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import io.bdx.speaktimer.R
import io.bdx.speaktimer.model.Talk

import kotlinx.android.synthetic.main.adapter_talk.view.*

class TalkAdapter (private val dataList : ArrayList<Talk>, private val listener : Listener) : RecyclerView.Adapter<TalkAdapter.ViewHolder>() {

    interface Listener {

        fun onItemClick(talk : Talk)
    }

    private val colors : Array<String> = arrayOf("#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0", "#42A5F5")

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(dataList[position], listener, colors, position)
    }

    override fun getItemCount(): Int = dataList.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_talk, parent, false)

        return ViewHolder(view)
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {

        fun bind(android: Talk, listener: Listener, colors : Array<String>, position: Int) {

            itemView.tv_name.text = android.title
            itemView.tv_version.text = android.summary
            itemView.tv_api_level.text = android.eventId
            itemView.setBackgroundColor(Color.parseColor(colors[position % 6]))

            itemView.setOnClickListener{ listener.onItemClick(android) }
        }
    }
}