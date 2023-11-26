package com.erldb

import java.lang.Exception
import java.util.concurrent.locks.ReentrantReadWriteLock

class NotReadyStatementAccess : IStatementAccess {

    fun notReady(): Exception {
        return RuntimeException("not ready yet")
    }

    override fun isClosed(): Boolean = true

    override fun close(): Result<Unit> = Result.success(Unit)

    override fun reset(): Result<Unit> {
        throw notReady()
    }

    override fun bindParameterCount(): Result<Int> {
        throw notReady()
    }

    override fun columnCount(): Result<Int> {
        throw notReady()
    }

    override fun clearBindings(): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun columnTableName(col: ZeroColumnIndex): Result<String> {
        throw notReady()
    }

    override fun columnName(col: ZeroColumnIndex): Result<String> {
        throw notReady()
    }

    override fun bind(pos: OneColumnIndex, v: SQLiteValue): Result<Unit> {
        throw notReady()
    }

    override fun columnNames(): Result<Array<String>> {
        throw notReady()
    }

    override fun db(): IDatabaseAccess {
        throw notReady()
    }

    override fun stepBy(n: Int): Result<List<Array<SQLiteValue>>?> {
        throw notReady()
    }
}
