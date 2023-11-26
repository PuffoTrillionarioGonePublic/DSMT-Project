package com.erldb

import java.sql.SQLException

import java.sql.Savepoint


class ErldbSavepoint : Savepoint {
    val id: Int
    val name: String?

    internal constructor(id: Int) {
        this.id = id
        name = null
    }

    internal constructor(id: Int, name: String?) {
        this.id = id
        this.name = name
    }

    @Throws(SQLException::class)
    override fun getSavepointId(): Int {
        return id
    }

    @Throws(SQLException::class)
    override fun getSavepointName(): String {
        return name ?: "SQLITE_SAVEPOINT_$id"
    }
}

