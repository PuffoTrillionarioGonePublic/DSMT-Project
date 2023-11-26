package it.unipi.dsmt.microservices.erldbadmin.controller

import it.unipi.dsmt.microservices.erldbadmin.dto.admin.AccessUpdateRequest
import it.unipi.dsmt.microservices.erldbadmin.dto.admin.BucketGetResponse
import it.unipi.dsmt.microservices.erldbadmin.dto.admin.GrantRequest
import it.unipi.dsmt.microservices.erldbadmin.dto.admin.UserResponse
import it.unipi.dsmt.microservices.erldbadmin.exception.TransparentException
import it.unipi.dsmt.microservices.erldbadmin.service.ErldbFilesystemService
import it.unipi.dsmt.microservices.erldbadmin.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@Scope("session")
@RequestMapping("/v1/admin")
open class AdminController {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var erldbFilesystemService: ErldbFilesystemService

    @GetMapping("/users")
    open fun getUsers(): ResponseEntity<UserResponse> {
        val users = this.userService.getUsers()
        return ResponseEntity.ok(UserResponse(users = users))
    }


    @GetMapping("/bucket")
    open fun getBuckets(): ResponseEntity<*> {
        val buckets = erldbFilesystemService.listBuckets()
        return ResponseEntity.ok().body(object {
            val buckets = buckets
        })
    }

    @GetMapping("/bucket/{bucket}")
    open fun getFiles(@PathVariable bucket: String): ResponseEntity<*> {
        val files = erldbFilesystemService.listFiles(bucket)
        return ResponseEntity.ok().body(
            BucketGetResponse(files = files)
        )
    }

    @DeleteMapping("/bucket/{bucket}/{file}")
    open fun deleteFile(@PathVariable bucket: String, @PathVariable file: String): ResponseEntity<*> {
        erldbFilesystemService.deleteFile(bucket, file)
        return ResponseEntity.ok().body(object {
            val message = "file deleted"
        })
    }

    @DeleteMapping("/bucket/{bucket}")
    open fun deleteBucket(@PathVariable bucket: String): ResponseEntity<*> {
        erldbFilesystemService.deleteBucket(bucket)
        return ResponseEntity.ok().body(object {
            val message = "bucket deleted"
        })
    }


    @GetMapping("/test")
    open fun dummy(): ResponseEntity<*> {
        return ResponseEntity.ok().body(object {
            val message = "you are an admin"
        })
    }


    @PostMapping("/grant")
    open fun grantFiles(@RequestBody grantRequest: GrantRequest): ResponseEntity<*> {
        val (usernames, files) = grantRequest
        for (file in files) {
            if (!file.contains("/")) {
                throw TransparentException("invalid file name")
            }
        }
        if (!userService.grantFiles(usernames, files)) throw TransparentException("can't grant files")

        return ResponseEntity.ok().body(object {
            val message = "files granted"
        })
    }


    @PostMapping("/revoke_access")
    fun revokeAccess(@RequestBody grantRequest: GrantRequest): ResponseEntity<*> {
        val (usernames, files) = grantRequest
        if (!userService.revokeFiles(usernames, files)) {
            throw TransparentException("can't revoke files")
        }
        return ResponseEntity.ok().body(object {
            val message = "files revoked"
        })


    }

    @GetMapping("/check_access")
    fun checkAccess(@RequestParam username: String, @RequestParam file: String): ResponseEntity<*> {
        if (!userService.checkAccess(username, file)) {
            throw TransparentException("user doesn't have access")
        }
        return ResponseEntity.ok().body(object {
            val message = "user has access"
        })
    }

    @PutMapping("/update_access")
    fun updateAccess(@RequestBody accessUpdateRequest: AccessUpdateRequest): ResponseEntity<*> {
        val (username, file, access) = accessUpdateRequest
        if (!userService.updateAccess(username, file, access)) {
            throw TransparentException("can't update access")
        }
        return ResponseEntity.ok().body(object {
            val message = "access updated"
        })
    }

    @GetMapping("/list_user_access")
    fun listUserAccess(@RequestParam username: String): ResponseEntity<*> {
        val files = userService.listUserAccess(username)
        return ResponseEntity.ok().body(object {
            val files = files
        })
    }

    @GetMapping("/list_resource_access")
    fun listResourceAccess(@RequestParam bucket: String, @RequestParam resource: String): ResponseEntity<*> {
        val users = userService.listResourceAccess(bucket, resource)
        val tmp = HashMap<String, List<String>>()
        return ResponseEntity.ok().body(object {
            val users = users
        })
    }


    @GetMapping("/register")
    open fun register(
        @RequestParam username: String, @RequestParam email: String, @RequestParam password: String
    ): ResponseEntity<*> {
        if (!userService.registerRegularUser(
                username, email, password
            )
        ) throw TransparentException("can't register user")
        return ResponseEntity.ok().body(object {
            val message = "user registered"
        })
    }


    @GetMapping("/list_access")
    fun listAccess(@RequestParam sortBy: String): ResponseEntity<*> {
        val access = userService.listAccess(sortBy)
        return ResponseEntity.ok().body(object {
            val access = access
        })
    }


    @DeleteMapping("/users/{username}")
    open fun deleteUser(@PathVariable username: String): ResponseEntity<*> {
        if (!userService.deleteRegularUserIfExists(username)) throw TransparentException("can't delete user")
        return ResponseEntity.ok().body(object {
            val message = "user deleted"
        })

    }

}