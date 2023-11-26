package com.erldb

import java.util.*

enum class TransactionMode {
    DEFERRED, IMMEDIATE, EXCLUSIVE;

    val value: String
        get() = name

    companion object {
        fun getMode(mode: String): TransactionMode {
            return TransactionMode.valueOf(mode.uppercase(Locale.getDefault()))
        }
    }
}