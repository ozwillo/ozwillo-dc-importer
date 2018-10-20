package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "publication_error")
@XmlAccessorType(XmlAccessType.FIELD)
class PublishConsultationRejected {
    @field:XmlElement(name = "publication_refusee")
    val publishConsultationRejectedState: PublishConsultationRejectedState? = null
}