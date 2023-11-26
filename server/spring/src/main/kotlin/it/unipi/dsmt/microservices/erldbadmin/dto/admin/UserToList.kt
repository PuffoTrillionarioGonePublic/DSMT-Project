package it.unipi.dsmt.microservices.erldbadmin.dto.admin

import java.sql.Timestamp

data class UserToList (
    private var id: Long = -1,
    private var username: String = "",
    private var email: String = "",
    private var createdAt: Timestamp? = null,
)