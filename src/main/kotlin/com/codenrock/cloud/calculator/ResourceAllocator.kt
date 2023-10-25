package com.codenrock.cloud.calculator

import com.codenrock.cloud.client.price.dto.Price
import com.codenrock.cloud.cluster.Resource

/**
 * Описание целевой нагрузки
 */
data class TargetLoad(val cpu: Double, val ram: Double)

/**
 * Аллокатор ресурсов
 */
interface ResourceAllocator {
    /**
     * Рассчитывает набор ресурсов, оптимальный по цене и покрывающий нагрузку
     *
     * @param perPodLoad Постоянная нагрузка на один под
     * @param requestLoad Нагрузка, вызванная обработкой запросов
     * @param prices Цены на доступные конфигурации
     *
     * @return Список ресурсов
     */
    fun allocate(perPodLoad: TargetLoad, requestLoad: TargetLoad, prices: List<Price>): List<Resource>
}