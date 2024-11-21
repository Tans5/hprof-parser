package com.tans.hprofparser

data class ObjectArray(
    val id: Long,
    val arrayClassName: String?,
    val stackTrace: StackTrace?,
    val arrayLength: Int,
    val elements: List<Any>
)