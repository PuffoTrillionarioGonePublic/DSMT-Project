package it.unipi.dsmt.microservices.erldbadmin.dto.admin

import it.unipi.dsmt.microservices.erldbadmin.exception.TransparentException

data class AccessUpdateRequest(
    val username: String,
    val file: String,
    val access: Boolean
) {
    init {
        //if (file.startsWith("private"))
        //    throw TransparentException("can't update access to private files")
    }
}