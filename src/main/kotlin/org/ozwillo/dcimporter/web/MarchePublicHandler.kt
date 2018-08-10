package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.rabbitMQ.Sender
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

@Component
class MarchePublicHandler(private val datacoreProperties: DatacoreProperties,
                          private val datacoreService: DatacoreService,
                          private val applicationProperties: ApplicationProperties,
                          private val sender: Sender) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MarchePublicHandler::class.java)

        private val MP_PROJECT = "marchepublic_0"
        private val ORG_TYPE = "orgfr:Organisation_0"
        private val CONSULTATION_TYPE = "marchepublic:consultation_0"
        private val LOT_TYPE = "marchepublic:lot_0"
        private val PIECE_TYPE = "marchepublic:piece_0"
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        return try {
            val dcConsultation = datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/${req.pathVariable("reference")}", bearer)
            ok().body(BodyInserters.fromObject(Consultation.fromDCObject(dcConsultation)))
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with reference ${req.pathVariable("reference")} does not exist"
                else -> "Unexpected error"
            }

            status(e.statusCode).body(BodyInserters.fromObject(body))
        }
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")

        try {
            datacoreService.getResourceFromURI(MP_PROJECT, ORG_TYPE, "FR/$siret", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Organization with SIRET $siret does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        return req.bodyToMono<Consultation>()
                .flatMap { consultation ->
                    try {
                        datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/${consultation.reference}", bearer)
                    }catch (e:HttpClientErrorException){
                        when{
                            e.statusCode == HttpStatus.NOT_FOUND -> LOGGER.debug("No already existing resource")
                            else -> status(e.statusCode).body(BodyInserters.fromObject("Unexpected error"))
                        }
                    }
                    val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret)
                    datacoreService.saveResource("marchepublic_0", "marchepublic:consultation_0",
                            dcConsultation, bearer)
                }
                .flatMap { result ->
                    val reference = result.resource.getUri().substringAfterLast('/')
                    val resourceUri = "${applicationProperties.url}/api/marche-public/$siret/consultation/$reference"
                    created(URI(resourceUri))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume(this::throwableToResponse)
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, ORG_TYPE, "FR/$siret", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Organization with SIRET $siret does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        try {
            datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/${req.pathVariable("reference")}", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with URI FR/$siret/${req.pathVariable("reference")} does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        return req.bodyToMono<Consultation>()
                .flatMap { consultation ->
                    val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret, req.pathVariable("reference"))
                    datacoreService.updateResource("marchepublic_0", "marchepublic:consultation_0", dcConsultation, bearer)
                }
                .flatMap { _ ->
                    ok().contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.empty<String>())
                }.onErrorResume(this::throwableToResponse)
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        val iri = "FR/${req.pathVariable("siret")}/${req.pathVariable("reference")}"

        return try {
            datacoreService.deleteResource(MP_PROJECT, CONSULTATION_TYPE, iri, bearer)
            status(HttpStatus.NO_CONTENT).body(BodyInserters.fromObject("la consultation ${req.pathVariable("reference")} a été supprimée"))
        }catch (e:HttpClientErrorException){
            return when(e.statusCode){
                HttpStatus.NOT_FOUND -> status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.responseBodyAsString))
                else -> badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.responseBodyAsString))
            }
        }
    }

    fun publish(req: ServerRequest): Mono<ServerResponse>{
        val bearer = extractBearer(req.headers())
        val currentDcResource:DCBusinessResourceLight
        return try {
            currentDcResource = datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/${req.pathVariable("siret")}/${req.pathVariable("reference")}", bearer)
            sender.send(currentDcResource, MP_PROJECT, CONSULTATION_TYPE, BindingKeyAction.PUBLISH)
            ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(currentDcResource))
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with reference ${req.pathVariable("reference")} does not exist or organization with SIRET ${req.pathVariable("siret")}"
                else -> "Unexpected error"
            }
            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }
    }

    fun getLot(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        val reference = req.pathVariable("reference")
        val uuid = req.pathVariable("uuid")
        return try {
            val dcLot = datacoreService.getResourceFromURI(MP_PROJECT, LOT_TYPE, "FR/$siret/$reference/$uuid", bearer)
            ok().body(BodyInserters.fromObject(Lot.toLot(dcLot)))
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Lot with UUID $uuid does not exist"
                else -> "Unexpected error"
            }

            status(e.statusCode).body(BodyInserters.fromObject(body))
        }
    }

    fun createLot(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, ORG_TYPE, "FR/$siret", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Organization with SIRET $siret does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        val reference = req.pathVariable("reference")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/$reference", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with reference $reference does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        return req.bodyToMono<Lot>()
                .flatMap { lot ->
                    val dcLot = lot.toDcObject(datacoreProperties.baseUri, siret, reference)
                    datacoreService.saveResource(MP_PROJECT, LOT_TYPE, dcLot, bearer)
                }
                .flatMap { result ->
                    val lot = result.resource.getUri().substringAfterLast('/')
                    // val notifyResult: Mono<String> = subscriptionService.notifyMock("marchepublic:consultation_0", it)
                    val resourceUri = "${applicationProperties.url}/api/marche-public/$siret/consultation/$reference/lot/$lot"
                    created(URI(resourceUri))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume(this::throwableToResponse)
    }

    fun updateLot(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, ORG_TYPE, "FR/$siret", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Organization with SIRET $siret does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        val reference = req.pathVariable("reference")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/$reference", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with reference $reference does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        val uuid = req.pathVariable("uuid")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, LOT_TYPE, "FR/$siret/$reference/$uuid", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Lot with URI FR/$siret/$reference/$uuid does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        return req.bodyToMono<Lot>()
                .flatMap { lot ->
                    val dcLot = lot.toDcObject(datacoreProperties.baseUri, siret, reference, uuid)
                    datacoreService.updateResource("marchepublic_0", "marchepublic:lot_0", dcLot, bearer)
                }
                .flatMap { _ ->
                    // val notifyResult: Mono<String> = subscriptionService.notifyMock("marchepublic:consultation_0", it)
                    ok().contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume(this::throwableToResponse)
    }

    fun deleteLot(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        val iri = "FR/${req.pathVariable("siret")}/${req.pathVariable("reference")}/${req.pathVariable("uuid")}"

        return try {
            datacoreService.deleteResource("marchepublic_0", "marchepublic:lot_0", iri, bearer)
            ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
        }catch (e:HttpClientErrorException){
            return when(e.statusCode){
                HttpStatus.NOT_FOUND -> status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.responseBodyAsString))
                else -> badRequest().body(BodyInserters.fromObject(e.responseBodyAsString))
            }
        }
    }

    fun getPiece(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        val reference = req.pathVariable("reference")
        val uuid = req.pathVariable("uuid")
        return try {
            val dcPiece = datacoreService.getResourceFromURI(MP_PROJECT, PIECE_TYPE, "FR/$siret/$reference/$uuid", bearer)
            ok().body(BodyInserters.fromObject(Piece.toPiece(dcPiece)))
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Piece with UUID $uuid does not exist"
                else -> "Unexpected error"
            }

            status(e.statusCode).body(BodyInserters.fromObject(body))
        }
    }

    fun createPiece(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, ORG_TYPE, "FR/$siret", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Organization with SIRET $siret does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        val reference = req.pathVariable("reference")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/$reference", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with reference $reference does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        return req.bodyToMono<Piece>()
                .flatMap { piece ->
                    val dcPiece = piece.toDcObject(datacoreProperties.baseUri, siret, reference)
                    datacoreService.saveResource("marchepublic_0", "marchepublic:piece_0",
                            dcPiece, bearer)
                }
                .flatMap { result ->
                    val pieceUuid = result.resource.getUri().substringAfterLast('/')
                    val resourceUri = "${applicationProperties.url}/api/marche-public/$siret/consultation/$reference/piece/$pieceUuid"
                    created(URI(resourceUri))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume(this::throwableToResponse)
    }

    fun updatePiece(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        val siret = req.pathVariable("siret")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, ORG_TYPE, "FR/$siret", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Organization with SIRET $siret does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        val reference = req.pathVariable("reference")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, CONSULTATION_TYPE, "FR/$siret/$reference", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Consultation with reference $reference does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        val uuid = req.pathVariable("uuid")
        try {
            datacoreService.getResourceFromURI(MP_PROJECT, PIECE_TYPE, "FR/$siret/$reference/$uuid", bearer)
        } catch (e: HttpClientErrorException) {
            val body = when(e.statusCode) {
                HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                HttpStatus.NOT_FOUND -> "Piece with reference $uuid does not exist"
                else -> "Unexpected error"
            }

            return status(e.statusCode).body(BodyInserters.fromObject(body))
        }

        return req.bodyToMono<Piece>()
                .flatMap { piece ->
                    val dcPiece = piece.toDcObject(datacoreProperties.baseUri, siret, reference, uuid)
                    datacoreService.updateResource("marchepublic_0", "marchepublic:piece_0", dcPiece, bearer)
                }
                .flatMap { _ ->
                    ok().contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume(this::throwableToResponse)
    }

    fun deletePiece(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        val iri = "FR/${req.pathVariable("siret")}/${req.pathVariable("reference")}/${req.pathVariable("uuid")}"
        return try {
            datacoreService.deleteResource("marchepublic_0", "marchepublic:piece_0", iri, bearer)
            ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
        }catch (e:HttpClientErrorException){
            return when(e.statusCode){
                HttpStatus.NOT_FOUND -> status(404).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.responseBodyAsString))
                else -> badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.responseBodyAsString))
            }
        }
    }

    private fun extractBearer(headers: ServerRequest.Headers): String {
        val authorizationHeader = headers.header("Authorization")
        if (authorizationHeader.isEmpty() || authorizationHeader.size > 1)
            return ""

        return authorizationHeader[0].split(" ")[1]
    }

    private fun throwableToResponse(throwable: Throwable): Mono<ServerResponse> {
        LOGGER.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> badRequest().body(BodyInserters.fromObject(throwable.responseBodyAsString))
            else -> {
                badRequest().body(BodyInserters.fromObject(throwable.message.orEmpty()))
            }
        }

    }
}