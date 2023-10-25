package com.codenrock.cloud.requirements.impl

import com.codenrock.cloud.common.LinearSystem
import com.codenrock.cloud.history.History
import com.codenrock.cloud.history.TimeSeriesReader
import com.codenrock.cloud.requirements.LoadRequirements
import com.codenrock.cloud.requirements.Params
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty1

class LoadRequirementsImpl(
    private val history: History,
    private val loadSeries: KProperty1<History, TimeSeriesReader<Double>>,
    private val countSeries: KProperty1<History, TimeSeriesReader<Int>>,
    defaultParams: Params
) : LoadRequirements {

    @Volatile
    private var preCalcParams =  defaultParams

    companion object {
        private val logger = LoggerFactory.getLogger(LoadRequirementsImpl::class.java)
    }

    @PostConstruct
    fun postConstruct() {
        loadSeries(history).subscribe(this::calcOnUpdate)
    }

    @PreDestroy
    fun preDestroy() {
        loadSeries(history).unsubscribe(this::calcOnUpdate)
    }

    private fun calcOnUpdate(series: TimeSeriesReader<Double>) {
        logger.info("Calc load requirements using ${loadSeries.name}")

        val rows2 = series.last2()
        if(rows2 == null) {
            logger.info("Not enough loading data, use calculated params on previous iteration")
            return
        }

        val (p1, p2) = rows2

        logger.debug("Load Row1 $p1")
        logger.debug("Load Row2 $p2")

        val (timestamp1, load1) = p1
        val (timestamp2, load2) = p2

        val podCount1 = countSeries(history).lastBefore(timestamp1)?.toDouble()
        val podCount2 = countSeries(history).lastBefore(timestamp2)?.toDouble()

        logger.debug("Pod Count Row1 $podCount1")
        logger.debug("Pod Count Row2 $podCount2")

        val requestCount1 = history.requestCount.lastBefore(timestamp1)?.toDouble()
        val requestCount2 = history.requestCount.lastBefore(timestamp2)?.toDouble()

        logger.debug("Request Count Row1 $requestCount1")
        logger.debug("Request Count Row2 $requestCount2")

        if(podCount1 == null || podCount2 == null || requestCount1 == null || requestCount2 == null) {
            logger.info("Not enough count data, use calculated params on previous iteration")
            return
        }

        val x = LinearSystem.solve(podCount1, requestCount1, podCount2, requestCount2, load1, load2)

        if(x == null) {
            logger.info("Cannot use existing data for calc, use calculated params on previous iteration")
            return
        }

        val (const, perRequest) = x

        logger.debug("Solving const: $const; perRequest: $perRequest")

        val params = Params(const, perRequest)
        if(!checkForCorrectness(params)) {
            logger.info("It seems that calculations isn't correct: $params, return defaults")
            return

        }

        preCalcParams = params
    }

    private fun checkForCorrectness(params: Params) =
        params.const >= 0 && params.const < 1 && params.perRequest > 0 && params.perRequest < 1

    override fun calc(): Params {
        return preCalcParams
    }
}
