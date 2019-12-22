package org.ozwillo.dcimporter.model

import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ProcessingStat(
    @Id val id: String? = null,
    val model: String,
    val organization: String?,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val action: String
)
