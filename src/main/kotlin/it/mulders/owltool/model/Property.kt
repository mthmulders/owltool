package it.mulders.owltool.model

interface Property {
    val name: String
    val pointsToOntologyClass: Boolean
}

data class DatatypeProperty(
    override val name: String,
    override val pointsToOntologyClass: Boolean,
    val rangeType: Class,
    val typeNamespacePrefix: String,
) : Property
