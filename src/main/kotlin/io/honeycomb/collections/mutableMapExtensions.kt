package io.honeycomb.collections

import io.honeycomb.HoneyConfig

fun <K, V> MutableMap<K, V>.put(key: K, value: V, honeyConfig: HoneyConfig): V? {
    observe({}, this::class.java.name, honeyConfig, key to value)
    return put(key, value)
}

operator fun <K, V> MutableMap<K, V>.set(key: K, honeyConfig: HoneyConfig, value: V) {
    observe({}, this::class.java.name, honeyConfig, key to value)
    put(key, value)
}

fun <K, V> MutableMap<K, V>.putAll(from: Map<out K, V>, honeyConfig: HoneyConfig) {
    observe({}, this::class.java.name, honeyConfig, from)
    putAll(from)
}

fun <K, V> MutableMap<K, V>.remove(key: K, honeyConfig: HoneyConfig): V? {
    observe({}, this::class.java.name, honeyConfig, key)
    return remove(key)
}

fun <K, V> MutableMap<K, V>.clear(honeyConfig: HoneyConfig) {
    observe({}, this::class.java.name, honeyConfig, size)
    clear()
}
