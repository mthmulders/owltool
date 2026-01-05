package it.mulders.owltool.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class DatatypePropertyTest {
    @Test
    fun `format dataType in Turtle syntax`() {
        // Arrange
        val p = DatatypeProperty("age", "integer", "xsd")

        // Act& Assert
        assertThat(p.dataType()).isEqualTo("xsd:integer")
    }
}