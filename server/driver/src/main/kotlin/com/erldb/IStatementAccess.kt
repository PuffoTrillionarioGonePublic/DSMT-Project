/**
 *
 * This file contains the definition of the IStatementAccess interface, which is used for accessing a SQLite statement.
 *
 * The interface provides methods for checking if the statement is closed, closing the statement, stepping the statement by n rows,
 * resetting the statement, binding parameters to the statement, clearing the bindings of the statement, and retrieving information
 * about the columns of the statement.
 *
 */
package com.erldb

import java.util.concurrent.locks.ReentrantReadWriteLock

interface IStatementAccess {

    /**
     * Interface for accessing a SQLite statement.
     */
        /**
         * Returns whether the statement is closed.
         */
        fun isClosed(): Boolean

        /**
         * Closes the statement.
         * @return the result code.
         */
        fun close(): Result<Unit>

    /**
         * Steps the statement by n rows.
         * @param n the number of rows to step.
         * @return a list of arrays of SQLite values.
         */
        fun stepBy(n: Int): Result<List<Array<SQLiteValue>>?>

        /**
         * Resets the statement.
         */
        fun reset(): Result<Unit>

        /**
         * Returns the number of parameters bound to the statement.
         */
        fun bindParameterCount(): Result<Int>

        /**
         * Returns the number of columns in the statement.
         */
        fun columnCount(): Result<Int>

        /**
         * Clears the bindings of the statement.
         * @return whether the bindings were cleared.
         */
        fun clearBindings(): Result<Boolean>

        /**
         * Returns the name of the table of the column at the given index.
         * @param col the index of the column.
         */
        fun columnTableName(col: ZeroColumnIndex): Result<String>

        /**
         * Returns the name of the column at the given index.
         * @param col the index of the column.
         */
        fun columnName(col: ZeroColumnIndex): Result<String>

        /**
         * Binds a value to the parameter at the given position.
         * @param pos the position of the parameter.
         * @param v the value to bind.
         */
        fun bind(pos: OneColumnIndex, v: SQLiteValue): Result<Unit>

        /**
         * Binds a value to the parameter at the given position.
         * @param pos the position of the parameter.
         * @param v the value to bind.
         */
        fun bind(pos: OneColumnIndex, v: Any?): Result<Unit> = bind(pos, SQLiteValue.from(v))

        /**
         * Returns the names of the columns in the statement.
         */
        fun columnNames(): Result<Array<String>>

        /**
         * Returns the database of the statement.
         */
        fun db(): IDatabaseAccess


    companion object {
        fun ensureAutoCommit(autoCommit: Boolean, dbAccess: IDatabaseAccess) {
            if (!autoCommit) {
                return
            }
            dbAccess.execute("BEGIN;")
            dbAccess.execute("COMMIT;")
        }
    }


}