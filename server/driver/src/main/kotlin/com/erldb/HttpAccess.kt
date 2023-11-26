package com.erldb

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * A class for making HTTP requests to a specified host and port.
 *
 * @property host The host to send requests to.
 * @property port The port to send requests to.
 */
open class HttpAccess(val nodes: List<Pair<String, Int>>) {
    @Volatile
    var open: Boolean = true

    /**
     * Checks if the HTTP connection is open. Throws a RuntimeException if it is not.
     */
    protected fun checkOpen() {
        if (!open) {
            throw RuntimeException("Not open")
        }
    }

    private var host: String? = null
    private var port: Int? = null


    init {
        //changeNode()
        this.host = nodes.first().first
        this.port = nodes.first().second
    }


    //private val BASE_URL: String = "http://$host:$port"

    private val client = OkHttpClient()
    private val JSON = "application/json".toMediaType()

    /**
     * Creates a URL for the specified target.
     *
     * @param target The target to create a URL for.
     * @return The URL for the specified target.
     */
    //fun createUrl(target: String): String = "$BASE_URL/?target=$target"

    /**
     * Sends an HTTP POST request to the specified target with no parameters.
     *
     * @param target The target to send the request to.
     * @return The response from the server as a JSONObject.
     */
    fun doPost(
        target: String
    ): JSONObject = doPost(target, mapOf())



    inline fun <reified R> doRequest(endpoint: String, params: Map<String, Any>): Result<R> {
        val postResult = doPost(endpoint, params)
        try {
            if (postResult.toString() == "{\"ok\":null}") {
                if (R::class == Unit::class) {
                    return Result.success(Unit as R)
                }
                return Result.success(null as R)
            }
            val rv = when (R::class) {
                Int::class -> postResult.getInt("ok") as R
                Long::class -> postResult.getLong("ok") as R
                String::class -> postResult.getString("ok") as R
                Double::class -> postResult.getDouble("ok") as R
                JSONArray::class -> postResult.getJSONArray("ok") as R
                JSONObject::class -> postResult.getJSONObject("ok") as R
                else -> postResult.get("ok") as R
            }
            return Result.success(rv)
        } catch (e: Exception) {
            try {
                val error = ErldbException(postResult.getString("error"))
                return Result.failure(error)
            } catch (e: Exception) {
                return Result.failure(ErldbException("unexpected error"))
            }
        }
    }

    fun changeNode() {
        if (nodes.size == 1) {
            throw RuntimeException("No more nodes to try")
        }
        var node: Pair<String, Int>
        var i = 0
        while (true) {
            node = nodes.random()
            if (host != node.first || port != node.second)
                break
            if (i >= 10)
                throw RuntimeException("No more nodes to try")
            i++
        }

        this.host = node.first
        this.port = node.second
    }


    fun customPost(target: String, requestBody: RequestBody): String? {
        var t: Long = 16;
        for (i in 0..<10) {
            try {
                val url = "http://$host:$port/?target=$target"
                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute().use { it.body?.string() }
                return response
            } catch (e: IOException) {
                t *= 2
                Thread.sleep(t)
                changeNode()
            }
        }
        throw RuntimeException("Failed to connect to any node")
    }

    /**
     * Sends an HTTP POST request to the specified target with the specified parameters.
     *
     * @param target The target to send the request to.
     * @param params The parameters to include in the request.
     * @return The response from the server as a JSONObject.
     */
    fun doPost(
        target: String, params: Map<String, Any?>
    ): JSONObject {
        val requestBody: RequestBody = JSONObject(params).toString().toRequestBody(JSON)
        val response = customPost(target, requestBody)
        //val request = Request.Builder().url(url).post(requestBody).build()
        //val response = client.newCall(request).execute().use { it.body?.string() }
        return JSONObject(response ?: "{}")
    }

}