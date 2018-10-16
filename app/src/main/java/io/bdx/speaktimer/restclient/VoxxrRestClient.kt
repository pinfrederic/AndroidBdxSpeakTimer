package io.bdx.speaktimer.restclient

import io.bdx.speaktimer.model.Talk
import io.reactivex.Observable
import retrofit2.http.GET

interface VoxxrRestClient {

    @GET("api/days/5ba95365e4b0b618af601b1a/presentations")
    fun getTalks(): Observable<List<Talk>>

}