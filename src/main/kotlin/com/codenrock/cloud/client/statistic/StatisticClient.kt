package com.codenrock.cloud.client.statistic

import com.codenrock.cloud.configuration.ApplicationProperties
import com.codenrock.cloud.configuration.WebClientConfiguration
import com.codenrock.cloud.client.statistic.dto.Statistic
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class StatisticClient(
    private val applicationProperties: ApplicationProperties,
    private val cloudWebClient: WebClient,
) {

    companion object {
        private const val PATH = "/statistic"
        private val log: Logger = LoggerFactory.getLogger(StatisticClient::class.java)
    }

    fun get(): Statistic = runBlocking {
        log.debug("#get: $PATH")
        cloudWebClient.get().uri {
            it.path(PATH).queryParam(WebClientConfiguration.QUERY_TOKEN, applicationProperties.token).build()
        }.retrieve().awaitBody()
    }
}

