package com.erldb

import java.sql.*


open class ErldbStatement : AbstractStatement {
    var conn: ErldbConnection
    var rs: ErldbResultSet?
    var statementAccess: IStatementAccess
    var sql: String
    var nextRow: Array<SQLiteValue>? = null
    var queryTimeoutSeconds: Int
    var updateCount: Long
    var exhaustedResults: Boolean

    @Volatile
    var open: Boolean
    var closeOnCompletion: Boolean
    var maxRows = 0L
    var autoCommit = true
    var dataToBeFetched: MutableList<Array<SQLiteValue>>
    var stepReturnedNull = false
    val connectionConfig: ErldbConfig
        get() = conn.config

    constructor(c: ErldbConnection) : this(c, "")

    constructor(c: ErldbConnection, sql: String) {
        this.sql = sql
        conn = c
        rs = ErldbResultSet(this)
        statementAccess = NotReadyStatementAccess()
        dataToBeFetched = arrayListOf()
        queryTimeoutSeconds = 0
        updateCount = 0
        exhaustedResults = false
        open = true
        closeOnCompletion = false
    }


    fun consumeRow(): Array<SQLiteValue>? = withGuard().execute {
        if (dataToBeFetched.isEmpty()) {
            step()
            if (dataToBeFetched.isEmpty()) {
                exhaustedResults = true
                return@execute null
            }
        }
        dataToBeFetched.removeAt(0)
    }


    fun withGuard(): Guard {
        if (!open) throw SQLException("statement is not executing")
        return Guard(this.statementAccess)
    }


    fun abstractExec(): Boolean = withGuard().execute {
        rs = rs ?: ErldbResultSet(this)
        var success = false
        AutoCloseable {
            conn.firstStatementExecuted = true
            if (!success) {
                statementAccess.close()
            }
        }.use {
            success = true
        }
        statementAccess.columnCount().onFailure {
            this.close()
        }.map {
            it != 0
        }.getOrThrow()
    }



    override fun execute(sql: String): Boolean = withGuard().execute {
        rs?.close()
        statementAccess.close()
        this.sql = sql

        rs = ErldbResultSet(this)
        statementAccess = conn.prepare(this)
        var success = false
        AutoCloseable {
            conn.firstStatementExecuted = true
            if (!success) {
                statementAccess.close()
            }
        }.use {
            success = true
        }
        val row = step()
        exhaustedResults = false
        if (row == null) {
            exhaustedResults = true
            false
        } else {
            rs?.currentRowValue = row
            IStatementAccess.ensureAutoCommit(autoCommit, conn.dbAccess)
            true
        }
    }





    override fun executeQuery(sql: String): ResultSet? = withGuard().execute {
        if (!execute(sql)) {
            return@execute null
        }
        if (rs == null) {
            return@execute null
        }
        resultSet
    }

    override fun getLargeMaxRows(): Long = withGuard().execute { maxRows }

    override fun setLargeMaxRows(max: Long) = withGuard().execute {
        if (max < 0) throw SQLException("max row count must be >= 0")
        maxRows = max
    }


    override fun executeLargeUpdate(sql: String): Long {
        if (execute(sql)) {
            throw SQLException(
                "query returns ResultSet", "UNKNOWN", 0xdeadbee
            )
        }
        return updateCount
    }


    override fun addBatch(sql: String) = withGuard().execute {
        TODO()
    }

    override fun clearBatch() = withGuard().execute {
        TODO()
    }

    override fun executeBatch(): IntArray = withGuard().execute { executeLargeBatch().map(Long::toInt).toIntArray() }

    override fun getConnection(): Connection = withGuard().execute { conn }

    override fun getGeneratedKeys(): ResultSet? = withGuard().execute {
        this.statementAccess.db().lastInsertRowId().let {
            it.map {
                val mat = arrayListOf(arrayListOf(SQLiteValue.from(it)))
                val rs = ListBasedResultSet(arrayListOf("key"), mat)
                rs
            }.getOrNull()
        }

    }

    override fun executeLargeBatch(): LongArray = withGuard().execute {
        TODO()
    }

    override fun getResultSet(): ResultSet? = withGuard().execute {
        if (exhaustedResults) null else {
            val rs = rs
            when (rs) {
                is ErldbResultSet -> {
                    if (dataToBeFetched.isEmpty()) {
                        exhaustedResults = true
                        null
                    } else {
                        if (rs.cols.isEmpty()) {
                            statementAccess.columnNames().onSuccess {
                                rs.cols.addAll(it)
                            }
                        }
                        rs.pastLastRow = stepReturnedNull
                        rs
                    }
                }

                else -> {
                    throw SQLException("ResultSet already requested")
                }
            }
        }
    }

    override fun cancel() = withGuard().execute { /*conn.interrupt()*/ }

    override fun getQueryTimeout(): Int = withGuard().execute { queryTimeoutSeconds }

    override fun setQueryTimeout(seconds: Int) = withGuard().execute {
        if (seconds < 0) {
            throw SQLException("query timeout must be >= 0")
        }
        queryTimeoutSeconds = seconds
    }


    override fun getMoreResults(current: Int): Boolean = when (current) {
        Statement.CLOSE_CURRENT_RESULT -> {
            withGuard()
            // we support a single result set, close it
            rs?.close()
            rs = null
            // as we don't have more result, change the update count to -1
            updateCount = -1
            exhaustedResults = true
            // always return false as we never have more results
            false
        }

        Statement.KEEP_CURRENT_RESULT, Statement.CLOSE_ALL_RESULTS -> {
            throw SQLFeatureNotSupportedException(
                "Argument not supported: Statement.KEEP_CURRENT_RESULT or Statement.CLOSE_ALL_RESULTS"
            )
        }

        else -> {
            throw SQLException("Invalid argument")

        }
    }


    fun <T> withConnectionTimeout(callable: () -> T): T {
        val origBusyTimeout: Int = conn.getBusyTimeout()
        if (queryTimeoutSeconds > 0) {
            conn.setBusyTimeout(1000 * queryTimeoutSeconds)
        }
        return AutoCloseable {
            if (queryTimeoutSeconds > 0) {
                conn.setBusyTimeout(origBusyTimeout)
            }
        }.use {
            callable()
        }
    }


    override fun getLargeUpdateCount(): Long = statementAccess.columnCount().onSuccess {
        if ((!statementAccess.isClosed() && (rs == null || rs?.open == false) && dataToBeFetched.isNotEmpty() && (it == 0))) updateCount
        else -1
    }.onFailure {
        this.close()
    }.getOrThrow().toLong()


    override fun isClosed(): Boolean = !open


    override fun closeOnCompletion() = withGuard().execute {
        closeOnCompletion = true
    }

    override fun isCloseOnCompletion(): Boolean = withGuard().execute { closeOnCompletion }


    fun closeNoResultSet() = withGuard().execute {
        statementAccess.close()
        statementAccess = NotReadyStatementAccess()
        rs = null
        open = false
    }

    override fun close() {
        if (isClosed) {
            return
        } else withGuard().execute {
            statementAccess.close()
            statementAccess = NotReadyStatementAccess()
            rs?.closeNoStatement()
            rs = null
            open = false
        }
    }


    fun step(): Array<SQLiteValue>? = withGuard().execute {
        val db = statementAccess.db()
        synchronized(db) {
            while (dataToBeFetched.isEmpty()) {
                statementAccess.stepBy(100).onFailure {
                    this.close()
                }.onSuccess {
                    it?.let {
                        stepReturnedNull = false
                        updateCount = 0
                        dataToBeFetched.addAll(it)
                    } ?: run {
                        stepReturnedNull = true
                        updateCount = db.changes().onFailure {
                            this.close()
                        }.getOrThrow()
                        return@execute null
                    }
                }.getOrThrow()
            }
        }
        dataToBeFetched[0]
    }

    fun columnNames() = withGuard().execute { statementAccess.columnNames() }
    fun reset() = withGuard().execute { statementAccess.reset() }
    fun bindParameterCount() = withGuard().execute { statementAccess.bindParameterCount() }
    fun columnCount() = withGuard().execute { statementAccess.columnCount() }
    fun columnTableName(col: ZeroColumnIndex) = withGuard().execute { statementAccess.columnTableName(col) }
    fun columnName(col: ZeroColumnIndex) = withGuard().execute { statementAccess.columnName(col) }
    fun bind(pos: OneColumnIndex, v: Any?) = withGuard().execute { statementAccess.bind(pos, v) }


}

