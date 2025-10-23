package it.mulders.owltool.impl

import it.mulders.owltool.OntologyLoader
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.Ontology
import jakarta.enterprise.context.ApplicationScoped
import org.apache.jena.ontology.OntClass
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.ModelFactory
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.InputStreamReader

@ApplicationScoped
class DefaultOntologyLoader : OntologyLoader {
    override fun load(
        input: InputStream,
        ontologyNamespace: String,
    ): Result<Ontology> {
        return InputStreamReader(input)
                .runCatching {
                    ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)
                        .read(this, "", "TTL") as OntModel
                }
                .map { model ->
                    model
                        .listClasses()
                        .asSequence()
                        .filter { ontClass -> ontClass.nameSpace == ontologyNamespace }
                        .onEach { ontClass ->
                            log.debug(
                                "Detected class; namespace={}, name={}",
                                ontClass.nameSpace,
                                ontClass.localName,
                            )
                        }.toSet()
                }
                .map { ontClassesInNamespace ->
                    val classes =
                        ontClassesInNamespace
                            .asSequence()
                            .filter { it.superClass?.nameSpace != ontologyNamespace }
                            .map { ontClass -> Class(ontClass.nameSpace, ontClass.localName) }
                            .map { it.withChildren(it.findChildClasses(ontClassesInNamespace)) }
                            .toSet()

                    Ontology(classes)
                }
    }

    private fun Class.findChildClasses(ontClassesInNamespace: Collection<OntClass>): Collection<Class> =
        ontClassesInNamespace
            .asSequence()
            .filter { it.superClass != null }
            .filter { it.superClass.nameSpace == this.namespace }
            .filter { it.superClass.localName == this.name }
            .map { Class(it.nameSpace, it.localName) }
            .map { it.withChildren(it.findChildClasses(ontClassesInNamespace)) }
            .toSet()

    companion object {
        private val log = LoggerFactory.getLogger(DefaultOntologyLoader::class.java)
    }
}
