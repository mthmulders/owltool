package it.mulders.owltool.impl

import it.mulders.owltool.OntologyLoader
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.Ontology
import jakarta.enterprise.context.ApplicationScoped
import org.apache.jena.ontapi.OntModelFactory
import org.apache.jena.ontapi.OntSpecification
import org.apache.jena.ontapi.model.OntClass
import org.apache.jena.ontapi.model.OntModel
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Optional
import kotlin.streams.asSequence

@ApplicationScoped
class DefaultOntologyLoader : OntologyLoader {
    override fun load(
        input: InputStream,
        ontologyNamespace: String,
    ): Result<Ontology> =
        InputStreamReader(input)
            .runCatching {
                OntModelFactory.createModel(OntSpecification.OWL2_DL_MEM).read(this, "", "TTL")
                    as OntModel
            }.map { model ->
                model
                    .hierarchyRoots()
                    .asSequence()
                    .onEach {
                        log.debug(
                            "Found ontology root class; namespace={}, name={}",
                            it.nameSpace,
                            it.localName,
                        )
                    }.map {
                        Class.of(it.nameSpace, it.localName).withChildren(it.findChildClasses(model))
                    }.toSet()
            }.map { Ontology(it) }

    private fun OntClass.findChildClasses(model: OntModel): Collection<Class> =
        subClasses(true)
            .asSequence()
            .map { Class.of(it.nameSpace, it.localName).withChildren(it.findChildClasses(model)) }
            .toSet()

    private fun <T> Optional<T>.unwrap(): T? = orElse(null)

    companion object {
        private val log = LoggerFactory.getLogger(DefaultOntologyLoader::class.java)
    }
}
