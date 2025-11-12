package it.mulders.owltool.impl

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isInstanceOf
import it.mulders.owltool.EXAMPLE_NAMESPACE
import it.mulders.owltool.model.Ontology
import it.mulders.owltool.model.Property
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class DefaultOntologyLoaderTest {
    private val loader = DefaultOntologyLoader()

    @Test
    fun `should return an instance of Ontology`() {
        // Arrange
        val input = ByteArrayInputStream(ByteArray(0))

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology -> assertThat(ontology).isInstanceOf(Ontology::class.java) },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    @Test
    fun `should load root classes from input`() {
        // Arrange
        val input = DefaultOntologyLoaderTest::class.java.getResourceAsStream("/ontologies/simple.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology -> assertThat(ontology.classes).hasSize(1) },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    @Test
    fun `should discover parent-child relations between classes`() {
        // Arrange
        val input = DefaultOntologyLoaderTest::class.java.getResourceAsStream("/ontologies/simple.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology ->
                val patientClass = ontology.classes.single { it.name == "Person" }
                assertThat(patientClass.children).hasSize(1)
            },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    @Test
    fun `should discover datatype properties of classes`() {
        // Arrange
        val input = DefaultOntologyLoaderTest::class.java.getResourceAsStream("/ontologies/simple.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology ->
                val personClass = ontology.classes.single { it.name == "Person" }
                assertThat(personClass.properties).containsOnly(
                    Property("hasBirthDate", "xsd:date")
                )
            },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }
}