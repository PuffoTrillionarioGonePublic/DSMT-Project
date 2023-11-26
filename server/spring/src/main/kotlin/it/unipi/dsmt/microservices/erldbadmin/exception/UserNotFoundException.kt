package it.unipi.dsmt.microservices.erldbadmin.exception

class UserNotFoundException(error: String) : RequestException(error) {
    override val status: Int
        get() = 404

}
