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
                PUT("/{siret}/consultation/{reference}", marchePublicHandler::update)
                DELETE("/{siret}/consultation/{reference}", marchePublicHandler::delete)
                POST("/{siret}/consultation/{reference}/lot", marchePublicHandler::createLot)
                PUT("/{siret}/consultation/{reference}/lot/{uuid}", marchePublicHandler::updateLot)
                DELETE("/{siret}/consultation/{reference}/lot/{uuid}", marchePublicHandler::deleteLot)
                POST("/{siret}/consultation/{reference}/piece", marchePublicHandler::createPiece)
                PUT("/{siret}/consultation/{reference}/piece/{uuid}", marchePublicHandler::updatePiece)
                DELETE("/{siret}/consultation/{reference}/piece/{uuid}", marchePublicHandler::deletePiece)
            }
        }
    }
}