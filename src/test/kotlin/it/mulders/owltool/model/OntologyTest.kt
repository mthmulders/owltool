package it.mulders.owltool.model

import assertk.all
import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import it.mulders.owltool.EXAMPLE_NAMESPACE
import org.junit.jupiter.api.Test

class OntologyTest {
    @Test
    fun `should recursively count classes`() {
        // Arrange
        val roots = setOf(
            Class.of(EXAMPLE_NAMESPACE, "root-1", setOf(
                Class.of(EXAMPLE_NAMESPACE, "root-1-a", setOf(
                    Class.of(EXAMPLE_NAMESPACE, "root-1-a-i"),
                    Class.of(EXAMPLE_NAMESPACE, "root-1-a-ii")
                )),
                Class.of(EXAMPLE_NAMESPACE, "root-1-b"),
                Class.of(EXAMPLE_NAMESPACE, "root-1-c")
            )),
            Class.of(EXAMPLE_NAMESPACE, "root-2", setOf(
                Class.of(EXAMPLE_NAMESPACE, "root-2-a")
            )),
            Class.of(EXAMPLE_NAMESPACE, "root-3")
        )
        val ontology = Ontology(roots)

        // Act
        val result = ontology.classCount()

        // Assert
        assertThat(result).isEqualTo(9)
    }

    @Test
    fun `should recursively find all classes in ontology`() {
        // Arrange
        val roots = setOf(
            Class.of(EXAMPLE_NAMESPACE, "root-1", setOf(
                Class.of(EXAMPLE_NAMESPACE, "root-1-a", setOf(
                    Class.of(EXAMPLE_NAMESPACE, "root-1-a-i"),
                    Class.of(EXAMPLE_NAMESPACE, "root-1-a-ii")
                )),
                Class.of(EXAMPLE_NAMESPACE, "root-1-b"),
                Class.of(EXAMPLE_NAMESPACE, "root-1-c")
            )),
            Class.of(EXAMPLE_NAMESPACE, "root-2", setOf(
                Class.of(EXAMPLE_NAMESPACE, "root-2-a")
            )),
            Class.of(EXAMPLE_NAMESPACE, "root-3")
        )
        val ontology = Ontology(roots)

        // Act
        val result = ontology.allClasses()

        // Assert
        assertThat(result).all {
            hasSize(9)
            extracting { it.name }
                .containsAtLeast(
                    "root-1",
                    "root-1-a",
                    "root-1-a-i",
                    "root-1-a-ii",
                    "root-1-b",
                    "root-1-c",
                    "root-2",
                    "root-2-a",
                    "root-3",
                )
        }
    }
}