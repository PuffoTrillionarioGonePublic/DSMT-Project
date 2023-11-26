package it.unipi.dsmt.microservices.erldbadmin.service

import com.fasterxml.jackson.annotation.JsonIgnore
import it.unipi.dsmt.microservices.erldbadmin.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class UserDetailsImpl(
    val id: Long?,
    private val username: String,
    val email: String?,
    @field:JsonIgnore private val password: String,
    private val isBanned: Boolean,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {


    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return !isBanned
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null || javaClass != other.javaClass)
            return false
        val user = other as UserDetailsImpl
        return id == user.id
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + username.hashCode()
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + password.hashCode()
        result = 31 * result + authorities.hashCode()
        return result
    }

    companion object {
        private const val serialVersionUID = 1L

        @JvmStatic
        fun build(user: User): UserDetailsImpl {

            val authorities = listOf(user.getIsAdmin())
                .map { if (it) "ADMIN" else "USER" }
                .map { SimpleGrantedAuthority("ROLE_$it") }

            return UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                //user.getEmail(),
                "",
                user.getPassword(),
                //user.getIsBanned(),
                false,
                authorities
            )
        }
    }
}