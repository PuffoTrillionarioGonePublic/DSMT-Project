package it.unipi.dsmt.microservices.erldbadmin.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated

@Validated
data class LoginRequest(
    @field:NotBlank @field:Size(min = 1, max = 20) var username: String,
    @field:NotBlank @field:Size(min = 1, max = 40) var password: String,
)