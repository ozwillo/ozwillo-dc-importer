package org.ozwillo.dcimporter.service.rabbitMQ

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.service.DatacoreService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import java.time.*
import java.time.format.DateTimeFormatter


@Service
class IoTReceiver(private val datacoreService: DatacoreService, private val datacoreProperties: DatacoreProperties) {

    private val logger: Logger = LoggerFactory.getLogger(IoTReceiver::class.java)

    @Value("\${datacore.model.iotProject}")
    private val datacoreIotProject: String = "iot_0"

    @Value("\${datacore.model.iotModel}")
    private val datacoreIotModel: String = "iot:device_0"

    @Value("\${amqp.config.iot.baseBindingKey}")
    private val baseBindingKey: String = ""

    /*
     * sample data received :
     *   [{"bt": 1548582039.971496, "bn": "8cf9574000000217:FrancK2", "v": 6.786213378906247, "u": "Cel", "n": "temperature"},
     *    {"v": 66.265625, "u": "%RH", "n": "humidity"},
     *    {"v": 3.3000000000000003, "u": "%EL", "n": "batteryLevel"},
     *    {"v": 57.25, "u": "dB", "n": "SNR"},
     *    {"v": -63, "u": "dBm", "n": "RSSI"},
     *    {"v": 60, "u": "s", "n": "Period"}]
     *
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
        // Making sure that the routing key respects the Queue bindingKey format
        if (routingKey.startsWith(baseBindingKey)) {
            val parsedMeasures: List<DeviceMeasure> = mapper.readValue(
                message,
                mapper.typeFactory.constructCollectionType(
                    List::class.java, DeviceMeasure::class.java
                )
            )
            logger.debug("Parsed measures : $parsedMeasures")

            // Generate the base IRI for all the measures
            // We'll add the final part on each mesure
            // TODO : there is some copypasta going on here
            val now = LocalDateTime.now()
            val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")
            val zonedDateTime = ZonedDateTime.of(now, ZoneOffset.UTC)
            val formattedDate = (zonedDateTime.format(df))

            val deviceId = routingKey.substringAfterLast(".")
            val baseIri = "$deviceId/$formattedDate"
            logger.debug("Generated base resource IRI : $baseIri")

            parsedMeasures.forEach { measure ->
                val finalIri = baseIri + "/" + measure.n
                val dcBusinessResource = DCResource(datacoreBaseUri = datacoreProperties.baseResourceUri(),
                    type = datacoreIotModel, iri = finalIri)

                dcBusinessResource.setStringValue("iotdevice:id", deviceId)
                dcBusinessResource.setDateTimeValue("iotdevice:time", now)
                dcBusinessResource.setStringValue("iotdevice:name", measure.n)
                dcBusinessResource.setFloatValue("iotdevice:value", measure.v)
                dcBusinessResource.setStringValue("iotdevice:unit", measure.u)
                dcBusinessResource.setStringValue("iotdevice:baseName", measure.bn)
                if (measure.bt > 0) {
                    val baseTime = Instant.ofEpochSecond(measure.bt).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    dcBusinessResource.setDateTimeValue("iotdevice:baseTime", baseTime)
                }
                dcBusinessResource.setStringValue("iotdevice:stringValue", measure.vs)
                logger.debug("Created DC business resource : $dcBusinessResource")

                datacoreService.saveResource(datacoreIotProject, datacoreIotModel, dcBusinessResource, null)
                    .subscribe { result ->
                        logger.debug("Saved resource in DC with result $result")
                    }
            }
        }

        channel.basicAck(tag, false)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class DeviceMeasure(
        val bt: Long = Long.MIN_VALUE,
        val bn: String = "",
        val vs: String = "",
        val v: Float = Float.MIN_VALUE,
        val u: String = "",
        val n: String = ""
    )
}
