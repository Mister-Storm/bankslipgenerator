package br.com.misterstorm.bankslipgenerator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BankslipgeneratorApplication

fun main(args: Array<String>) {
    runApplication<BankslipgeneratorApplication>(*args)
}
