package it.unipi.dsmt.microservices.erldbadmin.dto.logged

import org.springframework.validation.annotation.Validated

@Validated
data class QueryRequest(val bucket: String, val db: String, val query: String)