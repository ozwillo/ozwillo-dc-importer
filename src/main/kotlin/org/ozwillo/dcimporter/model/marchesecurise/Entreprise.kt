package org.ozwillo.dcimporter.model.marchesecurise

data class Entreprise(val nom: String,
                      val adresse1: String,
                      val adresse2: String,
                      val codePostal: Int,
                      val commune: String,
                      val pays: String,
                      val telephone: String,
                      val fax: String,
                      val siret: String,
                      val siren: String,
                      val naf: String,
                      val url: String){
}