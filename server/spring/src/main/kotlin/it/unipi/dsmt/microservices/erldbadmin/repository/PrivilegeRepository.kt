package it.unipi.dsmt.microservices.erldbadmin.repository

import it.unipi.dsmt.microservices.erldbadmin.model.Privilege
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PrivilegeRepository {
    @Modifying
    @Transactional
    fun grantFiles(usernames: Array<String>, files: Array<String>): Boolean
    @Modifying
    @Transactional
    fun revokeFiles(usernames: Array<String>, files: Array<String>): Boolean
    fun checkAccess(username: String, file: String): Boolean
    @Modifying
    @Transactional
    fun updateAccess(username: String, file: String, access: Boolean): Boolean
    fun listUserAccess(username: String): List<String>

    fun listAccess(sortBy: String): List<Privilege>
    fun listResourceAccess(bucket: String, filename: String): List<String>
}