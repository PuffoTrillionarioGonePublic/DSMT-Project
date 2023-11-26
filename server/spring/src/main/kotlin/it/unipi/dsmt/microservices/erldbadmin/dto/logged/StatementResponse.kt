package it.unipi.dsmt.microservices.erldbadmin.dto.logged

import org.springframework.validation.annotation.Validated


@Validated
data class StatementResponse(val columnNames: List<String>, val rows: List<List<Array<Any?>>>)
