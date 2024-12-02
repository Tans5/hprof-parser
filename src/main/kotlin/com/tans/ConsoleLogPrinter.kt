package com.tans

object ConsoleLogPrinter : ILogPrinter {

    override fun printLine(l: String) {
        println(l)
    }

    override fun flushBuffer() {  }

    override fun close() { }
}