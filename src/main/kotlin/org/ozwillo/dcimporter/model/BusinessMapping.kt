package org.ozwillo.dcimporter.model

import org.springframework.data.annotation.Id


data class BusinessMapping(
        @Id val id: String? = null,
        val dcId: String,
        val businessId: String,
        var businessId2: String = "",
        val applicationName: String,
        val type: String
)
