package com.tans.hprofparser

import okio.BufferedSource
import java.io.IOException
import kotlin.jvm.Throws

sealed class HprofRecord {

    abstract val bodyLength: Int

    data class StringRecord(
        val resId: Long,
        val string: String,
        override val bodyLength: Int
    ) : HprofRecord()

    data class LoadClassRecord(
        val classSerialNumber: Int,
        val id: Long,
        val stackTraceSerialNumber: Int,
        val classNameStrId: Long,
        val className: StringRecord?,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootUnknownRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootJniGlobalRecord(
        val id: Long,
        val refId: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootJniLocalRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val frameNumber: Int,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootJavaFrameRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val frameNumber: Int,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootNativeStackRecord(
        val id: Long,
        val threadSerialNumber: Int,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootStickyClassRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootThreadBlockRecord(
        val id: Long,
        val threadSerialNumber: Int,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootMonitorUsedRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootThreadObjectRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val frameNumber: Int,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootInternedStringRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootFinalizingRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootDebuggerRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootReferenceCleanupRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootVmInternalRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootJniMonitorRecord(
        val id: Long,
        val threadSerialNumber: Int,
        val stackDepth: Int,
        override val bodyLength: Int
    ) : HprofRecord()

    data class RootUnReachableRecord(
        val id: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data class HeapDumpRecord(
        val subRecords: List<HprofRecord>,
        override val bodyLength: Int
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
        val memberFields: List<MemberField>,
        override val bodyLength: Int
    ) : HprofRecord()


    data class InstanceDumpRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val classId: Long,
        val fieldValue: ByteArray,
        override val bodyLength: Int
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
        val elementIds: LongArray,
        override val bodyLength: Int
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
        val array: BooleanArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class CharArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: CharArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class FloatArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: FloatArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class DoubleArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: DoubleArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class ByteArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: ByteArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class ShortArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: ShortArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class IntArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: IntArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class LongArrayRecord(
        val id: Long,
        val stackTraceSerialNumber: Int,
        val array: LongArray,
        override val bodyLength: Int
    ) : HprofRecord()

    data class HeapDumpInfoRecord(
        val heapId: Long,
        val stringId: Long,
        override val bodyLength: Int
    ) : HprofRecord()

    data object HeapDumpEnd : HprofRecord() {
        override val bodyLength: Int = 0
    }

    data class UnknownRecord(
        val tag: HprofRecordTag,
        val timeStamp: Int,
        override val bodyLength: Int,
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
                val sr = readStringRecord(header, bodyLength)
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
                val r = readLoadClassRecord(header, stringsMap)
                loadClasses.add(r)
                loadClassesMap[r.id] = r
            }

            HprofRecordTag.ROOT_UNKNOWN -> {
                rootUnknown.add(readRootUnknownRecord(header))
            }

            /**
             * - id
             * - refId
             */
            HprofRecordTag.ROOT_JNI_GLOBAL -> {
                rootJniGlobal.add(readRootJniGlobalRecord(header))
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - frameNumber(int)
             */
            HprofRecordTag.ROOT_JNI_LOCAL -> {
                rootJniLocal.add(readRootJniLocalRecord(header))
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - frameNumber(int)
             */
            HprofRecordTag.ROOT_JAVA_FRAME -> {
                rootJavaFrame.add(readRootJavaFrameRecord(header))
            }

            /**
             * - id
             * - threadSerialNumber(int)
             */
            HprofRecordTag.ROOT_NATIVE_STACK -> {
                rootNativeStack.add(readRootNativeStackRecord(header))
            }

            HprofRecordTag.ROOT_STICKY_CLASS -> {
                rootStickyClass.add(readRootStickyClassRecord(header))
            }

            /**
             * - id
             * - threadSerialNumber(int)
             */
            HprofRecordTag.ROOT_THREAD_BLOCK -> {
                rootThreadBlock.add(readRootThreadBlockRecord(header))
            }

            HprofRecordTag.ROOT_MONITOR_USED -> {
                rootMonitorUsed.add(readRootMonitorUsedRecord(header))
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - frameNumber(int)
             */
            HprofRecordTag.ROOT_THREAD_OBJECT -> {
                rootThreadObject.add(readRootThreadObjectRecord(header))
            }

            HprofRecordTag.ROOT_INTERNED_STRING -> {
                rootInternalStringRecord.add(readRootInternedStringRecord(header))
            }

            HprofRecordTag.ROOT_FINALIZING -> {
                rootFinalizing.add(readRootFinalizingRecord(header))
            }

            HprofRecordTag.ROOT_DEBUGGER -> {
                rootDebugger.add(readRootDebuggerRecord(header))
            }

            HprofRecordTag.ROOT_REFERENCE_CLEANUP -> {
                rootReferenceCleanup.add(readRootReferenceCleanupRecord(header))
            }

            HprofRecordTag.ROOT_VM_INTERNAL -> {
                rootVmInternal.add(readRootVmInternalRecord(header))
            }

            /**
             * - id
             * - threadSerialNumber(int)
             * - stackDepth(int)
             */
            HprofRecordTag.ROOT_JNI_MONITOR -> {
                rootJniMonitor.add(readRootJniMonitorRecord(header))
            }

            HprofRecordTag.ROOT_UNREACHABLE -> {
                rootUnReachable.add(readRootUnreachableRecord(header))
            }

            HprofRecordTag.HEAP_DUMP, HprofRecordTag.HEAP_DUMP_SEGMENT -> {
                heapDumpRecord.add(readHeapDumpRecord(header, bodyLength))
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