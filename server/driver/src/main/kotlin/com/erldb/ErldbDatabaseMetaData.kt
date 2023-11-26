package com.erldb

import java.sql.*




class ErldbDatabaseMetaData : DatabaseMetaData {
    private val connection: ErldbConnection
    private var contextAccess: ContextAccess
    @Volatile
    private var open: Boolean = true


    fun checkOpen() {
        if (!open) {
            throw RuntimeException("Statement is closed")
        }
    }

    constructor(connection: ErldbConnection) {
        this.connection = connection
        this.contextAccess = ContextAccess.getInstance(connection.nodes)
    }

    override fun <T : Any?> unwrap(iface: Class<T>?): T? = checkOpen().let {
        iface?.cast(this)
    }

    override fun isWrapperFor(iface: Class<*>?): Boolean = checkOpen().let {
        iface?.isInstance(this) ?: false
    }

    override fun allProceduresAreCallable(): Boolean = checkOpen().let {
        false
    }

    override fun allTablesAreSelectable(): Boolean = checkOpen().let {
        true
    }

    override fun getURL(): String = checkOpen().let {
        TODO()
        //conn.url
    }

    override fun getUserName(): String? = checkOpen().let {
        null
    }

    override fun isReadOnly(): Boolean {
        TODO("Not yet implemented")
    }

    override fun nullsAreSortedHigh(): Boolean = checkOpen().let {
        true
    }

    override fun nullsAreSortedLow(): Boolean = checkOpen().let {
        false
    }

    override fun nullsAreSortedAtStart(): Boolean = checkOpen().let {
        true
    }

    override fun nullsAreSortedAtEnd(): Boolean = checkOpen().let {
        false
    }

    override fun getDatabaseProductName(): String = checkOpen().let {
        "Erldb"
    }

    override fun getDatabaseProductVersion(): String = checkOpen().let {
        //connection.libVersion()
        // TODO
        "0.0.1"
    }


    override fun getDriverName(): String = "Erldb JDBC Driver"

    override fun getDriverVersion(): String {
        TODO("Not yet implemented")
    }

    override fun getDriverMajorVersion(): Int = "0.0.1".split("\\.")[0].toInt()

    override fun getDriverMinorVersion(): Int = "0.0.1".split("\\.")[1].toInt()
    override fun usesLocalFiles(): Boolean = checkOpen().let {
        false
    }

    override fun usesLocalFilePerTable(): Boolean = checkOpen().let {
        false
    }

    override fun supportsMixedCaseIdentifiers(): Boolean = checkOpen().let {
        true
    }

    override fun storesUpperCaseIdentifiers(): Boolean = checkOpen().let {
        false
    }

    override fun storesLowerCaseIdentifiers(): Boolean = checkOpen().let {
        false
    }

    override fun storesMixedCaseIdentifiers(): Boolean = checkOpen().let {
        true
    }

    override fun supportsMixedCaseQuotedIdentifiers(): Boolean = checkOpen().let {
        false
    }

    override fun storesUpperCaseQuotedIdentifiers(): Boolean = checkOpen().let {
        false
    }

    override fun storesLowerCaseQuotedIdentifiers(): Boolean = checkOpen().let {
        false
    }

    override fun storesMixedCaseQuotedIdentifiers(): Boolean = checkOpen().let {
        false
    }

    override fun getIdentifierQuoteString(): String = checkOpen().let {
        "\""
    }


    override fun getSQLKeywords(): String = checkOpen().let {
        buildString {
            append("ABORT,ACTION,AFTER,ANALYZE,ATTACH,AUTOINCREMENT,BEFORE,")
            append("CASCADE,CONFLICT,DATABASE,DEFERRABLE,DEFERRED,DESC,DETACH,")
            append("EXCLUSIVE,EXPLAIN,FAIL,GLOB,IGNORE,INDEX,INDEXED,INITIALLY,INSTEAD,ISNULL,")
            append("KEY,LIMIT,NOTNULL,OFFSET,PLAN,PRAGMA,QUERY,")
            append("RAISE,REGEXP,REINDEX,RENAME,REPLACE,RESTRICT,")
            append("TEMP,TEMPORARY,TRANSACTION,VACUUM,VIEW,VIRTUAL")
        }
    }

    override fun getNumericFunctions(): String = checkOpen().let {
        ""
    }

    override fun getStringFunctions(): String = checkOpen().let {
        ""
    }

    override fun getSystemFunctions(): String = checkOpen().let {
        ""
    }

    override fun getTimeDateFunctions(): String = checkOpen().let {
        "DATE,TIME,DATETIME,JULIANDAY,STRFTIME"
    }

    override fun getSearchStringEscape(): String = checkOpen().let {
        "\\"
    }

    override fun getExtraNameCharacters(): String = checkOpen().let {
        ""
    }

    override fun supportsAlterTableWithAddColumn(): Boolean = checkOpen().let {
        false
    }

    override fun supportsAlterTableWithDropColumn(): Boolean = checkOpen().let {
        false
    }

    override fun supportsColumnAliasing(): Boolean = checkOpen().let {
        true
    }

    override fun nullPlusNonNullIsNull(): Boolean = checkOpen().let {
        true
    }

    override fun supportsConvert(): Boolean = checkOpen().let {
        false
    }

    override fun supportsConvert(p0: Int, p1: Int): Boolean = checkOpen().let {
        false
    }

    override fun supportsTableCorrelationNames(): Boolean = checkOpen().let {
        false
    }

    override fun supportsDifferentTableCorrelationNames(): Boolean = checkOpen().let {
        false
    }

    override fun supportsExpressionsInOrderBy(): Boolean = checkOpen().let {
        true
    }

    override fun supportsOrderByUnrelated(): Boolean = checkOpen().let {
        false
    }

    override fun supportsGroupBy(): Boolean = checkOpen().let {
        true
    }

    override fun supportsGroupByUnrelated(): Boolean = checkOpen().let {
        false
    }

    override fun supportsGroupByBeyondSelect(): Boolean = checkOpen().let {
        false
    }

    override fun supportsLikeEscapeClause(): Boolean = checkOpen().let {
        false
    }

    override fun supportsMultipleResultSets(): Boolean = checkOpen().let {
        false
    }

    override fun supportsMultipleTransactions(): Boolean = checkOpen().let {
        true
    }

    override fun supportsNonNullableColumns(): Boolean = checkOpen().let {
        true
    }

    override fun supportsMinimumSQLGrammar(): Boolean = checkOpen().let {
        true
    }

    override fun supportsCoreSQLGrammar(): Boolean = checkOpen().let {
        true
    }

    override fun supportsExtendedSQLGrammar(): Boolean = checkOpen().let {
        false
    }

    override fun supportsANSI92EntryLevelSQL(): Boolean = checkOpen().let {
        false
    }

    override fun supportsANSI92IntermediateSQL(): Boolean = checkOpen().let {
        false
    }

    override fun supportsANSI92FullSQL(): Boolean = checkOpen().let {
        false
    }

    override fun supportsIntegrityEnhancementFacility(): Boolean = checkOpen().let {
        false
    }

    override fun supportsOuterJoins(): Boolean = checkOpen().let {
        true
    }

    override fun supportsFullOuterJoins(): Boolean {
        return true
    }

    override fun supportsLimitedOuterJoins(): Boolean = checkOpen().let {
        true
    }

    override fun getSchemaTerm(): String = checkOpen().let {
        "schema"
    }

    override fun getProcedureTerm(): String {
        TODO("Not yet implemented")
    }

    override fun getCatalogTerm(): String = checkOpen().let {
        "catalog"
    }

    override fun isCatalogAtStart(): Boolean = checkOpen().let {
        true
    }

    override fun getCatalogSeparator(): String = checkOpen().let {
        "."
    }

    override fun supportsSchemasInDataManipulation(): Boolean = checkOpen().let {
        false
    }

    override fun supportsSchemasInProcedureCalls(): Boolean = checkOpen().let {
        false
    }

    override fun supportsSchemasInTableDefinitions(): Boolean = checkOpen().let {
        false
    }

    override fun supportsSchemasInIndexDefinitions(): Boolean = checkOpen().let {
        false
    }

    override fun supportsSchemasInPrivilegeDefinitions(): Boolean = checkOpen().let {
        false
    }

    override fun supportsCatalogsInDataManipulation(): Boolean = checkOpen().let {
        false
    }

    override fun supportsCatalogsInProcedureCalls(): Boolean = checkOpen().let {
        false
    }

    override fun supportsCatalogsInTableDefinitions(): Boolean = checkOpen().let {
        false
    }

    override fun supportsCatalogsInIndexDefinitions(): Boolean = checkOpen().let {
        false
    }

    override fun supportsCatalogsInPrivilegeDefinitions(): Boolean = false

    override fun supportsPositionedDelete(): Boolean = false

    override fun supportsPositionedUpdate(): Boolean = false

    override fun supportsSelectForUpdate(): Boolean = false

    override fun supportsStoredProcedures(): Boolean = false

    override fun supportsSubqueriesInComparisons(): Boolean = false

    override fun supportsSubqueriesInExists(): Boolean = true

    override fun supportsSubqueriesInIns(): Boolean = true

    override fun supportsSubqueriesInQuantifieds(): Boolean = false

    override fun supportsCorrelatedSubqueries(): Boolean = false

    override fun supportsUnion(): Boolean = true

    override fun supportsUnionAll(): Boolean = true

    override fun supportsOpenCursorsAcrossCommit(): Boolean = false

    override fun supportsOpenCursorsAcrossRollback(): Boolean = false

    override fun supportsOpenStatementsAcrossCommit(): Boolean = false

    override fun supportsOpenStatementsAcrossRollback(): Boolean = false

    override fun getMaxBinaryLiteralLength(): Int = 0

    override fun getMaxCharLiteralLength(): Int = 0

    override fun getMaxColumnNameLength(): Int = 0

    override fun getMaxColumnsInGroupBy(): Int = 0

    override fun getMaxColumnsInIndex(): Int = 0

    override fun getMaxColumnsInOrderBy(): Int = 0

    override fun getMaxColumnsInSelect(): Int = 0

    override fun getMaxColumnsInTable(): Int = 0

    override fun getMaxConnections(): Int = 0

    override fun getMaxCursorNameLength(): Int = 0

    override fun getMaxIndexLength(): Int = 0

    override fun getMaxSchemaNameLength(): Int = 0

    override fun getMaxProcedureNameLength(): Int = 0

    override fun getMaxCatalogNameLength(): Int = 0

    override fun getMaxRowSize(): Int = 0
    override fun doesMaxRowSizeIncludeBlobs(): Boolean = false

    override fun getMaxStatementLength(): Int = 0

    override fun getMaxStatements(): Int = 0

    override fun getMaxTableNameLength(): Int = 0

    override fun getMaxTablesInSelect(): Int = 0

    override fun getMaxUserNameLength(): Int = 0

    override fun getDefaultTransactionIsolation(): Int = Connection.TRANSACTION_SERIALIZABLE

    override fun supportsTransactions(): Boolean = true

    override fun supportsTransactionIsolationLevel(level: Int): Boolean = level == Connection.TRANSACTION_SERIALIZABLE

    override fun supportsDataDefinitionAndDataManipulationTransactions(): Boolean = true

    override fun supportsDataManipulationTransactionsOnly(): Boolean = false

    // Note that `checkOpen` would need to be a function that returns a Boolean indicating if the connection is open.
    override fun dataDefinitionCausesTransactionCommit(): Boolean = checkOpen().let { false }

    override fun dataDefinitionIgnoredInTransactions(): Boolean = checkOpen().let { false }
    override fun getProcedures(p0: String?, p1: String?, p2: String?): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL as PROCEDURE_CAT, NULL as PROCEDURE_SCHEM, ")
            append("NULL as PROCEDURE_NAME, NULL as UNDEF1, null as UNDEF2, NULL as UNDEF3, ")
            append("NULL as REMARKS, NULL as PROCEDURE_TYPE limit 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getProcedureColumns(p0: String?, p1: String?, p2: String?, p3: String?): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL as PROCEDURE_CAT, NULL as PROCEDURE_SCHEM, ")
            append("NULL as PROCEDURE_NAME, NULL as COLUMN_NAME, NULL as COLUMN_TYPE, ")
            append("NULL as DATA_TYPE, NULL as TYPE_NAME, NULL as PRECISION, ")
            append("NULL as LENGTH, NULL as SCALE, NULL as RADIX, NULL as NULLABLE, ")
            append("NULL as REMARKS LIMIT 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getTables(catalog: String?, p1: String?, p2: String?, p3: Array<out String>?): ResultSet? {
        val sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
        val stmt: Statement? = connection.createStatement()
        return stmt?.executeQuery(sql)
    }

    override fun getSchemas(): ResultSet {
        val stmt = connection.prepareStatement(
            "SELECT NULL as TABLE_SCHEM, NULL as TABLE_CATALOG limit 0;"
        )
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getSchemas(p0: String?, p1: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getCatalogs(): ResultSet {
        val columns = listOf("TABLE_CAT")
        val values = listOf(listOf(SQLiteValue.from("main")))
        return ListBasedResultSet(columns, values)
    }

    override fun getTableTypes(): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getColumns(p0: String?, p1: String?, p2: String?, p3: String?): ResultSet {
        TODO()
    }

    override fun getColumnPrivileges(p0: String?, p1: String?, p2: String?, p3: String?): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL as TABLE_CAT, NULL as TABLE_SCHEM, ")
            append("NULL as TABLE_NAME, NULL as COLUMN_NAME, NULL as DATA_TYPE, ")
            append("NULL as PRIVILEGE, NULL as IS_GRANTABLE LIMIT 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getTablePrivileges(p0: String?, p1: String?, p2: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getBestRowIdentifier(p0: String?, p1: String?, p2: String?, p3: Int, p4: Boolean): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL as SCOPE, NULL as COLUMN_NAME, ")
            append("NULL as DATA_TYPE, NULL as TYPE_NAME, NULL as COLUMN_SIZE, ")
            append("NULL as BUFFER_LENGTH, NULL as DECIMAL_DIGITS, NULL as PSEUDO_COLUMN limit 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getVersionColumns(p0: String?, p1: String?, p2: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getPrimaryKeys(p0: String?, p1: String?, p2: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getImportedKeys(p0: String?, p1: String?, p2: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getExportedKeys(p0: String?, p1: String?, p2: String?): ResultSet {
        TODO("Not yet implemented")
    }

    /*override fun getCrossReference(
        p0: String?, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?
    ): ResultSet {
        TODO("Not yet implemented")
    }*/

    override fun getCrossReference(
        pc: String?, ps: String?, pt: String?, fc: String?, fs: String?, ft: String?
    ): ResultSet {
        if (pt == null) {
            return getExportedKeys(fc, fs, ft)
        }
        if (ft == null) {
            return getImportedKeys(pc, ps, pt)
        }
        val query = """SELECT ? AS PKTABLE_CAT, 
                          ? AS PKTABLE_SCHEM, 
                          ? AS PKTABLE_NAME, 
                          '' AS PKCOLUMN_NAME, 
                          ? AS FKTABLE_CAT, 
                          ? AS FKTABLE_SCHEM, 
                          ? AS FKTABLE_NAME, 
                          '' AS FKCOLUMN_NAME, 
                          -1 AS KEY_SEQ, 
                          3 AS UPDATE_RULE, 
                          3 AS DELETE_RULE, 
                          '' AS FK_NAME, 
                          '' AS PK_NAME, 
                          ? AS DEFERRABILITY 
                   LIMIT 0"""

        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setString(1, pc)
        preparedStatement.setString(2, ps)
        preparedStatement.setString(3, pt)
        preparedStatement.setString(4, fc)
        preparedStatement.setString(5, fs)
        preparedStatement.setString(6, ft)
        preparedStatement.setInt(7, DatabaseMetaData.importedKeyInitiallyDeferred)

        return preparedStatement.executeQuery()
    }


    override fun getTypeInfo(): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getIndexInfo(p0: String?, p1: String?, p2: String?, p3: Boolean, p4: Boolean): ResultSet {
        TODO("Not yet implemented")
    }

    override fun supportsResultSetType(t: Int): Boolean {
        return t == ResultSet.TYPE_FORWARD_ONLY;
    }

    override fun supportsResultSetConcurrency(p0: Int, p1: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun ownUpdatesAreVisible(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun ownDeletesAreVisible(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun ownInsertsAreVisible(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun othersUpdatesAreVisible(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun othersDeletesAreVisible(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun othersInsertsAreVisible(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun updatesAreDetected(p0: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun deletesAreDetected(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun insertsAreDetected(p0: Int): Boolean = checkOpen().let {
        false
    }

    override fun supportsBatchUpdates(): Boolean = checkOpen().let {
        true
    }

    override fun getUDTs(p0: String?, p1: String?, p2: String?, p3: IntArray?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getConnection(): Connection = checkOpen().let { connection }


    override fun supportsSavepoints(): Boolean {
        TODO("Not yet implemented")
    }

    override fun supportsNamedParameters(): Boolean = checkOpen().let {
        false
    }

    override fun supportsMultipleOpenResults(): Boolean = checkOpen().let {
        false
    }



    override fun supportsGetGeneratedKeys(): Boolean = checkOpen().let {
        true
    }

    override fun getSuperTypes(p0: String?, p1: String?, p2: String?): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL AS TYPE_CAT, NULL AS TYPE_SCHEM, ")
            append("NULL AS TYPE_NAME, NULL AS SUPERTYPE_CAT, NULL AS SUPERTYPE_SCHEM, ")
            append("NULL AS SUPERTYPE_NAME LIMIT 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getSuperTables(p0: String?, p1: String?, p2: String?): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL AS TABLE_CAT, NULL AS TABLE_SCHEM, ")
            append("NULL AS TABLE_NAME, NULL AS SUPERTABLE_NAME LIMIT 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }

    override fun getAttributes(p0: String?, p1: String?, p2: String?, p3: String?): ResultSet {
        val stmt = connection.prepareStatement(buildString {
            append("SELECT NULL AS TYPE_CAT, NULL AS TYPE_SCHEM, ")
            append("NULL AS TYPE_NAME, NULL AS ATTR_NAME, NULL AS DATA_TYPE, ")
            append("NULL AS ATTR_TYPE_NAME, NULL AS ATTR_SIZE, NULL AS DECIMAL_DIGITS, ")
            append("NULL AS NUM_PREC_RADIX, NULL AS NULLABLE, NULL AS REMARKS, NULL AS ATTR_DEF, ")
            append("NULL AS SQL_DATA_TYPE, NULL AS SQL_DATETIME_SUB, NULL AS CHAR_OCTET_LENGTH, ")
            append("NULL AS ORDINAL_POSITION, NULL AS IS_NULLABLE, NULL AS SCOPE_CATALOG, ")
            append("NULL AS SCOPE_SCHEMA, NULL AS SCOPE_TABLE, NULL AS SOURCE_DATA_TYPE LIMIT 0;")
        })
        stmt.closeOnCompletion()
        return stmt.executeQuery()
    }


    override fun supportsResultSetHoldability(h: Int): Boolean = h == ResultSet.CLOSE_CURSORS_AT_COMMIT


    override fun getResultSetHoldability(): Int = ResultSet.CLOSE_CURSORS_AT_COMMIT

    override fun getDatabaseMajorVersion(): Int = checkOpen().let {
        //Integer.parseInt(connection.libVersion().split("\\.")[0])
        3
    }

    override fun getDatabaseMinorVersion(): Int = checkOpen().let {
        //Integer.parseInt(connection.libVersion().split("\\.")[1])
        40
    }

    override fun getJDBCMajorVersion(): Int = 3

    override fun getJDBCMinorVersion(): Int = 0
    override fun getSQLStateType(): Int = DatabaseMetaData.sqlStateSQL99

    override fun locatorsUpdateCopy(): Boolean = checkOpen().let {
        false
    }

    override fun supportsStatementPooling(): Boolean = checkOpen().let {
        false
    }

    override fun getRowIdLifetime(): RowIdLifetime {
        TODO("Not yet implemented")
    }

    override fun supportsStoredFunctionsUsingCallSyntax(): Boolean {
        TODO("Not yet implemented")
    }

    override fun autoCommitFailureClosesAllResultSets(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getClientInfoProperties(): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getFunctions(p0: String?, p1: String?, p2: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getFunctionColumns(p0: String?, p1: String?, p2: String?, p3: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun getPseudoColumns(p0: String?, p1: String?, p2: String?, p3: String?): ResultSet {
        TODO("Not yet implemented")
    }

    override fun generatedKeyAlwaysReturned(): Boolean {
        TODO("Not yet implemented")
    }
}
