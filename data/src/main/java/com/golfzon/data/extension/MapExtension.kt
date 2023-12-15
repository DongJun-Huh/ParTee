package com.golfzon.data.extension

import com.golfzon.data.common.getTypeToken
import com.golfzon.domain.model.Recruit
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer

inline fun <reified T> Map<String, Any>.toDataClass(): T {
    val gson = Gson()
    val json = gson.toJson(this)
    return gson.fromJson(json, getTypeToken<T>())
}

inline fun <reified T, reified V> Map<String, Any>.toDataClass(jsonDeserializer: JsonDeserializer<V>): T {
    val gsonBuilder = GsonBuilder()
    gsonBuilder.registerTypeAdapter(V::class.java, jsonDeserializer)
    val gson = gsonBuilder.create()
    val json = gson.toJson(this)
    return gson.fromJson(json, getTypeToken<Recruit>())
}