package io.bdx.speaktimer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import io.bdx.speaktimer.adapter.TalkAdapter
import io.bdx.speaktimer.model.Talk
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_talks_child.*
import java.util.*
import kotlin.collections.ArrayList


class TalksFragment : Fragment() {

    private val mCompositeDisposable = CompositeDisposable()
    private val mAdapter = TalkAdapter(ArrayList(Collections.emptyList()))

    companion object {

        private const val TALK_LIST = "TALK_LIST"

        fun newInstance(talksList: ArrayList<Talk>): TalksFragment {
            val talksFragment = TalksFragment()
            val bundle = Bundle()
            bundle.putSerializable(TALK_LIST, talksList)
            talksFragment.arguments = bundle
            return talksFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_talks_child, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Display waiting circle
        pbWaiting.visibility = View.VISIBLE

        initRecyclerView()

        //Get data from bundle and assign it
        mAdapter.dataList = arguments!!.getSerializable(TALK_LIST) as ArrayList<Talk>
        mAdapter.notifyDataSetChanged()
        rv_talks_list.adapter = mAdapter

        //Hide waiting circle
        pbWaiting.visibility = View.GONE
    }

    private fun initRecyclerView() {
        rv_talks_list.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        rv_talks_list.layoutManager = layoutManager
        rv_talks_list.adapter = mAdapter
    }

    override fun onDestroy() {
        mCompositeDisposable.clear()
        super.onDestroy()
    }

}
