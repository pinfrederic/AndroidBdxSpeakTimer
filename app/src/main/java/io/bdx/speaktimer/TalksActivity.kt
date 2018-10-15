package io.bdx.speaktimer

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.bdx.speaktimer.adapter.TalkAdapter
import io.bdx.speaktimer.model.Talk
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_talk.*
import kotlinx.android.synthetic.main.activity_talks_child.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*


class TalksActivity : AppCompatActivity(), TalkAdapter.Listener, NavigationView.OnNavigationItemSelectedListener {

    private val TAG = TalksActivity::class.java.simpleName

    private val BASE_URL = "http://appv3.voxxr.in/"
    private val mCompositeDisposable = CompositeDisposable()
    private val mAdapter = TalkAdapter(ArrayList(Collections.emptyList()))
    private val mapper = createMapper()
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk)
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)
        pbWaiting.visibility = View.VISIBLE

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        menu = nav_view.menu
        menu.add(Menu.NONE, 42, 1, "Programme")
        menu.add(Menu.NONE, 43, 2, "Credits")

        initRecyclerView()
        loadTalks()
    }


    private fun initRecyclerView() {
        rv_talks_list.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        rv_talks_list.layoutManager = layoutManager
        rv_talks_list.adapter = mAdapter
    }

    private fun loadTalks() {
        mCompositeDisposable.add(getTalks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .delaySubscription(2000, TimeUnit.MILLISECONDS)
                .subscribe({ result -> handleResponse(result) }, { error -> handleError(error) }))
    }

    private fun handleResponse(talkList: List<Talk>) {
        mAdapter.dataList = ArrayList(talkList)
        mAdapter.notifyDataSetChanged()
        rv_talks_list.adapter = mAdapter
        pbWaiting.visibility = View.GONE
        setRoomsInMenu(talkList)
    }

    private fun setRoomsInMenu(talkList: List<Talk>) {
        val subMenu = menu.addSubMenu(1, 2, 0, "Rooms")
        val byLocation = talkList.groupBy { talk -> talk.location }
        for (location in byLocation.keys.withIndex()) {
            subMenu.add(1, location.index, location.index, location.value.name)
        }
    }

    private fun handleError(error: Throwable) {
        pbWaiting.visibility = View.GONE
        throw error
    }

    private fun createMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }

    override fun onItemClick(talk: Talk) {
        startActivity(CountdownActivity.newIntent(this, talk))
    }

    private fun getTalks(): Observable<List<Talk>> {
        return Observable.fromCallable {
            val text = resources.openRawResource(R.raw.sample).bufferedReader().use { it.readText() }
            val typeFactory = mapper.typeFactory
            val collectionType = typeFactory.constructCollectionType(ArrayList::class.java, Talk::class.java)
            mapper.readValue<List<Talk>>(text, collectionType)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            42 -> {
                Toast.makeText(this, "Program", Toast.LENGTH_LONG).show()
            }
            43 -> {
                Toast.makeText(this, "Credits, licence & info", Toast.LENGTH_LONG).show()

            }
            else -> {
                Toast.makeText(this, "The room with id ${item.itemId} was clicked!", Toast.LENGTH_LONG).show()
            }
        }

        return true
    }

    override fun onDestroy() {
        mCompositeDisposable.clear()
        super.onDestroy()
    }

}
