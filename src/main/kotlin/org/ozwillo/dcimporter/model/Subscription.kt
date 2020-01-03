package org.ozwillo.dcimporter.model

import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Subscription(
    @Id val uuid: String = UUID.randomUUID().toString(),
    val applicationName: String,
    val url: String,
    val secret: String,
    val events: List<String>
)
