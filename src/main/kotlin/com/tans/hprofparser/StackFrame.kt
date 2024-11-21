package com.tans.hprofparser

data class StackFrame(
    val id: Long,
    val methodName: String?,
    val methodSignature: String?,
    val sourceFileName: String?,
    val clazz: LoadedClass?,
    val lineNumber: Int
)