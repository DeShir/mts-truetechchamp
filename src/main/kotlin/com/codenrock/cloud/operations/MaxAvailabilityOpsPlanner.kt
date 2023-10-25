package com.codenrock.cloud.operations

import com.codenrock.cloud.common.Levenstein
import com.codenrock.cloud.cluster.Resource
import com.codenrock.cloud.cluster.ResourceFact
import org.springframework.stereotype.Service

@Service("maxAvailabilityOpsPlanner")
class MaxAvailabilityOpsPlanner : OpsPlanner {
    //  предполагаем что прогноз сделан на 5 минут вперед
    override fun makeOperations(fact: List<ResourceFact>, needs: List<Resource>) : List<ResourceOperation> {
        // TODO вынести код в абстрактный класс
        @Suppress("DuplicatedCode")
        val ops = Levenstein.instructions(
            fact.sortedByDescending { it.cpu + it.ram },
            needs.sortedByDescending { it.cpu + it.ram },
            { e1, e2 -> e1.cpu == e2.cpu && e1.ram == e2.ram }) {
            when (it) {
                is Levenstein.Op.Add -> 1.0
                is Levenstein.Op.Delete -> 1.0
                is Levenstein.Op.Nop -> -10_000_000.0
                is Levenstein.Op.Replace -> 10.0
            }
        }.ops.mapNotNull {
            when (it) {
                is Levenstein.Op.Add -> CreateResource(it.e.cpu, it.e.ram)
                is Levenstein.Op.Delete -> DeleteResource(it.e.id)
                is Levenstein.Op.Nop -> null
                is Levenstein.Op.Replace -> UpdateResource(it.e1.id, it.e2.cpu, it.e2.ram)
            }
        }

        return if(ops.any { it is CreateResource}) {
            ops.filterIsInstance<CreateResource>()
        } else {
            ops
        }
    }

}
