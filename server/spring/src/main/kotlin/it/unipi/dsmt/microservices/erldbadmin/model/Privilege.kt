package it.unipi.dsmt.microservices.erldbadmin.model

import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter


@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "privileges")
open class Privilege(
    @Column(name = "username")
    private var username: String = "",
    @Column(name = "filename")
    private var filename: String = "",
    ) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private var id: Long? = null

    open fun getUsername(): String {
        return username
    }

    open fun getFilename(): String {
        return filename
    }
    open fun getId(): Long? {
        return id
    }

    open fun setUsername(username: String) {
        this.username = username
    }

    open fun setFilename(filename: String) {
        this.filename = filename
    }
}
