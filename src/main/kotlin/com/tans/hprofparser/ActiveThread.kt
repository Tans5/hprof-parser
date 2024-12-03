package com.tans.hprofparser

data class ActiveThread(
    val id: Long,
    val threadSerialNumber: Int,
    val frameNumber: Int,
    val threadName: String?
)