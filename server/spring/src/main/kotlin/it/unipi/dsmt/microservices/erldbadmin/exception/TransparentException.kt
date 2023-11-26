package it.unipi.dsmt.microservices.erldbadmin.exception


/// This is a transparent exception, it is not handled by the application
/// and it is used to return a message to the client.
class TransparentException (override val message: String) : Exception(message)