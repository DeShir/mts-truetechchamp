package com.codenrock.cloud.calculator.impl

import com.codenrock.cloud.calculator.ResourceAllocator
import com.codenrock.cloud.calculator.TargetLoad
import com.codenrock.cloud.client.price.dto.Price
import com.codenrock.cloud.cluster.Resource
import org.springframework.stereotype.Service

@Service("resourceAllocatorImpl")
class ResourceAllocatorImpl : ResourceAllocator {
    override fun allocate(perPodLoad: TargetLoad, requestLoad: TargetLoad, prices: List<Price>): List<Resource> {
        var cpu = requestLoad.cpu
        val ram = requestLoad.ram

        val ramPrices = prices.groupBy { it.ram }.map {
                SimpleAllocation.makeContainer(
                    it.key - perPodLoad.ram,
                    0.0,
                    it.value.sortedBy { y -> y.cpu }.map { p -> p.id }
                )
            }.sortedBy { it.value }

        val ramConfiguration = SimpleAllocation.allocate(ram, ramPrices).map {it.inner}

        val ids = mutableListOf<Int>()

        // набираем cpu
        for(ramId in ramConfiguration) {
            if(cpu <= 0) {
                ids.add(ramId[0])
            } else {
                val cpuPrices = ramId.map { i -> i to (prices.first { it.id == i }.cpu) - perPodLoad.cpu }
                val ind = cpuPrices.binarySearchBy(cpu) { it.second }
                if(ind > 0 ) {
                    cpu -= cpuPrices[ind].second
                    ids.add(cpuPrices[ind].first)

                } else {
                    val ind2 = -(ind + 1)
                    when (ind2) {
                        0 -> {
                            cpu -= cpuPrices[0].second
                            ids.add(cpuPrices[0].first)

                        }
                        cpuPrices.size -> {
                            cpu -= cpuPrices.last().second
                            ids.add(cpuPrices.last().first)
                        }
                        else -> {
                            // тут можно оптимизировать
                            ids.add(cpuPrices[ind2].first)
                            cpu -= cpuPrices[ind2].second

                        }
                    }
                }
            }

        }

        return ids.map { id ->
            val price = prices.first { it.id == id }
            Resource(price.cpu, price.ram, price.cost)
        }
    }
}