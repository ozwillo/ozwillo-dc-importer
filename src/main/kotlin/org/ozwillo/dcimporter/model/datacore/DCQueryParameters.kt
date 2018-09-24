package org.ozwillo.dcimporter.model.datacore

import java.util.ArrayList

class DCQueryParameters(subject: String,    //type orga
                        operator: DCOperator,   //opérateur = > <
                        ordering: DCOrdering,   //asc ou desc
                        target: String) : Iterable<DCQueryParameters.DCQueryParam> { //uri orga

    class DCQueryParam(val subject: String,
                       val operator: DCOperator,
                       val ordering: DCOrdering,
                       val objectAsString: String) {

        fun getObject(): String {
            val builder = StringBuilder()

            if ("".equals(objectAsString)) {
                // nothing
            } else {
                builder.append(objectAsString );
            }

            if (ordering != null) {
                builder.append(ordering.value)
            }

            return builder.toString()
        }

        // needed for parameters matching in Mockito testing
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DCQueryParam

            if (subject != other.subject) return false
            if (operator != other.operator) return false
            if (ordering != other.ordering) return false
            if (objectAsString != other.objectAsString) return false

            return true
        }

        override fun hashCode(): Int {
            var result = subject.hashCode()
            result = 31 * result + operator.hashCode()
            result = 31 * result + ordering.hashCode()
            result = 31 * result + objectAsString.hashCode()
            return result
        }


    }

    private var params: MutableList<DCQueryParam> = ArrayList()

    override fun iterator(): kotlin.collections.Iterator<DCQueryParam> {
        return params.iterator()
    }

    // needed for parameters matching in Mockito testing
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DCQueryParameters

        if (params != other.params) return false

        return true
    }

    override fun hashCode(): Int {
        return params.hashCode()
    }


    init {
        params.add(DCQueryParam(subject, operator, ordering, target))
    }
}
