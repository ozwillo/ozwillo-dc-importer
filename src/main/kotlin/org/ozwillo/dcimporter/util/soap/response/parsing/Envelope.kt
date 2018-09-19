package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.*

@XmlRootElement(name = "SOAP-ENV:Envelope")
@XmlAccessorType(XmlAccessType.FIELD)
class Envelope{
    @field:XmlElement(name = "SOAP-ENV:Body")
    private val body: Body? = null

}