package org.ozwillo.dcimporter.web

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.service.ProcessingStatService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import reactor.core.publisher.Mono

@Component
class ProcessingStatHandler(
    private val processingStatService: ProcessingStatService
) {

    fun getAll(req: ServerRequest): Mono<ServerResponse> {
        return try {
            val processingStats = processingStatService.getAll()
            ok().contentType(MediaType.APPLICATION_JSON).body(processingStats, ProcessingStat::class.java)
        } catch (e: IllegalArgumentException) {
            badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(e.message!!))
        }
    }

    fun getGeneralResume(req: ServerRequest): Mono<ServerResponse> {

        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")
        val zonedDateTime = ZonedDateTime.of(LocalDateTime.now().minusMonths(3), ZoneOffset.UTC)
        val formattedDate = (zonedDateTime.format(df))

        val date = if (req.queryParam("date").isPresent && req.queryParam("date").get().isNotEmpty())
            req.queryParam("date").get() else formattedDate

        return processingStatService.getGeneralStat(date)
            .flatMap { resume ->
                ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(resume))
            }
            .onErrorResume { e ->
                badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(e.message!!))
            }
    }
}
