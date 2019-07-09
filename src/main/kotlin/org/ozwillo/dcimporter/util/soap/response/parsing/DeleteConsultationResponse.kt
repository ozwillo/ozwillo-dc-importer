package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "ns1:supprimer_consultation_logResponse")
@XmlAccessorType(XmlAccessType.FIELD)
class DeleteConsultationResponse {
    @field:XmlElement(name = "return")
    val soapReturn: String? = null
}