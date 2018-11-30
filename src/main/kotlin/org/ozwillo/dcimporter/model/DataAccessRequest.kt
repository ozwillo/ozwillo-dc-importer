package org.ozwillo.dcimporter.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class DataAccessRequest(
    @Id val id: String? = null,
    val nom: String,
    val model: String,
    val organization: String,
    val email: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var state: AccessRequestState = AccessRequestState.SAVED
)

enum class AccessRequestState{
    SAVED,
    SENT,
    VALIDATED,
    REFUSED
}