package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "SOAP-ENV:Body")
@XmlAccessorType(XmlAccessType.FIELD)
class Body{
    @field:XmlElement(name = "ns1:creer_consultation_logResponse")
    private val createConsultationLogResponse: CreateConsultationLogResponse? = null
}