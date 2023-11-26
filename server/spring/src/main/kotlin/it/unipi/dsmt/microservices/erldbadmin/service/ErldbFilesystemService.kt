package it.unipi.dsmt.microservices.erldbadmin.service

interface ErldbFilesystemService {
    fun listFiles(bucket: String): List<String>
    fun deleteBucket(bucket: String)
    fun deleteFile(bucket: String, filename: String)
    fun listBuckets(): List<String>
}