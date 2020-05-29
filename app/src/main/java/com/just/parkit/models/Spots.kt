package com.just.parkit.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

// [START user_class]
@IgnoreExtraProperties
data class Spots(
    var count: Int? = null
) {

    // [START user_to_map]
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "count" to count
            )
    }
    // [END user_to_map]
}
// [END user_class]