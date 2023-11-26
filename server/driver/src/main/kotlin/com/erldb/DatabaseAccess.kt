package com.erldb

class DatabaseAccess : IDatabaseAccess, HttpAccess {

    var db: ErldbConnection
    var connectionId: Long

    constructor(nodes: List<Pair<String, Int>>, db: ErldbConnection, connectionId: Long) : super(nodes) {
        this.db = db
        this.connectionId = connectionId
    }


    override fun interrupt(): Result<Unit> = doRequest("interrupt", mapOf("Conn" to connectionId))


    override fun close(): Result<Unit> = doRequest("close", mapOf("Conn" to connectionId))

    override fun lastInsertRowId(): Result<Long> {
        return doRequest("last_insert_rowid", mapOf("Conn" to connectionId))
    }

    override fun execute(query: String, params: Array<SQLiteValue>): Result<Int> =
        doRequest("execute", mapOf("Conn" to connectionId, "Query" to query, "Params" to params))

    override fun prepare(query: String): Result<IStatementAccess> {
        val stmtId = doRequest<Long>("prepare", mapOf("Conn" to connectionId, "Query" to query))
        return stmtId.map {
            StatementAccess(
                nodes, db, connectionId, it
            )
        }
    }


    override fun limit(id: Int, value: Int): Result<Int> = Result.success(0)


    override fun changes(): Result<Long> = doRequest("changes", mapOf("Conn" to connectionId))

    override fun busyTimeout(ms: Int): Result<Unit> =
        doRequest("busy_timeout", mapOf("Conn" to connectionId, "Timeout" to ms))


}

