/**
 * Represents a result set returned by an ErldbStatement.
 *
 * This class provides methods to retrieve data from the result set, such as `getString`, `getInt`, `getBoolean`, etc.
 * It also provides methods to navigate the result set, such as `next` to move to the next row, and `wasNull` to check if the last retrieved value was null.
 *
 * @property pastLastRow Indicates whether the result set has reached the end.
 * @property stmt The ErldbStatement that created this result set.
 * @property open Indicates whether the result set is open.
 * @property cols The list of column names in the result set.
 * @property currentRow The current row number.
 * @property currentRowValue The values of the current row.
 * @property lastCol The index of the last accessed column.
 * @property connConfig The configuration of the connection used to create the statement that created this result set.
 */
package com.erldb

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.Reader
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.sql.*
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

// ErldbResultSet <--- 1 ---|--- 1 ---> ErldbStatement
class ErldbResultSet : AbstractErldbResultSet {
    var pastLastRow: Boolean
    var stmt: ErldbStatement

    @Volatile
    var open: Boolean
    var cols: ArrayList<String>
    var currentRow: Long
    var currentRowValue: Array<SQLiteValue>?
    var lastCol: OneColumnIndex?

    val connConfig: ErldbConfig
        get() = stmt.conn.config



    constructor(stmt: ErldbStatement) {
        pastLastRow = false
        this.stmt = stmt
        open = true
        cols = ArrayList()
        currentRow = 0
        currentRowValue = null
        lastCol = null
    }

    /**
     * Checks if the given [oneColumnIndex] is valid and returns the corresponding zero-based column index.
     * @throws SQLException if the [cols] list is empty or if the [oneColumnIndex] is out of bounds.
     * @return the zero-based column index corresponding to the given [oneColumnIndex].
     */
    fun checkCol(oneColumnIndex: OneColumnIndex): ZeroColumnIndex = checkOpen().let{
        if (cols.isEmpty()) {
            throw SQLException("SQLite JDBC: inconsistent internal state")
        }

        oneColumnIndex.value.let {
            if (!(1..cols.size).contains(it)) {
                throw SQLException("column $it out of bounds [1,${cols.size}]")
            }
        }
        return ZeroColumnIndex.fromOneColumnIndex(oneColumnIndex)
    }

    /**
     * Marks the given [oneColumnIndex] as the last accessed column and returns the corresponding zero-based column index.
     * @throws SQLException if the [oneColumnIndex] is invalid.
     * @return the zero-based column index corresponding to the given [oneColumnIndex].
     */
    fun markCol(oneColumnIndex: OneColumnIndex): ZeroColumnIndex = checkOpen().let {
        checkCol(oneColumnIndex)
        lastCol = oneColumnIndex
        return ZeroColumnIndex.fromOneColumnIndex(oneColumnIndex)
    }

    /**
     * Checks if the result set is open.
     * @throws SQLException if the result set is closed.
     */
    fun checkOpen() {
        if (!open) throw SQLException("ResultSet closed")
    }


    /**
     * Closes the ResultSet without closing the Statement.
     */
    fun closeNoStatement() {
        cols.clear()
        currentRowValue = null
        lastCol = null
        currentRow = 0
        pastLastRow = false
        open = false
    }

    /**
     * Closes the ResultSet and the Statement if closeOnCompletion is true.
     */
    override fun close() = checkOpen().let {
        closeNoStatement()
        if (stmt.closeOnCompletion) {
            stmt.closeNoResultSet()
        }
    }

    /**
     * Moves the cursor to the next row in the ResultSet.
     * @return true if the new current row is valid; false if there are no more rows
     */
    override fun next(): Boolean = checkOpen().let {
        if (!open || pastLastRow) {
            return false // finished ResultSet
        }
        lastCol = null
        // first row is loaded by execute(), so do not step() again
        /*if (currentRow == 0L) {
            currentRow++
            return true
        }*/
        if (stmt.maxRows != 0L && currentRow == stmt.maxRows) {
            return false
        }

        val row = stmt.consumeRow()
        if (row == null) {
            pastLastRow = true
            currentRowValue = null
            return false
        }
        currentRow++
        currentRowValue = row
        return true
    }

    /**
     * Returns the last column of the current row, or null if there is no current row.
     * @return The last column of the current row, or null if there is no current row.
     */
    fun getLastColumn(): SQLiteValue? = lastCol?.let {
        val col = it.toZeroColumnIndex().value.toInt()
        currentRowValue?.let { it[col] }
    }

    /**
     * Returns the value of the specified column in the current row, or null if there is no current row.
     * @param oneColumnIndex The index of the column to retrieve.
     * @return The value of the specified column in the current row, or null if there is no current row.
     */
    fun getColumn(oneColumnIndex: OneColumnIndex): SQLiteValue? {
        return currentRowValue?.let {
            val col = oneColumnIndex.toZeroColumnIndex().value.toInt()
            it[col]
        }
    }

    /**
     * Returns the value of the specified column in the current row, or null if there is no current row.
     * @param zeroColumnIndex The index of the column to retrieve.
     * @return The value of the specified column in the current row, or null if there is no current row.
     */
    fun getColumn(zeroColumnIndex: ZeroColumnIndex): SQLiteValue? = getColumn(zeroColumnIndex.toOneColumnIndex())

    /**
     * Returns the value of the specified column in the current row and marks it as the last column retrieved.
     * @param oneColumnIndex The index of the column to retrieve.
     * @return The value of the specified column in the current row and marks it as the last column retrieved.
     */
    fun retrieveAndMarkColumn(oneColumnIndex: Int): SQLiteValue? = checkOpen().let {
        getColumn(markCol(OneColumnIndex(oneColumnIndex)))
    }

    override fun wasNull(): Boolean = checkOpen().let {
        val lastCol = getLastColumn() ?: throw SQLException("No column selected")
        return lastCol is SQLiteValue.Null
    }

    override fun getString(oneColumnIndex: Int): String? =
        checkOpen().let { retrieveAndMarkColumn(oneColumnIndex)?.into<String>() }

    override fun getBoolean(oneColumnIndex: Int): Boolean = getInt(oneColumnIndex) != 0

    override fun getByte(oneColumnIndex: Int): Byte = getInt(oneColumnIndex).toByte()

    override fun getShort(oneColumnIndex: Int): Short = getInt(oneColumnIndex).toShort()

    override fun getInt(oneColumnIndex: Int): Int =
        retrieveAndMarkColumn(oneColumnIndex)?.into<Int>() ?: throw SQLException("Unexpected value type")

    override fun getLong(oneColumnIndex: Int): Long =
        retrieveAndMarkColumn(oneColumnIndex)?.into<Long>() ?: throw SQLException("Unexpected value type")

    override fun getFloat(oneColumnIndex: Int): Float = getDouble(oneColumnIndex).toFloat()

    override fun getDouble(oneColumnIndex: Int): Double = checkOpen().let {
        retrieveAndMarkColumn(oneColumnIndex)?.into<Double>() ?: throw SQLException("Unexpected value type")
    }

    override fun getBigDecimal(oneColumnIndex: Int): BigDecimal? = retrieveAndMarkColumn(oneColumnIndex)?.let {
        when (val value: SQLiteValue = it) {
            is SQLiteValue.Null -> null
            is SQLiteValue.Integer -> BigDecimal.valueOf(value.value)
            is SQLiteValue.Real -> BigDecimal.valueOf(value.value)
            is SQLiteValue.Text -> BigDecimal(value.value)
            is SQLiteValue.Blob -> throw SQLException("Unexpected value type: $value")
        }
    }

    override fun getBytes(oneColumnIndex: Int): ByteArray? = retrieveAndMarkColumn(oneColumnIndex)?.into<ByteArray>()

    override fun getDate(oneColumnIndex: Int): Date? = retrieveAndMarkColumn(oneColumnIndex)?.let {
        when (it) {
            is SQLiteValue.Null -> null
            is SQLiteValue.Text -> {
                val dateText = it.value
                if (dateText.isEmpty()) {
                    null
                } else try {
                    SimpleDateFormat(connConfig.dateStringFormat).parse(dateText)?.let { Date(it.time) }
                } catch (e: Exception) {
                    throw SQLException("Error parsing date", e)
                }
            }

            is SQLiteValue.Integer -> Date(it.value * connConfig.getDateMultiplier())
            else -> throw SQLException("Unexpected value type: $it")
        }
    }

    override fun getDate(oneColumnIndex: Int, cal: Calendar): Date? = retrieveAndMarkColumn(oneColumnIndex)?.let {
        when (val value = it) {
            is SQLiteValue.Null -> return null
            is SQLiteValue.Text -> {
                val dateText = value.value
                return if (dateText.isEmpty()) {
                    null
                } else try {
                    val simpleDateFormat = SimpleDateFormat(connConfig.dateStringFormat)
                    simpleDateFormat.timeZone = cal.timeZone
                    simpleDateFormat.parse(dateText)?.let { Date(it.time) }
                } catch (e: Exception) {
                    throw SQLException("Error parsing date", e)
                }
            }

            is SQLiteValue.Integer -> {
                val date = Date(value.value * connConfig.getDateMultiplier())
                cal.time = date
                return date
            }

            else -> throw SQLException("Unexpected value type: $value")
        }
    }

    override fun getTime(oneColumnIndex: Int): Time? = retrieveAndMarkColumn(oneColumnIndex)?.let {
        when (val value = it) {
            is SQLiteValue.Null -> null
            is SQLiteValue.Text -> {
                val dateText = value.value
                if (dateText.isEmpty()) {
                    null
                } else try {
                    SimpleDateFormat(connConfig.dateStringFormat).parse(dateText)?.let { Time(it.time) }
                } catch (e: Exception) {
                    throw SQLException("Error parsing time", e)
                }
            }

            is SQLiteValue.Integer -> Time(value.value * connConfig.getDateMultiplier())
            else -> throw SQLException("Unexpected value type: $value")
        }
    }

    override fun getTime(oneColumnIndex: Int, cal: Calendar): Time? = retrieveAndMarkColumn(oneColumnIndex)?.let {
        when (val value = it) {
            is SQLiteValue.Null -> null
            is SQLiteValue.Text -> {
                val dateText = it.into<String>()
                if (dateText.isNullOrEmpty()) {
                    null
                } else {
                    try {
                        val simpleDateFormat = SimpleDateFormat(connConfig.dateStringFormat)
                        simpleDateFormat.timeZone = cal.timeZone
                        simpleDateFormat.parse(dateText)?.let { Time(it.time) }
                    } catch (e: java.lang.Exception) {
                        throw SQLException("Error parsing time", e)
                    }
                }
            }

            is SQLiteValue.Integer -> {
                cal.timeInMillis = value.value * connConfig.getDateMultiplier()
                Time(cal.time.time)
            }

            else -> {
                throw SQLException("Unexpected value type: $value")
            }
        }
    }

    override fun getTimestamp(oneColumnIndex: Int): Timestamp? = retrieveAndMarkColumn(oneColumnIndex)?.let {
        when (it) {
            is SQLiteValue.Null -> null
            is SQLiteValue.Text -> {
                val dateText = it.value
                if (dateText.isEmpty()) {
                    null
                } else try {
                    val dateFormat = SimpleDateFormat(connConfig.dateStringFormat)
                    dateFormat.parse(dateText)?.let { Timestamp(it.time) }
                } catch (e: Exception) {
                    throw SQLException("Error parsing timestamp", e)
                }
            }


            is SQLiteValue.Integer -> {
                Timestamp(it.value * connConfig.getDateMultiplier())
            }

            else -> throw SQLException("Unexpected value type: $it")
        }
    }

    override fun getTimestamp(oneColumnIndex: Int, cal: Calendar): Timestamp? =
        retrieveAndMarkColumn(oneColumnIndex)?.let {
            when (it) {
                is SQLiteValue.Null -> null
                is SQLiteValue.Text -> {
                    val dateText = it.value
                    if (dateText.isEmpty()) {
                        null
                    } else try {
                        val dateFormat = SimpleDateFormat(connConfig.dateStringFormat)
                        dateFormat.timeZone = cal.timeZone
                        dateFormat.parse(dateText)?.let { Timestamp(it.time) }
                    } catch (e: Exception) {
                        throw SQLException("Error parsing timestamp", e)
                    }
                }

                is SQLiteValue.Integer -> {
                    cal.timeInMillis = it.value * connConfig.getDateMultiplier()
                    Timestamp(cal.time.time)
                }

                else -> throw SQLException("Unexpected value type: $it")
            }
        }

    override fun getAsciiStream(oneColumnIndex: Int): InputStream? = try {
        getString(oneColumnIndex)?.let { ByteArrayInputStream(it.toByteArray()) }
    } catch (e: UnsupportedEncodingException) {
        null
    }

    override fun getBinaryStream(oneColumnIndex: Int): InputStream? =
        getBytes(oneColumnIndex)?.let { ByteArrayInputStream(it) }

    override fun getMetaData(): ResultSetMetaData {
        checkOpen()
        return ErldbResultSetMetaData(this)
    }

    override fun getObject(oneColumnIndex: Int): Any? = retrieveAndMarkColumn(oneColumnIndex)?.toAny()

    override fun <T : Any?> getObject(oneColumnIndex: Int, type: Class<T>): T? {
        val f: (Int) -> Any? = when (type) {
            Int::class.java -> ::getInt
            Long::class.java -> ::getLong
            Float::class.java -> ::getFloat
            Double::class.java -> ::getDouble
            String::class.java -> ::getString
            Boolean::class.java -> ::getBoolean
            BigDecimal::class.java -> ::getBigDecimal
            ByteArray::class.java -> ::getBytes
            Date::class.java -> ::getDate
            Time::class.java -> ::getTime
            Timestamp::class.java -> ::getTimestamp
            LocalDate::class.java -> { t -> getDate(t)?.toLocalDate() }
            LocalTime::class.java -> { t -> getTime(t)?.toLocalTime() }
            LocalDateTime::class.java -> { t -> getTimestamp(t)?.toLocalDateTime() }
            else -> throw SQLException("Unsupported type: ${type.name}")
        }
        return f(oneColumnIndex) as T?
    }

    override fun findColumn(col: String): Int {
        checkOpen()
        val index = cols.indexOfFirst { it.equals(col, ignoreCase = true) }
        if (index == -1) throw SQLException("Column $col not found")
        return index + 1
    }

    override fun getCharacterStream(oneColumnIndex: Int): Reader? =
        checkOpen().let { getString(oneColumnIndex)?.reader() }

    override fun isBeforeFirst(): Boolean = checkOpen().let { currentRow == 0L }

    override fun isAfterLast(): Boolean = checkOpen().let { pastLastRow }

    override fun isFirst(): Boolean = checkOpen().let { currentRow == 1L }

    override fun getRow(): Int = checkOpen().let { currentRow.toInt() }

    override fun getType(): Int = checkOpen().let { ResultSet.TYPE_FORWARD_ONLY }

    override fun getConcurrency(): Int = checkOpen().let { ResultSet.CONCUR_READ_ONLY }

    override fun rowUpdated(): Boolean = checkOpen().let { false }

    override fun rowInserted(): Boolean = checkOpen().let { false }

    override fun rowDeleted(): Boolean = checkOpen().let { false }

    override fun getStatement(): Statement = checkOpen().let { stmt }

    override fun isClosed(): Boolean = !open

    override fun getNCharacterStream(oneColumnIndex: Int): Reader? =
        checkOpen().let { getString(oneColumnIndex)?.reader() }

}
