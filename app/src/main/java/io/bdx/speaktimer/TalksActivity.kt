package io.bdx.speaktimer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.bdx.speaktimer.adapter.TalkAdapter
import io.bdx.speaktimer.model.Talk
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_talks.*
import java.util.stream.Collectors

class TalksActivity : AppCompatActivity(), TalkAdapter.Listener {

    private val TAG = TalksActivity::class.java.simpleName

    private val BASE_URL = "http://appv3.voxxr.in/"
    private var mCompositeDisposable: CompositeDisposable? = null
    private var mAdapter: TalkAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talks)

        mCompositeDisposable = CompositeDisposable()

        initRecyclerView()
        loadTalks()
    }


    private fun initRecyclerView() {
        rv_talks_list.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
        rv_talks_list.layoutManager = layoutManager
    }

    private fun loadTalks() {

//        val requestInterface = Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build().create(VoxxrRestClient::class.java)
//
//        mCompositeDisposable?.add(requestInterface.getTalks()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(this::handleResponse, this::handleError))


        val text = resources.openRawResource(R.raw.sample).bufferedReader().use { it.readText() }
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val talksList: List<Talk> = mapper.readValue(text)

        Log.i(TAG, talksList.stream().map { it.title }.collect(Collectors.toList()).toString())
        handleResponse(talksList)



    }

    private fun handleResponse(talkList: List<Talk>) {
        mAdapter = TalkAdapter(ArrayList(talkList), this)
        rv_talks_list.adapter = mAdapter
    }

    private fun handleError(error: Throwable) {
        Log.d(TAG, error.localizedMessage)
        Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_LONG).show()
    }

    override fun onItemClick(talk: Talk) {
        startActivity(CountdownActivity.newIntent(this, talk))
    }

}
