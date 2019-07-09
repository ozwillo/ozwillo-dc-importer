package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "ns1:modifier_consultation_logResponse")
@XmlAccessorType(XmlAccessType.FIELD)
class UpdateConsultationLogResponse {
    @field:XmlElement(name = "return")
    val soapReturn: String? = null
}