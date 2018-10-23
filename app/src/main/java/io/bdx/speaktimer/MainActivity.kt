package io.bdx.speaktimer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.bdx.speaktimer.domain.Location
import io.bdx.speaktimer.domain.Talk
import io.bdx.speaktimer.ui.TalksFragment
import io.bdx.speaktimer.ui.TimerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.options_dialog.view.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var menu: Menu
    private val mCompositeDisposable = CompositeDisposable()
    private val mapper = createMapper()
    private lateinit var talks: List<Talk>
    private lateinit var byLocation: Map<Location, List<Talk>>
    private lateinit var preferences: SharedPreferences

    companion object {

        private const val MENU_PROGRAM_ID = 42
        private const val MENU_CREDITS_ID = 43
        private const val MENU_ROOMS_ID = 2

        private const val MENU_ROOMS_POSITION = 0
        private const val MENU_PROGRAM_POSITION = 1
        private const val MENU_CREDITS_POSITION = 2

        private const val MENU_ROOMS_GROUPS = 1

        private const val PREF_SERVER_URL = "PREF_SERVER_URL"
        private const val DEFAULT_SERVER_URL = "https://app.voxxr.in/api/days/5bc25e6fe4b05b8d2a14506f/presentations"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)
        preferences = getPreferences(Context.MODE_PRIVATE)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        menu = nav_view.menu
        menu.add(Menu.NONE, MENU_PROGRAM_ID, MENU_PROGRAM_POSITION, getString(R.string.program))
        menu.add(Menu.NONE, MENU_CREDITS_ID, MENU_CREDITS_POSITION, getString(R.string.credits))


        loadTalks()

    }

    private fun loadTalks() {
        mCompositeDisposable.add(getTalks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .delaySubscription(2000, TimeUnit.MILLISECONDS) //For testing web latency
                .subscribe({ result -> handleResponse(result) }, { error -> handleError(error) }))
    }

    private fun handleResponse(talks: List<Talk>) {
        this.talks = talks

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TalksFragment.newInstance(ArrayList(this.talks)))
                .commitNow()

        setRoomsInMenu()
    }

    private fun setRoomsInMenu() {
        val subMenu = menu.addSubMenu(MENU_ROOMS_GROUPS, MENU_ROOMS_ID, MENU_ROOMS_POSITION, getString(R.string.rooms))
        byLocation = this.talks.groupBy { talk -> talk.location }
        for (location in byLocation.keys.withIndex()) {
            subMenu.add(MENU_ROOMS_GROUPS, location.index, location.index, location.value.name)
        }
    }

    private fun handleError(error: Throwable) {
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
            MENU_PROGRAM_ID -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TalksFragment.newInstance(ArrayList(this.talks)))
                        .commitNow()
            }
            MENU_CREDITS_ID -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bordeaux.zenika.com/"))
                startActivity(browserIntent)
            }
            else -> {
                val location = this.byLocation.keys.withIndex().elementAt(item.itemId).value
                val talk = getCurrentTalk(byLocation.getOrDefault(location, Collections.emptyList<Talk>()))
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TimerFragment.newInstance(talk, location.name))
                        .commitNow()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getCurrentTalk(talks: List<Talk>): Talk? {
        return talks.filter { talk -> isCurrent(talk) }.getOrNull(0)
    }

    fun isCurrent(talk: Talk): Boolean {
        var from = LocalDateTime.parse(talk.from, DateTimeFormatter.ISO_DATE_TIME)
        var to = LocalDateTime.parse(talk.to, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()

        /*** Set the bdx.io date to today ***/
        from = from.withDayOfYear(now.dayOfYear)
        to = to.withDayOfYear(now.dayOfYear)
        /*** **************************** ***/

        return now.isAfter(from) && now.isBefore(to)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sync -> {
                performSynchronization()
                return true
            }
            R.id.action_sync_set_url -> {
                displayUrlPopup()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayUrlPopup() {
        val dialogView = layoutInflater.inflate(R.layout.options_dialog, null)

        dialogView.serverUrlText.setText(preferences.getString(PREF_SERVER_URL, DEFAULT_SERVER_URL))

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.server_url_label))
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok) { _, _ -> preferences.edit().putString(PREF_SERVER_URL, dialogView.serverUrlText.text.toString()).apply() }
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                .setNeutralButton(getString(R.string.set_default_label)) { dialog, _ ->
                    preferences.edit().putString(PREF_SERVER_URL, DEFAULT_SERVER_URL).apply()
                    dialog.dismiss()
                }
                .show()
    }

    private fun performSynchronization() {
        val url = preferences.getString(PREF_SERVER_URL, DEFAULT_SERVER_URL)

        val file = File(filesDir, "data.json")

        /**
         * TODO:
         * 1- Call server API with okhttp (and retrofit if json support required)
         * 2- and save the result to a file (not in res folder, see https://stackoverflow.com/a/41320554/2683000)
         * 3- and then, call loadTalks() as usual.
         **/

    }

}
