package com.pt.ordersystem.ordersystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class OrderSystemApplication

fun main(args: Array<String>) {
	runApplication<OrderSystemApplication>(*args)
}
