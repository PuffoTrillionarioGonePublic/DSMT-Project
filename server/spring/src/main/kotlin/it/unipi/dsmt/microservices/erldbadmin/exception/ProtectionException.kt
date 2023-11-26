package it.unipi.dsmt.microservices.erldbadmin.exception

class ProtectionException(error: String) : RequestException(error) {
    override val status: Int
        get() = 403

}
