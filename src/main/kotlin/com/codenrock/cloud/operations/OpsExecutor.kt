package com.codenrock.cloud.operations

import com.codenrock.cloud.client.resource.ResourceClient
import com.codenrock.cloud.client.resource.dto.ResourceRequest
import com.codenrock.cloud.client.resource.dto.ResourceType
import org.springframework.stereotype.Service

@Service
class OpsExecutor(
    private val resourceClient: ResourceClient,
) {
    fun doOperations(type: ResourceType, operations: List<ResourceOperation>) {
        operations.forEach {
            when (it) {
                is CreateResource -> resourceClient.post(ResourceRequest(it.cpu, it.ram, type))
                is DeleteResource -> resourceClient.delete(it.id)
                is UpdateResource -> resourceClient.put(it.id, ResourceRequest(it.cpu, it.ram, type))
            }
        }
    }

}