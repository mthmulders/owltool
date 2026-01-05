package it.mulders.owltool.model

interface Property {
    val name: String

    fun dataType(): String
}

data class DatatypeProperty(
    override val name: String,
    val typeLocalName: String,
    val typeNamespacePrefix: String,
) : Property {
    override fun dataType(): String {
        return if (typeNamespacePrefix.isEmpty()) {
            this.typeLocalName
        } else {
            "$typeNamespacePrefix:${this.typeLocalName}"
        }
    }
}

data class ObjectProperty(
    override val name: String,
    val ontologyClass: Class,
    val typeNamespacePrefix: String,
) : Property {
    override fun dataType(): String {
        return if (typeNamespacePrefix.isEmpty()) {
            ontologyClass.name ?: ontologyClass.identifier
        } else {
            "$typeNamespacePrefix:${ontologyClass.name}"
        }
    }
}
