package com.codenrock.cloud.history

import java.time.Instant
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

typealias UpdateConsumer<T> = (reader: TimeSeriesReader<T>) -> Unit

interface TimeSeriesReader<T> {
    fun rows(): List<Pair<Instant, T>>

    fun lastRows(count: Int): List<Pair<Instant, T>>
    fun last2(): Pair<Pair<Instant, T>, Pair<Instant, T>>?

    fun last(): Pair<Instant, T>?

    fun lastBefore(timestamp: Instant): T?

    fun subscribe(consumer: UpdateConsumer<T>)

    fun unsubscribe(consumer: UpdateConsumer<T>)
}

class TimeSeries<T>(private val maxSize: Int) : TimeSeriesReader<T> {

    private val lock = ReentrantReadWriteLock(true)

    private val rows = mutableListOf<Pair<Instant, T>>()

    private val subscribers = mutableSetOf<UpdateConsumer<T>>()

    constructor() : this(50)

    fun size() = rows.size

    fun maxSize() = maxSize

    fun add(timestamp: Instant, value: T) = lock.write {
        if (rows.isEmpty()) {
            rows.add(Pair(timestamp, value))
            subscribers.forEach { it(this) }
        } else if (rows.last().second != value) {

            val index = rows.binarySearchBy(timestamp) { it.first }
            if (index >= 0 && rows[index].second != value) {
                rows[index] = Pair(timestamp, value)
            } else if (index < 0) {
                rows.add(-(index + 1), Pair(timestamp, value))
            }

            if (rows.size > maxSize) {
                rows.removeFirst()
            }

            subscribers.forEach { it(this) }
        }
        return@write
    }

    fun reader(): TimeSeriesReader<T> = this
    override fun rows() = lock.read { rows.toList() }

    override fun lastRows(count: Int) = lock.read { rows.takeLast(count) }

    override fun last2(): Pair<Pair<Instant, T>, Pair<Instant, T>>? = lock.read {
        val rows = lastRows(2)
        if (rows.size != 2) {
            return null
        }
        return Pair(rows[0], rows[1])
    }

    override fun last(): Pair<Instant, T>? = rows.lastOrNull()

    override fun lastBefore(timestamp: Instant): T? = lock.read {
        val index = rows.binarySearchBy(timestamp) { it.first }

        return when {
            index >= 0 -> return rows[index].second
            -(index + 1) == 0 -> null
            else -> rows[-(index + 1) - 1].second
        }
    }

    override fun subscribe(consumer: UpdateConsumer<T>) {
        subscribers.add(consumer)
    }

    override fun unsubscribe(consumer: UpdateConsumer<T>) {
        subscribers.remove(consumer)
    }
}