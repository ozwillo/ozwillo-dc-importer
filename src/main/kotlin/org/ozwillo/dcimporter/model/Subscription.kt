package org.ozwillo.dcimporter.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Subscription(
        @Id val id: String? = null,
        val model: String,
        val organizationSiret: String,
        val additionalField: String,
        val additionalValue: String,
        val subscriberName: String
)
