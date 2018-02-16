package org.ozwillo.dcimporter.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType

@Configuration
class Routes(private val statusHandler: StatusHandler,
             private val publikHandler: PublikHandler) {

    @Bean
    fun router() = org.springframework.web.reactive.function.server.router {
        (accept(MediaType.APPLICATION_JSON) and "/api").nest {
            "/publik".nest {
                POST("/form", publikHandler::publish)
            }
            "/status".nest {
                GET("/", statusHandler::status)
            }
        }
    }
}