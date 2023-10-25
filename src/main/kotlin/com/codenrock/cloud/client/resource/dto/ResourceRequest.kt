package com.codenrock.cloud.client.resource.dto

data class ResourceRequest(
    val cpu: Int,
    val ram: Int,
    val type: ResourceType,
)
