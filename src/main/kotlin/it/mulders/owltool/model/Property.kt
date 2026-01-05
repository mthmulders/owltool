package it.mulders.owltool.model

interface Property {
    val name: String
}

data class DatatypeProperty(
    override val name: String,
    val typeLocalName: String,
    val typeNamespacePrefix: String,
) : Property

data class ObjectProperty(
    override val name: String,
    val ontologyClass: Class,
    val typeNamespacePrefix: String?,
) : Property
