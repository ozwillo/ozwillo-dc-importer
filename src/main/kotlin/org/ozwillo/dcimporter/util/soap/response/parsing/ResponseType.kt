package org.ozwillo.dcimporter.util.soap.response.parsing

open class ResponseType{
    open val type: String? = null
    open val properties: List<Properties>? = null
    open val lotNbr: Int? = null
    open val consultationState: String? = null
    open val value: String? = null
    open val dce: String? = null
    open val referenceMS: String? = null
    open val objet: String? = null
    open val dateCreation: String? = null
    open val datePublication: String? = null
    open val datePublicationF: String? = null
    open val dateCloture: String? = null
    open val dateClotureF: String? = null
    open val reference: String? = null
    open val nomenclature: String? = null
    open val finaliteMarche: String? = null
    open val typeMarche: String? = null
    open val prestation: String? = null
    open val departement: String? = null
    open val informatique: String? = null
    open val passe: String? = null
    open val emails: String? = null
    open val enLigne: String? = null
    open val alloti: String? = null
    open val invisible: String? = null
    open val errorState: String? = null
    open val responseObject: List<ResponseObject>? = null
}