package it.mulders.owltool.impl

import it.mulders.owltool.DiagramGenerator
import it.mulders.owltool.DiagramWriter
import it.mulders.owltool.OntologyLoader
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@ApplicationScoped
class DefaultDiagramGenerator(
    val loader: OntologyLoader,
    val writer: DiagramWriter,
) : DiagramGenerator {
    override fun generateDiagram(
        path: Path,
        namespace: String,
    ): Result<Path> {
        log.info("Generating diagram for $path")

        return loader
            .load(path.inputStream(), namespace)
            .map { ontology ->
                log.info("Ontology loaded, found ${ontology.classCount()} classes in namespace $namespace")

                val outputPath = determineOutputPath(path)
                writer.generateDiagram(ontology, determineOutputStream(outputPath))
                outputPath
            }
            .onFailure { t ->
                log.error("Failed to generate diagram for $path", t)
            }
    }

    private fun determineOutputPath(inputPath: Path): Path {
        val extension = inputPath.extension
        val newFileName = inputPath.fileName.toString().replace(extension, "puml")
        return inputPath.parent.resolve(newFileName)
    }

    fun determineOutputStream(path: Path): OutputStream = path.outputStream()

    companion object {
        private val log = LoggerFactory.getLogger(DefaultDiagramGenerator::class.java)
    }
}
