package io.bdx.speaktimer

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import io.bdx.speaktimer.adapter.TalkAdapter
import io.bdx.speaktimer.model.Talk
import io.bdx.speaktimer.restclient.VoxxrRestClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_talks.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TalksActivity : AppCompatActivity(), TalkAdapter.Listener {

    private val TAG = MainActivity::class.java.simpleName

    private val BASE_URL = "http://appv3.voxxr.in/"
    private val TALK_DATA = "TALK_DATA"
    private var mCompositeDisposable: CompositeDisposable? = null
    private var mTalksArrayList: java.util.ArrayList<Talk>? = null


    private var mAndroidArrayList: ArrayList<Talk>? = null

    private var mAdapter: TalkAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talks)


        mCompositeDisposable = CompositeDisposable()

        initRecyclerView()

        loadJSON()
    }


    private fun initRecyclerView() {

        rv_android_list.setHasFixedSize(true)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        rv_android_list.layoutManager = layoutManager
    }

    private fun loadJSON() {

        val requestInterface = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(VoxxrRestClient::class.java)

        mCompositeDisposable?.add(requestInterface.getTalks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError))

    }

    private fun handleResponse(talkList: List<Talk>) {

        mAndroidArrayList = ArrayList(talkList)
        mAdapter = TalkAdapter(mAndroidArrayList!!, this)

        rv_android_list.adapter = mAdapter
    }

    private fun handleError(error: Throwable) {

        Log.d(TAG, error.localizedMessage)

        Toast.makeText(this, "Error ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(talk: Talk) {

        startActivity(NiceCountdown.newIntent(this, talk))


    }

    companion object {

        fun newIntent(context: Context): Intent {

            val intent = Intent(context, TalksActivity::class.java)
            return intent
        }

    }

}
