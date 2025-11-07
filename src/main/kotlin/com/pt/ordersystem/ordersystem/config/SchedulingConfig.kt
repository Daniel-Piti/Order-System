package com.pt.ordersystem.ordersystem.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Configuration
class SchedulingConfig : SchedulingConfigurer {

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    val scheduler = ThreadPoolTaskScheduler().apply {
      poolSize = 5
      setThreadNamePrefix("scheduler-")
      isRemoveOnCancelPolicy = true
      initialize()
    }
    taskRegistrar.setTaskScheduler(scheduler)
  }
}

