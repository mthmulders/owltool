package it.mulders.owltool.model

interface Property {
    val name: String
    val inTargetNamespace: Boolean
}

data class DatatypeProperty(
    override val name: String,
    override val inTargetNamespace: Boolean,
    val ontologyClass: Class,
    val typeNamespacePrefix: String,
) : Property
