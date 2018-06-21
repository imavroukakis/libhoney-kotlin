package io.honeycomb.collections

import io.honeycomb.HoneyConfig

class ObservableMutableMap<K, V>(
    private val honeyConfig: HoneyConfig,
    private val prefix: String = "traceable-mutable-map",
    private val innerMap: MutableMap<K, V> = HashMap()
) : MutableMap<K, V> by innerMap {

    override fun put(key: K, value: V): V? {
        observe({}, prefix, honeyConfig, key to value)
        return innerMap.put(key, value)
    }

    operator fun set(key: K, value: V) {
        observe({}, this::class.java.name, honeyConfig, key to value)
        put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        observe({}, prefix, honeyConfig, from)
        return innerMap.putAll(from)
    }

    override fun clear() {
        observe({}, prefix, honeyConfig, size)
        innerMap.clear()
    }

    override fun remove(key: K): V? {
        observe({}, prefix, honeyConfig, key)
        return innerMap.remove(key)
    }
}
