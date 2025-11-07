package com.pt.ordersystem.ordersystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class OrderSystemApplication

fun main(args: Array<String>) {
	runApplication<OrderSystemApplication>(*args)
}
