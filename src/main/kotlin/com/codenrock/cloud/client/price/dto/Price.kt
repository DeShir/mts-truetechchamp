package com.codenrock.cloud.client.price.dto

import com.codenrock.cloud.client.resource.dto.ResourceType

data class Price(
    val cost: Int,
    val cpu: Int,
    val id: Int,
    val name: String,
    val ram: Int,
    val type: ResourceType,
)

