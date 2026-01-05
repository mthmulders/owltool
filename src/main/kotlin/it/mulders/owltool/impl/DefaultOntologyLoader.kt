package it.mulders.owltool.impl

import it.mulders.owltool.OntologyLoader
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.DatatypeProperty
import it.mulders.owltool.model.ObjectProperty
import it.mulders.owltool.model.Ontology
import it.mulders.owltool.model.Property
import jakarta.enterprise.context.ApplicationScoped
import org.apache.jena.ontapi.OntModelFactory
import org.apache.jena.ontapi.OntSpecification
import org.apache.jena.ontapi.model.OntClass
import org.apache.jena.ontapi.model.OntDataProperty
import org.apache.jena.ontapi.model.OntDataRange
import org.apache.jena.ontapi.model.OntModel
import org.apache.jena.ontapi.model.OntObjectProperty
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
                        Class
                            .of(it.nameSpace, it.localName)
                            .withChildren(it.findChildClasses(model))
                            .withProperties(it.findDatatypeProperties(model))
                            .withProperties(it.findObjectProperties(model))
                    }.toSet()
            }.map { Ontology(it) }

    private fun OntClass.findChildClasses(model: OntModel): Collection<Class> =
        subClasses(true)
            .asSequence()
            .map {
                Class
                    .of(it.nameSpace, it.localName)
                    .withChildren(it.findChildClasses(model))
                    .withProperties(it.findDatatypeProperties(model))
                    .withProperties(it.findObjectProperties(model))
            }.toSet()

    private fun OntClass.findObjectProperties(model: OntModel): Collection<Property> = model.objectProperties()
            .asSequence()
            .filter { property -> property.isDefinedOnDomain(this) }
            .flatMap { property ->
                property.ranges().map { range ->
                    ObjectProperty(
                        property.localName,
                        Class.of(range.nameSpace, range.localName),
                        model.getNsURIPrefix(range.nameSpace),
                    )
                }
                .asSequence()
            }.toSet()

    private fun OntClass.findDatatypeProperties(model: OntModel): Collection<Property> =
        model
            .dataProperties()
            .asSequence()
            .filter { property -> property.isDefinedOnDomain(this) }
            .map {
                Property(
                    it.localName,
                    it
                        .ranges()
                        .asSequence()
                        .map { r -> r.toOntologyDataType(model) }
                        .joinToString(","),
                )
            }.toSet()

    private fun OntDataProperty.isDefinedOnDomain(clazz: OntClass): Boolean =
        this.domains().anyMatch { it.nameSpace == clazz.nameSpace && it.localName == clazz.localName }

    private fun OntDataRange.toOntologyDataType(model: OntModel): String {
        val namespacePrefix = model.getNsURIPrefix(this.nameSpace)
        return if (namespacePrefix.isNullOrEmpty()) {
            this.localName
        } else {
            "$namespacePrefix:${this.localName}"
        }
    }

    private fun OntObjectProperty.isDefinedOnDomain(clazz: OntClass): Boolean =
        this.domains().anyMatch { it.nameSpace == clazz.nameSpace && it.localName == clazz.localName }

    private fun <T> Optional<T>.unwrap(): T? = orElse(null)

    companion object {
        private val log = LoggerFactory.getLogger(DefaultOntologyLoader::class.java)
    }
}
