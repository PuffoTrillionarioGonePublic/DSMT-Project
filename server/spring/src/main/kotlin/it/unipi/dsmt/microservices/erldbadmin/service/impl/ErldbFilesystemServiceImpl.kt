package it.unipi.dsmt.microservices.erldbadmin.service.impl

import it.unipi.dsmt.microservices.erldbadmin.service.ErldbFilesystemService
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.sql.SQLException

@Service
open class ErldbFilesystemServiceImpl : ErldbFilesystemService {


    @Value("\${erldb.connection.string}")
    private lateinit var baseUrl: String


    private fun nodes(): List<Pair<String, Int>> {
        println("baseUrl: $baseUrl")
        val pattern = "jdbc:(.+?)://((?:.+?:\\d+,)*.+?:\\d+)$".toRegex()
        val matchResult = pattern.find(baseUrl) ?: throw SQLException("invalid url")
        val hostPortString = matchResult.groups[2]?.value ?: throw SQLException("invalid url")
        val hostPortList = hostPortString.split(",")
        val hosts = hostPortList.map { it.split(":").first() }
        val ports = hostPortList.map { it.split(":")[1].toIntOrNull() ?: throw SQLException("invalid port number") }
        return hosts.zip(ports)
    }

    var _client: HttpAccess? = null
        get() {
            synchronized(this) {
                if (field == null) {
                    field = HttpAccess(nodes())
                }
                return field
            }
        }
        private set

    val client: HttpAccess
        get() = _client!!



    override fun listFiles(bucket: String): List<String> {
        //val client = client ?: HttpAccess(nodes())
        //this.client = client
        val result = client.doRequest<JSONArray>("list_files", mapOf("Bucket" to bucket))
        return result.getOrThrow().toList().map { it.toString() }
    }

    override fun deleteBucket(bucket: String) {
        client.doRequest<Unit>("delete_bucket", mapOf("Bucket" to bucket)).getOrThrow()
    }


    override fun deleteFile(bucket: String, filename: String) {
        client.doRequest<Unit>("delete_file", mapOf("Bucket" to bucket, "File" to filename)).getOrThrow()
    }

    override fun listBuckets(): List<String> {
        val result = client.doRequest<JSONArray>("list_buckets", mapOf())
        return result.getOrThrow().toList().map { it.toString() }
    }
}