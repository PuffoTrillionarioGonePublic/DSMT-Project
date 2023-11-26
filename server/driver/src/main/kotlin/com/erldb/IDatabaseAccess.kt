package com.erldb


/**
This interface is a wrapper for the SQLite C API. It is not intended to be used directly, but
rather through the [DB] class.
 */

/**
 * Interface for accessing an SQLite database.
 */
interface IDatabaseAccess {

    /**
     * Interrupts any pending database operation.
     */
    fun interrupt(): Result<Unit>

    /**
     * Closes the database.
     */
    fun close(): Result<Unit>

    /**
     * Executes the given SQL statement with the given parameters.
     *
     * @param sql The SQL statement to execute.
     * @param params The parameters to bind to the statement.
     * @return The number of rows affected by the statement.
     */
    fun execute(sql: String, params: Array<SQLiteValue>): Result<Int>

    /**
     * Executes the given SQL statement.
     *
     * @param sql The SQL statement to execute.
     * @return The number of rows affected by the statement.
     */
    fun execute(sql: String): Result<Int> = execute(sql, emptyArray())

    /**
     * Prepares the given SQL statement for execution.
     *
     * @param sql The SQL statement to prepare.
     * @return An [IStatementAccess] object representing the prepared statement.
     */
    fun prepare(sql: String): Result<IStatementAccess>

    /**
     * Sets the limit for the given ID.
     *
     * @param id The ID of the limit to set.
     * @param value The value to set the limit to.
     * @return The previous value of the limit.
     */
    fun limit(id: Int, value: Int): Result<Int>

    /**
     * Returns the number of rows affected by the last executed statement.
     */
    fun changes(): Result<Long>


    /**
     * Sets the busy timeout for the database.
     *
     * @param ms The timeout in milliseconds.
     */
    fun busyTimeout(ms: Int): Result<Unit>

    /**
     * Returns the row ID of the last inserted row.
     */
    fun lastInsertRowId(): Result<Long>
}


