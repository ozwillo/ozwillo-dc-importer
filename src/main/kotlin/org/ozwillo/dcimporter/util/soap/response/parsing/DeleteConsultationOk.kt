package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.*

@XmlRootElement(name = "suppression_consultation_ok")
@XmlAccessorType(XmlAccessType.FIELD)
class DeleteConsultationOk{
    @field:XmlElement(name= "consultation_suppr_ok")
    val okState: DeleteConsultationOkState? = null
}