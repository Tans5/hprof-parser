package com.tans.hprofparser

data class StackTrace(
    val stackTraceSerialNumber: Int,
    val threadSerialNumber: Int,
    val stackFrames: List<StackFrame>
)