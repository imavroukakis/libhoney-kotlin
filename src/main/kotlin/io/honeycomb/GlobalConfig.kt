package io.honeycomb

/**
 * Global object that holds additional key/value pairs that will be added to an [Event] before
 * it is transmitted.
 */
object GlobalConfig {
    private val fields = mutableMapOf<String, Any>()
    private val dynamicFields = mutableListOf<() -> Pair<String, Any>>()

    fun addDynamicField(dynamicField: () -> (Pair<String, Any>)) {
        dynamicFields.add(dynamicField)
    }

    fun addField(field: String, value: Any) {
        fields[field] = value
    }

    fun applyDynamicFields(event: Event): Event {
        return dynamicFields.fold(event, { currentEvent, newEvent ->
            val (field, value) = newEvent()
            currentEvent.add(field, value)
        })
    }

    fun applyFields(event: Event): Event {
        return event.add(fields)
    }
}
