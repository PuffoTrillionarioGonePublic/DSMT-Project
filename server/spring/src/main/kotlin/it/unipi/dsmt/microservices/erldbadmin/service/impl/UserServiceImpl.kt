package it.unipi.dsmt.microservices.erldbadmin.service.impl

import it.unipi.dsmt.microservices.erldbadmin.exception.ProtectionException
import it.unipi.dsmt.microservices.erldbadmin.exception.UserNotFoundException
import it.unipi.dsmt.microservices.erldbadmin.model.Privilege
import it.unipi.dsmt.microservices.erldbadmin.model.User
import it.unipi.dsmt.microservices.erldbadmin.repository.PrivilegeRepository
import it.unipi.dsmt.microservices.erldbadmin.repository.UserRepository
import it.unipi.dsmt.microservices.erldbadmin.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
open class UserServiceImpl : UserService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var privilegeRepository: PrivilegeRepository

    override fun createUserIfNotExists(username: String, password: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun checkUser(username: String, password: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun changePassword(username: String, oldPassword: String, newPassword: String): Boolean {
        TODO("Not yet implemented")
    }



    override fun banRegularUserIfExists(id: Long): Boolean {
        val user = userRepository.findById(id)
        return when {
            user != null -> {
                when {
                    !user.getIsAdmin() -> {
                        userRepository.banUser(user)
                        true
                    }

                    else -> {
                        throw ProtectionException("can't ban an admin")
                    }
                }

            }

            else -> {
                throw UserNotFoundException("user not found")
            }
        }
    }

    override fun getUsers(): List<String> {
        return this.userRepository.getUsers()
    }

    override fun registerRegularUser(username: String, email: String, password: String): Boolean {
        return try {
            userRepository.createUser(username, password)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteRegularUserIfExists(username: String): Boolean {
        return try {
            userRepository.delete(username)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun grantFiles(usernames: Array<String>, files: Array<String>): Boolean =
        privilegeRepository.grantFiles(usernames, files)

    override fun revokeFiles(usernames: Array<String>, files: Array<String>): Boolean =
        privilegeRepository.revokeFiles(usernames, files)

    override fun checkAccess(username: String, file: String): Boolean {
        //if (file.startsWith("private")) {
        //    return false
        //}
        return privilegeRepository.checkAccess(username, file)
    }

    override fun updateAccess(username: String, file: String, access: Boolean): Boolean =
        privilegeRepository.updateAccess(username, file, access)

    override fun listUserAccess(username: String): List<String> = privilegeRepository.listUserAccess(username)

    override fun listResourceAccess(bucket: String, filename: String): List<String> = privilegeRepository.listResourceAccess(bucket, filename)


    override fun listAccess(sortBy: String): List<Privilege> = privilegeRepository.listAccess(sortBy)
    override fun userExists(username: String): Boolean {
        return userRepository.userExists(username)
    }

    override fun createUser(user: User) {
        userRepository.createUser(user)
    }
}