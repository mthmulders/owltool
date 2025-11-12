package it.mulders.owltool.impl

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import it.mulders.owltool.EXAMPLE_NAMESPACE
import it.mulders.owltool.model.Class
import it.mulders.owltool.model.Ontology
import it.mulders.owltool.model.Property
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class PlantUmlDiagramWriterTest {
    private val diagramWriter = PlantUmlDiagramWriter()

    @Test
    fun `should write PlantUML header`() {
        // Arrange
        val ontology = Ontology(setOf())

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).contains("@startuml")
    }

    @Test
    fun `should write PlantUML footer`() {
        // Arrange
        val ontology = Ontology(setOf())

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).contains("@enduml")
    }

    @Test
    fun `should write single class`() {
        // Arrange
        val ontology = Ontology(setOf(Class.of(EXAMPLE_NAMESPACE, "Single")))

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).contains("class single as \"Single\"")
    }

    @Test
    fun `should write specialisation of class`() {
        // Arrange
        val ontology = Ontology(
            setOf(
                Class.of(EXAMPLE_NAMESPACE, "Parent", setOf(
                    Class.of(EXAMPLE_NAMESPACE, "Child")
            ))))

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).contains("parent <|-- child")
    }

    @Test
    fun `should assign identifier to named class`() {
        // Arrange
        val ontology = Ontology(setOf(Class.of(EXAMPLE_NAMESPACE, "NamedClass")))

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).contains("class namedclass as \"NamedClass\"")
    }

    @Test
    fun `should assign identifier to anonymous class`() {
        // Arrange
        val ontology = Ontology(setOf(Class.of(null, null)))

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).all {
            contains("as \"_\" <<anonymous>>")
        }
    }

    @Test
    fun `should write data property as field`() {
        // Arrange
        val ontology = Ontology(setOf(
            Class.of("EXAMPLE_NAMESPACE", "ClassWithProperty").withProperty(
                Property("age", "xsd:integer")
            )
        ))

        // Act
        val diagram = ontology.generateDiagram()

        // Assert
        assertThat(diagram).contains("+ age : xsd:integer")
    }

    private fun Ontology.generateDiagram(): String = ByteArrayOutputStream().use { stream ->
        diagramWriter.generateDiagram(this, stream)
        return stream.toString()
    }
}