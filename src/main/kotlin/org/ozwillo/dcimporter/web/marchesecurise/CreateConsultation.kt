package org.ozwillo.dcimporter.web.marchesecurise

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class CreateConsultation{
    companion object {
        @Throws(Exception::class)
        fun sendSoap(soapUrl:String, soapMessage:String):String{
            val url = URL(soapUrl)
            val connection:URLConnection = url.openConnection()
            val httpConnection:HttpURLConnection = connection as HttpURLConnection

            val byteArray:ByteArray = soapMessage.toByteArray()

            httpConnection.setRequestProperty("Content-Length", byteArray.size.toString())
            httpConnection.setRequestProperty("Content-Type", "text/xml; charset=utf8")
            httpConnection.setRequestProperty("SOAPAction", "")
            httpConnection.requestMethod = "POST"

            httpConnection.doOutput = true
            httpConnection.doInput = true

            val out:OutputStream = httpConnection.outputStream
            out.write(byteArray)
            out.close()
            var input: BufferedReader? = null
            val resultMessage = StringBuffer()
            try {
                val isr = InputStreamReader(httpConnection.inputStream)
                input = BufferedReader(isr)
                input.use {
                    var inputLine = it.readLine()
                    while (inputLine != null){
                        resultMessage.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                }
            }finally {
                input?.close()
            }
            return resultMessage.toString()
        }
    }
}