package com.codenrock.cloud.requirements

import org.springframework.stereotype.Component

@Component
class PodProperties {
    companion object {
        private const val REPLACING_TIME_LAG = 3.0
        private const val CREATING_TIME_LAG = 5.0
    }

    fun replacingTimeLag() = REPLACING_TIME_LAG
    fun creatingTimeLag() = CREATING_TIME_LAG
}