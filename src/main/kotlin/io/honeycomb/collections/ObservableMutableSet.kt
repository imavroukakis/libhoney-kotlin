package io.honeycomb.collections

import io.honeycomb.HoneyConfig

class ObservableMutableSet<T>(
    private val honeyConfig: HoneyConfig,
    private val prefix: String = "traceable-mutable-set",
    private val innerSet: MutableCollection<T> = HashSet()
) : MutableCollection<T> by innerSet {

    override fun add(element: T): Boolean {
        observe({}, prefix, honeyConfig, element)
        return innerSet.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        observe({}, prefix, honeyConfig, elements)
        return innerSet.addAll(elements)
    }

    override fun clear() {
        observe({}, prefix, honeyConfig, size)
        innerSet.clear()
    }

    override fun remove(element: T): Boolean {
        observe({}, prefix, honeyConfig, element)
        return innerSet.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        observe({}, prefix, honeyConfig, elements)
        return innerSet.removeAll(elements)
    }
}
