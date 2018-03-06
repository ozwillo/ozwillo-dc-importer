package org.ozwillo.dcimporter.model.maarch

data class MaarchFile(val status: String,
                      val fileFormat: String,
                      val table: String,
                      val collId: String,
                      val data: List<MaarchArrayData>,
                      val encodedFile: String)

data class MaarchArrayData(val column: String,
                           val value: String)
