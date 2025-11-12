package it.mulders.owltool.impl

import it.mulders.owltool.DiagramWriter
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.Ontology
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

    private fun BufferedWriter.writeClassToDiagram(clazz: Class) {
        val name = if (clazz.isAnonymous()) "_" else clazz.name
        val stereotype = if (clazz.isAnonymous()) "<<anonymous>>" else ""
        val identifier = clazz.identifier

        writeLn("class $identifier as \"$name\" $stereotype {")
        clazz.properties.forEach { property -> writeLn("+ ${property.name} : ${property.dataType}") }
        writeLn("}")
        newLine()

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
