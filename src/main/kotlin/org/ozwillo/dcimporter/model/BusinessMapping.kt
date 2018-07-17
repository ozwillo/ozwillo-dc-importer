package org.ozwillo.dcimporter.model

import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class BusinessMapping(
        @Indexed(name= "dcId.index", unique = true)
        val dcId: String,
        val businessId: String,
        val applicationName: String
)
