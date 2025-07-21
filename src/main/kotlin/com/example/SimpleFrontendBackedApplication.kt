package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleFrontendBackedApplication

fun main(args: Array<String>) {
    runApplication<SimpleFrontendBackedApplication>(*args)
}
