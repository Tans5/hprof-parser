package com.tans.hprofparser

import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset

private val BOOLEAN_SIZE = PrimitiveType.BOOLEAN.byteSize
private val CHAR_SIZE = PrimitiveType.CHAR.byteSize
private val BYTE_SIZE = PrimitiveType.BYTE.byteSize
private val SHORT_SIZE = PrimitiveType.SHORT.byteSize
private val INT_SIZE = PrimitiveType.INT.byteSize
private val LONG_SIZE = PrimitiveType.LONG.byteSize
private val FLOAT_SIZE = PrimitiveType.FLOAT.byteSize
private val DOUBLE_SIZE = PrimitiveType.DOUBLE.byteSize

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
        PrimitiveType.REFERENCE_HPROF_TYPE -> ValueHolder.ReferenceHolder(readId(identifierByteSize), identifierByteSize)
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
        value = value,
        size = INT_SIZE + BYTE_SIZE + value.size
    )
}

fun BufferedSource.readStaticField(identifierByteSize: Int): StaticField {
    val nameStringId = readId(identifierByteSize)
    val type = readUnsignedByte()
    val value = readValue(type = type, identifierByteSize = identifierByteSize)
    return StaticField(
        nameStringId = nameStringId,
        value = value,
        size = identifierByteSize + BYTE_SIZE + value.size
    )
}

fun BufferedSource.readMemberField(identifierByteSize: Int): MemberField {
    val id = readId(identifierByteSize)
    val type = readUnsignedByte()
    return MemberField(
        nameStringId = id,
        type = type,
        size = identifierByteSize + BYTE_SIZE
    )
}

fun BufferedSource.readStringRecord(header: HprofHeader, bodyLength: Int): HprofRecord.StringRecord {
    return HprofRecord.StringRecord(
        resId = readId(header.identifierByteSize),
        string = readUtf8((bodyLength - header.identifierByteSize).toLong()),
        bodyLength = bodyLength
    )
}

fun BufferedSource.readLoadClassRecord(
    header: HprofHeader,
    stringsMap: Map<Long, HprofRecord.StringRecord>): HprofRecord.LoadClassRecord {
    val classSerialNumber = readInt()
    val id = readId(header.identifierByteSize)
    val stackTraceSerialNumber = readInt()
    val classNameStrId = readId(header.identifierByteSize)
    return HprofRecord.LoadClassRecord(
        classSerialNumber = classSerialNumber,
        id = id,
        stackTraceSerialNumber = stackTraceSerialNumber,
        classNameStrId = classNameStrId,
        className = stringsMap[classNameStrId],
        bodyLength = INT_SIZE * 2 + header.identifierByteSize * 2
    )
}

fun BufferedSource.readRootUnknownRecord(
    header: HprofHeader
): HprofRecord.RootUnknownRecord {
    return HprofRecord.RootUnknownRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootJniGlobalRecord(
    header: HprofHeader
): HprofRecord.RootJniGlobalRecord {
    return HprofRecord.RootJniGlobalRecord(
        id = readId(header.identifierByteSize),
        refId = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize * 2
    )
}

fun BufferedSource.readRootJniLocalRecord(
    header: HprofHeader
): HprofRecord.RootJniLocalRecord {
    return HprofRecord.RootJniLocalRecord(
        id = readId(header.identifierByteSize),
        threadSerialNumber = readInt(),
        frameNumber = readInt(),
        bodyLength = header.identifierByteSize + INT_SIZE * 2
    )
}

fun BufferedSource.readRootJavaFrameRecord(
    header: HprofHeader
): HprofRecord.RootJavaFrameRecord {
    return HprofRecord.RootJavaFrameRecord(
        id = readId(header.identifierByteSize),
        threadSerialNumber = readInt(),
        frameNumber = readInt(),
        bodyLength = header.identifierByteSize + INT_SIZE * 2
    )
}

fun BufferedSource.readRootNativeStackRecord(
    header: HprofHeader
): HprofRecord.RootNativeStackRecord {
    return HprofRecord.RootNativeStackRecord(
        id = readId(header.identifierByteSize),
        threadSerialNumber = readInt(),
        bodyLength = header.identifierByteSize + INT_SIZE
    )
}

fun BufferedSource.readRootStickyClassRecord(
    header: HprofHeader
): HprofRecord.RootStickyClassRecord {
    return HprofRecord.RootStickyClassRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootThreadBlockRecord(
    header: HprofHeader
): HprofRecord.RootThreadBlockRecord {
    return HprofRecord.RootThreadBlockRecord(
        id = readId(header.identifierByteSize),
        threadSerialNumber = readInt(),
        bodyLength = header.identifierByteSize + INT_SIZE
    )
}

fun BufferedSource.readRootMonitorUsedRecord(
    header: HprofHeader
): HprofRecord.RootMonitorUsedRecord {
    return HprofRecord.RootMonitorUsedRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootThreadObjectRecord(
    header: HprofHeader
): HprofRecord.RootThreadObjectRecord {
    return HprofRecord.RootThreadObjectRecord(
        id = readId(header.identifierByteSize),
        threadSerialNumber = readInt(),
        frameNumber = readInt(),
        bodyLength = header.identifierByteSize + INT_SIZE * 2
    )
}

fun BufferedSource.readRootInternedStringRecord(
    header: HprofHeader
): HprofRecord.RootInternedStringRecord {
    return HprofRecord.RootInternedStringRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootFinalizingRecord(
    header: HprofHeader
): HprofRecord.RootFinalizingRecord {
    return HprofRecord.RootFinalizingRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootDebuggerRecord(
    header: HprofHeader
): HprofRecord.RootDebuggerRecord {
    return HprofRecord.RootDebuggerRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootReferenceCleanupRecord(
    header: HprofHeader
): HprofRecord.RootReferenceCleanupRecord {
    return HprofRecord.RootReferenceCleanupRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootVmInternalRecord(
    header: HprofHeader
): HprofRecord.RootVmInternalRecord {
    return HprofRecord.RootVmInternalRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readRootJniMonitorRecord(
    header: HprofHeader
): HprofRecord.RootJniMonitorRecord {
    return HprofRecord.RootJniMonitorRecord(
        id = readId(header.identifierByteSize),
        threadSerialNumber = readInt(),
        stackDepth = readInt(),
        bodyLength = header.identifierByteSize + INT_SIZE * 2
    )
}

fun BufferedSource.readRootUnreachableRecord(
    header: HprofHeader
): HprofRecord.RootUnReachableRecord {
    return HprofRecord.RootUnReachableRecord(
        id = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize
    )
}

fun BufferedSource.readClassDumpRecord(
    header: HprofHeader
): HprofRecord.ClassDumpRecord {
    var bodySize = 0
    val id = readId(header.identifierByteSize)
    bodySize += INT_SIZE
    val stackTraceSerialNumber = readInt()
    bodySize += INT_SIZE
    val superClassId = readId(header.identifierByteSize)
    bodySize += header.identifierByteSize
    val classLoaderId = readId(header.identifierByteSize)
    bodySize += header.identifierByteSize
    val signersId = readId(header.identifierByteSize)
    bodySize += header.identifierByteSize
    val protectionDomainId = readId(header.identifierByteSize)
    bodySize += header.identifierByteSize
    skip(2 * header.identifierByteSize.toLong())
    bodySize += header.identifierByteSize * 2
    val instanceSize = readInt()
    bodySize += INT_SIZE
    val constPoolSize = readUnsignedShort()
    bodySize += SHORT_SIZE
    val constFields = ArrayList<ConstField>()
    repeat(constPoolSize) {
        constFields.add(readConstField(header.identifierByteSize).apply { bodySize += size })
    }
    val staticFields = ArrayList<StaticField>()
    val staticFieldSize = readUnsignedShort()
    bodySize += SHORT_SIZE
    repeat(staticFieldSize) {
        staticFields.add(readStaticField(header.identifierByteSize).apply { bodySize += size })
    }
    val memberFields = ArrayList<MemberField>()
    val memberFieldSize = readUnsignedShort()
    bodySize += SHORT_SIZE
    repeat(memberFieldSize) {
        memberFields.add(readMemberField(header.identifierByteSize).apply { bodySize += size })
    }
    return HprofRecord.ClassDumpRecord(
        id = id,
        stackTraceSerialNumber = stackTraceSerialNumber,
        superClassId = superClassId,
        classLoaderId = classLoaderId,
        signersId = signersId,
        protectionDomainId = protectionDomainId,
        instanceSize = instanceSize,
        constFields = constFields,
        staticFields = staticFields,
        memberFields = memberFields,
        bodyLength = bodySize
    )
}

fun BufferedSource.readInstanceDumpRecord(
    header: HprofHeader
): HprofRecord.InstanceDumpRecord {
    val id = readId(header.identifierByteSize)
    val stackTraceSerialNumber = readInt()
    val classId = readId(header.identifierByteSize)
    val byteSize = readInt()
    val fieldValue = readByteArray(byteSize.toLong())
    return HprofRecord.InstanceDumpRecord(
        id = id,
        stackTraceSerialNumber = stackTraceSerialNumber,
        classId = classId,
        fieldValue = fieldValue,
        bodyLength = header.identifierByteSize * 2 + INT_SIZE * 2 + LONG_SIZE
    )
}

fun BufferedSource.readObjectArrayDumpRecord(
    header: HprofHeader
): HprofRecord.ObjectArrayRecord {
    val id = readId(header.identifierByteSize)
    val stackTraceSerialNumber = readInt()
    val arrayLength = readInt()
    val arrayClassId = readId(header.identifierByteSize)
    val elementIds = LongArray(arrayLength) { readId(header.identifierByteSize) }
    return HprofRecord.ObjectArrayRecord(
        id = id,
        stackTraceSerialNumber = stackTraceSerialNumber,
        arrayLength = arrayLength,
        arrayClassId = arrayClassId,
        elementIds = elementIds,
        bodyLength = header.identifierByteSize * (2 + arrayLength) + INT_SIZE * 2
    )
}

fun BufferedSource.readPrimitiveArrayDumpRecord(
    header: HprofHeader
): HprofRecord {
    var bodyLength: Int = 0
    val id = readId(header.identifierByteSize)
    bodyLength += header.identifierByteSize
    val stackTraceSerialNumber = readInt()
    bodyLength += INT_SIZE
    val arrayLength = readInt()
    bodyLength += INT_SIZE
    val r: HprofRecord = when(val type = readUnsignedByte()) {
        PrimitiveType.BOOLEAN.hprofType -> {
            HprofRecord.BoolArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = BooleanArray(arrayLength) { readByte().toInt() != 0 },
                bodyLength = bodyLength + arrayLength * BOOLEAN_SIZE
            )
        }

        PrimitiveType.CHAR.hprofType -> {
            HprofRecord.CharArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = CharArray(arrayLength) { readChar() },
                bodyLength = bodyLength + arrayLength * CHAR_SIZE
            )
        }

        PrimitiveType.FLOAT.hprofType -> {
            HprofRecord.FloatArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = FloatArray(arrayLength) { readFloat() },
                bodyLength = bodyLength + arrayLength * FLOAT_SIZE
            )
        }

        PrimitiveType.DOUBLE.hprofType -> {
            HprofRecord.DoubleArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = DoubleArray(arrayLength) { readDouble() },
                bodyLength = bodyLength + arrayLength * DOUBLE_SIZE
            )
        }

        PrimitiveType.BYTE.hprofType -> {
            HprofRecord.ByteArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = ByteArray(arrayLength) { readByte() },
                bodyLength = bodyLength + arrayLength * BYTE_SIZE
            )
        }

        PrimitiveType.SHORT.hprofType -> {
            HprofRecord.ShortArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = ShortArray(arrayLength) { readShort() },
                bodyLength = bodyLength + arrayLength * SHORT_SIZE
            )
        }

        PrimitiveType.INT.hprofType -> {
            HprofRecord.IntArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = IntArray(arrayLength) { readInt() },
                bodyLength = bodyLength + arrayLength * INT_SIZE
            )
        }

        PrimitiveType.LONG.hprofType -> {
            HprofRecord.LongArrayRecord(
                id = id,
                stackTraceSerialNumber = stackTraceSerialNumber,
                array = LongArray(arrayLength) { readLong() },
                bodyLength = bodyLength + arrayLength * LONG_SIZE
            )
        }

        else -> {
            throw IOException("Wrong PrimitiveType: $type")
        }
    }
    return r
}

fun BufferedSource.readHeapDumpInfoRecord(
    header: HprofHeader
): HprofRecord.HeapDumpInfoRecord {
    return HprofRecord.HeapDumpInfoRecord(
        heapId = readId(header.identifierByteSize),
        stringId = readId(header.identifierByteSize),
        bodyLength = header.identifierByteSize * 2
    )
}

fun BufferedSource.readHeapDumpRecord(
    header: HprofHeader,
    bodyLength: Int): HprofRecord.HeapDumpRecord {
    val body = readByteArray(bodyLength.toLong())
    ByteArrayInputStream(body).source().buffer().use {
        with(it) {
            val subRecords = ArrayList<HprofRecord>()
            while (!this.exhausted()) {
                val subTagInt = readUnsignedByte()
                val subTag = HprofRecordTag.entries.find { it.tag == subTagInt }
                if (subTag == null) {
                    throw IOException("Unknown tag=$subTagInt")
                }
                when (subTag) {
                    HprofRecordTag.ROOT_UNKNOWN -> {
                        subRecords.add(readRootUnknownRecord(header))
                    }
                    HprofRecordTag.ROOT_JNI_GLOBAL -> {
                        subRecords.add(readRootJniGlobalRecord(header))
                    }
                    HprofRecordTag.ROOT_JNI_LOCAL -> {
                        subRecords.add(readRootJniLocalRecord(header))
                    }
                    HprofRecordTag.ROOT_JAVA_FRAME -> {
                        subRecords.add(readRootJavaFrameRecord(header))
                    }
                    HprofRecordTag.ROOT_NATIVE_STACK -> {
                        subRecords.add(readRootNativeStackRecord(header))
                    }
                    HprofRecordTag.ROOT_STICKY_CLASS -> {
                        subRecords.add(readRootStickyClassRecord(header))
                    }
                    HprofRecordTag.ROOT_THREAD_BLOCK -> {
                        subRecords.add(readRootThreadBlockRecord(header))
                    }
                    HprofRecordTag.ROOT_MONITOR_USED -> {
                        subRecords.add(readRootMonitorUsedRecord(header))
                    }
                    HprofRecordTag.ROOT_THREAD_OBJECT -> {
                        subRecords.add(readRootThreadObjectRecord(header))
                    }
                    HprofRecordTag.ROOT_INTERNED_STRING -> {
                        subRecords.add(readRootInternedStringRecord(header))
                    }
                    HprofRecordTag.ROOT_FINALIZING -> {
                        subRecords.add(readRootFinalizingRecord(header))
                    }
                    HprofRecordTag.ROOT_DEBUGGER -> {
                        subRecords.add(readRootDebuggerRecord(header))
                    }
                    HprofRecordTag.ROOT_REFERENCE_CLEANUP -> {
                        subRecords.add(readRootReferenceCleanupRecord(header))
                    }

                    HprofRecordTag.ROOT_VM_INTERNAL -> {
                        subRecords.add(readRootVmInternalRecord(header))
                    }

                    HprofRecordTag.ROOT_JNI_MONITOR -> {
                        subRecords.add(readRootJniMonitorRecord(header))
                    }

                    HprofRecordTag.ROOT_UNREACHABLE -> {
                        subRecords.add(readRootUnreachableRecord(header))
                    }

                    /**
                     * - id
                     * - stackTraceSerialNumber(int)
                     * - superClassId
                     * - classLoaderId
                     * - signersId
                     * - protectionDomainId
                     * - skip 2 * identifierSize
                     * - instanceSize(int) // in bytes
                     * - constPoolCount(short)
                     *    - index(short)
                     *    - size(byte)
                     *    - value
                     * - staticFieldCount(short)
                     *    - id
                     *    - type(byte)
                     *    - value
                     * - memberFieldCount(short)
                     *    - id
                     *    - type(byte)
                     */
                    HprofRecordTag.CLASS_DUMP -> {
                        subRecords.add(readClassDumpRecord(header))
                    }

                    /**
                     * - id
                     * - stackTraceSerialNumber(int)
                     * - classId
                     * - fieldSize(int)
                     * - fieldValue
                     */
                    HprofRecordTag.INSTANCE_DUMP -> {
                        subRecords.add(readInstanceDumpRecord(header))
                    }

                    /**
                     * - id
                     * - stackTraceSerialNumber(int)
                     * - arrayLength(int)
                     * - arrayClassId
                     * - elementIds (count of arrayLength)
                     */
                    HprofRecordTag.OBJECT_ARRAY_DUMP -> {
                        subRecords.add(readObjectArrayDumpRecord(header))
                    }

                    /**
                     * - id
                     * - stackTraceSerialNumber(int)
                     * - arrayLength(int)
                     * - elementType(byte)
                     * - elementValues
                     */
                    HprofRecordTag.PRIMITIVE_ARRAY_DUMP -> {
                        subRecords.add(readPrimitiveArrayDumpRecord(header))
                    }

                    HprofRecordTag.PRIMITIVE_ARRAY_NODATA -> {
                        throw IOException("Not support PRIMITIVE_ARRAY_NODATA")
                    }

                    /**
                     * - heapId
                     * - stringId
                     */
                    HprofRecordTag.HEAP_DUMP_INFO -> {
                        subRecords.add(readHeapDumpInfoRecord(header))
                    }

                    else -> {
                        throw IOException("Wrong subTag: $subTag")
                    }
                }
            }
            return HprofRecord.HeapDumpRecord(
                subRecords = subRecords,
                bodyLength = bodyLength
            )
        }
    }
}
