package com.codenrock.cloud.requirements

import org.springframework.stereotype.Component

@Component
class MaxLoadRequirements {
    companion object {
        const val SAFE_FACTOR = 0.8
    }

    fun safeFactor() = SAFE_FACTOR
}
