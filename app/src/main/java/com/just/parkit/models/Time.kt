package com.just.parkit.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

// [START user_class]
@IgnoreExtraProperties
data class Time(
    var timestamp: Int? = null
) {

    // [START user_to_map]
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            ".sv" to timestamp
            )
    }
    // [END user_to_map]
}
// [END user_class]