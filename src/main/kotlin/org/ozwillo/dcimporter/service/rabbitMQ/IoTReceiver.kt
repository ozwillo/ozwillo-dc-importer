package org.ozwillo.dcimporter.service.rabbitMQ

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.Channel
import java.time.*
import java.time.format.DateTimeFormatter
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.IoTService
import org.ozwillo.dcimporter.util.extractDeviceId
import org.ozwillo.dcimporter.util.toZonedDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono

@Service
class IoTReceiver(
    private val datacoreService: DatacoreService,
    private val datacoreProperties: DatacoreProperties,
    private val ioTService: IoTService,
    private val ioTSender: IoTSender
) {

    private val logger: Logger = LoggerFactory.getLogger(IoTReceiver::class.java)

    @Value("\${datacore.model.iotProject}")
    private val datacoreIotProject: String = "iot_0"

    @Value("\${datacore.model.iotMeasure}")
    private val datacoreIotMeasure: String = "iot:measure_0"

    /*
     * sample data received :
     *   [{"bt": 1555970675.349918, "bn": "8cf9574000000217:FrancK2", "v": 16.54605224609375, "u": "Cel", "n": "temperature"},
     *    {"v": 53.5703125, "u": "%RH", "n": "humidity"},
     *    {"v": 3.42, "u": "%EL", "n": "batteryLevel"},
     *    {"v": 55.0, "u": "dB", "n": "SNR"},
     *    {"v": -68, "u": "dBm", "n": "RSSI"},
     *    {"v": 60, "u": "s", "n": "Period"},
     *    {"v": 43.636197, "u": "lat", "n": "latitude"},
     *    {"v": 6.913232, "u": "lon", "n": "longitude"}]
     *
     *   [{"bn":"84:F3:EB:0C:80:7E","n":"temperature","u":"Cel","v":30.3},
     *    {"n":"humidity","u":"%RH","v":60.5}]
     */
    @RabbitListener(queues = ["iot"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long) {
        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey
        routingByBindingKey(message, routingKey, channel, tag)
    }

    fun routingByBindingKey(message: String, routingKey: String, channel: Channel, tag: Long) {
        logger.debug("Received message $message on routing key $routingKey")

        // Parse the received data into a list of measures
        // (this is how data is received)
        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        val parsedMeasures: List<DeviceMeasure> = try {
            mapper.readValue(
                message,
                mapper.typeFactory.constructCollectionType(
                    List::class.java, DeviceMeasure::class.java
                )
            )
        } catch (e: JsonMappingException) {
            logger.warn("Unable to parse a received measure, ignoring it", e)
            emptyList()
        }

        if (parsedMeasures.isEmpty()) {
            channel.basicAck(tag, false)
            return
        }

        val deviceId = parsedMeasures.find { it.bn.isNotEmpty() }?.bn ?: routingKey.substringAfterLast(".")
        val measureTime = parsedMeasures.find { it.bt.isNotEmpty() }?.bt?.substringBefore(".")?.toZonedDateTime() ?: ZonedDateTime.now()
        val measureTimeAsString = measureTime.format(DateTimeFormatter.ISO_INSTANT)
        val measureLatitude = parsedMeasures.find { it.u.isNotEmpty() && it.u == "lat" }?.v
        val measureLongitude = parsedMeasures.find { it.u.isNotEmpty() && it.u == "lon" }?.v

        val recordableMeasures = parsedMeasures.filter {
            it.u.isNotEmpty() && it.u != "lat" && it.u != "lon"
        }

        recordableMeasures.toMono()
            .zipWith(ioTService.getOrCreateDevice(deviceId, measureLatitude, measureLongitude))
            .flatMapIterable {
                it.t1.map { measure -> Pair(measure, it.t2) }
            }.map {
                val finalIri = "${deviceId.extractDeviceId()}/${it.first.n}/$measureTimeAsString"
                val dcBusinessResource = DCResource(
                    datacoreBaseUri = datacoreProperties.baseResourceUri(),
                    type = datacoreIotMeasure, iri = finalIri
                )

                dcBusinessResource.setStringValue("iotmeasure:deviceId", it.second.getUri())
                dcBusinessResource.setFloatValue("iotmeasure:value", it.first.v)
                dcBusinessResource.setStringValue("iotmeasure:unit", it.first.u)
                dcBusinessResource.setStringValue("iotmeasure:type", it.first.n)
                dcBusinessResource.setDateTimeValue("iotmeasure:time", measureTime.toLocalDateTime())

                val latitude = measureLatitude ?: it.second.getFloatValue("iotdevice:lat")
                val longitude = measureLongitude ?: it.second.getFloatValue("iotdevice:lon")
                latitude?.let { dcBusinessResource.setFloatValue("iotmeasure:lat", it) }
                longitude?.let { dcBusinessResource.setFloatValue("iotmeasure:lon", it) }

                dcBusinessResource
            }.map { dcBusinessResource ->
                ioTSender.send(dcBusinessResource)
                dcBusinessResource
            }.flatMap { dcBusinessResource ->
                datacoreService.saveResource(datacoreIotProject, datacoreIotMeasure, dcBusinessResource, null)
            }.doOnComplete {
                channel.basicAck(tag, false)
            }.doOnError {
                logger.error("Measure recording failed with error $it")
                // Do not bother with failed recordings ...
                channel.basicAck(tag, false)
            }.subscribe()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class DeviceMeasure(
        val bt: String = "",
        val bn: String = "",
        val vs: String = "",
        val v: Float = Float.MIN_VALUE,
        val u: String = "",
        val n: String = ""
    )
}
