package it.unipi.dsmt.microservices.erldbadmin.controller

import org.springframework.context.annotation.Scope
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@Scope("session")
@RequestMapping("/v1/health")
open class HealthController {
    @GetMapping("/")
    open fun dummy(): ResponseEntity<*> {
        return ResponseEntity
            .ok()
            .body(object {
                val health = "it works"
            })

    }
}