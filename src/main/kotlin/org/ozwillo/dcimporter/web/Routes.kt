package org.ozwillo.dcimporter.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.net.URI
import org.springframework.core.io.ClassPathResource

@Configuration
class Routes(
    private val statusHandler: StatusHandler,
    private val publikHandler: PublikHandler,
    private val maarchHandler: MaarchHandler,
    private val marchePublicHandler: MarchePublicHandler,
    private val connectorsHandler: ConnectorsHandler,
    private val datacoreHandler: DatacoreHandler,
    private val dataAccessRequestHandler: DataAccessRequestHandler
) {

    @Bean
    fun router() = router {
        accept(MediaType.TEXT_HTML).nest {
            GET("/") { ServerResponse.permanentRedirect(URI("/index.html")).build() }
        }

        (accept(MediaType.APPLICATION_JSON) and

                "/api").nest {

            "/data_access_request".nest {
                POST("/{siret}/{request}", dataAccessRequestHandler::create)    //request = save for SAVED state, send for SENT state (meaning sent to validation task)
                PUT("/{siret}/{action}/{id}", dataAccessRequestHandler::update)     //action = valid (push "Valider" button, update state to VALIDATED) or reject (push "Annuler" button, update state to REFUSED) or send (in case of pre-filled form sent to validation) or save (in case or updating pre-filled form)
                GET("/{siret}/model", dataAccessRequestHandler::getModels)
                GET("/{siret}/{state}", dataAccessRequestHandler::get)  //state = saved (list of saved pre-filled form) or sent (list of elements waiting for validation)
                GET("/{id}", dataAccessRequestHandler::dataAccess)
            }

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
                GET("/{siret}/registre/{reference}/{type}", marchePublicHandler::getRegistreForConsultation)
                GET(
                    "/{siret}/registre/{reference}/retrait/resume",
                    marchePublicHandler::getRegistreRetraitResumeForConsultation
                )
            }
        }

        "/dc".nest {
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