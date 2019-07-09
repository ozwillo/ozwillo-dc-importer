package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.*

@XmlRootElement(name = "propriete")
@XmlAccessorType(XmlAccessType.FIELD)
class Properties {
    @field:XmlAttribute(name = "nom")
    val name: String? = null
    @field:XmlAttribute(name = "statut")
    val status: String? = null
    @field:XmlAttribute(name = "message")
    val message: String? = null
    @field:XmlAttribute(name = "suppression")
    val suppression: Boolean? = null
    @XmlValue
    val value: String? = null
}