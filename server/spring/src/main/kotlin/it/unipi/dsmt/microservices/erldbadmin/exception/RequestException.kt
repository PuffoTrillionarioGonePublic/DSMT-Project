package it.unipi.dsmt.microservices.erldbadmin.exception

abstract class RequestException : Exception {
    abstract val status: Int
    open var error: String = ""

    constructor() {}
    constructor(error: String) {
        this.error = error
    }
}