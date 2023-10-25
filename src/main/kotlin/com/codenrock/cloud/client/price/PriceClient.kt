package com.codenrock.cloud.client.price

import com.codenrock.cloud.client.price.dto.Price
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class PriceClient(
    private val cloudWebClient: WebClient
) {
    companion object {
        private const val PRICE_URL = "/price"
        private val log: Logger = LoggerFactory.getLogger(PriceClient::class.java)
    }

    fun list(): List<Price> = runBlocking {
        log.debug("#get list: $PRICE_URL")
        cloudWebClient.get().uri(PRICE_URL).retrieve().awaitBody()
    }
}