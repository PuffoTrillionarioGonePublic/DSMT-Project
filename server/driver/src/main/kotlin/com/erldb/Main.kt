package com.erldb

import java.sql.*
import java.util.*

val PIPPO = ErldbConnection("jdbc:erldb://localhost:8080,localhost:8081,localhost:8082/public/sample.db", null)
fun connect(): Connection = PIPPO

fun createTable() {
    val sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            date TEXT DEFAULT CURRENT_TIMESTAMP
        );
    """

    connect().let { conn ->
        conn.prepareStatement(sql)?.executeQuery()
    }
}

fun insertUser(name: String) {
    val sql = "INSERT INTO users(name) VALUES(?)"
    connect().let { conn ->
        val pstmt = conn.prepareStatement(sql)
        pstmt?.setString(1, name)
        pstmt?.executeUpdate()
    }
}


// do not use prepared statement because it is not supported by Erldb
fun insertUser2(name: String) {
    val sql = "INSERT INTO users(name) VALUES('$name');"
    connect().let { conn ->
        val stmt = conn.createStatement()
        stmt?.execute(sql)
        stmt?.generatedKeys?.let {
            if (it.next()) {
                println("Inserted user with id: ${it.getInt(1)}")
            }
        }
    }
}

fun wrongUpdateUsers() {
    try {
        val sql = "SELECT * FROM users"
        connect().let { conn ->
            val stmt = conn.createStatement()
            val count = stmt?.executeUpdate(sql)
            println("Not correct: $count rows")
        }
    } catch (e: SQLException) {
        println("This is expected: ${e.message}")
    }
}

fun wrongUpdateUsers2() {
    try {
        val sql = "SELEC32324T * FROM users43242432"
        connect().let { conn ->
            val stmt = conn.createStatement()
            val count = stmt?.executeUpdate(sql)
            println("Not correct: $count rows")
        }
    } catch (e: Exception) {
        println("This is expected: ${e.message}")
    }
}


fun updateUsersWithNoChange() {
    val sql = "UPDATE users SET name = 'Amico' WHERE 1 = 2"

    connect().let { conn ->
        val stmt = conn.createStatement()
        val count = stmt?.executeUpdate(sql)
        println("Updated $count rows")
    }
}

fun updateUsers() {
    val randomString = UUID.randomUUID().toString()
    val sql = "UPDATE users SET name = '$randomString' WHERE id = 1"
    connect().let { conn ->
        val stmt = conn.createStatement()
        val count = stmt?.executeUpdate(sql)
        println("Updated $count rows")
    }
}


fun updateUsersPrepared() {
    val randomString = UUID.randomUUID().toString()
    val sql = "UPDATE users SET name = ? WHERE id = ?"
    connect().let { conn ->
        val pstmt = conn.prepareStatement(sql)
        pstmt?.setString(1, randomString)
        pstmt?.setInt(2, 1)
        val count = pstmt?.executeUpdate()
        println("Updated $count rows")
    }
}

fun displayUsers() {
    val sql = "SELECT id, name, date FROM users WHERE id >= ?"
    connect().let { conn ->
        val stmt = conn.prepareStatement(sql)
        stmt?.setInt(1, 0)
        val rs: ResultSet? = stmt?.executeQuery()
        while (rs?.next() == true) {
            println("${rs.getInt("id")} \t ${rs.getString("name")} \t ${rs.getTime("date")}")
        }
    }
}

fun displayUsers2() {
    val sql = "SELECT id, name, date FROM users"
    connect().let { conn ->
        val stmt: Statement? = conn.createStatement()
        val rs: ResultSet? = stmt?.executeQuery(sql)
        while (rs?.next() == true) {
            println("${rs.getInt("id")} \t ${rs.getString("name")} \t ${rs.getTime("date")}")
        }
    }
}

fun getLibVersion(): String {
    connect().let { conn ->
        return conn.metaData.databaseProductVersion
    }
}


fun getTables(): List<String> {
    connect().let { conn ->
        val sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
        val stmt: Statement? = conn.createStatement()
        val rs: ResultSet? = stmt?.executeQuery(sql)
        val result = mutableListOf<String>()
        while (rs?.next() == true) {
            result.add(rs.getString("name"))
        }
        return result
    }
}




fun getCredentials() {

    val connection = ErldbConnection("jdbc:erldb://localhost:8080/private/credentials", null)

    val sql = "SELECT * FROM credentials"
    connection.use { conn ->
        val stmt: Statement? = conn.createStatement()
        // 			INSERT INTO credentials (username, password, is_admin, created_at) VALUES (?, ?, ?, ?);
        stmt?.executeQuery(sql).use { rs ->
            while (rs?.next() == true) {
                println("${rs.getString("username")} \t ${rs.getString("password")} \t ${rs.getBoolean("is_admin")} \t ${rs.getTimestamp("created_at")}")
            }
        }
    }
}




fun getAccces() {

    val connection = ErldbConnection("jdbc:erldb://localhost:8080/private/credentials", null)

    val sql = "SELECT * FROM privileges"
connection.use { conn ->
        val stmt: Statement? = conn.createStatement()
        stmt?.executeQuery(sql).use { rs ->
            while (rs?.next() == true) {
                println("${rs.getString("username")} \t ${rs.getString("filename")}")
            }
        }
    }


}


fun deleteUserFromCredentials() {

        val connection = ErldbConnection("jdbc:erldb://localhost:8080/private/credentials", null)
        val pragma = "PRAGMA foreign_keys = ON;"
        val sql = "DELETE FROM credentials WHERE username = 'ciaone'"
        connection.use { conn ->
            conn.createStatement().use {
                it?.executeUpdate(pragma)
            }
            conn.createStatement().use {
                it?.executeUpdate(sql)
            }
        }
}


fun pippo() {
    /*SELECT U.id, F.id
            FROM users U, files F
            WHERE
                U.username IN ($usernamesQuestionMarks)
                AND F.name IN ($filesQuestionMarks)*
     */
    val connection = ErldbConnection("jdbc:erldb://localhost:8080/private/credentials", null)

    val users = arrayOf("ciaone1", "plutis1")
    val files = arrayOf("public/plutis", "private/credentials")

    // kotlin cross join between two arrays
    val usersFiles = users.flatMap { user ->
        files.map { file ->
            Pair(user, file)
        }
    }

    val tmp = "SELECT ? AS username, ? AS filename UNION ALL "
    // do basically tmp * usersFiles.len
    val query = usersFiles.fold("") { acc, pair ->
        acc + tmp
    }.dropLast(" UNION ALL ".length) + ";"

    val queryUpdate = "" +
            "INSERT OR REPLACE INTO privileges (username, filename) " + query



    connection.use { conn ->
        conn.prepareStatement(queryUpdate).use {
            usersFiles.withIndex().forEach { (index, pair) ->
                it?.setString(index * 2 + 1, pair.first)
                it?.setString(index * 2 + 2, pair.second)
            }
            it?.executeUpdate()

        }
    }
}


// list tables
fun listTables() {
    val connection = ErldbConnection("jdbc:erldb://localhost:8080/public/sample.db", null)
    val sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
    connection.use { conn ->
        val stmt: Statement? = conn.createStatement()
        val rs: ResultSet? = stmt?.executeQuery(sql)
        while (rs?.next() == true) {
            println(rs.getString("name"))
        }
    }
}


fun listColumns() {
    val connection = ErldbConnection("jdbc:erldb://localhost:8080/public/sample.db", null)
    val sql = "SELECT * FROM users"
    connection.use { conn ->
        val stmt: Statement? = conn.createStatement()
        val rs: ResultSet? = stmt?.executeQuery(sql)
        val meta = rs?.metaData
        val columnCount = meta?.columnCount ?: 0
        for (i in 1..columnCount) {
            println(meta?.getColumnName(i))
        }
    }
}

fun getTablesMetadata(rs: ResultSet?): List<String> {
    val result = mutableListOf<String>()
    while (rs?.next() == true) {
        result.add(rs.getString(1))
    }
    return result
}


class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            createTable()
            println("Enter user name to add to the database (or type 'exit' to finish):")
            while (true) {
                val input = readlnOrNull()?.trim() ?: ""
                if (input.lowercase(Locale.getDefault()) == "exit") break
                insertUser2(input)
            }
            val stmt = PIPPO.createStatement()
            val rs = stmt!!.unwrap(ErldbStatement::class.java)
            rs.execute("SELECT * FROM users3")
            rs.columnNames().onSuccess {
                for (name in it) {
                    println(name)
                }
            }.onFailure {
                println("Error: ${it.toString()}")
            }
/*
            println("\nlib version: ${getLibVersion()}\n")



            println("Tables in the database: ${getTablesMetadata(PIPPO.metaData.getTables(null, null, null, null))}\n")

            println("\nUsers in the database:")
            displayUsers2()
            updateUsersWithNoChange()
            wrongUpdateUsers()
            wrongUpdateUsers2()

            updateUsers()
            println("\nUsers in the database:")
            displayUsers2()
            updateUsersPrepared()
            println("\nUsers in the database:")
            displayUsers2()

            connect().close()*/
        }
    }
}
