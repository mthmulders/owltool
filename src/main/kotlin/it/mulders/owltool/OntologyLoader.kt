package it.mulders.owltool

import it.mulders.owltool.model.Ontology
import java.io.InputStream

fun interface OntologyLoader {
    fun load(
        input: InputStream,
        ontologyNamespace: String,
    ): Result<Ontology>
}
