package com.erldb

import org.json.JSONArray

class ContextAccess private constructor(nodes: List<Pair<String, Int>>) : HttpAccess(nodes) {
    companion object {
        @Volatile
        var contextAccess: ContextAccess? = null


        fun getInstance(nodes: List<Pair<String, Int>>): ContextAccess {
            if (contextAccess == null) {
                synchronized(ContextAccess::class) {
                    if (contextAccess == null) {
                        contextAccess = ContextAccess(nodes)
                    }
                }
            }
            return contextAccess!!
        }
    }

    fun listFiles(): List<String> {
        val arr: JSONArray = doPost("list_files").getJSONArray("ok")
        val list = mutableListOf<String>()
        (0..<arr.length()).map(arr::getString).forEach(list::add)
        return list
    }

    fun createConnection(erldbConnection: ErldbConnection, bucket: String, filename: String, nodes: List<Pair<String, Int>>): DatabaseAccess {
        val connectionId = doPost("create_connection", mapOf("Bucket" to bucket, "File" to filename)).getLong("ok")
        return DatabaseAccess(nodes, erldbConnection, connectionId)
    }

    fun libVersion(): String = doPost("lib_version").getString("ok")


}