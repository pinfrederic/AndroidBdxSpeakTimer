package io.bdx.speaktimer.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.bdx.speaktimer.R
import io.bdx.speaktimer.domain.Talk
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_talks.*
import java.util.*
import kotlin.collections.ArrayList


class TalksFragment : Fragment() {

    private val mCompositeDisposable = CompositeDisposable()
    private val mAdapter = TalkAdapter(ArrayList(Collections.emptyList()))

    companion object {

        private const val TALK_LIST = "TALK_LIST"

        fun newInstance(talksList: ArrayList<Talk>): TalksFragment {
            val bundle = Bundle()
            bundle.putSerializable(TALK_LIST, talksList)
            val talksFragment = TalksFragment()
            talksFragment.arguments = bundle
            return talksFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_talks, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        displayWaitingCircle()
        initRecyclerView()
        updateTalksAndNotify()
        hideWaitingCircle()
    }

    private fun hideWaitingCircle() {
        pbWaiting.visibility = View.GONE
    }

    private fun displayWaitingCircle() {
        pbWaiting.visibility = View.VISIBLE
    }

    private fun updateTalksAndNotify() {
        mAdapter.talks = arguments!!.getSerializable(TALK_LIST) as ArrayList<Talk>
        mAdapter.notifyDataSetChanged()
        rv_talks_list.adapter = mAdapter
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
