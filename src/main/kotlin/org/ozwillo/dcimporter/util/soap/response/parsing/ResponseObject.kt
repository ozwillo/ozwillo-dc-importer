package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.*

@XmlRootElement(name = "objet")
@XmlAccessorType(XmlAccessType.FIELD)
class ResponseObject: ResponseType() {
    @field:XmlAttribute(name = "type")
    override val type: String? = null
    @field:XmlElement(name = "propriete")
    override val properties: List<Properties>? = null
    @field:XmlElement(name = "nombre_lots")
    override val lotNbr: Int? = null
}