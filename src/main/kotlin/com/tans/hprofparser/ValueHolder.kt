package com.tans.hprofparser

sealed class ValueHolder {
    abstract val size: Int

    data class ReferenceHolder(
        val value: Long,
        override val size: Int
    ) : ValueHolder() {
        val isNull
            get() = value == NULL_REFERENCE
    }

    data class BooleanHolder(val value: Boolean, override val size: Int = PrimitiveType.BOOLEAN.byteSize) : ValueHolder()
    data class CharHolder(val value: Char, override val size: Int = PrimitiveType.CHAR.byteSize) : ValueHolder()
    data class FloatHolder(val value: Float, override val size: Int = PrimitiveType.FLOAT.byteSize) : ValueHolder()
    data class DoubleHolder(val value: Double, override val size: Int = PrimitiveType.DOUBLE.byteSize) : ValueHolder()
    data class ByteHolder(val value: Byte, override val size: Int = PrimitiveType.BYTE.byteSize) : ValueHolder()
    data class ShortHolder(val value: Short, override val size: Int = PrimitiveType.SHORT.byteSize) : ValueHolder()
    data class IntHolder(val value: Int, override val size: Int = PrimitiveType.INT.byteSize) : ValueHolder()
    data class LongHolder(val value: Long, override val size: Int = PrimitiveType.LONG.byteSize) : ValueHolder()


    companion object {
        const val NULL_REFERENCE = 0L
    }
}