package it.mulders.owltool.cli

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import it.mulders.owltool.DiagramGenerator
import it.mulders.owltool.EXAMPLE_NAMESPACE
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class DiagramCommandTest {
    @Test
    fun `should pass input ontology and namespace to generator`() {
        // Arrange
        val inputPath = Path("target", "test-classes", "ontologies", "simple.ttl")
        val outputPath = Path("java.io.tmpdir", "output.puml")
        var invoked = false
        val generator = DiagramGenerator { path, namespace ->
            // Assert
            invoked = true
            Result.success(outputPath)
        }

        // Act
        DiagramCommand(generator).apply {
            input = inputPath.toString()
            namespace = EXAMPLE_NAMESPACE
        }.run()

        // Assert
        assertThat(invoked).isTrue()
    }

    @Test
    fun `should normalise namespace`() {
        // Arrange
        val inputPath = Path("target", "test-classes", "ontologies", "simple.puml")
        val outputPath = Path("java.io.tmpdir", "output.puml")
        val generator = DiagramGenerator { path, namespace ->
            // Assert
            assertThat(namespace).isEqualTo(EXAMPLE_NAMESPACE)
            Result.success(outputPath)
        }

        // Act
        DiagramCommand(generator).apply {
            input = inputPath.toString()
            namespace = "http://purl.org/net/ns/ex" // Note missing # at end
        }.run()
    }

    @Test
    fun `should load relative input files`() {
        // Arrange
        val inputPath = Path("target", "test-classes", "ontologies", "simple.puml")
        val outputPath = Path("java.io.tmpdir", "output.puml")
        val generator = DiagramGenerator { path, namespace ->
            // Assert
            assertThat(path).isEqualTo(inputPath.toAbsolutePath())
            Result.success(outputPath)
        }

        // Act
        DiagramCommand(generator).apply {
            input = inputPath.toString()
            namespace = "http://purl.org/net/ns/ex#"
        }.run()
    }

    @Test
    fun `should load absolute input files`() {
        // Arrange
        val inputPath = Path(System.getProperty("user.dir"), "target", "test-classes", "ontologies", "simple.puml").toAbsolutePath()
        val outputPath = Path("java.io.tmpdir", "output.puml")
        val generator = DiagramGenerator { path, namespace ->
            // Assert
            assertThat(path).isEqualTo(inputPath)
            Result.success(outputPath)
        }

        // Act
        DiagramCommand(generator).apply {
            input = inputPath.toString()
            namespace = "http://purl.org/net/ns/ex#"
        }.run()
    }
}