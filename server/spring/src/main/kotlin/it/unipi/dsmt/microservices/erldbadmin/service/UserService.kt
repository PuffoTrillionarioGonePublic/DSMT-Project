package it.unipi.dsmt.microservices.erldbadmin.service

import it.unipi.dsmt.microservices.erldbadmin.model.Privilege
import it.unipi.dsmt.microservices.erldbadmin.model.User
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface UserService {
    fun createUserIfNotExists(username: String, password: String): Boolean
    fun checkUser(username: String, password: String): Boolean
    fun changePassword(username: String, oldPassword: String, newPassword: String): Boolean
    @Modifying
    @Transactional
    fun banRegularUserIfExists(id: Long): Boolean

    fun getUsers(): List<String>
    fun registerRegularUser(username: String, email: String, password: String): Boolean
    fun deleteRegularUserIfExists(id: String): Boolean
    fun grantFiles(usernames: Array<String>, files: Array<String>): Boolean
    fun revokeFiles(usernames: Array<String>, files: Array<String>): Boolean
    fun checkAccess(username: String, file: String): Boolean
    fun updateAccess(username: String, file: String, access: Boolean): Boolean
    fun listUserAccess(username: String): List<String>
    fun listResourceAccess(bucket: String, filename: String): List<String>
    fun listAccess(sortBy: String): List<Privilege>
    fun userExists(username: String): Boolean
    fun createUser(user: User)
}