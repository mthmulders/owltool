package it.mulders.owltool.impl

import it.mulders.owltool.DiagramWriter
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.DatatypeProperty
import it.mulders.owltool.model.Ontology
import it.mulders.owltool.model.Property
import jakarta.enterprise.context.ApplicationScoped
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

@ApplicationScoped
class PlantUmlDiagramWriter : DiagramWriter {
    override fun generateDiagram(
        ontology: Ontology,
        output: OutputStream,
    ) {
        BufferedWriter(OutputStreamWriter(output)).use { writer ->
            writer.writeOntologyToDiagram(ontology)
        }
    }

    private fun BufferedWriter.writeOntologyToDiagram(ontology: Ontology) {
        write("@startuml")
        newLine()
        newLine()

        ontology.rootClasses().forEach { clazz -> writeClassToDiagram(clazz) }

        write("@enduml")
        newLine()
    }

    private fun Property.toPlantUmlType(): String =
        when (this) {
            is DatatypeProperty -> this.toPlantUmlType()
            else -> error("Unexpected property type: $this")
        }

    private fun DatatypeProperty.toPlantUmlType(): String =
        if (pointsToOntologyClass) {
            this.rangeType.identifier
        } else {
            "$typeNamespacePrefix:${this.rangeType.identifier}"
        }

    private fun BufferedWriter.writeClassToDiagram(clazz: Class) {
        val name = if (clazz.isAnonymous()) "_" else clazz.name
        val stereotype = if (clazz.isAnonymous()) "<<anonymous>>" else ""
        val identifier = clazz.identifier

        writeLn("class $identifier as \"$name\" $stereotype {")
        clazz.properties
            .filter { property -> !property.pointsToOntologyClass }
            .forEach { property -> writeLn("+ ${property.name} : ${property.toPlantUmlType()}") }
        writeLn("}")
        newLine()

        clazz.properties
            .filter { property -> property.pointsToOntologyClass }
            .forEach { property ->
                writeLn("$identifier --> ${property.toPlantUmlType()} : ${property.name}")
                newLine()
            }

        clazz.children.forEach {
            writeClassToDiagram(it)
            writeLn("$identifier <|-- ${it.identifier}")
            newLine()
        }
    }

    private fun BufferedWriter.writeLn(str: String) {
        write(str)
        newLine()
    }
}
