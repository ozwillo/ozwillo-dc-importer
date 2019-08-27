package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.datacore.DCResourceURI
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

@Service
class IoTService(
    private val datacoreService: DatacoreService,
    private val datacoreProperties: DatacoreProperties
) {

    @Value("\${datacore.model.iotProject}")
    private val datacoreIotProject: String = "iot_0"

    @Value("\${datacore.model.iotDevice}")
    private val datacoreIotDevice: String = "iot:device_0"

    fun getOrCreateDevice(deviceId: String): Mono<DCResourceURI> {

        return datacoreService.getResourceFromIRI(
            project = datacoreIotProject,
            iri = deviceId,
            type = datacoreIotDevice,
            bearer = null
        ).map {
            it.getUri()
        }.switchIfEmpty {
            val dcDeviceResource = DCResource(
                datacoreBaseUri = datacoreProperties.baseResourceUri(),
                type = datacoreIotDevice,
                iri = deviceId
            )
            datacoreService.saveResource(datacoreIotProject, datacoreIotDevice, dcDeviceResource, null)
                .map { it.getUri() }
        }
    }
}
