package io.honeycomb.collections

import io.honeycomb.HoneyConfig

fun <E> MutableCollection<E>.add(element: E, honeyConfig: HoneyConfig): Boolean {
    observe({}, this::class.java.name, honeyConfig, element)
    return add(element)
}

fun <E> MutableCollection<E>.addAll(elements: Collection<E>, honeyConfig: HoneyConfig): Boolean {
    observe({}, this::class.java.name, honeyConfig, elements)
    return addAll(elements)
}

fun <E> MutableCollection<E>.removeAll(elements: Collection<E>, honeyConfig: HoneyConfig): Boolean {
    observe({}, this::class.java.name, honeyConfig, elements)
    return removeAll(elements)
}

fun <E> MutableCollection<E>.remove(element: E, honeyConfig: HoneyConfig): Boolean {
    observe({}, this::class.java.name, honeyConfig, element)
    return remove(element)
}

fun <E> MutableCollection<E>.clear(honeyConfig: HoneyConfig) {
    observe({}, this::class.java.name, honeyConfig, size)
    clear()
}
