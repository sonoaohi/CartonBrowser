package com.example.cartonbrowser

import org.json.JSONObject

val defaultSearchEngineJsonString = "{\"Bing\":\"https:\\/\\/www.bing.com\\/search?q=%s\"}"

val toSearchEngineConfigJsonString: Map<String, String>.() -> String = {
    JSONObject(this).toString()
}

val toSearchEngineConfigMap: String.() -> MutableMap<String, String> = {
    val jsonObject: JSONObject = JSONObject(this)
    val mutableMap: MutableMap<String, String> = mutableMapOf()
    jsonObject.keys().forEach { key ->
        mutableMap[key] = jsonObject.getString(key)
    }
    mutableMap
}

fun getSearchUri(engineName: String, keyword: String, configMap: Map<String, String>): String {
    return configMap[engineName]!!.format(keyword)
}
