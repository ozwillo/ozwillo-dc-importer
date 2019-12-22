package org.ozwillo.dcimporter.model

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class DataAccessRequest(
    @Id val id: String? = null,
    val nom: String,
    val model: String,
    val organization: String,
    val email: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var state: AccessRequestState = AccessRequestState.SAVED,
    val fields: List<DataAccessRequestField>?
)

data class DataAccessRequestField(val name: String, val requested: Boolean)

enum class AccessRequestState {
    SAVED,
    SENT,
    VALIDATED,
    REFUSED
}
