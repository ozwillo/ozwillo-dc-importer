package org.ozwillo.dcimporter.util.soap.response.parsing

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "validation_erreur")
@XmlAccessorType(XmlAccessType.FIELD)
class CheckConsultationRejectedState: ResponseType(){
    @field:XmlAttribute(name = "erreur_0")
    override val errorState: String? = null
    @field:XmlAttribute(name = "cle")
    override val dce: String? = null
    @field:XmlAttribute(name = "reference")
    override val referenceMS: String? = null
    @field:XmlAttribute(name = "objet")
    override val objet: String? = null
    @field:XmlAttribute(name = "date_creation")
    override val dateCreation: String? = null
    @field:XmlAttribute(name = "date_publication")
    override val datePublication: String? = null
    @field:XmlAttribute(name = "date_publication_f")
    override val datePublicationF: String? = null
    @field:XmlAttribute(name = "date_cloture")
    override val dateCloture: String? = null
    @field:XmlAttribute(name = "date_cloture_f")
    override val dateClotureF: String? = null
    @field:XmlAttribute(name = "ref_interne")
    override val reference: String? = null
    @field:XmlAttribute(name = "nomenclature_interne")
    override val nomenclature: String? = null
    @field:XmlAttribute(name = "finalite_marche")
    override val finaliteMarche: String? = null
    @field:XmlAttribute(name = "type_marche")
    override val typeMarche: String? = null
    @field:XmlAttribute(name = "type_prestation")
    override val prestation: String? = null
    @field:XmlAttribute(name = "departements_prestation")
    override val departement: String? = null
    @field:XmlAttribute(name = "informatique")
    override val informatique: String? = null
    @field:XmlAttribute(name = "passe")
    override val passe: String? = null
    @field:XmlAttribute(name = "emails")
    override val emails: String? = null
    @field:XmlAttribute(name = "en_ligne")
    override val enLigne: String? = null
    @field:XmlAttribute(name = "a_lots")
    override val alloti: String? = null
    @field:XmlAttribute(name = "invisible")
    override val invisible: String? = null
}