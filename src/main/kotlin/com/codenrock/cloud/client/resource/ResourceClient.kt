package com.codenrock.cloud.client.resource

import com.codenrock.cloud.configuration.ApplicationProperties
import com.codenrock.cloud.configuration.WebClientConfiguration.Companion.QUERY_TOKEN
import com.codenrock.cloud.client.resource.dto.ResourceRequest
import com.codenrock.cloud.client.resource.dto.ResourceResponse
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody

@Service
class ResourceClient(
    private val applicationProperties: ApplicationProperties,
    private val cloudWebClient: WebClient,
) {

    companion object {
        private const val PATH = "/resource"
        private val log: Logger = LoggerFactory.getLogger(ResourceClient::class.java)
    }



    fun list(): List<ResourceResponse> = runBlocking {
        log.debug("#get list: $PATH")
        cloudWebClient.get().uri {
            it.path(PATH).queryParam(QUERY_TOKEN, applicationProperties.token).build()
        }.retrieve().awaitBody()
    }

    fun post(resource: ResourceRequest): Unit = runBlocking {
        log.debug("#post: $PATH $resource")
        cloudWebClient.post().uri {
            it.path(PATH).queryParam(QUERY_TOKEN, applicationProperties.token).build()
        }.bodyValue(resource).retrieve().awaitBodilessEntity()
    }

    fun put(id: Int, resource: ResourceRequest): Unit = runBlocking {
        log.debug("#put: $PATH/$id $resource")
        cloudWebClient.put().uri {
            it.path("$PATH/{id}").queryParam(QUERY_TOKEN, applicationProperties.token).build(id)
        }.bodyValue(resource).retrieve().awaitBodilessEntity()
    }

    fun delete(id: Int): Unit = runBlocking {
        log.debug("#delete: $PATH/$id")
        cloudWebClient.delete().uri {
            it.path("$PATH/{id}").queryParam(QUERY_TOKEN, applicationProperties.token).build(id)
        }.retrieve().awaitBodilessEntity()
    }

}