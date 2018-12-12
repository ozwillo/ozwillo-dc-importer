package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.service.ProcessingStatService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
class ProcessingStatHandler(
    private val processingStatService: ProcessingStatService
) {

    fun getAll(req: ServerRequest): Mono<ServerResponse> {
        return try {
            val processingStats = processingStatService.getAll()
            ok().contentType(MediaType.APPLICATION_JSON).body(processingStats, ProcessingStat::class.java)
        }catch (e: IllegalArgumentException){
            badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.message!!))
        }
    }

    fun getGeneralResume(req: ServerRequest): Mono<ServerResponse> {

        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")
        val zonedDateTime = ZonedDateTime.of(LocalDateTime.now().minusMonths(3), ZoneOffset.UTC)
        val formattedDate = (zonedDateTime.format(df))

        val date = if (req.queryParam("date").isPresent && !req.queryParam("date").get().isEmpty())
            req.queryParam("date").get() else formattedDate

        return processingStatService.getGeneralStat(date)
            .flatMap { resume ->
                ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(resume))
            }
            .onErrorResume { e ->
                badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.message!!))
            }
    }

    fun search(req: ServerRequest): Mono<ServerResponse> {

        val date = if (req.queryParam("date").isPresent) req.queryParam("date").get() else ""
        val model = if (req.queryParam("model").isPresent) req.queryParam("model").get() else ""
        val organization = if (req.queryParam("siret").isPresent) req.queryParam("siret").get() else ""
        val action = if (req.queryParam("action").isPresent) req.queryParam("action").get() else ""

        return try {
            val processingStats = processingStatService.searchBy(date, model, organization, action)
            ok().contentType(MediaType.APPLICATION_JSON).body(processingStats, ProcessingStat::class.java)
        }catch (e: IllegalArgumentException){
            badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.message!!))
        }

    }

}