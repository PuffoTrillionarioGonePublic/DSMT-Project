package com.erldb

import java.sql.*
import java.util.*

abstract class AbstractErldbConnection : Connection {

    override fun <T : Any?> unwrap(iface: Class<T>?): T? {
        return iface?.cast(this)
    }

    override fun isWrapperFor(iface: Class<*>?): Boolean {
        return iface?.isInstance(this) ?: false
    }


    override fun createStatement(): Statement = createStatement(
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT
    )

    override fun prepareStatement(sql: String): PreparedStatement =
        prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

    override fun prepareStatement(sql: String, rst: Int, rsc: Int): PreparedStatement =
        prepareStatement(sql, rst, rsc, ResultSet.CLOSE_CURSORS_AT_COMMIT)


    override fun prepareStatement(sql: String, autoC: Int): PreparedStatement = prepareStatement(sql)

    override fun prepareStatement(sql: String, colInds: IntArray): PreparedStatement = prepareStatement(sql)

    override fun prepareStatement(sql: String, colNames: Array<out String>): PreparedStatement = prepareStatement(sql)

    override fun prepareCall(sql: String): CallableStatement =
        prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

    override fun prepareCall(sql: String, rst: Int, rsc: Int): CallableStatement =
        prepareCall(sql, rst, rsc, ResultSet.CLOSE_CURSORS_AT_COMMIT)

    override fun prepareCall(sql: String, rst: Int, rsc: Int, rsh: Int): CallableStatement {
        throw SQLException("Store Procedures are not supported")
    }

    override fun setCatalog(catalog: String?) {
    }

    override fun getCatalog(): String? = null


    override fun getWarnings(): SQLWarning? = null

    override fun clearWarnings() {}

    override fun createClob(): Clob {
        throw SQLFeatureNotSupportedException()
    }

    override fun createBlob(): Blob {
        throw SQLFeatureNotSupportedException()
    }

    override fun createNClob(): NClob {
        throw SQLFeatureNotSupportedException()
    }

    override fun createSQLXML(): SQLXML {
        throw SQLFeatureNotSupportedException()
    }

    override fun getClientInfo(name: String?): String? = null


    override fun getClientInfo(): Properties? = null


    override fun createArrayOf(p0: String?, p1: Array<out Any>?): java.sql.Array? = null

    override fun getSchema(): String? = null


}