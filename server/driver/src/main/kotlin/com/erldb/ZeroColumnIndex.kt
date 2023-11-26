package com.erldb

/**
 * A wrapper over a long, that makes it explicit that it is a column index.
 * @property value The value of the column index.
 * @constructor Creates a [ZeroColumnIndex] instance with the given [value].
 * @throws IllegalArgumentException if [value] is less than 0.
 */
class ZeroColumnIndex {
    val value: Long

    constructor(value: Long) {
        if (value < 0) {
            throw IllegalArgumentException("column index must be >= 0")
        }
        this.value = value
    }

    constructor(value: Int) {
        if (value < 0) {
            throw IllegalArgumentException("column index must be >= 0")
        }
        this.value = value.toLong()
    }

    /**
     * Converts this [ZeroColumnIndex] to a [OneColumnIndex] by adding 1 to its [value].
     * @return A new [OneColumnIndex] instance.
     */
    fun toOneColumnIndex(): OneColumnIndex {
        return OneColumnIndex(value + 1)
    }

    // impl everything that a long can do
    companion object {
        /**
         * Creates a [ZeroColumnIndex] instance from a [OneColumnIndex] by subtracting 1 from its [OneColumnIndex.value].
         * @param oneColumnIndex The [OneColumnIndex] to convert.
         * @return A new [ZeroColumnIndex] instance.
         */
        fun fromOneColumnIndex(oneColumnIndex: OneColumnIndex): ZeroColumnIndex {
            return ZeroColumnIndex(oneColumnIndex.value - 1)
        }

        /**
         * Creates a [ZeroColumnIndex] instance from a raw [OneColumnIndex] value by subtracting 1 from it.
         * @param oneColumnIndex The raw [OneColumnIndex] value to convert.
         * @return A new [ZeroColumnIndex] instance.
         */
        fun fromRawOneColumnIndex(oneColumnIndex: Long): ZeroColumnIndex {
            return ZeroColumnIndex(oneColumnIndex - 1)
        }

        /**
         * Creates a [ZeroColumnIndex] instance from a raw [OneColumnIndex] value by subtracting 1 from it.
         * @param oneColumnIndex The raw [OneColumnIndex] value to convert.
         * @return A new [ZeroColumnIndex] instance.
         */
        fun fromRawOneColumnIndex(oneColumnIndex: Int): ZeroColumnIndex = fromRawOneColumnIndex(oneColumnIndex.toLong())
    }

    // implement operator arithmetic
    operator fun plus(other: ZeroColumnIndex): ZeroColumnIndex = ZeroColumnIndex(value + other.value)
    operator fun plus(other: Long): ZeroColumnIndex = ZeroColumnIndex(value + other)
    operator fun plus(other: Int): ZeroColumnIndex = ZeroColumnIndex(value + other)
    operator fun minus(other: ZeroColumnIndex): ZeroColumnIndex = ZeroColumnIndex(value - other.value)
    operator fun times(other: ZeroColumnIndex): ZeroColumnIndex = ZeroColumnIndex(value * other.value)
    operator fun div(other: ZeroColumnIndex): ZeroColumnIndex = ZeroColumnIndex(value / other.value)
    operator fun rem(other: ZeroColumnIndex): ZeroColumnIndex = ZeroColumnIndex(value % other.value)
    operator fun unaryMinus(): ZeroColumnIndex = ZeroColumnIndex(-value)
    operator fun inc(): ZeroColumnIndex = ZeroColumnIndex(value + 1)
    operator fun dec(): ZeroColumnIndex = ZeroColumnIndex(value - 1)

}