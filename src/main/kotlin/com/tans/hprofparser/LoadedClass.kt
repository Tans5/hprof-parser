package com.tans.hprofparser

data class LoadedClass(
    val classSerialNumber: Int,
    val id: Long,
    val stackTrackSerialNumber: Int,
    val className: String?
)