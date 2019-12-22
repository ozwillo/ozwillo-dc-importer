package org.ozwillo.dcimporter.model.maarch

import com.fasterxml.jackson.annotation.JsonProperty

data class MaarchContact(
    val lastname: String,
    val firstname: String,
    val contactType: Int,
    val isCorporatePerson: String,
    val email: String,
    val contactPurposeId: Int
    /*,
    val addressStreet: String,
    val addressZip: String,
    val addressTown: String,
    val Phone: String*/
) {

    // Needed because either that field is not serialized into the JSON data
    // Maybe it is due to the leading 'is' ?
    @JsonProperty("isCorporatePerson")
    fun getCorporatePersonJson(): String = isCorporatePerson
}
