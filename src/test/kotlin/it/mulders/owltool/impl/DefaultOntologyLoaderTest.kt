package it.mulders.owltool.impl

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import it.mulders.owltool.EXAMPLE_NAMESPACE
import it.mulders.owltool.model.Ontology
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

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
        val input = loadResource("/ontologies/simple.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology -> assertThat(ontology.rootClasses()).hasSize(1) },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    @Test
    fun `should discover parent-child relations between classes`() {
        // Arrange
        val input = loadResource("/ontologies/simple.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology ->
                val patientClass = ontology.rootClasses().single { it.name == "Person" }
                assertThat(patientClass.children).hasSize(1)
            },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    @Test
    fun `should discover datatype properties of classes`() {
        // Arrange
        val input = loadResource("/ontologies/simple.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology ->
                val personClass = ontology.allClasses().single { it.name == "Person" }
                assertThat(personClass.properties)
                    .extracting { it.name }
                    .containsOnly("hasBirthDate")
                val patientClass = ontology.allClasses().single { it.name == "Patient" }
                assertThat(patientClass.properties)
                    .extracting { it.name }
                    .containsOnly("hasPatientNumber")
            },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    @Test
    fun `should discover object properties of classes`() {
        // Arrange
        val input = loadResource("/ontologies/relations.ttl")

        // Act
        val result = loader.load(input, EXAMPLE_NAMESPACE)

        // Assert
        result.fold(
            onSuccess = { ontology ->
                val personClass = ontology.allClasses().single { it.name == "Person" }
                assertThat(personClass.properties)
                    .extracting { it.name }
                    .containsOnly("knows")
            },
            onFailure = { t -> fail("Loading ontology failed: ${t.message}") }
        )
    }

    private fun loadResource(resourcePath: String): InputStream {
        val result = DefaultOntologyLoaderTest::class.java.getResourceAsStream(resourcePath)
        assertThat(result).isNotNull()
        return result!!
    }
}