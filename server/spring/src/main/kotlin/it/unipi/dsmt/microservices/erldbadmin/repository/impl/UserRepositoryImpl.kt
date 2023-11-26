package it.unipi.dsmt.microservices.erldbadmin.repository.impl

import it.unipi.dsmt.microservices.erldbadmin.model.User
import it.unipi.dsmt.microservices.erldbadmin.repository.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.util.logging.Logger

@Repository
open class UserRepositoryImpl : UserRepository {
    private lateinit var entityManager: EntityManager

    @PersistenceContext
    fun setEntityManager(entityManager: EntityManager) {
        this.entityManager = entityManager
    }

    override fun checkCredentials(username: String, password: String): Boolean {
        val queryParams = HashMap<String, Any>()
        queryParams["username"] = username
        queryParams["password"] = password
        val query = entityManager.createQuery(
            """       
                SELECT count(*) > 0
                FROM User U
                WHERE
                    U.username = :username
                    AND U.password = :password
            """.trimIndent()
        )

        queryParams.forEach(query::setParameter)

        return query.singleResult as Boolean
    }

    override fun userExists(username: String): Boolean {
        val queryParams = HashMap<String, Any>()
        queryParams["username"] = username
        val query = entityManager.createQuery(
            """       
                SELECT count(*) > 0
                FROM User U
                WHERE U.username = :username
            """.trimIndent()
        )

        queryParams.forEach(query::setParameter)

        return query.singleResult as Boolean
    }

    override fun userExists(id: Long): Boolean {
        val queryParams = HashMap<String, Any>()
        queryParams["id"] = id

        val query = entityManager.createQuery(
            """       
                SELECT count(*) > 0
                FROM User U
                WHERE U.id = :id
            """.trimIndent()
        )

        queryParams.forEach(query::setParameter)

        return query.singleResult as Boolean
    }

    override fun createUser(username: String, password: String) {
        val user = User(username)
        user.setUsername(username)
        user.setPassword(password)
        entityManager.persist(user)
    }

    override fun createUser(user: User) {
        entityManager.persist(user)
    }

    override fun getUserFromUsername(username: String): User? {
        try {
            val queryParams = HashMap<String, Any>()
            queryParams["username"] = username

            val query = entityManager.createQuery(
                """       
                SELECT U
                FROM User U
                WHERE U.username = :username
            """.trimIndent()
            )

            queryParams.forEach(query::setParameter)

            return query.singleResult as User?
        } catch (e: Exception) {
            return null
        }
    }

    override fun changePassword(username: String, newPassword: String) {
        val user = User(username)
        user.setUsername(username)
        user.setPassword(newPassword)
        entityManager.merge(user)
    }

    override fun isAdmin(id: Long): Boolean {
        val queryParams = HashMap<String, Any>()
        queryParams["id"] = id

        val query = entityManager.createQuery(
            """       
                SELECT U.isAdmin
                FROM User U
                WHERE U.id = :id
            """.trimIndent()
        )

        queryParams.forEach(query::setParameter)

        return query.singleResult as Boolean
    }

    fun getUserFromId(id: Long): User? {
        try {
            val queryParams = HashMap<String, Any>()
            queryParams["id"] = id

            val query = entityManager.createQuery(
                """       
                SELECT U
                FROM User U
                WHERE U.id = :id
            """.trimIndent()
            )
            queryParams.forEach(query::setParameter)
            return query.singleResult as User?
        } catch (e: Exception) {
            return null
        }
    }

    override fun banUser(id: Long): Boolean {
        try {
            val user = getUserFromId(id) ?: return false
            //user.setIsBanned(true)
            entityManager.merge(user)
            return true
        } catch (e: Exception) {
            Logger.getLogger(UserRepositoryImpl::class.java.name).warning(e.message)
            return false
        }
    }

    override fun banUser(user: User): Boolean {
        user.getId()?.let {
            banUser(it)
            return true
        }
        return false
    }

    override fun findById(id: Long): User? {
        return getUserFromId(id)
    }

    override fun getUsers(): List<String> {
        try {
            val queryParams = HashMap<String, Any>()
            queryParams["id"] = 0
            val query = entityManager.createQuery(
                """       
                SELECT U.username
                FROM User U
                WHERE U.id >= :id
            """.trimIndent()
            )
            // TODO without a prepared statement, this does not work
            queryParams.forEach(query::setParameter)
            return query.resultList as List<String>
        } catch (e: Exception) {
            return emptyList()
        }
    }

    override fun delete(username: String) {
        val queryParams = HashMap<String, Any>()
        queryParams["username"] = username

        val query = entityManager.createQuery(
            """       
                DELETE
                FROM User U
                WHERE U.username = :username
            """.trimIndent()
        )

        queryParams.forEach(query::setParameter)

        query.executeUpdate()
    }



}