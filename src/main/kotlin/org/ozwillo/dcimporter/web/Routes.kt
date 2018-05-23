package org.ozwillo.dcimporter.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(private val statusHandler: StatusHandler,
             private val publikHandler: PublikHandler,
             private val maarchHandler: MaarchHandler,
             private val marchePublicHandler: MarchePublicHandler) {

    @Bean
    fun router() = router {
        (accept(MediaType.APPLICATION_JSON) and "/api").nest {
            "/publik".nest {
                POST("/form", publikHandler::publish)
            }
            "/maarch".nest {
                PUT("/status", maarchHandler::status)
            }
            "/status".nest {
                GET("/", statusHandler::status)
            }
            "/marche-public".nest {
                POST("/{siret}/consultation", marchePublicHandler::create)
            }
        }
    }
}