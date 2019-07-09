package org.ozwillo.dcimporter.util

enum class BindingKeyAction(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    PUBLISH("publish"),
    CHECK("check"),
    GET("get");
}