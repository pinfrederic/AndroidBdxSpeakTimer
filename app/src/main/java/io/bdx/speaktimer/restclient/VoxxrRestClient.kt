package io.bdx.speaktimer.restclient

import io.bdx.speaktimer.model.Talk
import io.reactivex.Observable
import retrofit2.http.GET

interface VoxxrRestClient {

    @GET("api/days/5b9ac2d45b2e1d3ec25cd0a8/presentations")
    fun getTalks(): Observable<List<Talk>>
}