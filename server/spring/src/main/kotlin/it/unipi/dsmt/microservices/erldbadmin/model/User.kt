package it.unipi.dsmt.microservices.erldbadmin.model

import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import java.sql.Timestamp


@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "credentials")
open class User(
    @Column(name = "username")
    private var username: String = "",
    @Column(name = "password")
    private var password: String = "",
    @Column(name = "isAdmin")
    private var isAdmin: Boolean = false,
    @Column(name = "createdAt")
    private var createdAt: Timestamp = Timestamp(System.currentTimeMillis()),
    ) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var id: Long? = null

    open fun setIsAdmin(isAdmin: Boolean) {
        this.isAdmin = isAdmin
    }

    open fun getIsAdmin(): Boolean {
        return isAdmin
    }

    open fun getUsername(): String {
        return username
    }

    open fun getPassword(): String {
        return password
    }

    open fun getId(): Long? {
        return id
    }

    open fun setUsername(username: String) {
        this.username = username
    }

    open fun setPassword(password: String) {
        this.password = password
    }

    open fun getCreatedAt(): Timestamp {
        return createdAt
    }

    open fun setCreatedAt(createdAt: Timestamp) {
        this.createdAt = createdAt
    }

    open fun setId(id: Long?) {
        this.id = id
    }


}