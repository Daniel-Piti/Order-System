package com.pt.ordersystem.ordersystem.jobs

import com.pt.ordersystem.ordersystem.domains.order.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ExpireEmptyOrdersJob(
  private val orderRepository: OrderRepository,
) {

  private val logger = LoggerFactory.getLogger(ExpireEmptyOrdersJob::class.java)

  @Scheduled(fixedDelayString = CommonTimes.ONE_HOUR) // initialDelayString: wait 10s before first run
  @Transactional
  fun updateStatusOfEmptyExpiredOrders() {
    logger.info("RUNNING: updateStatusOfEmptyExpiredOrders")

    val now = LocalDateTime.now()
    val updatedCount = orderRepository.bulkExpireEmptyOrders(currentTime = now, updatedAt = now)

    logger.info("updateStatusOfEmptyExpiredOrders: Order expiration sweep completed | expiredCount = $updatedCount ")
  }
}

