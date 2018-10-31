package org.ozwillo.dcimporter.model.publik

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class FormModel(
    val display_id: String,
    val last_update_time: String,
    val display_name: String,
    val submission: Submission,
    val url: String,
    @JsonProperty
    val fields: HashMap<String, Any>,
    val receipt_time: String,
    val user: User?,
    val criticality_level: Int,
    val id: String,
    var workflowStatus: String?
) {

    @JsonProperty("workflow")
    private fun unpackNested(workflow: Map<String, Any>) {
        val status = workflow["status"] as Map<String, String>
        this.workflowStatus = status["name"].orEmpty()
    }
}
