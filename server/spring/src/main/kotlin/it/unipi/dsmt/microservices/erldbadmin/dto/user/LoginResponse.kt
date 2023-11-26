package it.unipi.dsmt.microservices.erldbadmin.dto.user


data class LoginResponse(val token: String, val username: String, val isAdmin: Boolean, val expirationDate: String)

