package org.ozwillo.dcimporter.model.datacore

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/** used to split id in order to encode its path elements if it's not disabled */
private const val URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC = "\\$\\-_\\.\\+!\\*'\\(\\)"
private const val URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS = ":@~&,;=/"
const val URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC =
    URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC + URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS

typealias DCModelType = String

fun DCModelType.encodeUriPathSegment(): String {
    val sb = StringBuilder()
    try {
        for (c in this.toCharArray()) {
            if ((c.toInt() in 48..57) || // number
                (c.toInt() in 65..90) || // upper case
                (c.toInt() in 97..122) || // lower case
                URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC.indexOf(c) != -1
            ) { // among safe chars
                sb.append(c)
            } else {
                sb.append(URLEncoder.encode(String(Character.toChars(c.toInt())), "UTF-8"))
            }
        }
    } catch (e: UnsupportedEncodingException) {
        // should never happens for UTF-8
        throw RuntimeException(e)
    }
    return sb.toString()
}
