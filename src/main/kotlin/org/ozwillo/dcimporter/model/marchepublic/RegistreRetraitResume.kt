package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.sirene.Organization
import java.time.LocalDateTime

data class RegistreRetraitResume(val consultationUri: String,
                                 val personneUriList: List<String>,
                                 val OrgUriList: List<String>,
                                 val nbreRetrait: Int,
                                 val datePremierRetrait : LocalDateTime,
                                 val dateDernierRetrait: LocalDateTime,
                                 var personnes: MutableList<Personne>,
                                 var entreprises: MutableList<Organization>)