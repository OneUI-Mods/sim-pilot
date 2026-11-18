package dev.simpilot

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/** One deadline for input, output and exit, with bounded capture and scoped cleanup. */
internal object CommandOutput {
    data class Result(val code: Int, val text: String)

    fun collect(
        process: Process,
        timeoutMillis: Long,
        maximumBytes: Int,
        separateError: Boolean = false,
        input: InputStream? = null,
    ): Result {
        require(timeoutMillis in 1..120_000 && maximumBytes in 1..8_388_608)
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
        val io = Executors.newCachedThreadPool { task ->
            Thread(task, "CommandOutput").apply { isDaemon = true }
        }
        fun <T> submit(task: () -> T) =
            io.submit(
                Callable {
                    try {
                        task()
                    } catch (error: Throwable) {
                        process.destroyForcibly()
                        throw error
                    }
                },
            )
        val output = submit { read(process.inputStream, maximumBytes) }
        val errors = if (separateError) submit { read(process.errorStream, maximumBytes) } else null
        val writer = input?.let { stream ->
            submit {
                stream.use { source -> process.outputStream.use { source.copyTo(it) } }
            }
        }
        fun remaining(): Long {
            val nanos = deadline - System.nanoTime()
            if (nanos <= 0) throw TimeoutException("Command timed out")
            return nanos
        }
        try {
            if (input == null) process.outputStream.close()
            if (!process.waitFor(remaining(), TimeUnit.NANOSECONDS))
                throw TimeoutException("Command timed out")
            writer?.get(remaining(), TimeUnit.NANOSECONDS)
            val stdout = output.get(remaining(), TimeUnit.NANOSECONDS)
            val stderr = errors?.get(remaining(), TimeUnit.NANOSECONDS) ?: ByteArray(0)
            if (stdout.size + stderr.size > maximumBytes)
                throw IOException("Command output too large")
            return Result(
                process.exitValue(),
                String(stdout, Charsets.UTF_8) +
                    if (stderr.isEmpty()) "" else "\n" + String(stderr, Charsets.UTF_8),
            )
        } catch (error: ExecutionException) {
            throw IOException("Command I/O failed", error.cause)
        } catch (error: TimeoutException) {
            throw IOException("Command timed out", error)
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw error
        } finally {
            process.destroyForcibly()
            output.cancel(true)
            errors?.cancel(true)
            writer?.cancel(true)
            io.shutdownNow()
        }
    }

    private fun read(stream: InputStream, maximumBytes: Int): ByteArray = stream.use { input ->
        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (bytes.size() + count > maximumBytes) throw IOException("Command output too large")
            bytes.write(buffer, 0, count)
        }
        bytes.toByteArray()
    }
}
