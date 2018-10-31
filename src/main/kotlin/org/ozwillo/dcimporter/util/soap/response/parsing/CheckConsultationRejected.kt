package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "validation_error")
@XmlAccessorType(XmlAccessType.FIELD)
class CheckConsultationRejected {
    @field:XmlElement(name = "validation_erreur")
    val checkConsultationRejectedState: CheckConsultationRejectedState? = null
}