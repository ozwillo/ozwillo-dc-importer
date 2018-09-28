package org.ozwillo.dcimporter.model.maarch

data class MaarchResource(val resId: String,
                          val data: List<MaarchArrayData>,
                          val table: String)