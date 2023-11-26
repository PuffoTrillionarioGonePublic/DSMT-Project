package com.erldb

import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.sql.*
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*




fun invalid(): SQLException {
    return SQLException("Invalid operation")
}

class ErldbPreparedStatement : AbstractErldbPreparedStatement {
    private lateinit var columnNames: Array<String>
    private var paramCount: Int = 0
    private var resultsWaiting = false
    private var parameters: MutableMap<Int, SQLiteValue> = TreeMap()

    constructor(connection: ErldbConnection, sql: String) : super(connection, sql)  {
        this.sql = sql
        try {
            statementAccess = connection.dbAccess.prepare(sql).getOrThrow()
            columnNames = statementAccess.columnNames().getOrThrow()
            paramCount = statementAccess.bindParameterCount().getOrThrow()
        } catch (ex: Exception) {
            close()
            throw ex
        }
    }

    override fun executeQuery(): ResultSet? = withGuard().execute {
        try {
            rs?.close()
            statementAccess.reset()
            exhaustedResults = false

            getParams().iterator().withIndex().forEach { (i, v) ->
                statementAccess.bind(OneColumnIndex(i + 1), v)
            }

            val stepResult = step()
            if (stepResult == null) {
                exhaustedResults = true
                null
            } else {
                rs = ErldbResultSet(this)
                resultsWaiting = true
                resultSet
            }
        } catch (e: Exception) {
            null
        }
    }


    override fun executeUpdate(): Int = withGuard().execute {
        executeLargeUpdate().toInt()
    }

    fun getParams(): Array<SQLiteValue> = withGuard().execute {
        try {
            val last = parameters.keys.last() - 1
            val rv: Array<SQLiteValue> = Array(last + 1) {
                SQLiteValue.Null {}
            }
            parameters.forEach { (k, v) ->
                rv[k - 1] = v
            }
            rv
        } catch (ex: NoSuchElementException) {
            emptyArray()
        }
    }

    override fun executeLargeUpdate(): Long = withGuard().execute {
        if (columnNames.isNotEmpty()) {
            throw SQLException("Query returns results")
        }
        try {
            rs?.close()
            statementAccess.reset().getOrThrow()
            exhaustedResults = false

            getParams().forEachIndexed { i, v ->
                statementAccess.bind(OneColumnIndex(i + 1), v)
            }
            val db = statementAccess.db()
            synchronized(db) {
                val stepResult = step()
                if (stepResult != null) {
                    throw SQLException("Query returns results")
                }
                exhaustedResults = true
                statementAccess.db().changes().getOrThrow()
            }
        } catch(ex: Exception) {
            this.close()
            throw ex
        }
    }

    override fun execute(): Boolean = withGuard().execute {
        rs?.close()
        statementAccess.reset()

        getParams().forEachIndexed { i, v ->
            statementAccess.bind(OneColumnIndex(i + 1), v)
        }

        val stepResult = step()
        if (stepResult == null) {
            exhaustedResults = true
            false
        } else {
            rs = ErldbResultSet(this)
            rs?.currentRowValue = stepResult
            IStatementAccess.ensureAutoCommit(autoCommit, conn.dbAccess)
            true
        }
    }

    override fun setNull(oneColumnIndex: Int, p1: Int) = setNull(oneColumnIndex, p1, null)
    override fun setNull(oneColumnIndex: Int, p1: Int, p2: String?) = withGuard().execute {
        parameters[oneColumnIndex] = SQLiteValue.Null {}
    }

    override fun setBoolean(oneColumnIndex: Int, value: Boolean) {
        setInt(oneColumnIndex, if (value) 1 else 0)
    }

    override fun setByte(oneColumnIndex: Int, value: Byte) {
        setInt(oneColumnIndex, value.toInt())
    }

    override fun setShort(oneColumnIndex: Int, value: Short) {
        setInt(oneColumnIndex, value.toInt())
    }

    override fun setInt(oneColumnIndex: Int, value: Int) = withGuard().execute {
        parameters[oneColumnIndex] = SQLiteValue.Integer(value.toLong())
    }

    override fun setLong(oneColumnIndex: Int, value: Long) {
        parameters[oneColumnIndex] = SQLiteValue.Integer(value)
    }

    override fun setFloat(oneColumnIndex: Int, value: Float) = setDouble(oneColumnIndex, value.toDouble())


    override fun setDouble(oneColumnIndex: Int, value: Double) = withGuard().execute {
        parameters[oneColumnIndex] = SQLiteValue.Real(value)
    }

    override fun setBigDecimal(oneColumnIndex: Int, p1: BigDecimal?) {
        throw unsupported()
    }

    override fun setString(oneColumnIndex: Int, value: String) = withGuard().execute {
        parameters[oneColumnIndex] = SQLiteValue.Text(value)
    }

    override fun setBytes(oneColumnIndex: Int, value: ByteArray) = withGuard().execute {
        parameters[oneColumnIndex] = SQLiteValue.Blob(value)
    }


    @Throws(SQLException::class)
    protected fun setDateByMilliseconds(pos: Int, value: Long, calendar: Calendar) = withGuard().execute {
        val date = Date(value)
        val formattedDate = SimpleDateFormat(connectionConfig.dateStringFormat).format(date)
        parameters[pos] = SQLiteValue.Text(formattedDate)
    }

    override fun setDate(oneColumnIndex: Int, x: Date) = setDate(oneColumnIndex, x, Calendar.getInstance())


    override fun setDate(oneColumnIndex: Int, x: Date, cal: Calendar) =
        setDateByMilliseconds(oneColumnIndex, x.time, cal)


    override fun setTime(oneColumnIndex: Int, x: Time) = setTime(oneColumnIndex, x, Calendar.getInstance())


    override fun setTime(oneColumnIndex: Int, x: Time, cal: Calendar) =
        setDateByMilliseconds(oneColumnIndex, x.time, cal)


    override fun setTimestamp(oneColumnIndex: Int, x: Timestamp) =
        setTimestamp(oneColumnIndex, x, Calendar.getInstance())


    override fun setTimestamp(oneColumnIndex: Int, x: Timestamp, cal: Calendar) =
        setDateByMilliseconds(oneColumnIndex, x.time, cal)


    @Throws(SQLException::class)
    private fun readBytes(istream: InputStream, length: Int): ByteArray {
        if (length < 0) {
            throw SQLException("Error reading stream. Length should be non-negative")
        }
        val bytes = ByteArray(length)
        return try {
            var bytesRead: Int
            var totalBytesRead = 0
            while (totalBytesRead < length) {
                bytesRead = istream.read(bytes, totalBytesRead, length - totalBytesRead)
                if (bytesRead == -1) {
                    throw IOException("End of stream has been reached")
                }
                totalBytesRead += bytesRead
            }
            bytes
        } catch (cause: IOException) {
            val exception = SQLException("Error reading stream")
            exception.initCause(cause)
            throw exception
        }
    }

    override fun setAsciiStream(oneColumnIndex: Int, istream: InputStream, length: Int) =
        setUnicodeStream(oneColumnIndex, istream, length)


    @Deprecated("Deprecated in Java")
    override fun setUnicodeStream(oneColumnIndex: Int, istream: InputStream, length: Int) = withGuard().execute {
        if (length == 0) {
            setNull(oneColumnIndex, Types.NULL)
        }
        try {
            setString(oneColumnIndex, String(readBytes(istream, length), Charsets.UTF_8))
        } catch (e: UnsupportedEncodingException) {
            throw SQLException("Unsupported encoding: ${e.message}")
        }
    }

    override fun setBinaryStream(oneColumnIndex: Int, istream: InputStream, length: Int) = withGuard().execute {
        if (length == 0) {
            setNull(oneColumnIndex, Types.NULL)
        }
        setBytes(oneColumnIndex, readBytes(istream, length))
    }

    override fun clearParameters() = withGuard().execute {

        try {
            if (!statementAccess.clearBindings().getOrThrow()) {
                throw SQLException("Failed to clear parameters")
            }
            parameters.clear()
        } catch (ex: Exception) {
            this.close()
        }
    }

    override fun setObject(oneColumnIndex: Int, v: Any?, t: Int) {
        setObject(oneColumnIndex, v)
    }

    override fun setObject(oneColumnIndex: Int, v: Any?) = withGuard().execute {
        if (v == null) {
            setNull(oneColumnIndex, Types.NULL)
        } else {
            when (v) {
                is String -> setString(oneColumnIndex, v)
                is Byte -> setByte(oneColumnIndex, v)
                is Short -> setShort(oneColumnIndex, v)
                is Int -> setInt(oneColumnIndex, v)
                is Long -> setLong(oneColumnIndex, v)
                is Float -> setFloat(oneColumnIndex, v)
                is Double -> setDouble(oneColumnIndex, v)
                is Boolean -> setBoolean(oneColumnIndex, v)
                is ByteArray -> setBytes(oneColumnIndex, v)
                is Date -> setDate(oneColumnIndex, v)
                is Time -> setTime(oneColumnIndex, v)
                is Timestamp -> setTimestamp(oneColumnIndex, v)
                is BigDecimal -> setBigDecimal(oneColumnIndex, v)
                is InputStream -> setBinaryStream(oneColumnIndex, v)
                is Blob -> setBlob(oneColumnIndex, v)
                is Clob -> setClob(oneColumnIndex, v)
                else -> throw SQLException("Unsupported parameter type: " + v.javaClass.name)
            }
        }
    }

    override fun setObject(oneColumnIndex: Int, v: Any?, t: Int, s: Int) = setObject(oneColumnIndex, v)


    override fun setCharacterStream(oneColumnIndex: Int, reader: Reader, length: Int) = withGuard().execute {
        try {
            val sb = StringBuffer()
            val cbuf = CharArray(4096)
            var cnt: Int
            while (reader.read(cbuf).also { cnt = it } > 0) {
                sb.append(cbuf, 0, cnt)
            }
            setString(oneColumnIndex, sb.toString())
        } catch (e: IOException) {
            throw SQLException(
                "Cannot read from character stream, exception message: ${e.message}"
            )
        }

    }

    override fun getMetaData(): ResultSetMetaData {
        TODO("Not yet implemented")
    }

    override fun getParameterMetaData(): ParameterMetaData {
        TODO("Not yet implemented")
    }

    override fun addBatch() {}

}
