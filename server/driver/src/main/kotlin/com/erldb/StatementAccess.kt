/**
 * This class represents an implementation of the [IStatementAccess] interface and extends the [HttpAccess] class.
 * It provides methods to interact with a statement in a remote SQLite database.
 * @property host The host name of the remote SQLite database.
 * @property port The port number of the remote SQLite database.
 * @property db The [ErldbConnection] object representing the connection to the remote SQLite database.
 * @property connectionId The ID of the connection to the remote SQLite database.
 * @property statementId The ID of the statement in the remote SQLite database.
 */
package com.erldb

import org.json.JSONArray

class StatementAccess(
    nodes: List<Pair<String, Int>>,
    private val db: ErldbConnection,
    private val connectionId: Long,
    private val statementId: Long
) : IStatementAccess, HttpAccess(
    nodes
) {
    override fun isClosed(): Boolean = !open

    /**
     * Closes the statement and releases any resources associated with it.
     * @throws [SQLException] if a database access error occurs.
     */
    override fun close(): Result<Unit> = checkOpen().let {
        open = false
        doRequest(
            "finalize", mapOf(
                "Conn" to connectionId, "Stmt" to statementId
            )
        )
    }

    /**
     * Binds the given [v] value to the parameter at the given [pos] position.
     * @param pos The position of the parameter to bind.
     * @param v The value to bind to the parameter.
     */
    override fun bind(pos: OneColumnIndex, v: SQLiteValue): Result<Unit> = checkOpen().let {
        val params = mapOf(
            "Conn" to connectionId, "Stmt" to statementId, "N" to pos.value, "Value" to v.toJSON()
        )
        doRequest("bind", params)//.getString("ok") == "success"
    }

    /**
     * Returns an array of column names for the current result set.
     * @return Array of column names.
     */
    override fun columnNames(): Result<Array<String>> = checkOpen().let {
        doRequest<JSONArray>(
            "column_names", mapOf(
                "Conn" to connectionId, "Stmt" to statementId
            )
        ).map { it.iterator().asSequence().map { it.toString() }.toList().toTypedArray() }
    }

    /**
     * Executes the statement `n` times and returns the resulting rows as a list of arrays of [SQLiteValue] objects.
     *
     * @param n The number of times to execute the statement.
     * @return A list of arrays of [SQLiteValue] objects representing the resulting rows, or null if an error occurred.
     */
    override fun stepBy(n: Int): Result<List<Array<SQLiteValue>>?> = checkOpen().let {
        val rows = doRequest<JSONArray?>(
            "step_by", mapOf(
                "Conn" to connectionId, "Stmt" to statementId, "N" to n
            )
        )
        rows.map {
            it?.let { rows ->
                val v = mutableListOf<Array<SQLiteValue>>()
                for (i in 0..<rows.length()) {
                    val row = rows.getJSONArray(i)
                    val cols = row.length()
                    val m = mutableListOf<SQLiteValue>()
                    for (j in 0..<cols) {
                        val tmp = SQLiteValue.fromJSON(row.getJSONArray(j))
                        m.add(tmp)
                    }
                    v.add(m.toTypedArray())
                }
                v
            }
        }

    }

    /**
     * Resets the statement to its initial state.
     * @return Unit
     */
    override fun reset(): Result<Unit> = checkOpen().let {
        doRequest(
            "reset", mapOf(
                "Conn" to connectionId, "Stmt" to statementId
            )
        )
    }

    /**
     * Returns the number of parameters in the prepared statement.
     * @return The number of parameters in the prepared statement.
     */
    override fun bindParameterCount(): Result<Int> = checkOpen().let {
        doRequest(
            "bind_parameter_count", mapOf(
                "Conn" to connectionId, "Stmt" to statementId
            )
        )
    }

    /**
     * Clears all the bindings on the current statement.
     * @return true if the bindings were successfully cleared, false otherwise.
     */
    override fun clearBindings(): Result<Boolean> = checkOpen().let {
        doRequest(
            "clear_bindings", mapOf(
                "Conn" to connectionId, "Stmt" to statementId
            )
        )
    }

    /**
     * Returns the number of columns in the result set.
     * @return the number of columns in the result set.
     */
    override fun columnCount(): Result<Int> = checkOpen().let {
        doRequest(
            "column_count", mapOf(
                "Conn" to connectionId, "Stmt" to statementId
            )
        )
    }

    /**
     * Returns the name of the table that the specified column belongs to.
     *
     * @param col The zero-based index of the column.
     * @return The name of the table that the specified column belongs to.
     */
    override fun columnTableName(col: ZeroColumnIndex): Result<String> = checkOpen().let {
        doRequest(
            "column_table_name", mapOf(
                "Conn" to connectionId, "Stmt" to statementId, "col" to col.value
            )
        )
    }


    /**
     * Returns the name of the column at the specified index.
     *
     * @param col the zero-based index of the column
     * @return the name of the column
     */
    override fun columnName(col: ZeroColumnIndex): Result<String> = checkOpen().let {
        doRequest(
            "column_name", mapOf(
                "Conn" to connectionId, "Stmt" to statementId, "index" to col.value
            )
        )
    }

    /**
     * Returns the database access object.
     * @return the database access object.
     */
    override fun db(): IDatabaseAccess = checkOpen().let { db.dbAccess }

}
