package com.codenrock.cloud.history

import com.codenrock.cloud.client.resource.dto.ResourceResponse
import com.codenrock.cloud.client.resource.dto.ResourceType
import com.codenrock.cloud.client.statistic.dto.Statistic
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset

@Component
class History {

    companion object {
        private val logger = LoggerFactory.getLogger(History::class.java)
        private const val SERIES_SIZE = 100
    }

    private val _requestCount = TimeSeries<Long>(SERIES_SIZE)
    val requestCount = _requestCount.reader()

    private val _dbRamLoad = TimeSeries<Double>(SERIES_SIZE)
    val dbRamLoad = _dbRamLoad.reader()

    private val _dbCpuLoad = TimeSeries<Double>(SERIES_SIZE)
    val dbCpuLoad = _dbCpuLoad.reader()

    private val _vmRamLoad = TimeSeries<Double>(SERIES_SIZE)
    val vmRamLoad = _vmRamLoad.reader()

    private val _vmCpuLoad = TimeSeries<Double>(SERIES_SIZE)
    val vmCpuLoad = _vmCpuLoad.reader()

    private val _podDbCount = TimeSeries<Int>(SERIES_SIZE)
    val podDbCount = _podDbCount.reader()

    private val _podVmCount = TimeSeries<Int>(SERIES_SIZE)
    val podVmCount = _podVmCount.reader()

    private val _online = TimeSeries<Boolean>(1)
    val online = _online.reader()

    private val _offlineTime = TimeSeries<Long>(SERIES_SIZE)
    val offlineTime = _offlineTime.reader()

    private val _costTotal = TimeSeries<Long>(SERIES_SIZE)
    val costTotal = _costTotal.reader()


    fun update(statistic: Statistic, resources: List<ResourceResponse>) {
        logger.info("Update history: statistic.timestamp = ${statistic.timestamp}, resources.size = ${resources.size}")

        val current = Instant.now()

        val (dbCount, vmCount) = resources
            .filter { !it.failed }
            .fold(Pair(0, 0)) { (dbCount, vmCount), resource ->
                when (resource.type) {
                    ResourceType.DB -> dbCount + 1 to vmCount
                    ResourceType.VM -> dbCount to vmCount + 1
                }
            }

        _podDbCount.add(current, dbCount)
        _podVmCount.add(current, vmCount)

        val timestamp = statistic.timestamp.toInstant(ZoneOffset.UTC)
        _requestCount.add(timestamp, statistic.requests)
        _dbRamLoad.add(timestamp, statistic.dbRam * statistic.dbRamLoad / 100.0)
        _dbCpuLoad.add(timestamp, statistic.dbCpu * statistic.dbCpuLoad / 100.0)
        _vmRamLoad.add(timestamp, statistic.vmRam * statistic.vmRamLoad / 100.0)
        _vmCpuLoad.add(timestamp, statistic.vmCpu * statistic.vmCpuLoad / 100.0)
        _online.add(timestamp, statistic.online)
        _offlineTime.add(timestamp, statistic.offlineTime)
        _costTotal.add(timestamp, statistic.costTotal)

        logger.info("Update history done")
    }

    fun logState() {
        logger.info("History state information")
        logger.info("requestCount size: ${_requestCount.size()}; max size: ${_requestCount.maxSize()}")
        logger.info("dbRamLoad size: ${_dbRamLoad.size()}; max size: ${_dbRamLoad.maxSize()}")
        logger.info("dbCpuLoad size: ${_dbCpuLoad.size()}; max size: ${_dbCpuLoad.maxSize()}")
        logger.info("vmRamLoad size: ${_vmRamLoad.size()}; max size: ${_vmRamLoad.maxSize()}")
        logger.info("vmCpuLoad size: ${_vmCpuLoad.size()}; max size: ${_vmCpuLoad.maxSize()}")
        logger.info("podDbCount size: ${_podDbCount.size()}; max size: ${_podDbCount.maxSize()}")
        logger.info("podVmCount size: ${_podVmCount.size()}; max size: ${_podVmCount.maxSize()}")
    }
}
