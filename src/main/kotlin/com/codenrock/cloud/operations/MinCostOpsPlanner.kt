package com.codenrock.cloud.operations

import com.codenrock.cloud.cluster.Resource
import com.codenrock.cloud.cluster.ResourceFact
import com.codenrock.cloud.common.Levenstein
import com.codenrock.cloud.requirements.PodProperties
import org.springframework.stereotype.Service

@Service("minCostOpsPlanner")
class MinCostOpsPlanner(
    private val podProperties: PodProperties
) : OpsPlanner {
    //  предполагаем что прогноз сделан на 5 минут вперед
    override fun makeOperations(fact: List<ResourceFact>, needs: List<Resource>) =
        Levenstein.instructions(fact.sortedByDescending { it.cpu + it.ram },
            needs.sortedByDescending { it.cpu + it.ram },
            { e1, e2 -> e1.cpu == e2.cpu && e1.ram == e2.ram }) {
            when (it) {
                is Levenstein.Op.Add -> podProperties.creatingTimeLag() * it.e.cost
                is Levenstein.Op.Delete -> -podProperties.creatingTimeLag() * it.e.cost
                is Levenstein.Op.Nop -> -10_000_000.0
                is Levenstein.Op.Replace -> podProperties.replacingTimeLag() * it.e1.cost + (podProperties.creatingTimeLag() - podProperties.replacingTimeLag()) * it.e2.cost
            }
        }.ops.mapNotNull {
            when (it) {
                is Levenstein.Op.Add -> CreateResource(it.e.cpu, it.e.ram)
                is Levenstein.Op.Delete -> DeleteResource(it.e.id)
                is Levenstein.Op.Nop -> null
                is Levenstein.Op.Replace -> UpdateResource(it.e1.id, it.e2.cpu, it.e2.ram)
            }
        }
}
