package com.just.parkit.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

// [START user_class]
@IgnoreExtraProperties
data class Park(
    var phoneNumber: String? = "",
    var checkout: Int? = 0,
    var startTime: Int? = 0,
    var status: Int? = 0
) {

    // [START user_to_map]
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "phoneNumber" to phoneNumber,
            "checkout" to checkout,
            "startTime" to startTime,
            "status" to status
            )
    }
    // [END user_to_map]
}
// [END user_class]