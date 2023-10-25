package com.codenrock.cloud.client.statistic.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Statistic(
    @JsonProperty("availability")
    val availability: Double,
    @JsonProperty("cost_total")
    val costTotal: Long,
    @JsonProperty("db_cpu")
    val dbCpu: Long,
    @JsonProperty("db_cpu_load")
    val dbCpuLoad: Double,
    @JsonProperty("db_ram")
    val dbRam: Long,
    @JsonProperty("db_ram_load")
    val dbRamLoad: Double,
    @JsonProperty("last1")
    val last1: Long,
    @JsonProperty("last15")
    val last15: Long,
    @JsonProperty("last5")
    val last5: Long,
    @JsonProperty("last_day")
    val lastDay: Long,
    @JsonProperty("last_hour")
    val lastHour: Long,
    @JsonProperty("last_week")
    val lastWeek: Long,
    @JsonProperty("offline_time")
    val offlineTime: Long,
    @JsonProperty("online")
    val online: Boolean,
    @JsonProperty("online_time")
    val onlineTime: Long,
    @JsonProperty("requests")
    val requests: Long,
    @JsonProperty("requests_total")
    val requestsTotal: Long,
    @JsonProperty("response_time")
    val responseTime: Long,
    @JsonProperty("timestamp")
    val timestamp: LocalDateTime,
    @JsonProperty("user_name")
    val userName: String,
    @JsonProperty("vm_cpu")
    val vmCpu: Long,
    @JsonProperty("vm_cpu_load")
    val vmCpuLoad: Double,
    @JsonProperty("vm_ram")
    val vmRam: Long,
    @JsonProperty("vm_ram_load")
    val vmRamLoad: Double
)
