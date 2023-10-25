package com.codenrock.cloud.calculator.impl

object SimpleAllocation {
    interface Container<T> {
        val value: Double
        val cost: Double
        val inner: T
    }

    fun <T> makeContainer(value: Double, cost: Double, inner: T) = object : Container<T> {
        override val value: Double
            get() = value
        override val cost: Double
            get() = cost
        override val inner: T
            get() = inner

    }


    fun makeContainer(value: Double, cost: Double, inner: List<Int>) = object : Container<List<Int>> {
        override val value: Double
            get() = value
        override val cost: Double
            get() = cost
        override val inner: List<Int>
            get() = inner

    }

    fun allocate(value: Double, list: List<Container<List<Int>>>): List<Container<List<Int>>> {
        if (list.isEmpty()) {
            return listOf()
        }

        val index = list.binarySearchBy(value) { it.value }

        if (index >= 0) {
            return listOf(list[index])
        }

        val index1 = -(1 + index)

        if (index1 < list.size) {
            return listOf(list[index1])
        }

        val n = (value / list[index1 - 1].value).toInt()
        return List(n) { list[index1 - 1] } + allocate(value - n * list[index1 - 1].value, list.dropLast(1))

    }
}
