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

    fun getOrCreateDevice(fullDeviceId: String): Mono<DCResourceURI> {

        // fullDeviceId can be either (for the moment) :
        //   - a MAC address, eg 84:F3:EB:0C:8A:71
        //   - a composite name, eg 8cf9574000000237:SalleServeur
        val splittedFullDeviceId = fullDeviceId.split(":")
        val deviceId = if (splittedFullDeviceId.size > 2) fullDeviceId else splittedFullDeviceId[0]
        val deviceName = if (splittedFullDeviceId.size == 2) splittedFullDeviceId[1] else fullDeviceId

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
            dcDeviceResource.setStringValue("iotdevice:id", deviceId)
            dcDeviceResource.setStringValue("iotdevice:name", deviceName)
            datacoreService.saveResource(datacoreIotProject, datacoreIotDevice, dcDeviceResource, null)
                .map { it.getUri() }
        }
    }
}
