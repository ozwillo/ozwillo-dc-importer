package org.ozwillo.dcimporter.config

import java.util.ArrayList

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "publik")
class Prop {

    var instance: List<Map<String, String>> = ArrayList()

    override fun toString(): String {
        return "Prop [instance=$instance]"
    }

}
