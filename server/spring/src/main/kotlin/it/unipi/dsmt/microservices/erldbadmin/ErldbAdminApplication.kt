package it.unipi.dsmt.microservices.erldbadmin

import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.InitBinder


@SpringBootApplication
open class ErldbAdminApplication {

    @InitBinder
    open fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
    }
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ErldbAdminApplication::class.java, *args)
        }
    }
}