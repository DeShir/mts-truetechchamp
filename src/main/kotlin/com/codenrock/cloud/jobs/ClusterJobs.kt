package com.codenrock.cloud.jobs

import com.codenrock.cloud.cluster.ClusterManager
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Управление кластером
 */
@Component
class ClusterJobs(
    private val clusterManager: ClusterManager,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ClusterManager::class.java)
    }

    @Scheduled(fixedRate = 2 * 30, timeUnit = TimeUnit.SECONDS, initialDelay = 5)
    fun doManagingJob() {
        logger.info("#task: Cluster managing start")
        clusterManager.manage()
        logger.info("#task: Cluster managing finish")
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 0)
    fun doSanitizingJob() {
        logger.info("#task: Cluster sanitizing start")
        clusterManager.sanitize()
        logger.info("#task: Cluster sanitizing finish")
    }


    //@Scheduled(fixedRate = 5 * 60, timeUnit = TimeUnit.SECONDS, initialDelay = 10 * 60)
    fun removeMonkey() {
        logger.info("#task: Remove monkey start")
        clusterManager.removeRnd()
        logger.info("#task: Remove monkey finish")
    }
}