package it.unipi.dsmt.microservices.erldbadmin.dto.admin

import it.unipi.dsmt.microservices.erldbadmin.exception.TransparentException

data class GrantRequest (
    val usernames: Array<String>,
    val files: Array<String>,
) {
    init {
      //  if (files.any { it.startsWith("private") })
      //      throw TransparentException("can't grant access to private files")
    }
}