package it.unipi.dsmt.microservices.erldbadmin.repository.impl

import it.unipi.dsmt.microservices.erldbadmin.model.Privilege
import it.unipi.dsmt.microservices.erldbadmin.repository.PrivilegeRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository


@Repository
open class PrivilegeRepositoryImpl : PrivilegeRepository {
    private lateinit var entityManager: EntityManager

    @PersistenceContext
    fun setEntityManager(entityManager: EntityManager) {
        this.entityManager = entityManager
    }

  override fun grantFiles(usernames: Array<String>, files: Array<String>): Boolean {
      val queryBody = usernames.flatMap {
          files.map { "SELECT ? AS username, ? AS filename" }
      }.joinToString(separator = " UNION ALL ")
      val finalQuery = "INSERT OR REPLACE INTO privileges (username, filename) SELECT * FROM ($queryBody);"
      return cartesianProductQuery(finalQuery, usernames, files)
  }

    override fun revokeFiles(usernames: Array<String>, files: Array<String>): Boolean {
        val whereClause = usernames.flatMap {
            files.map { "username = ? AND filename = ?" }
        }.joinToString(separator = " OR ")
        val finalQuery = "DELETE FROM privileges WHERE $whereClause;"
        return cartesianProductQuery(finalQuery, usernames, files)
    }

    private fun cartesianProductQuery(query: String, usernames: Array<String>, files: Array<String>): Boolean {
        val entityManagerQuery = entityManager.createNativeQuery(query)
        val parameterPairs = usernames.flatMap { user -> files.map { file -> user to file } }
        parameterPairs.flatMap { it.toList() }
            .forEachIndexed { index, param ->
                entityManagerQuery.setParameter(index + 1, param)
            }
        return entityManagerQuery.executeUpdate() > 0
    }



    override fun checkAccess(username: String, file: String): Boolean {
        val queryParams = HashMap<String, Any>()
        queryParams["username"] = username
        queryParams["file"] = file
        val query = entityManager.createQuery(
            """       
                SELECT count(*) > 0
                FROM Privilege P
                WHERE
                    P.username = :username
                    AND P.filename = :file
            """.trimIndent()
        )

        queryParams.forEach(query::setParameter)

        return query.singleResult as Boolean
    }

    override fun updateAccess(username: String, file: String, access: Boolean): Boolean {
        val queryParams = HashMap<String, Any>()
        queryParams["username"] = username
        queryParams["file"] = file
        if (access) {
            val privilege = Privilege()
            privilege.setUsername(username)
            privilege.setFilename(file)
            try {
                entityManager.persist(privilege)
            } catch (ex: Exception) {
                return false
            }
            return true
        } else {
            val query = entityManager.createQuery(
                "DELETE FROM Privilege p WHERE p.username = :username AND p.filename = :file"
            )
            query.setParameter("username", username)
            query.setParameter("file", file)
            return query.executeUpdate() > 0
        }
    }

    override fun listUserAccess(username: String): List<String> {
        try {
            val queryParams = HashMap<String, Any>()
            queryParams["username"] = username
            val query = entityManager.createQuery(
                """       
                SELECT P.filename
                FROM Privilege P
                WHERE P.username = :username
            """.trimIndent(), String::class.java
            )

            queryParams.forEach(query::setParameter)

            return query.resultList
        } catch (ex: Exception) {
            return emptyList()
        }
    }


    override fun listAccess(sortBy: String): List<Privilege> {
        try {
            val sortOptions = mapOf(
                "username" to "username", "filename" to "filename"
            )
            return sortOptions[sortBy]?.let {
                val queryParams = HashMap<String, Any?>()
                queryParams["order"] = sortOptions[sortBy]
                var query = """       
                    SELECT P.username, P.filename
                    FROM Privilege P
                    ORDER BY :order
                """.trimIndent()

                query = query.trimIndent()
                val q = entityManager.createQuery(query)
                queryParams.forEach(q::setParameter)
                q.resultList as List<Privilege>
            } ?: emptyList()
        } catch (ex: Exception) {
            return emptyList()
        }
    }


    override fun listResourceAccess(bucket: String, filename: String): List<String> {
        try {
            val queryParams = HashMap<String, Any?>()
            queryParams["fullname"] = "$bucket/$filename"
            val query = entityManager.createQuery(
                """       
                SELECT P.username
                FROM Privilege P
                WHERE P.filename = :fullname
            """.trimIndent()
            )

            queryParams.forEach(query::setParameter)

            return query.resultList as List<String>
        } catch (ex: Exception) {
            return emptyList()
        }
    }
}