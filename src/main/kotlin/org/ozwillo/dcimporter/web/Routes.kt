package org.ozwillo.dcimporter.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(private val statusHandler: StatusHandler,
             private val publikHandler: PublikHandler,
             private val maarchHandler: MaarchHandler,
             private val marchePublicHandler: MarchePublicHandler,
             private val connectorsHandler: ConnectorsHandler,
             private val datacoreHandler: DatacoreHandler) {

    @Bean
    fun router() = router {
        (accept(MediaType.APPLICATION_JSON) and

            "/api").nest {

                "/publik".nest {
                    POST("/{siret}/form", publikHandler::publish)
                }
                "/maarch".nest {
                    PUT("/status", maarchHandler::status)
                }
                "/status".nest {
                    GET("/", statusHandler::status)
                }
                "/marche-public".nest {
                    GET("/{siret}/consultation", marchePublicHandler::getAllConsultationsForSiret)
                    GET("/{siret}/consultation/{reference}", marchePublicHandler::get)
                    POST("/{siret}/consultation", marchePublicHandler::create)
                    PUT("/{siret}/consultation/{reference}", marchePublicHandler::update)
                    DELETE("/{siret}/consultation/{reference}", marchePublicHandler::delete)
                    POST("/{siret}/consultation/{reference}/publish", marchePublicHandler::publish)
                    GET("/{siret}/consultation/{reference}/lot/{uuid}", marchePublicHandler::getLot)
                    POST("/{siret}/consultation/{reference}/lot", marchePublicHandler::createLot)
                    PUT("/{siret}/consultation/{reference}/lot/{uuid}", marchePublicHandler::updateLot)
                    DELETE("/{siret}/consultation/{reference}/lot/{uuid}", marchePublicHandler::deleteLot)
                    GET("/{siret}/consultation/{reference}/piece/{uuid}", marchePublicHandler::getPiece)
                    POST("/{siret}/consultation/{reference}/piece", marchePublicHandler::createPiece)
                    PUT("/{siret}/consultation/{reference}/piece/{uuid}", marchePublicHandler::updatePiece)
                    DELETE("/{siret}/consultation/{reference}/piece/{uuid}", marchePublicHandler::deletePiece)

                    //Registres
                    POST("/{siret}/registre/type/{type}/{reference}", marchePublicHandler::refreshDatacoreRegistreForGivenConsultation)
                    GET("/{siret}/registre/type/{type}/{reference}", marchePublicHandler::getRegistreForConsultation)
                }
        }

        "/dc".nest{
            POST("/type/{type}", datacoreHandler::createResourceWithOrganization)
            PUT("/type/{type}", datacoreHandler::updateResourceWithOrganization)
        }

        "/configuration".nest {
            GET("/{applicationName}/{siret}/connectors", connectorsHandler::getAllByAppName)
            POST("/{applicationName}/{siret}/connectors", connectorsHandler::createNewConnectors)
            PUT("/{applicationName}/{siret}/connectors", connectorsHandler::updateConnectors)
            DELETE("/{applicationName}/{siret}/connectors", connectorsHandler::deleteConnectors)
        }
    }
}