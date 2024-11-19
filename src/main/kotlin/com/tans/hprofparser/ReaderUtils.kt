package com.tans.hprofparser

import okio.BufferedSource
import java.nio.charset.Charset

private val BOOLEAN_SIZE = PrimitiveType.BOOLEAN.byteSize
private val CHAR_SIZE = PrimitiveType.CHAR.byteSize
private val BYTE_SIZE = PrimitiveType.BYTE.byteSize
private val SHORT_SIZE = PrimitiveType.SHORT.byteSize
private val INT_SIZE = PrimitiveType.INT.byteSize
private val LONG_SIZE = PrimitiveType.LONG.byteSize

private val BOOLEAN_TYPE = PrimitiveType.BOOLEAN.hprofType
private val CHAR_TYPE = PrimitiveType.CHAR.hprofType
private val FLOAT_TYPE = PrimitiveType.FLOAT.hprofType
private val DOUBLE_TYPE = PrimitiveType.DOUBLE.hprofType
private val BYTE_TYPE = PrimitiveType.BYTE.hprofType
private val SHORT_TYPE = PrimitiveType.SHORT.hprofType
private val INT_TYPE = PrimitiveType.INT.hprofType
private val LONG_TYPE = PrimitiveType.LONG.hprofType

private const val INT_MASK = 0xffffffffL
private const val BYTE_MASK = 0xff

fun BufferedSource.readUnsignedInt(): Long {
    return readInt().toLong() and INT_MASK
}

fun BufferedSource.readUnsignedByte(): Int {
    return readByte().toInt() and BYTE_MASK
}

fun BufferedSource.readUnsignedShort(): Int {
    return readShort().toInt() and 0xFFFF
}

fun BufferedSource.readId(identifierByteSize: Int): Long {
    // As long as we don't interpret IDs, reading signed values here is fine.
    return when (identifierByteSize) {
        1 -> readByte().toLong()
        2 -> readShort().toLong()
        4 -> readInt().toLong()
        8 -> readLong()
        else -> throw IllegalArgumentException("ID Length must be 1, 2, 4, or 8")
    }
}

fun BufferedSource.readBoolean(): Boolean {
    return readByte()
        .toInt() != 0
}

fun BufferedSource.readString(
    byteCount: Int,
    charset: Charset
): String {
    return readString(byteCount.toLong(), charset)
}

fun BufferedSource.readChar(): Char {
    return readString(CHAR_SIZE, Charsets.UTF_16BE)[0]
}

fun BufferedSource.readFloat(): Float {
    return Float.fromBits(readInt())
}

fun BufferedSource.readDouble(): Double {
    return Double.fromBits(readLong())
}

fun BufferedSource.readValue(type: Int, identifierByteSize: Int): ValueHolder {
    return when (type) {
        PrimitiveType.REFERENCE_HPROF_TYPE -> ValueHolder.ReferenceHolder(readId(identifierByteSize))
        BOOLEAN_TYPE -> ValueHolder.BooleanHolder(readBoolean())
        CHAR_TYPE -> ValueHolder.CharHolder(readChar())
        FLOAT_TYPE -> ValueHolder.FloatHolder(readFloat())
        DOUBLE_TYPE -> ValueHolder.DoubleHolder(readDouble())
        BYTE_TYPE -> ValueHolder.ByteHolder(readByte())
        SHORT_TYPE -> ValueHolder.ShortHolder(readShort())
        INT_TYPE -> ValueHolder.IntHolder(readInt())
        LONG_TYPE -> ValueHolder.LongHolder(readLong())
        else -> throw IllegalStateException("Unknown type $type")
    }
}

fun BufferedSource.readConstField(identifierByteSize: Int): ConstField {
    val index = readUnsignedInt().toInt()
    val type = readUnsignedByte()
    val value = readValue(
        type = type,
        identifierByteSize = identifierByteSize
    )
    return ConstField(
        index = index,
        value = value
    )
}

fun BufferedSource.readStaticField(identifierByteSize: Int): StaticField {
    val nameStringId = readId(identifierByteSize)
    val type = readUnsignedByte()
    val value = readValue(type = type, identifierByteSize = identifierByteSize)
    return StaticField(
        nameStringId = nameStringId,
        value = value
    )
}

fun BufferedSource.readMemberField(identifierByteSize: Int): MemberField {
    val id = readId(identifierByteSize)
    val type = readUnsignedByte()
    return MemberField(
        nameStringId = id,
        type = type
    )
}