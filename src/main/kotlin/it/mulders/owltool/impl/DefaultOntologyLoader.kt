package it.mulders.owltool.impl

import it.mulders.owltool.OntologyLoader
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.DatatypeProperty
import it.mulders.owltool.model.Ontology
import it.mulders.owltool.model.Property
import jakarta.enterprise.context.ApplicationScoped
import org.apache.jena.ontapi.OntModelFactory
import org.apache.jena.ontapi.OntSpecification
import org.apache.jena.ontapi.model.OntClass
import org.apache.jena.ontapi.model.OntDataProperty
import org.apache.jena.ontapi.model.OntModel
import org.apache.jena.ontapi.model.OntObjectProperty
import org.apache.jena.rdf.model.Resource
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.sequences.toSet
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
                        Class
                            .of(it.nameSpace, it.localName)
                            .withChildren(it.findChildClasses(model))
                            .withProperties(it.findProperties(model))
                    }.toSet()
            }.map { Ontology(it) }

    private fun OntClass.findChildClasses(model: OntModel): Collection<Class> =
        subClasses(true)
            .asSequence()
            .map {
                Class
                    .of(it.nameSpace, it.localName)
                    .withChildren(it.findChildClasses(model))
                    .withProperties(it.findProperties(model))
            }.toSet()

    private fun Resource.prefixOrNamespace(): String =
        if (this.nameSpace.isNullOrEmpty()) {
            ""
        } else {
            model.getNsURIPrefix(this.nameSpace) ?: this.nameSpace
        }

    private fun OntClass.findProperties(model: OntModel): Collection<Property> =
        this
            .properties()
            .asSequence()
            .flatMap { property ->
                property
                    .ranges()
                    .asSequence()
                    .onEach { range ->
                        log.debug(
                            "Found property {} on class {} with range {}:{}",
                            property.localName,
                            this.localName,
                            range.prefixOrNamespace(),
                            range.localName,
                        )
                    }.map { range ->
                        DatatypeProperty(
                            property.localName,
                            this.nameSpace == range.nameSpace,
                            Class.of(range.nameSpace, range.localName),
                            range.prefixOrNamespace(),
                        )
                    }.toSet()
            }.toSet()

    private fun OntDataProperty.isDefinedOnDomain(clazz: OntClass): Boolean =
        this.domains().anyMatch { it.nameSpace == clazz.nameSpace && it.localName == clazz.localName }

    private fun OntObjectProperty.isDefinedOnDomain(clazz: OntClass): Boolean =
        this.domains().anyMatch { it.nameSpace == clazz.nameSpace && it.localName == clazz.localName }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultOntologyLoader::class.java)
    }
}
