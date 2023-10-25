package com.codenrock.cloud.client.resource.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ResourceResponse(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("cost")
    val cost: Int,
    @JsonProperty("cpu")
    val cpu: Int,
    @JsonProperty("cpu_load")
    val cpuLoad: Float,
    @JsonProperty("failed")
    val failed: Boolean,
    @JsonProperty("ram")
    val ram: Int,
    @JsonProperty("ram_load")
    val ramLoad: Float,
    @JsonProperty("type")
    val type: ResourceType,
    @JsonProperty("failed_until")
    val failedUntil: LocalDateTime
)
