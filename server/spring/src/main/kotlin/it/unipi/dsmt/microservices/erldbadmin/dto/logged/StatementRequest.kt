package it.unipi.dsmt.microservices.erldbadmin.dto.logged

import org.springframework.validation.annotation.Validated

@Validated
data class StatementRequest(
    val bucket: String, val db: String, val statement: String, val params: List<Array<Any?>>? = null
)