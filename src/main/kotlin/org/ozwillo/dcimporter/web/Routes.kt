package org.ozwillo.dcimporter.web

import java.net.URI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(
    private val publikHandler: PublikHandler,
    private val maarchHandler: MaarchHandler,
    private val connectorsHandler: ConnectorsHandler,
    private val datacoreHandler: DatacoreHandler,
    private val dataAccessRequestHandler: DataAccessRequestHandler,
    private val processingStatHandler: ProcessingStatHandler
) {

    @Bean
    fun router() = router {
        accept(MediaType.TEXT_HTML).nest {
            GET("/") { ServerResponse.permanentRedirect(URI("/index.html")).build() }
        }

        (accept(MediaType.APPLICATION_JSON) and "/api").nest {

            "/data-access".nest {
                POST("", dataAccessRequestHandler::create)
                PUT("/{id}/{action}", dataAccessRequestHandler::update) // action = valid (push "Valider" button, update state to VALIDATED) or reject (push "Annuler" button, update state to REFUSED) or send (in case of pre-filled form sent to validation) or save (in case or updating pre-filled form)
                GET("", dataAccessRequestHandler::get)
                GET("/{id}", dataAccessRequestHandler::dataAccess)
            }

            "/stat-view".nest {
                GET("/processing", processingStatHandler::getAll)
                GET("/processing-resume", processingStatHandler::getGeneralResume)
            }

            "/publik".nest {
                POST("/{siret}/form", publikHandler::publish)
            }
            "/maarch".nest {
                PUT("/status", maarchHandler::status)
            }
        }

        "/dc".nest {
            POST("/type/{type}", datacoreHandler::createResource)
            PUT("/type/{type}", datacoreHandler::updateResource)
            DELETE("/type/{type}/{iri}", datacoreHandler::deleteResource)
            GET("/type/{type}", datacoreHandler::searchResources)
            GET("/organization", datacoreHandler::getOrganization)
            GET("/models", datacoreHandler::getModels)
            GET("/model/{type}", datacoreHandler::getModel)
        }

        "/configuration".nest {
            GET("/connectors", connectorsHandler::getAllWithoutSecret)
            GET("/connectors/{id}", connectorsHandler::get)
            POST("/connectors/{id}/clone", connectorsHandler::clone)
            DELETE("/connectors/{id}", connectorsHandler::delete)
            GET("/{applicationName}/{siret}/connectors", connectorsHandler::getAllByAppName)
            POST("/{applicationName}/{siret}/connectors", connectorsHandler::createNewConnectors)
            PUT("/{applicationName}/{siret}/connectors", connectorsHandler::updateConnectors)
            DELETE("/{applicationName}/{siret}/connectors", connectorsHandler::deleteBySiretAndAppName)
        }
    }
}
