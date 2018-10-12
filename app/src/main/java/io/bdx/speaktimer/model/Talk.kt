package io.bdx.speaktimer.model

import java.io.Serializable

data class Talk(
        val eventId: String,
        val title: String,
        val summary: String,
        val from: String,
        val to: String,
        val location: Location
) : Serializable