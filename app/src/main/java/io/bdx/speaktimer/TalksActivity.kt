package io.bdx.speaktimer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.bdx.speaktimer.adapter.TalkAdapter
import io.bdx.speaktimer.model.Talk
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_talks.*


class TalksActivity : AppCompatActivity(), TalkAdapter.Listener {

    private val TAG = TalksActivity::class.java.simpleName

    private val BASE_URL = "http://appv3.voxxr.in/"
    private var mCompositeDisposable: CompositeDisposable? = null
    private var mAdapter: TalkAdapter? = null
    private var disposable: Disposable? = null
    private val mapper = createMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talks)
        pbWaiting.visibility = View.VISIBLE

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

//        val text = resources.openRawResource(R.raw.sample).bufferedReader().use { it.readText() }
//        val mapper = jacksonObjectMapper()
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//        val talksList: List<Talk> = mapper.readValue(text)
//        Log.i(TAG, talksList.stream().map { it.title }.collect(Collectors.toList()).toString())
//
//
//        val observable = Observable.fromArray(talksList)

        disposable = getTalks()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe ({ result -> handleResponse(result) }, { error -> handleError(error) })

    }

    private fun handleResponse(talkList: List<Talk>) {
        mAdapter = TalkAdapter(ArrayList(talkList), this)
        rv_talks_list.adapter = mAdapter
        pbWaiting.visibility = View.GONE
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

    private fun getTalks() : Observable<List<Talk>> {
        val text = resources.openRawResource(R.raw.sample).bufferedReader().use { it.readText() }
        val typeFactory = mapper.typeFactory
        val collectionType = typeFactory.constructCollectionType(ArrayList::class.java, Talk::class.java)
        return Observable.create<List<Talk>> {
            mapper.readValue(text, collectionType)
        }
    }

}
