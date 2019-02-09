package org.ozwillo.dcimporter.model.egm

data class EgmFile (
    val status: String,
    val fileFormat: String,
    val table: String,
    val collId: String,
    val data: List<EgmArrayData>,
    val encodedFile: String
)

data class EgmArrayData(
    val column: String,
    val value: String
)
