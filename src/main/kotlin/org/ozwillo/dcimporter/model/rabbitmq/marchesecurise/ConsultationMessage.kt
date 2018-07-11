package org.ozwillo.dcimporter.model.rabbitmq.marchesecurise

import org.ozwillo.dcimporter.model.marchepublic.Consultation

data class ConsultationMessage(val uri:String,
                               val consultation:Consultation){
}