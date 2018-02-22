package org.ozwillo.dcimporter.model.publik

import java.util.HashMap

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FormModel(
    var display_id: String,
    var last_update_time: String,
    var display_name: String,
    var submission: Submission,
    var url: String,
    @JsonProperty
    var fields: HashMap<String, Any>,
    var receipt_time: String,
    var user: User,
    var criticality_level: Int,
    var id: String,
    var workflowStatus: String) {

    @JsonProperty("workflow")
    private fun unpackNested(workflow: Map<String, Any>) {
        val status = workflow["status"] as Map<String, String>
        this.workflowStatus = status["name"].orEmpty()
    }
}
