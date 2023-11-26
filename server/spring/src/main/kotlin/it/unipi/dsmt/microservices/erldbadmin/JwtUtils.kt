package it.unipi.dsmt.microservices.erldbadmin


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import it.unipi.dsmt.microservices.erldbadmin.service.UserDetailsImpl
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils
import java.util.*


@Component
class JwtUtils {
    @Value("\${puffomeet.app.jwtSecret}")
    private lateinit var jwtSecret: String

    @Value("\${puffomeet.app.jwtExpirationMs}")
    private var jwtExpirationMs: Int = 0

    @Value("\${puffomeet.app.jwtCookieName}")
    private lateinit var jwtCookie: String
    fun getJwtFromCookies(request: HttpServletRequest): String? {
        val cookie = WebUtils.getCookie(request, jwtCookie)
        return cookie?.value
    }

    fun generateJwtCookie(userPrincipal: UserDetailsImpl): ResponseCookie {
        val jwt = generateTokenFromUsername(userPrincipal.username)
        return ResponseCookie.from(jwtCookie, jwt).path("/api").maxAge((24 * 60 * 60).toLong()).httpOnly(true).build()
    }

    fun createCleanJwtCookie(): ResponseCookie {
        return ResponseCookie.from(jwtCookie, null).path("/api").maxAge(0).httpOnly(true).build()
    }

    fun getUserNameFromJwtToken(token: String?): String {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).body.subject
    }

    fun validateJwtToken(authToken: String?): Boolean {
        return try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            true
        } catch (e: Exception) {
            logger.error("Invalid JWT token: {}", e.message)
            false
        }
    }

    private fun generateTokenFromUsername(username: String): String {
        return Jwts.builder().setSubject(username).setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs)).signWith(SignatureAlgorithm.HS512, jwtSecret).compact()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }
}