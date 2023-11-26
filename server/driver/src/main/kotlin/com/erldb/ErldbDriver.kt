package com.erldb

import java.sql.*
import java.util.*
import java.util.logging.Logger


class ErldbDriver : Driver {
    companion object {
        val PREFIX = "jdbc:erldb:"
        init {
            try {
                DriverManager.registerDriver(ErldbDriver())
            } catch (e: SQLException) {
                throw RuntimeException("Can't register driver!", e)
            }
        }
    }
    override fun connect(url: String, info: Properties?): Connection? {
        if (!acceptsURL(url)) {
            return null
        }
        return ErldbConnection(url.trim(), info ?: Properties())
    }
    override fun acceptsURL(url: String): Boolean = url.lowercase().startsWith(PREFIX)
    override fun getPropertyInfo(p0: String?, p1: Properties?): Array<DriverPropertyInfo> = emptyArray()

    override fun getMajorVersion(): Int = 0
    override fun getMinorVersion(): Int = 0
    override fun jdbcCompliant(): Boolean = false
    override fun getParentLogger(): Logger? = null
}

