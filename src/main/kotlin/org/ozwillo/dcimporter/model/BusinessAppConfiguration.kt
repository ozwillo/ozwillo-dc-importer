package org.ozwillo.dcimporter.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class BusinessAppConfiguration(
    @Id val id: String? = null,
    val applicationName: String,
    val displayName: String,
    val baseUrl: String,
    val organizationSiret: String,
    val instanceId: String? = null,
    val login: String? = null,
    val password: String? = null,
    val secretOrToken: String? = null
)
