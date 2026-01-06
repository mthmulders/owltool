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
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.InputStreamReader
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
            }.toSet()

    private fun OntClass.findDatatypeProperties(model: OntModel): Collection<Property> {
        return model
            .dataProperties()
            .asSequence()
            .filter { property -> property.isDefinedOnDomain(this) }
            .flatMap { property ->
                property
                    .domains()
                    .asSequence()
                    .onEach { log.info("    -> range: {}", it) }
                    .map { domain ->
                        log.debug(
                            "Detected datatype property; name={}, domain={}:{}",
                            property.localName,
                            domain.nameSpace,
                            domain.localName
                        )
                        val prefix = if (domain.nameSpace.isNullOrEmpty()) "" else model.getNsURIPrefix(domain.nameSpace) ?: ""
                        DatatypeProperty(
                            property.localName,
                            model.nsPrefixMap.containsValue(domain.nameSpace),
                            Class.of(domain.nameSpace, domain.localName),
                            prefix,
                        )
                    }
            }.toSet()
    }

    private fun OntDataProperty.isDefinedOnDomain(clazz: OntClass): Boolean =
        this.domains().anyMatch { it.nameSpace == clazz.nameSpace && it.localName == clazz.localName }

    private fun OntObjectProperty.isDefinedOnDomain(clazz: OntClass): Boolean =
        this.domains().anyMatch { it.nameSpace == clazz.nameSpace && it.localName == clazz.localName }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultOntologyLoader::class.java)
    }
}
