package it.unipi.dsmt.microservices.erldbadmin.controller

import com.erldb.ErldbException
import com.erldb.SQLiteValue
import it.unipi.dsmt.microservices.erldbadmin.JwtUtils
import it.unipi.dsmt.microservices.erldbadmin.dto.erldb.SQLiteValueDTO
import it.unipi.dsmt.microservices.erldbadmin.dto.error.ErrorResponse
import it.unipi.dsmt.microservices.erldbadmin.dto.logged.*
import it.unipi.dsmt.microservices.erldbadmin.service.ErldbService
import it.unipi.dsmt.microservices.erldbadmin.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.*

fun toDto(sqliteValue: SQLiteValue): SQLiteValueDTO {
    // a really ugly way to convert SQLiteValue to SQLiteValueDTO
    return SQLiteValueDTO.fromJSON(sqliteValue.toJSON())
}

fun fromDto(sqliteValueDTO: SQLiteValueDTO): SQLiteValue {
    return SQLiteValue.fromJSON(sqliteValueDTO.toJSON())
}


@RestController
@Scope("session")
@RequestMapping("/v1/logged")
open class LoggedController {
    @Autowired
    private lateinit var userService: UserService


    @Autowired
    private lateinit var erldbService: ErldbService


    @Autowired
    lateinit var jwtUtils: JwtUtils


    @GetMapping("/check-auth")
    open fun AmIAuthenticated(): ResponseEntity<*> {
        return ResponseEntity.ok().body(object {
            val authenticated = "you are authenticated"
        })
    }

    @GetMapping("/lib_version")
    open fun libVersion(): ResponseEntity<*> {
        val libVersion = erldbService.libVersion()
        return ResponseEntity.ok().body(object {
            val version = libVersion
        })
    }


    fun doListUserAccess(request: HttpServletRequest): Result<List<String>?> {
        val authorizationHeader: String? = request.getHeader("Authorization")

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Result.failure(RuntimeException("Missing or invalid Authorization header"))
        }
        val token = authorizationHeader.substring(7)
        val username = jwtUtils.getUserNameFromJwtToken(token)
        if (username == "admin") {
            return Result.success(null)
        }
        val files = userService.listUserAccess(username)
        return Result.success(files)
    }

    @GetMapping("/list_user_access")
    open fun listUserAccess(request: HttpServletRequest): ResponseEntity<*> {
        val result = doListUserAccess(request)
        return result.map {
            ResponseEntity.ok().body(object {
                val files = it
            })
        }.getOrElse {
            ResponseEntity.badRequest().body(it.message)
        }

    }

    fun checkAccess(request: HttpServletRequest, bucket: String, db: String): Boolean {
        val tmp = doListUserAccess(request)
        if (tmp.isFailure) {
            return false
        }
        val value = tmp.getOrNull()
        return value?.contains("$bucket/$db") ?: true
    }


    fun hasAccessOrThrow(request: HttpServletRequest, bucket: String, db: String) {
        if (!checkAccess(request, bucket, db)) {
            throw AccessDeniedException("Access denied")
        }
    }


    @GetMapping("/tables")
    open fun getTables(
        @RequestParam bucket: String, @RequestParam db: String, request: HttpServletRequest
    ): ResponseEntity<TablesResponse> {
        hasAccessOrThrow(request, bucket, db)
        val tables = erldbService.tablesList(bucket, db)
        return ResponseEntity.ok().body(
            TablesResponse(
                tables = tables
            )
        )
    }


    @PostMapping("/query")
    open fun query(
        @RequestBody queryRequest: QueryRequest, request: HttpServletRequest
    ): ResponseEntity<QueryResponse> {
        val (bucket, db, query) = queryRequest
        hasAccessOrThrow(request, bucket, db)
        val (columnNames, rawRows) = erldbService.query(bucket, db, query)
        val rows = rawRows.map { row -> row.map { toDto(it).asArray() } }
        return ResponseEntity.ok().body(
            QueryResponse(
                columnNames = columnNames, rows = rows
            )
        )
    }


    @PostMapping("/statement")
    open fun statement(
        @RequestBody statementRequest: StatementRequest, request: HttpServletRequest
    ): ResponseEntity<StatementResponse> {
        val (bucket, db, statement, par) = statementRequest
        val rawParams = arrayListOf<SQLiteValueDTO>()
        par?.let {
            for (scan in it) {
                SQLiteValueDTO.fromArray(scan).let { rawParams.add(it) }
            }
        }
        hasAccessOrThrow(request, bucket, db)
        val params = rawParams.map { fromDto(it) }
        val (columnNames, rawRows) = erldbService.query(bucket, db, statement, params)
        val rows = rawRows.map { values -> values.map { toDto(it).asArray() } }
        return ResponseEntity.ok().body(
            StatementResponse(
                columnNames = columnNames, rows = rows
            )
        )
    }

    @ExceptionHandler(ErldbException::class)
    open fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        // TODO, this should be handled at lower level
        val msg = e.message?.let {
            if (it.startsWith("rusqlite: ")) {
                it.substring("rusqlite: ".length)
            } else {
                it
            }
        }
        return ResponseEntity.ok().body(ErrorResponse(error = msg))
    }


}
