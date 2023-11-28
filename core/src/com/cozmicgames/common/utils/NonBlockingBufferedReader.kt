package com.cozmicgames.server.utils

import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class NonBlockingBufferedReader(reader: Reader) : Closeable {
    constructor(input: InputStream) : this(InputStreamReader(input))

    private val lines = LinkedBlockingQueue<String>()
    private var closed = false
    private var backgroundReaderThread = thread(isDaemon = true) {
        var bufferedReader: BufferedReader? = null
        try {
            bufferedReader = if (reader is BufferedReader) reader else BufferedReader(reader)

            while (!Thread.interrupted()) {
                val line = bufferedReader.readLine() ?: break
                lines.add(line)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            closed = true
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    fun readLine(): String? {
        return try {
            if (closed && lines.size == 0) null else lines.poll(0L, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            throw IOException("The BackgroundReaderThread was interrupted!")
        }
    }

    override fun close() {
        backgroundReaderThread.interrupt()
        backgroundReaderThread.join()
    }
}
