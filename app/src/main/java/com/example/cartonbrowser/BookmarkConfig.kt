package com.example.cartonbrowser

import org.json.JSONObject

data class Bookmark(var bookmarkTitle: String, var bookmarkUri: String)

val toBookmarkConfigJsonString: MutableSet<Bookmark>.() -> String = {
    JSONObject(this.map {
        it.bookmarkTitle to it.bookmarkUri
    }.toMap()).toString()
}

val toBookmarkConfigSet: String.() -> MutableSet<Bookmark> = {
    val jsonObject: JSONObject = JSONObject(this)
    val mutableSet: MutableSet<Bookmark> = mutableSetOf()
    jsonObject.keys().forEach { key ->
        mutableSet.add(Bookmark(key, jsonObject.getString(key)))
    }
    mutableSet
}
