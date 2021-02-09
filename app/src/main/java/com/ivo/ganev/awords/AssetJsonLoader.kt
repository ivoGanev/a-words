package com.ivo.ganev.awords

import android.content.Context
import com.ivo.ganev.awords.extensions.openJsonAsset
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import timber.log.Timber.e as debugError

class AssetJsonLoader(val context: Context) {
    fun adjectives(): JSONArray {
        val json = context.openJsonAsset("adjs.json")
        return JSONObject(json).getJSONArray("adjs")
    }

    fun adverbs(): JSONArray {
        val json = context.openJsonAsset("adverbs.json")
        return JSONObject(json).getJSONArray("adverbs")

    }

    fun nouns(): JSONArray {
        val json = context.openJsonAsset("nouns.json")
        return JSONObject(json).getJSONArray("nouns")

    }

    fun verbs(): JSONArray {
        val json = context.openJsonAsset("verbs.json")
        return JSONObject(json).getJSONArray("verbs")
    }
}