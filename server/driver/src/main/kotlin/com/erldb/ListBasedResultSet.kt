/**
 * This class represents a ResultSet implementation that is backed by a list of rows and columns.
 * It is used to provide a fake implementation of a ResultSet for testing purposes.
 * @property columnNames The list of column names.
 * @property data The list of rows and columns.
 */
 
package com.erldb

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*


class ListBasedResultSet(columnNames: List<String>, data: List<List<SQLiteValue>>) : ResultSet {
    private val columnNames: List<String>
    private val data: List<List<SQLiteValue>>
    private var currentIndex = -1

    init {
        this.columnNames = columnNames
        this.data = data
    }

    @Throws(SQLException::class)
    override fun next(): Boolean {
        if (currentIndex < data.size - 1) {
            currentIndex++
            return true
        }
        return false
    }


    /**
     * Returns the value of the specified column in the current row of this ResultSet object as a generic type.
     * The type of the returned value depends on the type of the column.
     *
     * @param columnIndex the index of the column, starting from 1
     * @param onNull a function to be called when the column value is null
     * @param onBlob a function to be called when the column value is a blob
     * @param onInt a function to be called when the column value is an integer
     * @param onReal a function to be called when the column value is a real number
     * @param onText a function to be called when the column value is a text
     *
     * @return the value of the specified column in the current row of this ResultSet object as a generic type
     *
     * @throws SQLException if the column index is invalid
     */

    fun getGeneric(
        columnIndex: Int,
        onNull: (SQLiteValue) -> Any?,
        onBlob: (SQLiteValue) -> Any?,
        onInt: (SQLiteValue) -> Any?,
        onReal: (SQLiteValue) -> Any?,
        onText: (SQLiteValue) -> Any?
    ): Any? {
        val row = data[currentIndex]
        if (row.isEmpty()) {
            throw SQLException("Invalid column index")
        }
        return when (row[0]) {
            is SQLiteValue.Null -> onNull(row[columnIndex - 1])
            is SQLiteValue.Blob -> onBlob(row[columnIndex - 1])
            is SQLiteValue.Integer -> onInt(row[columnIndex - 1])
            is SQLiteValue.Real -> onReal(row[columnIndex - 1])
            is SQLiteValue.Text -> onText(row[columnIndex - 1])
        }

    }


    @Throws(SQLException::class)
    override fun getInt(columnIndex: Int): Int = getLong(columnIndex).toInt()

    @Throws(SQLException::class)
    override fun getString(columnIndex: Int): String? {
        return getGeneric(columnIndex,
            { null },
            { throw SQLException("Invalid column type") },
            { throw SQLException("Invalid column type") },
            { throw SQLException("Invalid column type") },
            { row -> (row as SQLiteValue.Text).value }) as String?
    }

    // Implement all other ResultSet methods (no-op or throw UnsupportedOperationException)
    // ... (other methods)
    @Throws(SQLException::class)
    override fun close() {
        // No resources to close in this fake implementation
    } // ... (other methods)

    override fun <T : Any?> unwrap(p0: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun isWrapperFor(p0: Class<*>?): Boolean {
        TODO("Not yet implemented")
    }


    override fun wasNull(): Boolean {
        TODO("Not yet implemented")
    }


    override fun getString(col: String?): String? = getString(findColumn(col))

    override fun getBoolean(b: Int): Boolean = getInt(b) != 0

    override fun getBoolean(p0: String?): Boolean = getBoolean(findColumn(p0))

    override fun getByte(p0: Int): Byte = getInt(p0).toByte()

    override fun getByte(p0: String?): Byte = getByte(findColumn(p0))

    override fun getShort(p0: Int): Short = getInt(p0).toShort()

    override fun getShort(p0: String?): Short = getShort(findColumn(p0))

    override fun getInt(p0: String?): Int = getInt(findColumn(p0))

    override fun getLong(p0: Int): Long = getGeneric(p0,
        { 0L },
        { throw SQLException("Invalid column type") },
        { row -> (row as SQLiteValue.Integer).value },
        { throw SQLException("Invalid column type") },
        { throw SQLException("Invalid column type") }) as Long

    override fun getLong(p0: String?): Long {
        TODO("Not yet implemented")
    }

    override fun getFloat(p: Int): Float = getGeneric(p,
        { 0f },
        { throw SQLException("Invalid column type") },
        { row -> (row as SQLiteValue.Integer).value.toFloat() },
        { throw SQLException("Invalid column type") },
        { throw SQLException("Invalid column type") }) as Float


    override fun getFloat(p0: String?): Float = getFloat(findColumn(p0))
    override fun getDouble(p0: Int): Double = getGeneric(p0,
        { 0.0 },
        { throw SQLException("Invalid column type") },
        { row -> (row as SQLiteValue.Integer).value.toDouble() },
        { row -> (row as SQLiteValue.Real).value },
        { throw SQLException("Invalid column type") }) as Double

    override fun getDouble(p0: String?): Double = getDouble(findColumn(p0))

    override fun getBigDecimal(p0: Int, p1: Int): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getBigDecimal(p0: String?, p1: Int): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getBigDecimal(p0: Int): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getBigDecimal(p0: String?): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getBytes(p0: Int): ByteArray = getGeneric(p0,
        { throw SQLException("Invalid column type") },
        { row -> (row as SQLiteValue.Blob).value },
        { throw SQLException("Invalid column type") },
        { throw SQLException("Invalid column type") },
        { throw SQLException("Invalid column type") }) as ByteArray

    override fun getBytes(p0: String?): ByteArray = getBytes(findColumn(p0))

    override fun getDate(p0: Int): Date {
        TODO("Not yet implemented")
    }

    override fun getDate(p0: String?): Date = getDate(findColumn(p0))

    override fun getDate(p0: Int, p1: Calendar?): Date = getDate(p0)

    override fun getDate(p0: String?, p1: Calendar?): Date = getDate(p0)
    override fun getTime(p0: Int): Time {
        TODO("Not yet implemented")
    }

    override fun getTime(p0: String?): Time {
        TODO("Not yet implemented")
    }

    override fun getTime(p0: Int, p1: Calendar?): Time {
        TODO("Not yet implemented")
    }

    override fun getTime(p0: String?, p1: Calendar?): Time {
        TODO("Not yet implemented")
    }

    override fun getTimestamp(p0: Int): Timestamp {
        TODO("Not yet implemented")
    }

    override fun getTimestamp(p0: String?): Timestamp {
        TODO("Not yet implemented")
    }

    override fun getTimestamp(p0: Int, p1: Calendar?): Timestamp {
        TODO("Not yet implemented")
    }

    override fun getTimestamp(p0: String?, p1: Calendar?): Timestamp {
        TODO("Not yet implemented")
    }

    override fun getAsciiStream(p0: Int): InputStream {
        TODO("Not yet implemented")
    }

    override fun getAsciiStream(p0: String?): InputStream {
        TODO("Not yet implemented")
    }

    override fun getUnicodeStream(p0: Int): InputStream {
        TODO("Not yet implemented")
    }

    override fun getUnicodeStream(p0: String?): InputStream {
        TODO("Not yet implemented")
    }

    override fun getBinaryStream(p0: Int): InputStream {
        TODO("Not yet implemented")
    }

    override fun getBinaryStream(p0: String?): InputStream {
        TODO("Not yet implemented")
    }

    override fun getWarnings(): SQLWarning {
        TODO("Not yet implemented")
    }

    override fun clearWarnings() {
        TODO("Not yet implemented")
    }

    override fun getCursorName(): String {
        TODO("Not yet implemented")
    }

    override fun getMetaData(): ResultSetMetaData {
        return object : ResultSetMetaData {
            override fun <T : Any?> unwrap(p0: Class<T>?): T {
                TODO("Not yet implemented")
            }

            override fun isWrapperFor(p0: Class<*>?): Boolean {
                TODO("Not yet implemented")
            }

            override fun getColumnCount(): Int {
                return columnNames.size
            }

            override fun isAutoIncrement(p0: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun isCaseSensitive(p0: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun isSearchable(p0: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun isCurrency(p0: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun isNullable(p0: Int): Int {
                TODO("Not yet implemented")
            }

            override fun isSigned(p0: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun getColumnDisplaySize(p0: Int): Int {
                TODO("Not yet implemented")
            }

            override fun getColumnLabel(p0: Int): String {
                TODO("Not yet implemented")
            }

            override fun getColumnName(i: Int): String {
                return columnNames[i - 1]
            }

            override fun getSchemaName(p0: Int): String {
                TODO("Not yet implemented")
            }

            override fun getPrecision(p0: Int): Int {
                TODO("Not yet implemented")
            }

            override fun getScale(p0: Int): Int {
                TODO("Not yet implemented")
            }

            override fun getTableName(p0: Int): String {
                TODO("Not yet implemented")
            }

            override fun getCatalogName(p0: Int): String {
                TODO("Not yet implemented")
            }



            override fun getColumnType(index: Int): Int = data.firstOrNull()?.let {
                when (it[index - 1]) {
                    is SQLiteValue.Null -> Types.NULL
                    is SQLiteValue.Blob -> Types.BLOB
                    is SQLiteValue.Integer -> Types.INTEGER
                    is SQLiteValue.Real -> Types.REAL
                    is SQLiteValue.Text -> Types.VARCHAR
                }
            } ?: Types.NULL


            override fun getColumnTypeName(p0: Int): String  {
                return when (getColumnType(p0)) {
                    Types.NULL -> "NULL"
                    Types.BLOB -> "BLOB"
                    Types.INTEGER -> "INTEGER"
                    Types.REAL -> "REAL"
                    Types.VARCHAR -> "VARCHAR"
                    else -> "UNKNOWN"
                }
            }

            override fun isReadOnly(p0: Int): Boolean = false

            override fun isWritable(p0: Int): Boolean = true

            override fun isDefinitelyWritable(p0: Int): Boolean = true

            override fun getColumnClassName(p0: Int): String =
                when (getColumnType(p0)) {
                    Types.NULL -> "java.lang.Object"
                    Types.BLOB -> "java.lang.Object"
                    Types.INTEGER -> "java.lang.Long"
                    Types.REAL -> "java.lang.Double"
                    Types.VARCHAR -> "java.lang.String"
                    else -> "java.lang.Object"
                }


        }
    }

    override fun getObject(p0: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getObject(p0: String?): Any {
        TODO("Not yet implemented")
    }

    override fun getObject(p0: Int, p1: MutableMap<String, Class<*>>?): Any {
        TODO("Not yet implemented")
    }

    override fun getObject(p0: String?, p1: MutableMap<String, Class<*>>?): Any {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getObject(p0: Int, p1: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getObject(p0: String?, p1: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun findColumn(s: String?): Int {
        s?.let { s ->
            columnNames.forEachIndexed { index, name ->
                if (name == s) {
                    return index + 1
                }
            }
        }
        return -1
    }

    override fun getCharacterStream(p0: Int): Reader {
        TODO("Not yet implemented")
    }

    override fun getCharacterStream(p0: String?): Reader {
        TODO("Not yet implemented")
    }

    override fun isBeforeFirst(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAfterLast(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFirst(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLast(): Boolean {
        TODO("Not yet implemented")
    }

    override fun beforeFirst() {
        TODO("Not yet implemented")
    }

    override fun afterLast() {
        TODO("Not yet implemented")
    }

    override fun first(): Boolean {
        TODO("Not yet implemented")
    }

    override fun last(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRow(): Int {
        TODO("Not yet implemented")
    }

    override fun absolute(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun relative(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun previous(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setFetchDirection(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getFetchDirection(): Int {
        TODO("Not yet implemented")
    }

    override fun setFetchSize(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getFetchSize(): Int {
        TODO("Not yet implemented")
    }

    override fun getType(): Int {
        TODO("Not yet implemented")
    }

    override fun getConcurrency(): Int {
        TODO("Not yet implemented")
    }

    override fun rowUpdated(): Boolean {
        TODO("Not yet implemented")
    }

    override fun rowInserted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun rowDeleted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun updateNull(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun updateNull(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun updateBoolean(p0: Int, p1: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updateBoolean(p0: String?, p1: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updateByte(p0: Int, p1: Byte) {
        TODO("Not yet implemented")
    }

    override fun updateByte(p0: String?, p1: Byte) {
        TODO("Not yet implemented")
    }

    override fun updateShort(p0: Int, p1: Short) {
        TODO("Not yet implemented")
    }

    override fun updateShort(p0: String?, p1: Short) {
        TODO("Not yet implemented")
    }

    override fun updateInt(p0: Int, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun updateInt(p0: String?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun updateLong(p0: Int, p1: Long) {
        TODO("Not yet implemented")
    }

    override fun updateLong(p0: String?, p1: Long) {
        TODO("Not yet implemented")
    }

    override fun updateFloat(p0: Int, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun updateFloat(p0: String?, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun updateDouble(p0: Int, p1: Double) {
        TODO("Not yet implemented")
    }

    override fun updateDouble(p0: String?, p1: Double) {
        TODO("Not yet implemented")
    }

    override fun updateBigDecimal(p0: Int, p1: BigDecimal?) {
        TODO("Not yet implemented")
    }

    override fun updateBigDecimal(p0: String?, p1: BigDecimal?) {
        TODO("Not yet implemented")
    }

    override fun updateString(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun updateString(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun updateBytes(p0: Int, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun updateBytes(p0: String?, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun updateDate(p0: Int, p1: Date?) {
        TODO("Not yet implemented")
    }

    override fun updateDate(p0: String?, p1: Date?) {
        TODO("Not yet implemented")
    }

    override fun updateTime(p0: Int, p1: Time?) {
        TODO("Not yet implemented")
    }

    override fun updateTime(p0: String?, p1: Time?) {
        TODO("Not yet implemented")
    }

    override fun updateTimestamp(p0: Int, p1: Timestamp?) {
        TODO("Not yet implemented")
    }

    override fun updateTimestamp(p0: String?, p1: Timestamp?) {
        TODO("Not yet implemented")
    }

    override fun updateAsciiStream(p0: Int, p1: InputStream?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateAsciiStream(p0: String?, p1: InputStream?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateAsciiStream(p0: Int, p1: InputStream?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateAsciiStream(p0: String?, p1: InputStream?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateAsciiStream(p0: Int, p1: InputStream?) {
        TODO("Not yet implemented")
    }

    override fun updateAsciiStream(p0: String?, p1: InputStream?) {
        TODO("Not yet implemented")
    }

    override fun updateBinaryStream(p0: Int, p1: InputStream?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateBinaryStream(p0: String?, p1: InputStream?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateBinaryStream(p0: Int, p1: InputStream?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateBinaryStream(p0: String?, p1: InputStream?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateBinaryStream(p0: Int, p1: InputStream?) {
        TODO("Not yet implemented")
    }

    override fun updateBinaryStream(p0: String?, p1: InputStream?) {
        TODO("Not yet implemented")
    }

    override fun updateCharacterStream(p0: Int, p1: Reader?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateCharacterStream(p0: String?, p1: Reader?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateCharacterStream(p0: Int, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateCharacterStream(p0: String?, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateCharacterStream(p0: Int, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun updateCharacterStream(p0: String?, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun updateObject(p0: Int, p1: Any?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateObject(p0: Int, p1: Any?) {
        TODO("Not yet implemented")
    }

    override fun updateObject(p0: String?, p1: Any?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun updateObject(p0: String?, p1: Any?) {
        TODO("Not yet implemented")
    }

    override fun insertRow() {
        TODO("Not yet implemented")
    }

    override fun updateRow() {
        TODO("Not yet implemented")
    }

    override fun deleteRow() {
        TODO("Not yet implemented")
    }

    override fun refreshRow() {
        TODO("Not yet implemented")
    }

    override fun cancelRowUpdates() {
        TODO("Not yet implemented")
    }

    override fun moveToInsertRow() {
        TODO("Not yet implemented")
    }

    override fun moveToCurrentRow() {
        TODO("Not yet implemented")
    }

    override fun getStatement(): Statement {
        TODO("Not yet implemented")
    }

    override fun getRef(p0: Int): Ref {
        TODO("Not yet implemented")
    }

    override fun getRef(p0: String?): Ref {
        TODO("Not yet implemented")
    }

    override fun getBlob(p0: Int): Blob {
        TODO("Not yet implemented")
    }

    override fun getBlob(p0: String?): Blob {
        TODO("Not yet implemented")
    }

    override fun getClob(p0: Int): Clob {
        TODO("Not yet implemented")
    }

    override fun getClob(p0: String?): Clob {
        TODO("Not yet implemented")
    }

    override fun getArray(p0: Int): Array {
        TODO("Not yet implemented")
    }

    override fun getArray(p0: String?): Array {
        TODO("Not yet implemented")
    }

    override fun getURL(p0: Int): URL {
        TODO("Not yet implemented")
    }

    override fun getURL(p0: String?): URL {
        TODO("Not yet implemented")
    }

    override fun updateRef(p0: Int, p1: Ref?) {
        TODO("Not yet implemented")
    }

    override fun updateRef(p0: String?, p1: Ref?) {
        TODO("Not yet implemented")
    }

    override fun updateBlob(p0: Int, p1: Blob?) {
        TODO("Not yet implemented")
    }

    override fun updateBlob(p0: String?, p1: Blob?) {
        TODO("Not yet implemented")
    }

    override fun updateBlob(p0: Int, p1: InputStream?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateBlob(p0: String?, p1: InputStream?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateBlob(p0: Int, p1: InputStream?) {
        TODO("Not yet implemented")
    }

    override fun updateBlob(p0: String?, p1: InputStream?) {
        TODO("Not yet implemented")
    }

    override fun updateClob(p0: Int, p1: Clob?) {
        TODO("Not yet implemented")
    }

    override fun updateClob(p0: String?, p1: Clob?) {
        TODO("Not yet implemented")
    }

    override fun updateClob(p0: Int, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateClob(p0: String?, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateClob(p0: Int, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun updateClob(p0: String?, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun updateArray(p0: Int, p1: Array?) {
        TODO("Not yet implemented")
    }

    override fun updateArray(p0: String?, p1: Array?) {
        TODO("Not yet implemented")
    }

    override fun getRowId(p0: Int): RowId {
        TODO("Not yet implemented")
    }

    override fun getRowId(p0: String?): RowId {
        TODO("Not yet implemented")
    }

    override fun updateRowId(p0: Int, p1: RowId?) {
        TODO("Not yet implemented")
    }

    override fun updateRowId(p0: String?, p1: RowId?) {
        TODO("Not yet implemented")
    }

    override fun getHoldability(): Int {
        TODO("Not yet implemented")
    }

    override fun isClosed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun updateNString(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun updateNString(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun updateNClob(p0: Int, p1: NClob?) {
        TODO("Not yet implemented")
    }

    override fun updateNClob(p0: String?, p1: NClob?) {
        TODO("Not yet implemented")
    }

    override fun updateNClob(p0: Int, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateNClob(p0: String?, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateNClob(p0: Int, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun updateNClob(p0: String?, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun getNClob(p0: Int): NClob {
        TODO("Not yet implemented")
    }

    override fun getNClob(p0: String?): NClob {
        TODO("Not yet implemented")
    }

    override fun getSQLXML(p0: Int): SQLXML {
        TODO("Not yet implemented")
    }

    override fun getSQLXML(p0: String?): SQLXML {
        TODO("Not yet implemented")
    }

    override fun updateSQLXML(p0: Int, p1: SQLXML?) {
        TODO("Not yet implemented")
    }

    override fun updateSQLXML(p0: String?, p1: SQLXML?) {
        TODO("Not yet implemented")
    }

    override fun getNString(p0: Int): String {
        TODO("Not yet implemented")
    }

    override fun getNString(p0: String?): String {
        TODO("Not yet implemented")
    }

    override fun getNCharacterStream(p0: Int): Reader {
        TODO("Not yet implemented")
    }

    override fun getNCharacterStream(p0: String?): Reader {
        TODO("Not yet implemented")
    }

    override fun updateNCharacterStream(p0: Int, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateNCharacterStream(p0: String?, p1: Reader?, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun updateNCharacterStream(p0: Int, p1: Reader?) {
        TODO("Not yet implemented")
    }

    override fun updateNCharacterStream(p0: String?, p1: Reader?) {
        TODO("Not yet implemented")
    }
}