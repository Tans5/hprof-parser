package com.tans

import java.io.File
import java.io.Writer

class LocalFileLogPrinter(private val outputFile: File) : ILogPrinter {

    private val lineWriter: Writer by lazy {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        outputFile.createNewFile()
        outputFile.outputStream().writer(Charsets.UTF_8).buffered()
    }

    override fun printLine(l: String) {
        lineWriter.appendLine(l)
    }

    override fun flushBuffer() {
        lineWriter.flush()
    }

    override fun close() {
        lineWriter.close()
    }

}