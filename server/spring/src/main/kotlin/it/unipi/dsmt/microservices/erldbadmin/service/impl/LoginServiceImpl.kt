package it.unipi.dsmt.microservices.erldbadmin.service.impl

import it.unipi.dsmt.microservices.erldbadmin.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LoginServiceImpl @Autowired internal constructor(private val userRepository: UserRepository)  {
     fun createUserIfNotExists(username: String, password: String): Boolean {
        if (userRepository.userExists(username)) {
            return false
        }
        userRepository.createUser(username, password)
        return true
    }

     fun checkUser(username: String, password: String): Boolean {
        return userRepository.checkCredentials(username, password)
    }

     fun changePassword(username: String, oldPassword: String, newPassword: String): Boolean {
        if (!userRepository.checkCredentials(username, oldPassword)) {
            return false
        }
        userRepository.changePassword(username, newPassword)
        return true
    }

     fun banRegularUserIfExists(id: Long): String? {
        TODO("Not yet implemented")
    }
}