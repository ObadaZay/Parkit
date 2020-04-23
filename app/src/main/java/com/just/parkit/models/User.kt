package com.just.parkit.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

// [START user_class]
@IgnoreExtraProperties
data class User(
    var firstName: String? = "",
    var fatherName: String? = "",
    var familyName: String? = "",
    var phoneNumber: String? = "",
    var password: String? = ""
) {

    // [START user_to_map]
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "firstName" to firstName,
            "fatherName" to fatherName,
            "familyName" to familyName,
            "phoneNumber" to phoneNumber,
            "password" to password
        )
    }
    // [END user_to_map]
}
// [END user_class]