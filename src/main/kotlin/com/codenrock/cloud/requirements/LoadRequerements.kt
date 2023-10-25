package com.codenrock.cloud.requirements

data class Params(val const: Double, val perRequest: Double)

interface LoadRequirements {
    fun calc(): Params
}
