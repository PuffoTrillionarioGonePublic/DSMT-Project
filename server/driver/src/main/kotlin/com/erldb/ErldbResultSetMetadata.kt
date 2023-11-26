package com.erldb

import java.sql.ResultSetMetaData

class ErldbResultSetMetaData : ResultSetMetaData {

    var resultSet: ErldbResultSet
    constructor(resultSet: ErldbResultSet) {
        this.resultSet = resultSet
    }

    override fun <T : Any?> unwrap(p0: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun isWrapperFor(p0: Class<*>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getColumnCount(): Int =
        resultSet.cols.size


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

    override fun getColumnName(oneColumnIndex: Int): String {
        return resultSet.cols[oneColumnIndex - 1]
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

    override fun getColumnType(p0: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getColumnTypeName(p0: Int): String {
        TODO("Not yet implemented")
    }

    override fun isReadOnly(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isWritable(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDefinitelyWritable(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getColumnClassName(p0: Int): String {
        TODO("Not yet implemented")
    }
}