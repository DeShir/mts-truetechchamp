package com.codenrock.cloud.operations

import com.codenrock.cloud.cluster.Resource
import com.codenrock.cloud.cluster.ResourceFact
import com.codenrock.cloud.common.Levenstein
import com.codenrock.cloud.requirements.PodProperties
import org.springframework.stereotype.Service

@Service("maxRatingOpsPlanner")
class MaxRatingOpsPlanner(
    private val podProperties: PodProperties
) : OpsPlanner {
    companion object {
        private const val OFFLINE_WEIGHT = 0.7
        private const val COST_WEIGHT = 0.3 / 100.0
    }

    private fun weight(t: Double, a: Double, c: Int) =
        t * (a * OFFLINE_WEIGHT + COST_WEIGHT * c)

    //  предполагаем что прогноз сделан на 5 минут вперед
    override fun makeOperations(fact: List<ResourceFact>, needs: List<Resource>): List<ResourceOperation> {

        val isLoadGrow = needs.sumOf { it.cpu + it.ram } > fact.sumOf { it.cpu + it.ram }

        @Suppress("DuplicatedCode") val ops = Levenstein.instructions(fact.sortedByDescending { it.cpu + it.ram },
            needs.sortedByDescending { it.cpu + it.ram },
            { e1, e2 -> e1.cpu == e2.cpu && e1.ram == e2.ram }) {
            when (it) {
                is Levenstein.Op.Add -> weight(podProperties.creatingTimeLag(), 0.0, it.e.cost)
                is Levenstein.Op.Delete -> if (isLoadGrow)
                    weight(podProperties.creatingTimeLag(), 1.0, -it.e.cost)
                else
                    weight(podProperties.creatingTimeLag(), 0.0, -it.e.cost)

                is Levenstein.Op.Replace ->
                    weight(podProperties.replacingTimeLag(), 1.0, it.e1.cost) +
                            weight(with(podProperties) { creatingTimeLag() - replacingTimeLag() }, 1.0, it.e2.cost)

                is Levenstein.Op.Nop -> -10_000_000.0
            }
        }.ops.mapNotNull {
            when (it) {
                is Levenstein.Op.Add -> CreateResource(it.e.cpu, it.e.ram)
                is Levenstein.Op.Delete -> DeleteResource(it.e.id)
                is Levenstein.Op.Nop -> null
                is Levenstein.Op.Replace -> UpdateResource(it.e1.id, it.e2.cpu, it.e2.ram)
            }
        }

        return if (ops.any { it is CreateResource }) {
            ops.filterIsInstance<CreateResource>()
        } else {
            ops
        }
    }

}
