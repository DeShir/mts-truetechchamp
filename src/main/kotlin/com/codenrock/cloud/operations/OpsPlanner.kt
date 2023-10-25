package com.codenrock.cloud.operations

import com.codenrock.cloud.cluster.Resource
import com.codenrock.cloud.cluster.ResourceFact

sealed interface ResourceOperation

data class CreateResource(val cpu: Int, val ram: Int) : ResourceOperation
data class UpdateResource(val id: Int, val cpu: Int, val ram: Int) : ResourceOperation
data class DeleteResource(val id: Int) : ResourceOperation
interface OpsPlanner {
    fun makeOperations(fact: List<ResourceFact>, needs: List<Resource>): List<ResourceOperation>
}