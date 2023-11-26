package it.unipi.dsmt.microservices.erldbadmin.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated

@Validated
data class SignupRequest(
    @field:NotBlank @field:Size(min = 3, max = 20) var username: String,
    var email: /*@NotBlank @Size(max = 50) @Email*/ String,
    @field:NotBlank @field:Size(min = 6, max = 40) var password: String,
)