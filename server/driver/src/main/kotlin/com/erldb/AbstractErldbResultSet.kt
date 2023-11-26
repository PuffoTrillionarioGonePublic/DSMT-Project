package com.erldb

import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Date
import java.util.*

abstract class AbstractErldbResultSet : ResultSet {

    /// NOT IMPLEMENTED

    override fun isLast(): Boolean {
        throw SQLFeatureNotSupportedException("not supported by sqlite")
    }

    override fun beforeFirst() {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun afterLast() {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun first(): Boolean {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun last(): Boolean {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun absolute(row: Int): Boolean {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun relative(rows: Int): Boolean {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun previous(): Boolean {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun updateNull(col: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNull(col: String) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBoolean(col: Int, x: Boolean) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBoolean(col: String, x: Boolean) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateByte(col: Int, x: Byte) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateByte(col: String, x: Byte) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateShort(c: Int, x: Short) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateShort(c: String, x: Short) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateInt(col: Int, x: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateInt(col: String, x: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateLong(col: Int, x: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateLong(col: String, x: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateFloat(col: Int, x: Float) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateFloat(col: String, x: Float) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateDouble(col: Int, x: Double) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateDouble(col: String, x: Double) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBigDecimal(col: Int, x: BigDecimal?) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBigDecimal(col: String, x: BigDecimal?) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateString(col: Int, x: String) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateString(col: String, x: String) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBytes(col: Int, x: ByteArray) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBytes(col: String, x: ByteArray) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateDate(col: Int, x: Date) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateDate(col: String, x: Date) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateTime(col: Int, x: Time) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateTime(col: String, x: Time) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateTimestamp(col: Int, x: Timestamp) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateTimestamp(col: String, x: Timestamp) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateAsciiStream(col: Int, x: InputStream, l: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateAsciiStream(col: String, x: InputStream, l: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateAsciiStream(col: Int, x: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateAsciiStream(col: String, x: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateAsciiStream(col: Int, x: InputStream) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateAsciiStream(col: String, x: InputStream) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBinaryStream(col: Int, x: InputStream, l: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBinaryStream(col: String, x: InputStream, l: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBinaryStream(col: Int, x: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBinaryStream(col: String, x: InputStream, l: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBinaryStream(col: Int, x: InputStream) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBinaryStream(col: String, x: InputStream) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateCharacterStream(c: Int, x: Reader, l: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateCharacterStream(c: String, x: Reader, l: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateCharacterStream(c: Int, x: Reader, l: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateCharacterStream(c: String, x: Reader, l: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateCharacterStream(c: Int, x: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateCharacterStream(c: String, x: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateObject(c: Int, x: Any?, s: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateObject(c: Int, x: Any?) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateObject(c: String, x: Any?, s: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateObject(c: String, x: Any?) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun insertRow() {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun updateRow() {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun deleteRow() {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun refreshRow() {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun cancelRowUpdates() {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun moveToInsertRow() {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun moveToCurrentRow() {
        throw SQLException("ResultSet is TYPE_FORWARD_ONLY")
    }

    override fun getRef(i: Int): Ref {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getRef(col: String): Ref {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getBlob(col: Int): Blob {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getBlob(col: String): Blob {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getClob(col: Int): Clob {
        TODO("Not yet implemented")
    }

    override fun getClob(col: String): Clob {
        TODO("Not yet implemented")
    }

    override fun getArray(i: Int): java.sql.Array {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getArray(col: String): java.sql.Array {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getURL(col: Int): URL {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getURL(col: String): URL {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateRef(col: Int, x: Ref) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateRef(col: String, x: Ref) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBlob(col: Int, x: Blob) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBlob(col: String, x: Blob) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBlob(col: Int, inputStream: InputStream, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBlob(col: String, inputStream: InputStream, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBlob(col: Int, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateBlob(col: String, inputStream: InputStream) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateClob(col: Int, x: Clob) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateClob(col: String, x: Clob) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateClob(col: Int, reader: Reader, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateClob(col: String, reader: Reader, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateClob(col: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateClob(col: String, reader: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateArray(col: Int, x: java.sql.Array) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateArray(col: String, x: java.sql.Array) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getRowId(col: Int): RowId {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getRowId(col: String): RowId {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateRowId(col: Int, x: RowId) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateRowId(col: String, x: RowId) {
        throw SQLFeatureNotSupportedException("not implemented")
    }


    override fun updateNString(col: Int, nString: String) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNString(col: String, nString: String) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNClob(col: Int, nClob: NClob) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNClob(col: String, nClob: NClob) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNClob(col: Int, reader: Reader, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNClob(col: String, reader: Reader, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNClob(col: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNClob(col: String, reader: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getNClob(col: Int): NClob {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getNClob(col: String): NClob {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getSQLXML(col: Int): SQLXML {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getSQLXML(col: String): SQLXML {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateSQLXML(col: Int, xmlObject: SQLXML) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateSQLXML(col: String, xmlObject: SQLXML) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getNString(col: Int): String {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getNString(col: String): String {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNCharacterStream(col: Int, reader: Reader, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNCharacterStream(col: String, reader: Reader, length: Long) {
        throw SQLFeatureNotSupportedException("not implemented")
    }


    override fun getObject(col: Int, type: MutableMap<String, Class<*>>): Any {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun getObject(col: String, type: MutableMap<String, Class<*>>): Any {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNCharacterStream(col: Int, reader: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    override fun updateNCharacterStream(col: String, reader: Reader) {
        throw SQLFeatureNotSupportedException("not implemented")
    }


    /// DEPRECATED


    @Deprecated("Deprecated in Java", ReplaceWith("getAsciiStream(col)"))
    override fun getUnicodeStream(col: Int): InputStream? = getAsciiStream(col)

    @Deprecated("Deprecated in Java", ReplaceWith("getAsciiStream(col)"))
    override fun getUnicodeStream(col: String): InputStream? = getAsciiStream(col)


    @Deprecated("Deprecated in Java")
    override fun getBigDecimal(col: Int, s: Int): BigDecimal {
        throw SQLFeatureNotSupportedException("not implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getBigDecimal(col: String, s: Int): BigDecimal {
        throw SQLFeatureNotSupportedException("not implemented")
    }


    // NOT SO IMPORTANT
    override fun <T : Any?> unwrap(iface: Class<T>): T = iface.cast(this)

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)


    override fun getDouble(col: String): Double = getDouble(findColumn(col))
    override fun getLong(col: String): Long = getLong(findColumn(col))
    override fun getInt(col: String): Int = getInt(findColumn(col))
    override fun getByte(col: String): Byte = getByte(findColumn(col))
    override fun getString(col: String): String? = getString(findColumn(col))

    override fun getBoolean(col: String): Boolean = getBoolean(findColumn(col))


    override fun getShort(col: String): Short = getShort(findColumn(col))

    override fun getFloat(col: String): Float = getFloat(findColumn(col))


    override fun getTimestamp(col: String, cal: Calendar): Timestamp? = getTimestamp(findColumn(col), cal)
    override fun getTimestamp(col: String): Timestamp? = getTimestamp(findColumn(col))
    override fun getTime(col: String, cal: Calendar): Time? = getTime(findColumn(col), cal)
    override fun getTime(col: String): Time? = getTime(findColumn(col))
    override fun getDate(col: String, cal: Calendar): Date? = getDate(findColumn(col), cal)
    override fun getDate(col: String): Date? = getDate(findColumn(col), Calendar.getInstance())
    override fun getBytes(col: String): ByteArray? = getBytes(findColumn(col))
    override fun getBigDecimal(col: String): BigDecimal? = getBigDecimal(findColumn(col))


    override fun getNCharacterStream(col: String): Reader? = getNCharacterStream(findColumn(col))
    override fun getCharacterStream(col: String): Reader? = getCharacterStream(findColumn(col))
    override fun <T : Any?> getObject(columnLabel: String, type: Class<T>): T? =
        getObject(findColumn(columnLabel), type)

    override fun getObject(col: String): Any? = getObject(findColumn(col))
    override fun getAsciiStream(col: String): InputStream? = getAsciiStream(findColumn(col))

    override fun getBinaryStream(col: String): InputStream? = getBinaryStream(findColumn(col))


    override fun getCursorName(): String? = null


    override fun getHoldability(): Int = 0

    override fun getWarnings(): SQLWarning? = null

    override fun clearWarnings() = Unit

    override fun setFetchDirection(d: Int) {
        if (d != ResultSet.FETCH_FORWARD) {
            throw SQLException("only FETCH_FORWARD direction supported")
        }
    }

    override fun getFetchDirection(): Int = ResultSet.FETCH_FORWARD

    override fun setFetchSize(rows: Int) {
        throw SQLFeatureNotSupportedException("not implemented")
    }
    override fun getFetchSize(): Int {
        throw SQLFeatureNotSupportedException("not implemented")
    }


}