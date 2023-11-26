package com.erldb

import java.sql.Connection

class ErldbConfig (
    var transactionMode: TransactionMode,
    var autoCommit: Boolean,
    var readOnly: Boolean,
    var explicitReadOnly: Boolean,
    var openModeFlags: Int,
) {
    companion object {
        const val DEFAULT_DATE_STRING_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }

    val busyTimeout: Int = 0
    var dateStringFormat = DEFAULT_DATE_STRING_FORMAT

    fun getDateMultiplier(): Long {
        // TODO
        return 1
    }

    // TODO check default values
    val transactionIsolation: Int = Connection.TRANSACTION_SERIALIZABLE

    //val readOnly: bool = false
    val transactionPrefix: String
        get() = when (transactionMode) {
            TransactionMode.DEFERRED -> "begin;"
            TransactionMode.IMMEDIATE -> "begin immediate;"
            TransactionMode.EXCLUSIVE -> "begin exclusive;"
        }



}