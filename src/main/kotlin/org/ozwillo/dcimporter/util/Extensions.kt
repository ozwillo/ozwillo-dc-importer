package org.ozwillo.dcimporter.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.hmac(algorithm: String, salt: String): String {
    val secretKeySpec = SecretKeySpec(salt.toByteArray(), algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secretKeySpec)
    val bytes = mac.doFinal(this.toByteArray())
    return Base64.getEncoder().encodeToString(bytes)
}

fun String.toZonedDateTime(): ZonedDateTime {
    val instant = Instant.ofEpochSecond(this.toLong())
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
}

fun String.extractDeviceId(): String {
    val splittedFullDeviceId = this.split(":")
    return if (splittedFullDeviceId.size > 2) this else splittedFullDeviceId[0]
}

fun String.parseDevice(): Pair<String, String> {
    // fullDeviceId can be either (for the moment) :
    //   - a MAC address, eg 84:F3:EB:0C:8A:71
    //   - a composite name, eg 8cf9574000000237:SalleServeur
    val splittedFullDeviceId = this.split(":")
    val deviceId = if (splittedFullDeviceId.size > 2) this else splittedFullDeviceId[0]
    val deviceName = if (splittedFullDeviceId.size == 2) splittedFullDeviceId[1] else this
    return Pair(deviceId, deviceName)
}
