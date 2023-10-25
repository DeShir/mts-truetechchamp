package com.codenrock.cloud.predictor

import com.codenrock.cloud.common.Approximation.leastSquaresFun
import com.codenrock.cloud.common.Approximation.leastSquaresFunOpt1
import com.codenrock.cloud.history.History
import com.codenrock.cloud.history.TimeSeriesReader
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RequestPredictor(
    private val history: History
) {
    companion object {
        private const val MIN_REQUEST_COUNT = 500.0
        private const val MAX_REQUEST_COUNT = 22500.0
    }

    @Volatile
    private var predictFun: (timestamp: Double) -> Double = {
        MIN_REQUEST_COUNT
    }

    @PostConstruct
    fun postConstruct() {
        history.requestCount.subscribe(this::calculatePredictFun)
    }

    @PreDestroy
    fun preDestroy() {
        history.requestCount.unsubscribe(this::calculatePredictFun)
    }

    private fun defaultIfNaN(f: (x: Double) -> Double, value: Double): (x: Double) -> Double =
        { x -> f(x).takeIf { it.isFinite() } ?: value }

    private fun calculatePredictFun(series: TimeSeriesReader<Long>) {
        val (x, y) = series.rows()
            .fold(Pair(listOf<Double>(), listOf<Double>())) { (x, y), (timestamp, count) ->
                x + timestamp.epochSecond.toDouble() to y + count.toDouble()
            }

        predictFun = when {
            x.isEmpty() -> { _ -> MIN_REQUEST_COUNT }
            x.size == 1 -> { _ -> y.first() }
            x.size < 4 -> defaultIfNaN(leastSquaresFun(1, x.takeLast(3).toDoubleArray(), y.takeLast(3).toDoubleArray()), y.last())
            x.size < 10 -> defaultIfNaN(leastSquaresFun(2, x.takeLast(3).toDoubleArray(), y.takeLast(3).toDoubleArray()), y.last())
            else -> defaultIfNaN(leastSquaresFunOpt1(x.takeLast(10).toDoubleArray(), y.takeLast(10).toDoubleArray()), y.last())
        }


    }

    fun predict(timestamp: Instant): Double {
        val res = predictFun(timestamp.epochSecond.toDouble())

        if(res < MIN_REQUEST_COUNT) return MIN_REQUEST_COUNT
        if(res > MAX_REQUEST_COUNT) return MAX_REQUEST_COUNT
        return res
    }
}
