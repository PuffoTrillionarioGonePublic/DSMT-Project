package it.unipi.dsmt.microservices.erldbadmin.service.impl

import it.unipi.dsmt.microservices.erldbadmin.repository.UserRepository
import it.unipi.dsmt.microservices.erldbadmin.service.UserDetailsImpl.Companion.build
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class UserDetailsServiceImpl : UserDetailsService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val rv = userRepository.getUserFromUsername(username)
            ?: throw UsernameNotFoundException("User Not Found with username: $username")
        return build(rv)
    }
}