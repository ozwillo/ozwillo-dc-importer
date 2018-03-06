package org.ozwillo.dcimporter.model.publik

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class PublikConfiguration(
        @Id val id: String? = null,
        val domain: String,
        val organizationName: String,
        val secret: String)
