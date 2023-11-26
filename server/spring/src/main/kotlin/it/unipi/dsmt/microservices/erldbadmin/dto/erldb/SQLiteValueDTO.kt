package it.unipi.dsmt.microservices.erldbadmin.dto.erldb

/**
 *
 * This file contains the definition of the `SQLiteValue` class, which represents a value that can be stored in a SQLite database.
 * The class is a sealed class with five subclasses: `Null`, `Integer`, `Real`, `Text`, and `Blob`.
 * Each subclass represents a different type of value that can be stored in a SQLite database.
 * The `SQLiteValue` class provides methods to convert the value to different types, such as `into()` and `toAny()`.
 * It also provides methods to convert the value to and from JSON format, such as `fromJSON()` and `toJSON()`.
 */


import org.json.JSONArray
import org.json.JSONException
import java.lang.ClassCastException
import java.sql.*
import java.time.LocalDateTime
import java.util.*



sealed class SQLiteValueDTO {
    data class Null(val value: Any?) : SQLiteValueDTO()
    data class Integer(val value: Long) : SQLiteValueDTO()
    data class Real(val value: Double) : SQLiteValueDTO()
    data class Text(val value: String) : SQLiteValueDTO()
    data class Blob(val value: ByteArray) : SQLiteValueDTO() {
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



    fun asArray(): Array<Any?> {
        return when (this) {
            is Null -> arrayOf(0 as Any, null as Any?)
            is Integer -> arrayOf(1, value)
            is Real -> arrayOf(2, value)
            is Text -> arrayOf(3, value)
            is Blob -> arrayOf(4, value)
        }
    }


    fun toAny(): Any? = when (this) {
        is Null -> null
        is Integer -> value
        is Real -> value
        is Text -> value
        is Blob -> value
    }


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

        fun fromArray(arr: Array<Any?>): SQLiteValueDTO {
            return when (arr[0]) {
                0 -> Null(arr[1])
                1 -> Integer((arr[1] as Int).toLong())
                2 -> Real(when (arr[1]) {
                    is Int -> (arr[1] as Int).toDouble()
                    is Long -> (arr[1] as Long).toDouble()
                    is Float -> (arr[1] as Float).toDouble()
                    is Double -> (arr[1] as Double)
                    else -> throw ClassCastException("Cannot cast ${arr[1]} to Double")
                })
                3 -> Text(arr[1] as String)
                4 -> Blob(Base64.getDecoder().decode(arr[1] as String))
                else -> throw ClassCastException("Unknown type for SQLiteValue")
            }
        }

        fun from(value: Any?): SQLiteValueDTO {
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

        fun fromJSON(json: JSONArray): SQLiteValueDTO {
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