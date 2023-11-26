package it.unipi.dsmt.microservices.erldbadmin.controller

import it.unipi.dsmt.microservices.erldbadmin.JwtUtils
import it.unipi.dsmt.microservices.erldbadmin.dto.user.LoginRequest
import it.unipi.dsmt.microservices.erldbadmin.dto.user.LoginResponse
import it.unipi.dsmt.microservices.erldbadmin.dto.user.SignupRequest
import it.unipi.dsmt.microservices.erldbadmin.dto.user.SignupResponse
import it.unipi.dsmt.microservices.erldbadmin.exception.RequestException
import it.unipi.dsmt.microservices.erldbadmin.model.User
import it.unipi.dsmt.microservices.erldbadmin.service.UserDetailsImpl
import it.unipi.dsmt.microservices.erldbadmin.service.UserService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

//import java.util.*


@RestController
@RequestMapping("/v1/user")
@Validated
open class UserController {
    @Autowired
    lateinit var authenticationManager: AuthenticationManager


    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var encoder: PasswordEncoder

    @Autowired
    lateinit var jwtUtils: JwtUtils


    @PostMapping("/login")
    open fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val (username, password) = loginRequest
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                username,
                password
            )
        )
        SecurityContextHolder.getContext().authentication = authentication
        val userDetails = authentication.principal as UserDetailsImpl
        val jwtCookie = jwtUtils.generateJwtCookie(userDetails)
        val roles = userDetails.authorities.map { it.authority.substringAfter("_") }

        val millis = if (jwtCookie.maxAge.seconds > 0L) System.currentTimeMillis() + jwtCookie.maxAge.toMillis() else 0L
        val instant = Instant.ofEpochMilli(millis)
        val expiresAt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))

        return ResponseEntity.ok().body(
            LoginResponse(
                username = userDetails.username,
                token = jwtCookie.value,
                isAdmin = roles.contains("ADMIN"),
                expirationDate = expiresAt.toString(),
            )
        )
    }


    @PostMapping("/signup")
    open fun signup(@Valid @RequestBody signUpRequest: SignupRequest): ResponseEntity<SignupResponse> {
        val (username, email, password) = signUpRequest
        if (userService.userExists(username)) {
            throw object : RequestException() {
                override val status: Int
                    get() = 400
                override var error = "Username is already taken!"
            }
        }
        val user = User(
            username = username,
            password = encoder.encode(password),
        )
        userService.createUser(user)
        return ResponseEntity.ok().body(
            SignupResponse(message = "User registered successfully!")
        )
    }
}