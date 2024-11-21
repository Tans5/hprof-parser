package com.tans.hprofparser

data class StaticField(
    val nameStringId: Long,
    val nameString: String? = null,
    val value: ValueHolder,
    val size: Int
)