package it.unipi.dsmt.microservices.erldbadmin


import it.unipi.dsmt.microservices.erldbadmin.service.impl.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter


private const val BEARER_AUTHORIZATION_HEADER_STARTER = "Bearer "


class AuthTokenFilter : OncePerRequestFilter() {

    @Autowired
    lateinit var jwtUtils: JwtUtils

    @Autowired
    lateinit var userDetailsService: UserDetailsServiceImpl

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        request.getHeader("Authorization")?.let {
            if (it.startsWith(BEARER_AUTHORIZATION_HEADER_STARTER)) {
                val jwtToken = it.substring(BEARER_AUTHORIZATION_HEADER_STARTER.length)
                if (jwtUtils.validateJwtToken(jwtToken)) {
                    authenticate(jwtToken, request)
                }
            }
        }
        filterChain.doFilter(request, response)
    }


    private fun authenticate(jwtToken: String, request: HttpServletRequest) {
        val username = jwtUtils.getUserNameFromJwtToken(jwtToken)
        val userDetails = userDetailsService.loadUserByUsername(username)
        if (!userDetails.isEnabled) {
            throw DisabledException("User is disabled")
        }
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )

        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }


    private fun parseJwt(request: HttpServletRequest): String? {
        return jwtUtils.getJwtFromCookies(request)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthTokenFilter::class.java)
    }
}

