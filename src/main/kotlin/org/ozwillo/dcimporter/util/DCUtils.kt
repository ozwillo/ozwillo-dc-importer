package org.ozwillo.dcimporter.util

import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class DCUtils {

    companion object {

        private const val URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC = "\\$\\-_\\.\\+!\\*'\\(\\):@~&,;=/"

        fun getUri(baseUri: String, type: String, iri: String): String = "$baseUri/${encodeUriPathSegment(type)}/$iri"

        private fun encodeUriPathSegment(uriPathSegment: String): String {
            val sb = StringBuilder()
            try {
                for (c in uriPathSegment.toCharArray()) {
                    if ( (c.toInt() in 48..57) // number
                            || (c.toInt() in 65..90) // upper case
                            || (c.toInt() in 97..122) // lower case
                            || URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC.indexOf(c) != -1) { // among safe chars
                        sb.append(c)
                    } else {
                        sb.append(URLEncoder.encode(String(Character.toChars(c.toInt())) , "UTF-8"))
                    }
                }
            } catch (e: UnsupportedEncodingException) {
                // should never happens for UTF-8
                throw RuntimeException(e)
            }
            return sb.toString()
        }

        fun templateToString(templatePath:String):String {
            val resource = ClassPathResource(templatePath)
            val bos = ByteArrayOutputStream()

            try {
                FileCopyUtils.copy(resource.getInputStream(), bos)
            }catch (e:IOException){
                e.printStackTrace()
            }

            return bos.toString()
        }

        fun booleanToInt(bool:Boolean):Int{
            return if(bool)
                1
            else
                0
        }

        fun intToBoolean(i:Int):Boolean{
            return i==1
        }

        fun intListToString(ints:List<Int>):String{
            var result = ""
            for (index in ints.indices){
                result+="${ints[index]};"
            }
            result=result.substring(0,result.length-1)
            return result
        }

        fun stringListToString(stringList:List<String>):String{
            var result = ""
            for (index in stringList.indices){
                result+="${stringList[index]};"
            }
            result=result.substring(0,result.length-1)
            return result
        }
    }
}
