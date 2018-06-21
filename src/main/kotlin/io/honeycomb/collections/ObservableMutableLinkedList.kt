package io.honeycomb.collections

import io.honeycomb.HoneyConfig
import java.util.LinkedList

class ObservableMutableLinkedList<T>(
    private val honeyConfig: HoneyConfig,
    private val prefix: String = "traceable-mutable-list",
    private val innerList: MutableCollection<T> = LinkedList()
) : MutableCollection<T> by innerList {

    override fun add(element: T): Boolean {
        observe({}, prefix, honeyConfig, element)
        return innerList.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        observe({}, prefix, honeyConfig, elements)
        return innerList.addAll(elements)
    }

    override fun clear() {
        observe({}, prefix, honeyConfig, size)
        innerList.clear()
    }

    override fun remove(element: T): Boolean {
        observe({}, prefix, honeyConfig, element)
        return innerList.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        observe({}, prefix, honeyConfig, elements)
        return innerList.removeAll(elements)
    }
}
