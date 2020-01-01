package org.ozwillo.dcimporter.model

import java.time.LocalDateTime
import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class NotificationLog(
    @Id val id: String = UUID.randomUUID().toString(),
    val subscriptionId: String,
    val eventType: String,
    val notificationDate: LocalDateTime,
    val result: Int,
    val errorMessage: String? = null
)
