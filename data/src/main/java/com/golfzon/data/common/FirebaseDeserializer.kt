package com.golfzon.data.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class TimestampDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime? {
        val timestamp = json.asJsonObject
        val seconds = timestamp.get("seconds").asLong
        val nanoseconds = timestamp.get("nanoseconds").asLong
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoseconds), ZoneId.systemDefault())
    }
}