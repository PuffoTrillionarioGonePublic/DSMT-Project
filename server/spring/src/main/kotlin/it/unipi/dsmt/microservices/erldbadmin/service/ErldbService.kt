package it.unipi.dsmt.microservices.erldbadmin.service

import com.erldb.SQLiteValue

interface ErldbService {
    fun setBusyTimeout(timeout: Int)
    fun libVersion(): String

    fun dbList(): List<String>


    fun tablesList(bucket: String, db: String): List<String>
    fun query(bucket: String, db: String, statement: String, params: List<SQLiteValue>): Pair<List<String>, List<Array<SQLiteValue>>>

    fun query(bucket: String, db: String, statement: String): Pair<List<String>, List<Array<SQLiteValue>>> {
        return query(bucket, db, statement, listOf())
    }
}