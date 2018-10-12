package io.bdx.speaktimer.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Location(
        @JsonProperty("_id") val id: String,
        val name: String,
        val fullName: String
) : Serializable