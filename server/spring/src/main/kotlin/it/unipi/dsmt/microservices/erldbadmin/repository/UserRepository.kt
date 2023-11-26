package it.unipi.dsmt.microservices.erldbadmin.repository

import it.unipi.dsmt.microservices.erldbadmin.model.User
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserRepository {
    fun checkCredentials(username: String, password: String): Boolean

    fun userExists(username: String): Boolean

    fun userExists(id: Long): Boolean

    @Modifying
    @Transactional
    fun createUser(username: String, password: String)

    @Modifying
    @Transactional
    fun createUser(user: User)

    fun getUserFromUsername(username: String): User?

    @Modifying
    @Transactional
    fun changePassword(username: String, newPassword: String)
    fun isAdmin(id: Long): Boolean

    @Modifying
    @Transactional
    fun banUser(id: Long): Boolean

    @Modifying
    @Transactional
    fun banUser(user: User): Boolean

    fun findById(id: Long): User?

    fun getUsers(): List<String>
    fun delete(username: String)

}