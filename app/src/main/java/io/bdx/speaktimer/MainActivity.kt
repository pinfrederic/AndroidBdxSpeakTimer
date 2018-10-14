package io.bdx.speaktimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.bdx.speaktimer.model.Talk
import io.bdx.speaktimer.ui.main.TimerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_talk.*
import kotlinx.android.synthetic.main.activity_talks_child.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var menu: Menu
    private val mCompositeDisposable = CompositeDisposable()
    private val mapper = createMapper()
    private lateinit var talkList: List<Talk>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        menu = nav_view.menu
        menu.add(Menu.NONE, 42, 1, "Programme")
        menu.add(Menu.NONE, 43, 2, "Credits")


        loadTalks()

    }

    private fun loadTalks() {
        mCompositeDisposable.add(getTalks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .delaySubscription(2000, TimeUnit.MILLISECONDS)
                .subscribe({ result -> handleResponse(result) }, { error -> handleError(error) }))
    }

    private fun handleResponse(talkList: List<Talk>) {
        this.talkList = talkList

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TimerFragment.newInstance(talkList[0]))
                .commitNow()

        setRoomsInMenu()
    }

    private fun setRoomsInMenu() {
        val subMenu = menu.addSubMenu(1, 2, 0, "Rooms")
        val byLocation = this.talkList.groupBy { talk -> talk.location }
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
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TimerFragment.newInstance(this.talkList[item.itemId]))
                        .commitNow()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

}
