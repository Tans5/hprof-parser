package com.tans.hprofparser

sealed class ValueHolder {
    data class ReferenceHolder(val value: Long) : ValueHolder() {
        val isNull
            get() = value == NULL_REFERENCE
    }

    data class BooleanHolder(val value: Boolean) : ValueHolder()
    data class CharHolder(val value: Char) : ValueHolder()
    data class FloatHolder(val value: Float) : ValueHolder()
    data class DoubleHolder(val value: Double) : ValueHolder()
    data class ByteHolder(val value: Byte) : ValueHolder()
    data class ShortHolder(val value: Short) : ValueHolder()
    data class IntHolder(val value: Int) : ValueHolder()
    data class LongHolder(val value: Long) : ValueHolder()

    companion object {
        const val NULL_REFERENCE = 0L
    }
}