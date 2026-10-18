package dev.simpilot

import java.io.InputStream
import java.io.OutputStream

internal object DiagnosticLogRepair {
    fun copyWithoutNullBytes(input: InputStream, output: OutputStream): Long {
        val source = ByteArray(DEFAULT_BUFFER_SIZE)
        val cleaned = ByteArray(DEFAULT_BUFFER_SIZE)
        var removed = 0L

        while (true) {
            val count = input.read(source)
            if (count < 0) break

            var cleanCount = 0
            for (index in 0 until count) {
                val byte = source[index]
                if (byte.toInt() == 0) {
                    removed++
                } else {
                    cleaned[cleanCount++] = byte
                }
            }
            if (cleanCount > 0) output.write(cleaned, 0, cleanCount)
        }
        return removed
    }
}
