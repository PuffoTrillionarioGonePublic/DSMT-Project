package it.unipi.dsmt.microservices.erldbadmin.service.impl

import com.erldb.ErldbConnection
import com.erldb.ErldbStatement
import com.erldb.SQLiteValue
import it.unipi.dsmt.microservices.erldbadmin.service.ErldbService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet


const val DEFAULT_BUSY_TIMEOUT = 5000

@Service
open class ErldbServiceImpl : ErldbService {

    var busyTimeoutValue = DEFAULT_BUSY_TIMEOUT

    @Value("\${erldb.connection.string}")
    private lateinit var baseUrl: String
    fun createConnection(bucket: String, filename: String): Connection {
        val rv = ErldbConnection(
            "$baseUrl/$bucket/$filename", null
        )
        rv.setBusyTimeout(busyTimeoutValue)
        return rv
    }

    override fun setBusyTimeout(timeout: Int) {
        busyTimeoutValue = timeout
    }


    fun resultSetToStringList(resultSet: ResultSet): List<String> {
        val columnCount = resultSet.metaData.columnCount
        if (columnCount != 1) {
            throw Exception("resultSetToStringList: columnCount != 1")
        }
        val result = mutableListOf<String>()
        while (resultSet.next()) {
            result.add(resultSet.getString(1))
        }
        return result
    }


    override fun libVersion(): String = createConnection("private", "credentials").use {
        it.metaData.databaseProductVersion
    }


    override fun dbList(): List<String> = createConnection("private", "credentials").use {
        val catalogs: ResultSet = it.metaData.catalogs
        resultSetToStringList(catalogs)
    }

    override fun query(
        bucket: String, db: String, statement: String, params: List<SQLiteValue>
    ): Pair<List<String>, List<Array<SQLiteValue>>> = createConnection(bucket, db).use {
        val preparedStatement: PreparedStatement? = it.prepareStatement(statement)
        params.forEachIndexed { index, value ->
            preparedStatement?.setObject(index + 1, value.toAny())
        }
        if (preparedStatement == null) {
            return Pair(emptyList(), emptyList())
        }
        preparedStatement.executeQuery()?.use { resultSet ->
            val columnCount = resultSet.metaData?.columnCount
            columnCount?.let {
                val columnNames = (1..it).map { resultSet.metaData.getColumnName(it) }
                val result = mutableListOf<Array<SQLiteValue>>()
                while (resultSet.next()) {
                    val row = (1..columnCount).map { SQLiteValue.from(resultSet.getObject(it)) }.toTypedArray()
                    result.add(row)
                }
                Pair(columnNames, result)
            }
        } ?: run {
            val stmt = preparedStatement.unwrap(ErldbStatement::class.java)
            val columnNames = stmt.columnNames()
            var a = Pair<List<String>, List<Array<SQLiteValue>>>(emptyList(), emptyList())
            columnNames.onSuccess { a = Pair(it.toList(), emptyList()) }
            return a
        }
    }


    override fun tablesList(bucket: String, db: String): List<String> = createConnection(bucket, db).use {
        try {
            it.metaData.getTables(null, null, null, null).use {
                resultSetToStringList(it)
            }
        } catch (_: Exception) {
            return@use emptyList()
        }
    }
}