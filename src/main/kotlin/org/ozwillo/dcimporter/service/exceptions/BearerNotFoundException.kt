package org.ozwillo.dcimporter.service.exceptions

import java.lang.RuntimeException

class BearerNotFoundException : RuntimeException() {
    fun toErrorMessage() =
        """
            {
              "message": "Missing Authorization header in request"
            }
        """.trimIndent()
}
