package com.codenrock.cloud.client.resource.dto

import com.fasterxml.jackson.annotation.JsonProperty

enum class ResourceType {
    @JsonProperty("db")
    DB,
    @JsonProperty("vm")
    VM;
}