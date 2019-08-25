package io.honeycomb

/**
 * Global object that holds additional key/value pairs that will be added to an [Event] before
 * it is transmitted.
 */
object GlobalConfig {
    private val fields = mutableMapOf<String, Any>()
    private val dynamicFields = mutableListOf<Lazy<Pair<String, Any>>>()

    fun addField(dynamicField: () -> (Pair<String, Any>)) {
        dynamicFields.add(lazy(dynamicField))
    }

    fun addField(field: String, value: Any) {
        fields[field] = value
    }

    fun applyFields(event: Event): Event {
        return dynamicFields.fold(event) { currentEvent: Event, dynamicField: Lazy<Pair<String, Any>> ->
            val (field, value) = dynamicField.value
            currentEvent.add(field, value)
        }.add(fields)
    }

    fun clearAllFields() {
        fields.clear()
        dynamicFields.clear()
    }
}
