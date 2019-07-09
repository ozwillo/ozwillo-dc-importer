package org.ozwillo.dcimporter.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class ProcessingStat(
    @Id val id: String? = null,
    val model: String,
    val organization: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val action: String
)