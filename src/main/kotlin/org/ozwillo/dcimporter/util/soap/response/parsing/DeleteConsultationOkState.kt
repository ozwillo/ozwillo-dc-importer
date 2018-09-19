package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.*

@XmlRootElement(name = "consultation_suppr_ok")
@XmlAccessorType(XmlAccessType.FIELD)
class DeleteConsultationOkState: ResponseType(){
    @field:XmlAttribute(name = "etat_consultation")
    override val consultationState: String? = null
}