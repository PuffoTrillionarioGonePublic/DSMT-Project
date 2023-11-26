package com.erldb

/**
 * Represents a one-based column index.
 * @property value The one-based column index value.
 * @throws IllegalArgumentException if the value is less than 1.
 */
class OneColumnIndex {
    val value: Long

    /**
     * Constructs a [OneColumnIndex] instance with the given value.
     * @param value The one-based column index value.
     * @throws IllegalArgumentException if the value is less than 1.
     */
    constructor(value: Long) {
        if (value < 1) {
            throw IllegalArgumentException("column index must be >= 1")
        }
        this.value = value
    }

    /**
     * Constructs a [OneColumnIndex] instance with the given value.
     * @param value The one-based column index value.
     * @throws IllegalArgumentException if the value is less than 1.
     */
    constructor(value: Int) {
        if (value < 1) {
            throw IllegalArgumentException("column index must be >= 1")
        }
        this.value = value.toLong()
    }

    /**
     * Converts this one-based column index to a zero-based column index.
     * @return The corresponding [ZeroColumnIndex] instance.
     */
    fun toZeroColumnIndex(): ZeroColumnIndex {
        return ZeroColumnIndex(value - 1)
    }

    companion object {
        /**
         * Constructs a [OneColumnIndex] instance from the given zero-based column index.
         * @param zeroColumnIndex The zero-based column index.
         * @return The corresponding [OneColumnIndex] instance.
         */
        fun fromZeroColumnIndex(zeroColumnIndex: ZeroColumnIndex): OneColumnIndex {
            return OneColumnIndex(zeroColumnIndex.value + 1)
        }
    }
}