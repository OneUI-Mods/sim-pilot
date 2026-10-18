package dev.simpilot

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticLogRepairTest {
    @Test
    fun removesSparseNullRegionsWithoutChangingJsonLines() {
        val input =
            "{\"event\":\"before\"}\n".toByteArray() +
                ByteArray(4) +
                "{\"event\":\"after\"}\n".toByteArray()
        val output = ByteArrayOutputStream()

        val removed = DiagnosticLogRepair.copyWithoutNullBytes(ByteArrayInputStream(input), output)

        assertEquals(4, removed)
        assertArrayEquals(
            "{\"event\":\"before\"}\n{\"event\":\"after\"}\n".toByteArray(),
            output.toByteArray(),
        )
    }
}
