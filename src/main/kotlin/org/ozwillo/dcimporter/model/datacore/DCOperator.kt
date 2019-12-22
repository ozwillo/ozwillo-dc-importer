package org.ozwillo.dcimporter.model.datacore

enum class DCOperator(val value: String) {
    EMPTY(""),
    EQ(""),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    NE("<>"),
    IN("\$in"),
    NIN("\$nin"),
    REGEX("\$regex"),
    EXISTS("\$exists"),
    FULLTEXT("\$fulltext");
}
