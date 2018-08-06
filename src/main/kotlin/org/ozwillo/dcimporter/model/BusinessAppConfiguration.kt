package org.ozwillo.dcimporter.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class BusinessAppConfiguration(
        @Id val id: String? = null,
        val applicationName: String,
        val domain: String,
        val organizationName: String,
        val secret: String)
