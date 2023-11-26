package it.unipi.dsmt.microservices.erldbadmin.dto.admin

import org.springframework.validation.annotation.Validated

@Validated
data class UserResponse (
    var users: List<String>,
)