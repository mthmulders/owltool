package it.mulders.owltool.model

import assertk.all
import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ObjectPropertyTest {
    @Test
    fun `format dataType in Turtle syntax for named classes`() {
        // Arrange
        val p = ObjectProperty("age", Class.of("http://purl.org/net/ns/ex#", "Person"), "ex")

        // Act& Assert
        assertThat(p.dataType()).isEqualTo("ex:Person")
    }

    @Test
    fun `format dataType in Turtle syntax for unnamed classes`() {
        // Arrange
        val p = ObjectProperty("age", Class.of(null, null), null)

        // Act& Assert
        assertThat(p.dataType()).all {
            doesNotContain("ex")
            doesNotContain(":")
            doesNotContain("Person")
        }
    }
}