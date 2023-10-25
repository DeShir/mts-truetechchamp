package com.codenrock.cloud.requirements

import com.codenrock.cloud.history.History
import com.codenrock.cloud.requirements.impl.LoadRequirementsImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoadRequirementsConfiguration {

    @Bean
    fun dbCpuLoadRequirements(history: History) = LoadRequirementsImpl(
        history,
        History::dbCpuLoad,
        History::podDbCount,
        Params(.05, .001)
    )

    @Bean
    fun dbRamLoadRequirements(history: History) = LoadRequirementsImpl(
        history,
        History::dbRamLoad,
        History::podDbCount,
        Params(.52, .001)
    )

    @Bean
    fun vmCpuLoadRequirements(history: History) = LoadRequirementsImpl(
        history,
        History::vmCpuLoad,
        History::podVmCount,
        Params(.05, .001)
    )

    @Bean
    fun vmRamLoadRequirements(history: History) = LoadRequirementsImpl(
        history,
        History::vmRamLoad,
        History::podVmCount,
        Params(.34, .005)
    )
}