package com.tans.hprofparser

import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.jvm.Throws

sealed class HprofRecord {

    data class StringRecord(
        val resId: Long,
        val string: String
    ) : HprofRecord()

    data class LoadClassRecord(
        val classSerialNumber: Int,
        val id: Long,
        val stackTraceSerialNumber: Int,
        val classNameStrId: Long,
        val className: StringRecord?
    ) : HprofRecord()

    data class RootUnknownRecord(
        val id: Long
    ) : HprofRecord()

    data class RootJniGlobalRecord(
        val id: Long,
        val refId: Long
    ) : HprofRecord()

    data class RootJniLocalRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val frameNumber: Int
    ) : HprofRecord()

    data class RootJavaFrameRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val frameNumber: Int
    ) : HprofRecord()

    data class RootNativeStackRecord(
        val id: Long,
        val threadSerialNumber: Int,
    ) : HprofRecord()

    data class RootStickyClassRecord(
        val id: Long
    ) : HprofRecord()

    data class RootThreadBlockRecord(
        val id: Long,
        val threadSerialNumber: Int,
    ) : HprofRecord()

    data class RootMonitorUsedRecord(
        val id: Long
    ) : HprofRecord()

    data class RootThreadObjectRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val frameNumber: Int
    ) : HprofRecord()

    data class RootInternedStringRecord(
        val id: Long
    ) : HprofRecord()

    data class RootFinalizingRecord(
        val id: Long
    ) : HprofRecord()

    data class RootDebuggerRecord(
        val id: Long
    ) : HprofRecord()

    data class RootReferenceCleanupRecord(
        val id: Long
    ) : HprofRecord()

    data class RootVmInternalRecord(
        val id: Long
    ) : HprofRecord()

    data class RootJniMonitorRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val stackDepth: Int
    ) : HprofRecord()

    data class RootUnReachableRecord(
        val id: Long
    ) : HprofRecord()

    data class HeapDumpRecord(
        val subRecords: List<HprofRecord>
    ) : HprofRecord()

    data class ClassDumpRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val superClassId: Long,
        val classLoaderId: Long,
        val signersId: Long,
        val protectionDomainId: Long,
        // in bytes
        val instanceSize: Int,
        val constFields: List<ConstField>,
        val staticFields: List<StaticField>,
        val memberFields: List<MemberField>
    ) : HprofRecord()


    data class InstanceDumpRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val classId: Long,
        val fieldValue: ByteArray
    ) : HprofRecord() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InstanceDumpRecord

            if (id != other.id) return false
            if (stackTraceSerialNumber != other.stackTraceSerialNumber) return false
            if (classId != other.classId) return false
            if (!fieldValue.contentEquals(other.fieldValue)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + stackTraceSerialNumber
            result = 31 * result + classId.hashCode()
            result = 31 * result + fieldValue.contentHashCode()
            return result
        }
    }

    data class ObjectArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val arrayLength: Int,
        val arrayClassId: Long,
        val elementIds: LongArray
    ) : HprofRecord() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ObjectArrayRecord

            if (id != other.id) return false
            if (stackTraceSerialNumber != other.stackTraceSerialNumber) return false
            if (arrayLength != other.arrayLength) return false
            if (arrayClassId != other.arrayClassId) return false
            if (!elementIds.contentEquals(other.elementIds)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + stackTraceSerialNumber
            result = 31 * result + arrayLength
            result = 31 * result + arrayClassId.hashCode()
            result = 31 * result + elementIds.contentHashCode()
            return result
        }
    }

    data class BoolArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: BooleanArray
    ) : HprofRecord()

    data class CharArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: CharArray
    ) : HprofRecord()

    data class FloatArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: FloatArray
    ) : HprofRecord()

    data class DoubleArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: DoubleArray
    ) : HprofRecord()

    data class ByteArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: ByteArray
    ) : HprofRecord()

    data class ShortArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: ShortArray
    ) : HprofRecord()

    data class IntArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: IntArray
    ) : HprofRecord()

    data class LongArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: LongArray
    ) : HprofRecord()

    data class HeapDumpInfoRecord(
        val heapId: Long,
        val stringId: Long
    ) : HprofRecord()

    data object HeapDumpEnd : HprofRecord()

    data class UnknownRecord(
        val tag: HprofRecordTag,
        val timeStamp: Int,
        val bodyLength: Int,
        val body: ByteArray
    ) : HprofRecord() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as UnknownRecord

            if (tag != other.tag) return false
            if (timeStamp != other.timeStamp) return false
            if (bodyLength != other.bodyLength) return false
            if (!body.contentEquals(other.body)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tag.hashCode()
            result = 31 * result + timeStamp
            result = 31 * result + bodyLength
            result = 31 * result + body.contentHashCode()
            return result
        }
    }
}

@Throws(IOException::class)
fun BufferedSource.parseRecords(header: HprofHeader): Map<Class<out HprofRecord>, List<HprofRecord>> {

    val strings = ArrayList<HprofRecord.StringRecord>()
    val loadClasses = ArrayList<HprofRecord.LoadClassRecord>()

    val rootUnknown = ArrayList<HprofRecord.RootUnknownRecord>()
    val rootJniGlobal = ArrayList<HprofRecord.RootJniGlobalRecord>()
    val rootJniLocal = ArrayList<HprofRecord.RootJniLocalRecord>()
    val rootJavaFrame = ArrayList<HprofRecord.RootJavaFrameRecord>()
    val rootNativeStack = ArrayList<HprofRecord.RootNativeStackRecord>()
    val rootStickyClass = ArrayList<HprofRecord.RootStickyClassRecord>()
    val rootThreadBlock = ArrayList<HprofRecord.RootThreadBlockRecord>()
    val rootMonitorUsed = ArrayList<HprofRecord.RootMonitorUsedRecord>()
    val rootThreadObject = ArrayList<HprofRecord.RootThreadObjectRecord>()
    val rootInternalStringRecord = ArrayList<HprofRecord.RootInternedStringRecord>()
    val rootFinalizing = ArrayList<HprofRecord.RootFinalizingRecord>()
    val rootDebugger = ArrayList<HprofRecord.RootDebuggerRecord>()
    val rootReferenceCleanup = ArrayList<HprofRecord.RootReferenceCleanupRecord>()
    val rootVmInternal = ArrayList<HprofRecord.RootVmInternalRecord>()
    val rootJniMonitor = ArrayList<HprofRecord.RootJniMonitorRecord>()
    val rootUnReachable = ArrayList<HprofRecord.RootUnReachableRecord>()

    val heapDumpRecord = ArrayList<HprofRecord.HeapDumpRecord>()

    val heapDumpEnd = ArrayList<HprofRecord.HeapDumpEnd>()

    val unknown = ArrayList<HprofRecord.UnknownRecord>()


    val ret = HashMap<Class<out HprofRecord>, List<HprofRecord>>()

    ret[HprofRecord.StringRecord::class.java] = strings
    ret[HprofRecord.LoadClassRecord::class.java] = loadClasses

    ret[HprofRecord.RootUnknownRecord::class.java] = rootUnknown
    ret[HprofRecord.RootJniGlobalRecord::class.java] = rootJniGlobal
    ret[HprofRecord.RootJniLocalRecord::class.java] = rootJniLocal
    ret[HprofRecord.RootJavaFrameRecord::class.java] = rootJavaFrame
    ret[HprofRecord.RootNativeStackRecord::class.java] = rootNativeStack
    ret[HprofRecord.RootStickyClassRecord::class.java] = rootStickyClass
    ret[HprofRecord.RootThreadBlockRecord::class.java] = rootThreadBlock
    ret[HprofRecord.RootMonitorUsedRecord::class.java] = rootMonitorUsed
    ret[HprofRecord.RootThreadObjectRecord::class.java] = rootThreadObject
    ret[HprofRecord.RootInternedStringRecord::class.java] = rootInternalStringRecord
    ret[HprofRecord.RootFinalizingRecord::class.java] = rootFinalizing
    ret[HprofRecord.RootDebuggerRecord::class.java] = rootDebugger
    ret[HprofRecord.RootReferenceCleanupRecord::class.java] = rootReferenceCleanup
    ret[HprofRecord.RootVmInternalRecord::class.java] = rootVmInternal
    ret[HprofRecord.RootJniMonitorRecord::class.java] = rootJniMonitor
    ret[HprofRecord.RootUnReachableRecord::class.java] = rootUnReachable

    ret[HprofRecord.HeapDumpRecord::class.java] = heapDumpRecord

    ret[HprofRecord.HeapDumpEnd::class.java] = heapDumpEnd

    ret[HprofRecord.UnknownRecord::class.java] = unknown

    val stringsMap = HashMap<Long, HprofRecord.StringRecord>()
    val loadClassesMap = HashMap<Long, HprofRecord.LoadClassRecord>()

    while (!exhausted()) {
        val tagInt = this.readUnsignedByte()
        val tag = HprofRecordTag.entries.find { it.tag == tagInt }
        if (tag == null) {
            throw IOException("Unknown tag=$tagInt")
        }
        val timeStamp = this.readInt()
        val bodyLength = this.readInt()

        when (tag) {

            /**
             * - resId
             * - string body
             */
            HprofRecordTag.STRING_IN_UTF8 -> {
                val sr = HprofRecord.StringRecord(
                    resId = readId(header.identifierByteSize),
                    string = readUtf8((bodyLength - header.identifierByteSize).toLong())
                )
                stringsMap[sr.resId] = sr
                strings.add(sr)
            }

            /**
             * - classSerialNumber(Int)
             * - id
             * - stackTraceSerialNumber(Int)
             * - classNameStringId
             */
            HprofRecordTag.LOAD_CLASS -> {
                val classSerialNumber = readInt()
                val id = readId(header.identifierByteSize)
                val stackTraceSerialNumber = readInt()
                val classNameStrId = readId(header.identifierByteSize)
                val r = HprofRecord.LoadClassRecord(
                   classSerialNumber = classSerialNumber,
                    id = id,
                    stackTraceSerialNumber = stackTraceSerialNumber,
                    classNameStrId = classNameStrId,
                    className = stringsMap[classNameStrId]
                )
                loadClasses.add(r)
                loadClassesMap[id] = r
            }

            HprofRecordTag.ROOT_UNKNOWN -> {
                val id = readId(header.identifierByteSize)
                val r = HprofRecord.RootUnknownRecord(id = id)
                rootUnknown.add(r)
            }

            /**
             * - id
             * - refId
             */
            HprofRecordTag.ROOT_JNI_GLOBAL -> {
                val r = HprofRecord.RootJniGlobalRecord(
                    id = readId(header.identifierByteSize),
                    refId = readId(header.identifierByteSize)
                )
                rootJniGlobal.add(r)
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - frameNumber(int)
             */
            HprofRecordTag.ROOT_JNI_LOCAL -> {
                val r = HprofRecord.RootJniLocalRecord(
                    id = readId(header.identifierByteSize),
                    threadSerialNumber = readInt(),
                    frameNumber = readInt()
                )
                rootJniLocal.add(r)
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - frameNumber(int)
             */
            HprofRecordTag.ROOT_JAVA_FRAME -> {
                val r = HprofRecord.RootJavaFrameRecord(
                    id = readId(header.identifierByteSize),
                    threadSerialNumber = readInt(),
                    frameNumber = readInt()
                )
                rootJavaFrame.add(r)
            }

            /**
             * - id
             * - threadSerialNumber(int)
             */
            HprofRecordTag.ROOT_NATIVE_STACK -> {
                val r = HprofRecord.RootNativeStackRecord(
                    id = readId(header.identifierByteSize),
                    threadSerialNumber = readInt()
                )
                rootNativeStack.add(r)
            }

            HprofRecordTag.ROOT_STICKY_CLASS -> {
                val r = HprofRecord.RootStickyClassRecord(
                    id = readId(header.identifierByteSize)
                )
                rootStickyClass.add(r)
            }

            /**
             * - id
             * - threadSerialNumber(int)
             */
            HprofRecordTag.ROOT_THREAD_BLOCK -> {
                val r = HprofRecord.RootThreadBlockRecord(
                    id = readId(header.identifierByteSize),
                    threadSerialNumber = readInt()
                )
                rootThreadBlock.add(r)
            }

            HprofRecordTag.ROOT_MONITOR_USED -> {
                val r = HprofRecord.RootMonitorUsedRecord(
                    id = readId(header.identifierByteSize)
                )
                rootMonitorUsed.add(r)
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - frameNumber(int)
             */
            HprofRecordTag.ROOT_THREAD_OBJECT -> {
                val r = HprofRecord.RootThreadObjectRecord(
                    id = readId(header.identifierByteSize),
                    threadSerialNumber = readInt(),
                    frameNumber = readInt()
                )
                rootThreadObject.add(r)
            }

            HprofRecordTag.ROOT_INTERNED_STRING -> {
                val r = HprofRecord.RootInternedStringRecord(
                    id = readId(header.identifierByteSize),
                )
                rootInternalStringRecord.add(r)
            }

            HprofRecordTag.ROOT_FINALIZING -> {
                val r = HprofRecord.RootFinalizingRecord(
                    id = readId(header.identifierByteSize)
                )
                rootFinalizing.add(r)
            }

            HprofRecordTag.ROOT_DEBUGGER -> {
                val r = HprofRecord.RootDebuggerRecord(
                    id = readId(header.identifierByteSize)
                )
                rootDebugger.add(r)
            }

            HprofRecordTag.ROOT_REFERENCE_CLEANUP -> {
                val r = HprofRecord.RootReferenceCleanupRecord(
                    id = readId(header.identifierByteSize)
                )
                rootReferenceCleanup.add(r)
            }

            HprofRecordTag.ROOT_VM_INTERNAL -> {
                val r = HprofRecord.RootVmInternalRecord(
                    id = readId(header.identifierByteSize)
                )
                rootVmInternal.add(r)
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - stackDepth(int)
             */
            HprofRecordTag.ROOT_JNI_MONITOR -> {
                val r = HprofRecord.RootJniMonitorRecord(
                    id = readId(header.identifierByteSize),
                    threadSerialNumber = readInt(),
                    stackDepth = readInt()
                )
                rootJniMonitor.add(r)
            }

            HprofRecordTag.ROOT_UNREACHABLE -> {
                val r = HprofRecord.RootUnReachableRecord(
                    id = readId(header.identifierByteSize)
                )
                rootUnReachable.add(r)
            }

            HprofRecordTag.HEAP_DUMP, HprofRecordTag.HEAP_DUMP_SEGMENT -> {
                val body = readByteArray(bodyLength.toLong())
                ByteArrayInputStream(body).source().buffer().use {
                    val subRecords = ArrayList<HprofRecord>()
                    while (!this.exhausted()) {
                        val subTagInt = readUnsignedByte()
                        val subTag = HprofRecordTag.entries.find { it.tag == subTagInt }
                        if (subTag == null) {
                            throw IOException("Unknown tag=$tagInt")
                        }
                        when (subTag) {
                            HprofRecordTag.ROOT_UNKNOWN -> {
                                val id = readId(header.identifierByteSize)
                                val r = HprofRecord.RootUnknownRecord(id = id)
                                subRecords.add(r)
                            }
                            HprofRecordTag.ROOT_JNI_GLOBAL -> {
                                val r = HprofRecord.RootJniGlobalRecord(
                                    id = readId(header.identifierByteSize),
                                    refId = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }
                            HprofRecordTag.ROOT_JNI_LOCAL -> {
                                val r = HprofRecord.RootJniLocalRecord(
                                    id = readId(header.identifierByteSize),
                                    threadSerialNumber = readInt(),
                                    frameNumber = readInt()
                                )
                                subRecords.add(r)
                            }
                            HprofRecordTag.ROOT_JAVA_FRAME -> {
                                val r = HprofRecord.RootJavaFrameRecord(
                                    id = readId(header.identifierByteSize),
                                    threadSerialNumber = readInt(),
                                    frameNumber = readInt()
                                )
                                subRecords.add(r)
                            }
                            HprofRecordTag.ROOT_NATIVE_STACK -> {
                                val r = HprofRecord.RootNativeStackRecord(
                                    id = readId(header.identifierByteSize),
                                    threadSerialNumber = readInt()
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_STICKY_CLASS -> {
                                val r = HprofRecord.RootStickyClassRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_THREAD_BLOCK -> {
                                val r = HprofRecord.RootThreadBlockRecord(
                                    id = readId(header.identifierByteSize),
                                    threadSerialNumber = readInt()
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_MONITOR_USED -> {
                                val r = HprofRecord.RootMonitorUsedRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_THREAD_OBJECT -> {
                                val r = HprofRecord.RootThreadObjectRecord(
                                    id = readId(header.identifierByteSize),
                                    threadSerialNumber = readInt(),
                                    frameNumber = readInt()
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_INTERNED_STRING -> {
                                val r = HprofRecord.RootInternedStringRecord(
                                    id = readId(header.identifierByteSize),
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_FINALIZING -> {
                                val r = HprofRecord.RootFinalizingRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_DEBUGGER -> {
                                val r = HprofRecord.RootDebuggerRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_REFERENCE_CLEANUP -> {
                                val r = HprofRecord.RootReferenceCleanupRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_VM_INTERNAL -> {
                                val r = HprofRecord.RootVmInternalRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_JNI_MONITOR -> {
                                val r = HprofRecord.RootJniMonitorRecord(
                                    id = readId(header.identifierByteSize),
                                    threadSerialNumber = readInt(),
                                    stackDepth = readInt()
                                )
                                subRecords.add(r)
                            }

                            HprofRecordTag.ROOT_UNREACHABLE -> {
                                val r = HprofRecord.RootUnReachableRecord(
                                    id = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
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
                                val id = readId(header.identifierByteSize)
                                val stackTraceSerialNumber = readInt()
                                val superClassId = readId(header.identifierByteSize)
                                val classLoaderId = readId(header.identifierByteSize)
                                val signersId = readId(header.identifierByteSize)
                                val protectionDomainId = readId(header.identifierByteSize)
                                skip(2 * header.identifierByteSize.toLong())
                                val instanceSize = readInt()
                                val constPoolSize = readUnsignedShort()
                                val constFields = ArrayList<ConstField>()
                                repeat(constPoolSize) {
                                    constFields.add(readConstField(header.identifierByteSize))
                                }
                                val staticFields = ArrayList<StaticField>()
                                val staticFieldSize = readUnsignedShort()
                                repeat(staticFieldSize) {
                                    staticFields.add(readStaticField(header.identifierByteSize))
                                }
                                val memberFields = ArrayList<MemberField>()
                                val memberFieldSize = readUnsignedShort()
                                repeat(memberFieldSize) {
                                    memberFields.add(readMemberField(header.identifierByteSize))
                                }
                                subRecords.add(HprofRecord.ClassDumpRecord(
                                    id = id,
                                    stackTraceSerialNumber = stackTraceSerialNumber,
                                    superClassId = superClassId,
                                    classLoaderId = classLoaderId,
                                    signersId = signersId,
                                    protectionDomainId = protectionDomainId,
                                    instanceSize = instanceSize,
                                    constFields = constFields,
                                    staticFields = staticFields,
                                    memberFields = memberFields
                                ))
                            }

                            /**
                             * - id
                             * - stackTraceSerialNumber(int)
                             * - classId
                             * - fieldSize(int)
                             * - fieldValue
                             */
                            HprofRecordTag.INSTANCE_DUMP -> {
                                val id = readId(header.identifierByteSize)
                                val stackTraceSerialNumber = readInt()
                                val classId = readId(header.identifierByteSize)
                                val byteSize = readInt()
                                val fieldValue = readByteArray(byteSize.toLong())
                                subRecords.add(
                                    HprofRecord.InstanceDumpRecord(
                                        id = id,
                                        stackTraceSerialNumber = stackTraceSerialNumber,
                                        classId = classId,
                                        fieldValue = fieldValue
                                    )
                                )
                            }

                            /**
                             * - id
                             * - stackTraceSerialNumber(int)
                             * - arrayLength(int)
                             * - arrayClassId
                             * - elementIds (count of arrayLength)
                             */
                            HprofRecordTag.OBJECT_ARRAY_DUMP -> {
                                val id = readId(header.identifierByteSize)
                                val stackTraceSerialNumber = readInt()
                                val arrayLength = readInt()
                                val arrayClassId = readId(header.identifierByteSize)
                                val elementIds = LongArray(arrayLength) { readId(header.identifierByteSize) }
                                subRecords.add(
                                    HprofRecord.ObjectArrayRecord(
                                        id = id,
                                        stackTraceSerialNumber = stackTraceSerialNumber,
                                        arrayLength = arrayLength,
                                        arrayClassId = arrayClassId,
                                        elementIds = elementIds
                                    )
                                )
                            }

                            /**
                             * - id
                             * - stackTraceSerialNumber(int)
                             * - arrayLength(int)
                             * - elementType(byte)
                             * - elementValues
                             */
                            HprofRecordTag.PRIMITIVE_ARRAY_DUMP -> {
                                val id = readId(header.identifierByteSize)
                                val stackTraceSerialNumber = readInt()
                                val arrayLength = readInt()
                                val r: HprofRecord = when(val type = readUnsignedByte()) {
                                    PrimitiveType.BOOLEAN.hprofType -> {
                                        HprofRecord.BoolArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = BooleanArray(arrayLength) { readByte().toInt() != 0 }
                                        )
                                    }

                                    PrimitiveType.CHAR.hprofType -> {
                                        HprofRecord.CharArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = CharArray(arrayLength) { readChar() }
                                        )
                                    }

                                    PrimitiveType.FLOAT.hprofType -> {
                                        HprofRecord.FloatArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = FloatArray(arrayLength) { readFloat() }
                                        )
                                    }

                                    PrimitiveType.DOUBLE.hprofType -> {
                                        HprofRecord.DoubleArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = DoubleArray(arrayLength) { readDouble() }
                                        )
                                    }

                                    PrimitiveType.BYTE.hprofType -> {
                                        HprofRecord.ByteArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = ByteArray(arrayLength) { readByte() }
                                        )
                                    }

                                    PrimitiveType.SHORT.hprofType -> {
                                        HprofRecord.ShortArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = ShortArray(arrayLength) { readShort() }
                                        )
                                    }

                                    PrimitiveType.INT.hprofType -> {
                                        HprofRecord.IntArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = IntArray(arrayLength) { readInt() }
                                        )
                                    }

                                    PrimitiveType.LONG.hprofType -> {
                                        HprofRecord.LongArrayRecord(
                                            id = id,
                                            stackTraceSerialNumber = stackTraceSerialNumber,
                                            array = LongArray(arrayLength) { readLong() }
                                        )
                                    }

                                    else -> {
                                        throw IOException("Wrong PrimitiveType: $type")
                                    }
                                }
                                subRecords.add(r)
                            }

                            HprofRecordTag.PRIMITIVE_ARRAY_NODATA -> {
                                throw IOException("Not support PRIMITIVE_ARRAY_NODATA")
                            }

                            /**
                             * - heapId
                             * - stringId
                             */
                            HprofRecordTag.HEAP_DUMP_INFO -> {
                                val r = HprofRecord.HeapDumpInfoRecord(
                                    heapId = readId(header.identifierByteSize),
                                    stringId = readId(header.identifierByteSize)
                                )
                                subRecords.add(r)
                            }

                            else -> {
                                throw IOException("Wrong subTag: $subTag")
                            }
                        }
                    }
                    val r = HprofRecord.HeapDumpRecord(
                        subRecords = subRecords
                    )
                    heapDumpRecord.add(r)
                }
            }

            HprofRecordTag.HEAP_DUMP_END -> {
                heapDumpEnd.add(HprofRecord.HeapDumpEnd)
            }

            else -> {
                val body = readByteArray(bodyLength.toLong())
                unknown.add(
                    HprofRecord.UnknownRecord(
                        tag = tag,
                        timeStamp = timeStamp,
                        bodyLength = bodyLength,
                        body = body
                    )
                )
            }
        }
    }
    return ret
}