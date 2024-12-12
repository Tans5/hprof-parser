package com.tans.hprofparser

data class ActiveThreadStackFrame(
    val id: Long,
    val threadSerialNumber: Int,
    val frameNumber: Int,
    val refInstance: Instance?
)
