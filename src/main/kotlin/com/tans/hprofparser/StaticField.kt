package com.tans.hprofparser

data class StaticField(
    val nameStringId: Long,
    val value: ValueHolder,
    val size: Int
)