package org.ozwillo.dcimporter.model.marchesecurise

import java.time.LocalDateTime

data class ReponseE(val cleReponse: String,
                    val dce: String,
                    val cleEntreprise: String,
                    val nomEntreprise: String,
                    val nomContact: String,
                    val emailContact: String,
                    val dateDepot: LocalDateTime,
                    val poids: Int,
                    val entreprise: Entreprise)

enum class Ordre(val value: String){
    DATE_PREMIER_RETRAIT("date_retrait"),
    DATE_DERNIER_RETRAIT("date_retrait_r"),
    ENTREPRISE("denomination_ent")
}

enum class SensOrdre{
    ASC,
    DESC
}