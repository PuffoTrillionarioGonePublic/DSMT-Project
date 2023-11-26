/**
 * This class represents a connection to a SQLite database. It provides methods for creating statements and prepared statements, executing SQL queries and updates, and managing transactions.
 *
 * The class also contains properties and methods for managing the state of the connection, such as the `open` property, which indicates whether the connection is open or closed, and the `checkOpen()` method, which throws an exception if the connection is closed.
 *
 * The `ErldbConnection` class is used by the `ErldbDriver` class to connect to a SQLite database and execute SQL queries and updates.
 */

package com.erldb

import java.sql.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

// TODO: unify DB class into ErldbConnection
// TODO: DB class was used as synchronization object
// TODO: add `checkOpen()` to all methods
class ErldbConnection : AbstractErldbConnection {
    var config: ErldbConfig
    var contextAccess: ContextAccess
    var firstStatementExecuted = false
    var savePoint = AtomicInteger(0)

    var nodes: List<Pair<String, Int>>

    @Volatile
    var open: Boolean = true

    val dbAccess: IDatabaseAccess

    val stmts: MutableSet<IStatementAccess> = ConcurrentHashMap.newKeySet()
    var typeMapField: MutableMap<String, Class<*>> = emptyMap<String, Class<*>>().toMutableMap()
    var thisReadOnly = false // TODO: questo nome non è chiaro

    constructor(url: String, prop: Properties?) {
        config = ErldbConfig(
            com.erldb.TransactionMode.DEFERRED, true, false, false, SQLiteOpenMode.READWRITE.flag
        )

        val pattern = "jdbc:(.+?)://((?:.+?:\\d+,)*.+?:\\d+)/(.*?)/(.*?)$".toRegex()
        val matchResult = pattern.find(url)

        if (matchResult == null) throw SQLException("invalid url")

        val hostPortString = matchResult.groups[2]?.value ?: throw SQLException("invalid url")
        val hostPortList = hostPortString.split(",")

        val hosts = hostPortList.map { it.split(":").first() }
        val ports = hostPortList.map { it.split(":")[1].toIntOrNull() ?: throw SQLException("invalid port number") }

        val bucket = matchResult.groups[3]?.value ?: throw SQLException("invalid url")
        val filename = matchResult.groups[4]?.value ?: throw SQLException("invalid url")

        nodes = hosts.zip(ports)
        contextAccess = ContextAccess.getInstance(nodes)
        dbAccess = contextAccess.createConnection(this, bucket, filename, nodes)
    }

    fun withGuard(): Guard {
        if (!open) throw SQLException("statement is not executing")
        return Guard(this.dbAccess)
    }


    /**
     * Creates a new Statement object with the given parameters.
     *
     * @param rsType the result set type to be used for the Statement object
     * @param rsConcurr the result set concurrency to be used for the Statement object
     * @return a new Statement object with the given parameters
     */
    override fun createStatement(rsType: Int, rsConcurr: Int): Statement = withGuard().execute {
        createStatement(rsType, rsConcurr, ResultSet.CLOSE_CURSORS_AT_COMMIT)
    }

    /**
     * Checks if the database connection is open. Throws a SQLException if the connection is closed.
     *//*fun checkOpen() {
        if (isClosed) throw SQLException("database connection closed")
    }*/

    /**
     * Checks if the given cursor type, concurrency mode and holdability are supported by SQLite.
     * @throws SQLException if any of the parameters are not supported by SQLite.
     */
    fun checkCursor(rst: Int, rsc: Int, rsh: Int) = withGuard().execute {
        if (rst != ResultSet.TYPE_FORWARD_ONLY) throw SQLException("SQLite only supports TYPE_FORWARD_ONLY cursors")
        if (rsc != ResultSet.CONCUR_READ_ONLY) throw SQLException("SQLite only supports CONCUR_READ_ONLY cursors")
        if (rsh != ResultSet.CLOSE_CURSORS_AT_COMMIT) throw SQLException("SQLite only supports closing cursors at commit")
    }

    /**
     * Creates a new [Statement] object with the given parameters.
     *
     * @param rst The ResultSet type to be used.
     * @param rsc The ResultSet concurrency to be used.
     * @param rsh The ResultSet holdability to be used.
     * @return A new [ErldbStatement] object.
     * @throws SQLException If the connection is closed or the ResultSet parameters are invalid.
     */
    override fun createStatement(rst: Int, rsc: Int, rsh: Int): Statement = withGuard().execute {
        checkCursor(rst, rsc, rsh)
        ErldbStatement(this)
    }

    /**
     * Prepares a statement for execution and returns a [PreparedStatement] object.
     *
     * @param sql the SQL statement to be prepared
     * @param rst the result set type to be used for the [PreparedStatement] object
     * @param rsc the concurrency type to be used for the [PreparedStatement] object
     * @param rsh the holdability to be used for the [PreparedStatement] object
     * @return a new [PreparedStatement] object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs or the connection is closed
     */
    override fun prepareStatement(sql: String, rst: Int, rsc: Int, rsh: Int): PreparedStatement = withGuard().execute {
        checkCursor(rst, rsc, rsh)
        ErldbPreparedStatement(this, sql)
    }


    /**
     * Returns the given SQL string without modification.
     *
     * @param sql the SQL string to be returned
     * @return the SQL string passed as parameter
     */
    override fun nativeSQL(sql: String): String = withGuard().execute { sql }

    /**
     * Sets the auto-commit mode for this connection.
     *
     * @param ac true to enable auto-commit mode; false to disable it
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    override fun setAutoCommit(ac: Boolean) = withGuard().execute {
        if (config.autoCommit != ac) {
            config.autoCommit = ac

            if (config.autoCommit) {
                prepareAndStep("commit;", ac)
                config.transactionMode = TransactionMode.DEFERRED
            } else {
                prepareAndStep(config.transactionPrefix, ac)
            }
        }
    }

    override fun getAutoCommit(): Boolean = withGuard().execute {
        config.autoCommit
    }

    override fun commit() {
        //TODO("Not yet implemented")
    }

    /**
     * Rollbacks the current transaction.
     * @throws SQLException if the database is in auto-commit mode.
     */
    override fun rollback() = withGuard().let {
        if (autoCommit) {
            throw SQLException("database in auto-commit mode")
        }
        prepareAndStep("rollback;", autoCommit)
        prepareAndStep(config.transactionPrefix, autoCommit)
        firstStatementExecuted = false
    }

    /**
     * Rollbacks the current transaction to the specified savepoint.
     *
     * @param savepoint the savepoint to rollback to
     * @throws SQLException if the database is in auto-commit mode
     */
    override fun rollback(savepoint: Savepoint) = withGuard().execute {
        if (autoCommit) {
            throw SQLException("database in auto-commit mode")
        }
        prepareAndStep(
            "ROLLBACK TO SAVEPOINT ${savepoint.savepointName}", autoCommit
        )
    }

    override fun isClosed(): Boolean = !open

    override fun getMetaData(): DatabaseMetaData = withGuard().execute {
        ErldbDatabaseMetaData(this)
    }

    /**
     * Sets the read-only flag for this connection.
     *
     * @param ro the new value for the read-only flag
     * @throws SQLException if the read-only flag cannot be changed after executing statements or if trying to change the flag for an implicit read-only connection
     */
    override fun setReadOnly(ro: Boolean) = withGuard().execute {
        if (config.explicitReadOnly) {
            if (ro != thisReadOnly && firstStatementExecuted) {
                throw SQLException("Cannot change read-only flag after executing statements")
            }
        } else {
            if (ro != isReadOnly()) {
                throw SQLException("Cannot change read-only flag for implicit read-only connection")
            }
        }
        thisReadOnly = ro
    }


    override fun isReadOnly(): Boolean = withGuard().execute {
        (config.explicitReadOnly && thisReadOnly) || ((config.openModeFlags and SQLiteOpenMode.READONLY.flag) != 0)
    }

    /**
     * Sets the transaction isolation level for this connection.
     *
     * @param level the transaction isolation level to set. Must be one of TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ, or TRANSACTION_SERIALIZABLE in java.sql.Connection.
     * @throws SQLException if the given transaction isolation level is unsupported.
     */
    override fun setTransactionIsolation(level: Int) = withGuard().execute {
        when (level) {
            Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, Connection.TRANSACTION_SERIALIZABLE -> {
                prepareAndStep("PRAGMA read_uncommitted = false;", autoCommit)
            }

            Connection.TRANSACTION_READ_UNCOMMITTED -> {
                prepareAndStep("PRAGMA read_uncommitted = true;", autoCommit)
            }

            else -> throw SQLException(
                "Unsupported transaction isolation level: $level. Must be one of TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ, or TRANSACTION_SERIALIZABLE in java.sql.Connection"
            )
        }
    }

    override fun getTransactionIsolation(): Int = withGuard().execute { config.transactionIsolation }


    override fun getTypeMap(): MutableMap<String, Class<*>> = withGuard().execute { typeMapField }

    override fun setTypeMap(map: MutableMap<String, Class<*>>) = withGuard().execute {
        typeMapField = map
    }

    override fun setHoldability(h: Int) = withGuard().execute {
        if (h != ResultSet.CLOSE_CURSORS_AT_COMMIT) {
            throw SQLException("SQLite only supports CLOSE_CURSORS_AT_COMMIT")
        }
    }

    override fun getHoldability(): Int = withGuard().execute {
        ResultSet.CLOSE_CURSORS_AT_COMMIT
    }

    override fun setSavepoint(): Savepoint = withGuard().execute {
        if (autoCommit) {
            // when a SAVEPOINT is the outermost savepoint and not
            // with a BEGIN...COMMIT then the behavior is the same
            // as BEGIN DEFERRED TRANSACTION
            // https://www.sqlite.org/lang_savepoint.html
            config.autoCommit = false
        }
        val sp: Savepoint = ErldbSavepoint(savePoint.incrementAndGet())
        prepareAndStep("SAVEPOINT ${sp.savepointName}", false)
        sp
    }

    override fun setSavepoint(name: String): Savepoint = withGuard().execute {
        if (autoCommit) {
            // when a SAVEPOINT is the outermost savepoint and not
            // with a BEGIN...COMMIT then the behavior is the same
            // as BEGIN DEFERRED TRANSACTION
            // https://www.sqlite.org/lang_savepoint.html
            config.autoCommit = false
        }
        val sp: Savepoint = ErldbSavepoint(savePoint.incrementAndGet(), name)
        prepareAndStep("SAVEPOINT ${sp.savepointName}", false)
        sp
    }

    override fun releaseSavepoint(savepoint: Savepoint) = withGuard().execute {
        if (autoCommit) {
            throw SQLException("database in auto-commit mode")
        }
        prepareAndStep(
            "RELEASE SAVEPOINT ${savepoint.savepointName}", autoCommit
        )
    }


    override fun isValid(timeout: Int): Boolean = withGuard().execute {
        createStatement().use { it.execute("SELECT 1") }
    }

    override fun setClientInfo(name: String?, value: String?) = withGuard().execute {
        println("ErldbConnection.setClientInfo()")
    }

    override fun setClientInfo(properties: Properties?) = withGuard().execute {
        println("ErldbConnection.setClientInfo()")
    }


    override fun createStruct(t: String, attr: Array<out Any>): Struct = withGuard().execute {
        println("ErldbConnection.createStruct()")
        TODO()
    }

    override fun setSchema(schema: String) = withGuard().execute {
        println("ErldbConnection.setSchema()")
    }


    override fun abort(executor: Executor) = withGuard().execute {
        println("ErldbConnection.abort()")
    }

    override fun setNetworkTimeout(executor: Executor, milliseconds: Int) = withGuard().execute {
        //println("ErldbConnection.setNetworkTimeout()")
    }

    override fun getNetworkTimeout(): Int = withGuard().execute { 0 }

    /**
     * Closes a database connection and finalizes any remaining statements before the closing
     * operation.
     *
     * @throws SQLException
     * @see [https://www.sqlite.org/c3ref/close.html](https://www.sqlite.org/c3ref/close.html)
     */

    override fun close() = try {
        withGuard().execute {
            for (stmt in stmts) {
                stmt.close()
            }
            stmts.clear()
            open = false
            val ignored = dbAccess.close()
        }
    } catch (_: Exception) {}

    fun setBusyTimeout(busyTimeout: Int) = withGuard().execute {
        dbAccess.busyTimeout(busyTimeout)
    }

    fun getBusyTimeout(): Int = withGuard().execute {
        println("ErldbConnection.getBusyTimeout()")
        0
    }

    fun getSQLiteDatabaseMetaData(): DatabaseMetaData = withGuard().execute {
        ErldbDatabaseMetaData(this)
    }


    /**
     * Executes an SQL statement using the process of compiling, evaluating, and destroying the
     * prepared statement object.
     *
     * @param sql SQL statement to be executed.
     * @throws SQLException
     * @see [https://www.sqlite.org/c3ref/exec.html](https://www.sqlite.org/c3ref/exec.html)
     */

    fun prepareAndStep(sql: String, autoCommit: Boolean) = withGuard().execute {
        prepare(sql).let { stmt ->
            AutoCloseable { stmt.close() }.use {
                val rv = stmt.stepBy(1)
                IStatementAccess.ensureAutoCommit(autoCommit, dbAccess)
            }
        }
    }

    /**
     * Complies an SQL statement.
     *
     * @param stmt The SQL statement to compile.
     * @throws SQLException
     * @see [https://www.sqlite.org/c3ref/prepare.html](https://www.sqlite.org/c3ref/prepare.html)
     */

    fun prepare(stmt: ErldbStatement): IStatementAccess = withGuard().execute {
        val rv = dbAccess.prepare(stmt.sql)
        if (!stmts.add(stmt.statementAccess)) {
            throw IllegalStateException("Already added pointer to statements set")
        }
        rv.getOrThrow()
    }


    /**
     * Execute an SQL INSERT, UPDATE or DELETE statement with the Stmt object and an array of
     * parameter values of the SQL statement..
     *
     * @param stmt Stmt object.
     * @param vals Array of parameter values.
     * @return Number of database rows that were changed or inserted or deleted by the most recently
     * completed SQL.
     * @throws SQLException
     */

    fun executeUpdate(stmt: ErldbStatement, vals: Array<Any?>): Long = withGuard().execute {
        val params = vals.iterator().asSequence().map { SQLiteValue.from(it) }.toList().toTypedArray()
        dbAccess.execute(stmt.sql, params)
            .onFailure { this.close() }
            .getOrThrow()
            .toLong()
    }


    /**
     * SQLite and the JDBC API have very different ideas about the meaning of auto-commit. Under
     * JDBC, when executeUpdate() returns in auto-commit mode (the default), the programmer assumes
     * the data has been written to disk. In SQLite however, a call to sqlite3_step() with an INSERT
     * statement can return SQLITE_OK, and yet the data is still in limbo.
     *
     *
     * This limbo appears when another statement on the database is active, e.g. a SELECT. SQLite
     * auto-commit waits until the final read statement finishes, and then writes whatever updates
     * have already been OKed. So if a program crashes before the reads are complete, data is lost.
     * E.g:
     *
     *
     * select begins insert select continues select finishes
     *
     *
     * Works as expected, however
     *
     *
     * select beings insert select continues crash
     *
     *
     * Results in the data never being written to disk.
     *
     *
     * As a solution, we call "commit" after every statement in auto-commit mode.
     *
     * @throws SQLException
     */

    // IDatabaseAccess wrapper methods
    // fun errorMessage(): String = withGuard().execute { dbAccess.errorMessage() }

   // fun interrupt() = withGuard().execute { dbAccess.interrupt() }

    fun libVersion(): String = withGuard().execute { "3.40" }//withGuard().execute { contextAccess.libVersion() }


    fun prepare(sql: String): IStatementAccess = withGuard().execute { dbAccess.prepare(sql).getOrThrow() }

//    fun limit(id: Int, value: Int): Int = withGuard().execute { dbAccess.limit(id, value)
//        .onFailure { this.close() }
//        .getOrThrow()
//    }

    //fun changes(): Long = withGuard().execute { dbAccess.changes().onFailure {  }

    //fun totalChanges(): Long = withGuard().execute { dbAccess.totalChanges() }

//    fun busyTimeout(ms: Int) = withGuard().execute { dbAccess.busyTimeout(ms) }


//    fun enableLoadExtension(onoff: Boolean) = withGuard().execute { dbAccess.enableLoadExtension(onoff) }


    // TODO: controllare parametri di questa funzione
    //fun columnMetadata(
    //    stmt: SafeStmtPtr, col: Int, dbName: String, tableName: String
    //): Array<Triple<Boolean, Boolean, Boolean>> = dbAccess.columnMetadata(stmt, col, dbName, tableName)

}

