package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.*

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
class Data: ResponseType(){
    @field:XmlElement(name = "objet")
    override val responseObject: List<ResponseObject>? = null
}