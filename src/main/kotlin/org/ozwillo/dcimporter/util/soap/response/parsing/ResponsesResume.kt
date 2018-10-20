package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "reponses")
@XmlAccessorType(XmlAccessType.FIELD)
class ResponsesResume {
    @field:XmlAttribute(name = "nb_total")
    val nbResponse: Int? = null
}