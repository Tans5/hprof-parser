package com.tans

import java.io.Closeable

interface ILogPrinter: Closeable {

    fun printLine(l: String)

    fun flushBuffer()
}