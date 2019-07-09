package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "SOAP-ENV:Envelope")
@XmlAccessorType(XmlAccessType.FIELD)
class Envelope {
    @field:XmlElement(name = "SOAP-ENV:Body")
    private val body: Body? = null

}