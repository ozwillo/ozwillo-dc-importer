package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.util.parseDevice
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

    fun getOrCreateDevice(fullDeviceId: String, latitude: Float?, longitude: Float?): Mono<DCResource> {

        val deviceIdAndName = fullDeviceId.parseDevice()

        return datacoreService.getResourceFromIRI(
            project = datacoreIotProject,
            iri = deviceIdAndName.first,
            type = datacoreIotDevice,
            bearer = null
        ).switchIfEmpty {
            val dcDeviceResource = DCResource(
                datacoreBaseUri = datacoreProperties.baseResourceUri(),
                type = datacoreIotDevice,
                iri = deviceIdAndName.first
            )
            dcDeviceResource.setStringValue("iotdevice:id", deviceIdAndName.first)
            dcDeviceResource.setStringValue("iotdevice:name", deviceIdAndName.second)
            latitude?.let { dcDeviceResource.setFloatValue("iotdevice:lat", it) }
            longitude?.let { dcDeviceResource.setFloatValue("iotdevice:lon", it) }
            datacoreService.saveResource(datacoreIotProject, datacoreIotDevice, dcDeviceResource, null)
        }
    }
}
