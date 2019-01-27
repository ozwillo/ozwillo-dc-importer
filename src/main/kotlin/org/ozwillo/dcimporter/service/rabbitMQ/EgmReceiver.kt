package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.service.DatacoreService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class EgmReceiver(private val datacoreService: DatacoreService) {

    private val logger: Logger = LoggerFactory.getLogger(EgmReceiver::class.java)

    @Value("\${datacore.model.iotProject}")
    private val datacoreIotProject: String = "iot_0"

    @Value("\${datacore.model.iotModel}")
    private val datacoreIotModel: String = "iot:device_0"

    @RabbitListener(queues = ["egm"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long) {
        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey
        /*
         * sample data received :
         *   [{"bt": 1548582039.971496, "bn": "8cf9574000000217:FrancK2", "v": 6.786213378906247, "u": "Cel", "n": "temperature"},
         *    {"v": 66.265625, "u": "%RH", "n": "humidity"},
         *    {"v": 3.3000000000000003, "u": "%EL", "n": "batteryLevel"},
         *    {"v": 57.25, "u": "dB", "n": "SNR"},
         *    {"v": -63, "u": "dBm", "n": "RSSI"},
         *    {"v": 60, "u": "s", "n": "Period"}]
         *
         * TODO : the generic JsonConverter won't work here, a specific deserialzed is thus required
         * TODO : what's more, there will 5 new resources for each received data
         */
        // val resource = JsonConverter.jsonToObject(message)

        routingByBindingKey(message, routingKey, channel, tag)
    }

    fun routingByBindingKey(message: String, routingKey: String, channel: Channel, tag: Long) {
        logger.debug("Received message $message on routing key $routingKey")

        // TODO : call the datacoreService for each new resource
        // TODO : use the same type for each resource ?
//        datacoreService.saveResource(datacoreIotProject, datacoreIotModel, DCBusinessResourceLight("URI"), null)
//            .subscribe { result ->
//                logger.debug("Saved resource in DC with result $result")
//            }
        channel.basicAck(tag, false)
    }
}
