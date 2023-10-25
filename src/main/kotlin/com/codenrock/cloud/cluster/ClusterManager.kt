package com.codenrock.cloud.cluster

import com.codenrock.cloud.calculator.ResourceAllocator
import com.codenrock.cloud.calculator.TargetLoad
import com.codenrock.cloud.client.price.PriceClient
import com.codenrock.cloud.client.price.dto.Price
import com.codenrock.cloud.client.resource.ResourceClient
import com.codenrock.cloud.client.resource.dto.ResourceResponse
import com.codenrock.cloud.client.resource.dto.ResourceType.*
import com.codenrock.cloud.history.History
import com.codenrock.cloud.operations.OpsExecutor
import com.codenrock.cloud.operations.OpsPlanner
import com.codenrock.cloud.operations.ResourceOperation
import com.codenrock.cloud.predictor.RequestPredictor
import com.codenrock.cloud.requirements.LoadRequirements
import com.codenrock.cloud.requirements.MaxLoadRequirements
import com.codenrock.cloud.requirements.Params
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.math.ceil

data class Resource(val cpu: Int, val ram: Int, val cost: Int)
data class ResourceFact(val id: Int, val cpu: Int, val ram: Int, val active: Boolean, val cost: Int)

@Service
class ClusterManager(
    private val resourceClient: ResourceClient,
    private val requestPredictor: RequestPredictor,
    private val dbCpuLoadRequirements: LoadRequirements,
    private val dbRamLoadRequirements: LoadRequirements,
    private val vmCpuLoadRequirements: LoadRequirements,
    private val vmRamLoadRequirements: LoadRequirements,
    private val priceClient: PriceClient,
    @Qualifier("resourceAllocatorImpl")
    private val resourceAllocatorImpl: ResourceAllocator,
    @Qualifier("maxAvailabilityOpsPlanner")
    private val onlineOpsPlanner: OpsPlanner,
    @Qualifier("minCostOpsPlanner")
    private val offlineOpsPlanner: OpsPlanner,
    private val executor: OpsExecutor,
    private val maxLoadRequirements: MaxLoadRequirements,
    private val history: History
) {
    companion object {
        private val POD_INIT_TIME_LAG = Duration.ofMinutes(10)
        private val PREDICTION_TIME = Duration.ofMinutes(5)
        private val logger = LoggerFactory.getLogger(ClusterManager::class.java)
        private const val MAX_POD_SIZE = 100
    }

    fun sanitize() {
        val moment = Instant.now().minus(POD_INIT_TIME_LAG)
        val frozenResources = resourceClient.list().filter {
            it.failed && it.failedUntil.toInstant(ZoneOffset.UTC).isBefore(moment)
        }
        logger.info("Found ${frozenResources.size} frozen resources")
        frozenResources.forEach {
            resourceClient.delete(it.id)
        }
    }

    fun manage() {
        val resources = resourceClient.list()

        if (resources.size >= MAX_POD_SIZE) {
            removeAll()
            return
        }

        if (resources.any { it.failed }) {
            logger.info("There are inactive resources, skipping manage phase")
            return
        }

        val prices = priceClient.list()

        val moment = Instant.now().plus(PREDICTION_TIME)
        val requestCount = requestPredictor.predict(moment)
        logger.info("Prediction of request count at $moment is $requestCount")

        values()
            .map { type ->
                logger.info("Managing resources for $type")

                val (ramParams, cpuParams) = when (type) {
                    DB -> dbRamLoadRequirements.calc() to dbCpuLoadRequirements.calc()
                    VM -> vmRamLoadRequirements.calc() to vmCpuLoadRequirements.calc()
                }
                val currentResources = resources.filter { it.type == type }
                val currentPrices = prices.filter { it.type == type }

                type to makeOperations(cpuParams, ramParams, currentPrices, requestCount, currentResources)
            }
            .forEach { (type, operations) ->
                executor.doOperations(type, operations)
            }
    }


    private fun makeOperations(
        cpuParams: Params,
        ramParams: Params,
        prices: List<Price>,
        requestCount: Double,
        currentResources: List<ResourceResponse>
    ): List<ResourceOperation> {
        val requestLoad = TargetLoad(
            ceil(cpuParams.perRequest * requestCount / maxLoadRequirements.safeFactor()),
            ceil(ramParams.perRequest * requestCount / maxLoadRequirements.safeFactor()),
        )

        val perPodLoad = TargetLoad(
            cpuParams.const / maxLoadRequirements.safeFactor(),
            ramParams.const / maxLoadRequirements.safeFactor()
        )

        logger.info("Target Request Load is $requestLoad")

        val predictedResources = resourceAllocatorImpl.allocate(
            perPodLoad, requestLoad, prices
        )

        logger.info("Current resource count: ${currentResources.size}")
        logger.info("Current resource cpu: ${currentResources.sumOf { it.cpu }} ram: ${currentResources.sumOf { it.ram }}")
        logger.info("Predicted resource count: ${predictedResources.size}")
        logger.info("Predicted resource cpu: ${predictedResources.sumOf { it.cpu }} ram: ${predictedResources.sumOf { it.ram }}")

        val planner = if (isOnline()) onlineOpsPlanner else offlineOpsPlanner

        val operations = planner.makeOperations(
            currentResources.map { ResourceFact(it.id, it.cpu, it.ram, !it.failed, it.cost) },
            predictedResources
        )
        logger.info("Planned ${operations.size} operations")

        return operations
    }

    @Suppress("unused")
    fun removeAll() {
        resourceClient.list().forEach {
            resourceClient.delete(it.id)
        }
    }

    fun removeRnd() {
        val resourcesByType = resourceClient.list().groupBy { it.type }

        (resourcesByType.getOrDefault(DB, listOf()).drop(1) + resourcesByType.getOrDefault(VM, listOf()).drop(1))
            .forEach {
                resourceClient.delete(it.id)
            }
    }

    private fun isOnline(): Boolean {
        return history.online.last()?.second ?: true
    }
}

