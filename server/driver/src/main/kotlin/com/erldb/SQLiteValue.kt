/**
 *
 * This file contains the definition of the `SQLiteValue` class, which represents a value that can be stored in a SQLite database.
 * The class is a sealed class with five subclasses: `Null`, `Integer`, `Real`, `Text`, and `Blob`.
 * Each subclass represents a different type of value that can be stored in a SQLite database.
 * The `SQLiteValue` class provides methods to convert the value to different types, such as `into()` and `toAny()`.
 * It also provides methods to convert the value to and from JSON format, such as `fromJSON()` and `toJSON()`.
 */
package com.erldb


import org.json.JSONArray
import org.json.JSONException
import java.sql.*
import java.time.LocalDateTime
import java.util.*



sealed class SQLiteValue {
    data class Null(val value: Any?) : SQLiteValue()
    data class Integer(val value: Long) : SQLiteValue()
    data class Real(val value: Double) : SQLiteValue()
    data class Text(val value: String) : SQLiteValue()
    data class Blob(val value: ByteArray) : SQLiteValue() {
        override fun equals(other: Any?): Boolean {
            return when {
                this === other -> true
                javaClass != other?.javaClass -> false
                else -> {
                    other as Blob
                    value.contentEquals(other.value)
                }
            }
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    fun toAny(): Any? = when (this) {
        is Null -> null
        is Integer -> value
        is Real -> value
        is Text -> value
        is Blob -> value
    }

    /**
     * Converts the SQLiteValue to the specified type [T].
     * Returns null if the SQLiteValue is Null.
     * Throws SQLException if the specified type is not supported.
     *
     * @return The converted value of type [T].
     */

    inline fun <reified T> into(): T? {
        return when (this) {
            is Null -> {
                null
            }
            is Integer -> {
                when {
                    T::class == Byte::class -> value.toByte() as T
                    T::class == Short::class -> value.toShort() as T
                    T::class == Int::class -> value.toInt() as T
                    T::class == Long::class -> value as T
                    else -> throw SQLException("Unsupported type: ${T::class.java.name}")
                }
            }

            is Real -> {
                when {
                    T::class == Float::class -> value.toFloat() as T
                    T::class == Double::class -> value as T
                    else -> throw SQLException("Unsupported type: ${T::class.java.name}")
                }
            }

            is Text -> {
                when {
                    T::class == String::class -> value as T
                    T::class == CharArray::class -> value.toCharArray() as T
                    T::class == Array<Char>::class -> value.toCharArray() as T
                    else -> throw SQLException("Unsupported type: ${T::class.java.name}")
                }
            }

            is Blob -> {
                when {
                    T::class == ByteArray::class -> value as T
                    T::class == Array<Byte>::class -> value.toTypedArray() as T
                    else -> throw SQLException("Unsupported type: ${T::class.java.name}")
                }
            }
        }
    }

    companion object {
        /**
         * Converts a value of any type to an SQLiteValue.
         *
         * @param value The value to convert.
         * @return The converted SQLiteValue.
         * @throws SQLException if the type of the value is not supported.
         */
        fun from(value: Any?): SQLiteValue {
            return when (value) {
                null -> Null(null)
                is Boolean -> Integer(if (value) 1 else 0)
                is Byte -> Integer(value.toLong())
                is Short -> Integer(value.toLong())
                is Int -> Integer(value.toLong())
                is Long -> Integer(value)
                is Float -> Real(value.toDouble())
                is Double -> Real(value)
                is String -> Text(value)
                is ByteArray -> Blob(value)
                is LocalDateTime -> Text(value.toString())
                else -> throw SQLException("Unsupported type: ${value.javaClass.name}")
            }
        }

        /**
         * Parses a JSONArray and returns a corresponding SQLiteValue object.
         *
         * @param json the JSONArray to parse
         * @return the corresponding SQLiteValue object
         * @throws JSONException if the data type is unknown
         */
        fun fromJSON(json: JSONArray): SQLiteValue {
            val dataType = json.getInt(0)
            return when (dataType) {
                0 -> Null(null)
                1 -> Integer(json.getLong(1))
                2 -> Real(json.getDouble(1))
                3 -> Text(json.getString(1))
                4 -> {
                    val decoded = Base64.getDecoder().decode(json.getString(1))
                    Blob(decoded)
                }

                else -> throw JSONException("Unknown type for SQLiteValue")
            }
        }
    }
    /**
     * Converts the SQLiteValue object to a JSONArray, where first index is type and second index is the value
     * 
     * @return JSONArray representing the SQLiteValue object.
     */
    fun toJSON(): JSONArray {
        val arr = JSONArray()
        when (this) {
            is Null -> {
                arr.put(0)
                arr.put("null")
            }

            is Integer -> {
                arr.put(1)
                arr.put(this.value)
            }

            is Real -> {
                arr.put(2)
                arr.put(this.value)
            }

            is Text -> {
                arr.put(3)
                arr.put(this.value)
            }

            is Blob -> {
                arr.put(4)
                arr.put(Base64.getEncoder().encodeToString(this.value))
            }
        }
        return arr
    }

}