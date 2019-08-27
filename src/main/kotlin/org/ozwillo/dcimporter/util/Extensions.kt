package org.ozwillo.dcimporter.util

import java.time.ZonedDateTime
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.time.ZoneOffset
import java.time.Instant



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
