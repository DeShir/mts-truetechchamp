package com.codenrock.cloud.jobs

import com.codenrock.cloud.history.History
import com.codenrock.cloud.client.resource.ResourceClient
import com.codenrock.cloud.client.statistic.StatisticClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


/**
 * Сбор данный для расчетов
 */
@Component
class HistoryJob(
    private val history: History,
    private val resourceClient: ResourceClient,
    private val statisticClient: StatisticClient,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(HistoryJob::class.java)
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS, initialDelay = 0)
    fun doCollectionJob() {
        logger.info("#task: History collection start")

        val resources = resourceClient.list()
        val statistic = statisticClient.get()

        history.update(statistic, resources)
        history.logState()
        logger.info("#task: History collection finish")
    }
}