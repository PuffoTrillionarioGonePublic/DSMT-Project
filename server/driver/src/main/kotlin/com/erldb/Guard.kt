package com.erldb

/**
 * A synchronization guard that allows executing a function in a thread-safe manner.
 * @property lock The lock object used for synchronization.
 */
class Guard {
    val lock: Any

    constructor(lock: Any) {
        this.lock = lock
    }

    /**
     * Executes the given function in a thread-safe manner.
     * @param f The function to execute.
     * @return The result of the function.
     */
    fun <T> execute(f: () -> T): T = synchronized(this.lock) { f() }
}