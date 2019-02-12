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

    override fun toString(): String {
        return "FormModel(display_id='$display_id', last_update_time='$last_update_time', " +
                "display_name='$display_name', submission=$submission, url='$url', " +
                "fields=${fields.filterKeys { key -> key != "courrier" }}, " +
                "receipt_time='$receipt_time', user=$user, criticality_level=$criticality_level, id='$id', " +
                "workflowStatus=$workflowStatus)"
    }
}
