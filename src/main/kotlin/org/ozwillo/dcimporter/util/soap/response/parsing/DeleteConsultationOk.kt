package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "suppression_consultation_ok")
@XmlAccessorType(XmlAccessType.FIELD)
class DeleteConsultationOk {
    @field:XmlElement(name = "consultation_suppr_ok")
    val okState: DeleteConsultationOkState? = null
}