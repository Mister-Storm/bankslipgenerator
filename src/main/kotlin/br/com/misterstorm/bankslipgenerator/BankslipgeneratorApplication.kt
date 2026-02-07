package br.com.misterstorm.bankslipgenerator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableAsync
class BankslipgeneratorApplication

fun main(args: Array<String>) {
    runApplication<BankslipgeneratorApplication>(*args)
}
