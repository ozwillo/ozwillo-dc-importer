package org.ozwillo.dcimporter.util

import org.apache.commons.codec.binary.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.hmac(algorithm: String, salt: String): String {
    val secretKeySpec = SecretKeySpec(salt.toByteArray(), algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secretKeySpec)
    val bytes = mac.doFinal(this.toByteArray())
    return Base64.encodeBase64String(bytes)
}
