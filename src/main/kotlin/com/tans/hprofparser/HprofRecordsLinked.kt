package com.tans.hprofparser

import java.io.IOException

data class HprofRecordsLinked(
    val stringsDic: Map<Long, HprofRecord.StringRecord>,
    val loadedClassesDic: Map<Long, LoadedClass>,
    val loadedClassesSerialNumberDic: Map<Int, LoadedClass>,
    val stackFramesDic: Map<Long, StackFrame>,
    val stackTracesDic: Map<Int, StackTrace>,
    val rootsDic: Map<Long, HprofRecord>,
    val instancesDic: Map<Long, Instance>,
    val classDumpsDic: Map<Long, ClassDump>,
    val heapDumpInfoDic: Map<Long, HeapDumpInfo>
) {

    fun queryString(id: Long): HprofRecord.StringRecord? = stringsDic[id]

    fun queryLoadClass(id: Long): LoadedClass? = loadedClassesDic[id]

    fun queryLoadClassBySerialNumber(serialNumber: Int): LoadedClass? = loadedClassesSerialNumberDic[serialNumber]

    fun queryStackFrame(id: Long): StackFrame? = stackFramesDic[id]

    fun queryStackTrackBySerialNumber(serialNumber: Int): StackTrace? = stackTracesDic[serialNumber]

    fun queryRoot(id: Long): HprofRecord? = rootsDic[id]

    fun queryInstance(id: Long): Instance? = instancesDic[id]

    fun queryClassDump(id: Long): ClassDump? = classDumpsDic[id]

    fun queryHeapDumpInfo(id: Long): HeapDumpInfo? = heapDumpInfoDic[id]
}

@Suppress("UNCHECKED_CAST")
fun linkRecords(records: Map<Class<*>, List<HprofRecord>>): HprofRecordsLinked {

    // Strings
    val stringsDic = HashMap<Long, HprofRecord.StringRecord>()
    val stringRecords = (records[HprofRecord.StringRecord::class.java] ?: emptyList()) as List<HprofRecord.StringRecord>
    for (r in stringRecords) {
        stringsDic[r.resId] = r
    }

    // Loaded classes
    val loadedClassesDic = HashMap<Long, LoadedClass>()
    val loadedClassesSerialNumberDic = HashMap<Int, LoadedClass>()
    val classesRecords = (records[HprofRecord.LoadedClassRecord::class.java] ?: emptyList()) as List<HprofRecord.LoadedClassRecord>
    for (r in classesRecords) {
        val loadedClass = LoadedClass(
            classSerialNumber = r.classSerialNumber,
            id = r.id,
            stackTrackSerialNumber = r.stackTraceSerialNumber,
            className = stringsDic[r.classNameStrId]?.string
        )
        loadedClassesDic[r.id] = loadedClass
        loadedClassesSerialNumberDic[r.classSerialNumber] = loadedClass
    }

    // Stack frames
    val stackFramesDic = HashMap<Long, StackFrame>()
    val stackFramesRecords = (records[HprofRecord.StackFrameRecord::class.java] ?: emptyList()) as List<HprofRecord.StackFrameRecord>
    for (r in stackFramesRecords) {
        val stackFrame = StackFrame(
            id = r.id,
            methodName = stringsDic[r.methodNameStringId]?.string,
            methodSignature = stringsDic[r.methodSignatureStringId]?.string,
            sourceFileName = stringsDic[r.sourceFileNameStringId]?.string,
            clazz = loadedClassesSerialNumberDic[r.classSerialNumber],
            lineNumber = r.lineNumber
        )
        stackFramesDic[r.id] = stackFrame
    }

    // Stack traces
    val stackTracesDic = HashMap<Int, StackTrace>()
    val stackTraceRecords = (records[HprofRecord.StackTraceRecord::class.java] ?: emptyList()) as List<HprofRecord.StackTraceRecord>
    for (r in stackTraceRecords) {
        val stackTrace = StackTrace(
            stackTraceSerialNumber = r.stackTraceSerialNumber,
            threadSerialNumber = r.threadSerialNumber,
            stackFrames = r.stackFrameIds.toList().mapNotNull {
                stackFramesDic[it]
            }
        )
        stackTracesDic[stackTrace.stackTraceSerialNumber] = stackTrace
    }

    // Merge HeapDumpRecord's sub records
    val heapDumpSubRecords = HashMap<Class<*>, MutableList<HprofRecord>>()
    val heapDumpRecords = (records[HprofRecord.HeapDumpRecord::class.java] ?: emptyList()) as List<HprofRecord.HeapDumpRecord>
    for (r in heapDumpRecords) {
        for (c in r.subRecords) {
            var list = heapDumpSubRecords[c.javaClass]
            if (list == null) {
                list = mutableListOf()
                heapDumpSubRecords[c.javaClass] = list
            }
            list.add(c)
        }
    }

    // roots
    val rootClasses = listOf(
        HprofRecord.RootUnknownRecord::class.java,
        HprofRecord.RootJniGlobalRecord::class.java,
        HprofRecord.RootJniLocalRecord::class.java,
        HprofRecord.RootJavaFrameRecord::class.java,
        HprofRecord.RootNativeStackRecord::class.java,
        HprofRecord.RootStickyClassRecord::class.java,
        HprofRecord.RootThreadBlockRecord::class.java,
        HprofRecord.RootMonitorUsedRecord::class.java,
        HprofRecord.RootThreadObjectRecord::class.java,
        HprofRecord.RootInternedStringRecord::class.java,
        HprofRecord.RootFinalizingRecord::class.java,
        HprofRecord.RootDebuggerRecord::class.java,
        HprofRecord.RootReferenceCleanupRecord::class.java,
        HprofRecord.RootVmInternalRecord::class.java,
        HprofRecord.RootJniMonitorRecord::class.java,
        HprofRecord.RootUnreachableRecord::class.java,
    )
    val rootsDic = HashMap<Long, HprofRecord>()
    for (clazz in rootClasses) {
        val rs = heapDumpSubRecords[clazz] ?: emptyList()
        val method = clazz.getDeclaredMethod("getId")
        for (r in rs) {
            val id = method.invoke(r) as Long
            rootsDic[id] = r
        }
    }

    val instanceDic = HashMap<Long, Instance>()
    // InstanceDumpRecord
    val instanceDumpRecords = (heapDumpSubRecords[HprofRecord.InstanceDumpRecord::class.java] ?: emptyList()) as List<HprofRecord.InstanceDumpRecord>
    for (r in instanceDumpRecords) {
        val objectInstance = Instance.ObjectInstance(
            id = r.id,
            stackTrace = stackTracesDic[r.stackTraceSerialNumber],
            clazz = loadedClassesDic[r.classId],
            value = r.fieldValue
        )
        instanceDic[r.id] = objectInstance
    }

    // PrimitiveArrayRecord
    val primitiveArrayRecordClasses = listOf(
        HprofRecord.BoolArrayRecord::class.java,
        HprofRecord.CharArrayRecord::class.java,
        HprofRecord.FloatArrayRecord::class.java,
        HprofRecord.DoubleArrayRecord::class.java,
        HprofRecord.ByteArrayRecord::class.java,
        HprofRecord.ShortArrayRecord::class.java,
        HprofRecord.IntArrayRecord::class.java,
        HprofRecord.LongArrayRecord::class.java,
    )
    for (c in primitiveArrayRecordClasses) {
        for (r in (heapDumpSubRecords[c] ?: emptyList())) {
            when (r) {
                is HprofRecord.BoolArrayRecord -> {
                    Instance.BoolArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.CharArrayRecord -> {
                    Instance.CharArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.FloatArrayRecord -> {
                    Instance.FloatArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.DoubleArrayRecord -> {
                    Instance.DoubleArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.ByteArrayRecord -> {
                    Instance.ByteArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.ShortArrayRecord -> {
                    Instance.ShortArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.IntArrayRecord -> {
                    Instance.IntArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                is HprofRecord.LongArrayRecord -> {
                    Instance.LongArrayInstance(
                        id = r.id,
                        stackTrace = stackTracesDic[r.stackTraceSerialNumber],
                        array = r.array
                    )
                }
                else -> {
                    throw IOException("Wrong class type: ${r::class.java}")
                }
            }
        }
    }

    // ObjectArrayRecord
    val objectArrayRecord = (heapDumpSubRecords[HprofRecord.ObjectArrayRecord::class.java] ?: emptyList()) as List<HprofRecord.ObjectArrayRecord>
    val tempObjectArrayInstances = mutableListOf<Pair<Instance.ObjectArrayInstance, LongArray>>()
    for (r in objectArrayRecord) {
        val arrayInstance = Instance.ObjectArrayInstance(
            id = r.id,
            stackTrace = stackTracesDic[r.stackTraceSerialNumber],
            arrayLength = r.arrayLength,
            arrayClass = loadedClassesDic[r.arrayClassId],
            elements = ArrayList()
        )
        tempObjectArrayInstances.add(arrayInstance to r.elementIds)
        instanceDic[r.id] = arrayInstance
    }
    for ((instance, elementIds) in tempObjectArrayInstances) {
        for (id in elementIds) {
            (instance.elements as ArrayList<Instance?>).add(instanceDic[id])
        }
    }

    // ClassDumpRecord
    val classDumpsDic = HashMap<Long, ClassDump>()
    val classDumpRecords = (heapDumpSubRecords[HprofRecord.ClassDumpRecord::class.java] ?: emptyList()) as List<HprofRecord.ClassDumpRecord>
    fun fixRefValueHolder(valueHolder: ValueHolder): ValueHolder {
        return if (valueHolder is ValueHolder.ReferenceHolder) {
            valueHolder.copy(refInstance = instanceDic[valueHolder.value])
        } else {
            valueHolder
        }
    }
    for (r in classDumpRecords) {
        val classDump = ClassDump(
            id = r.id,
            clazz = loadedClassesDic[r.id],
            stackTrace = stackTracesDic[r.stackTraceSerialNumber],
            supperClass = loadedClassesDic[r.superClassId],
            classLoader = instanceDic[r.classLoaderId],
            signersId = r.signersId,
            protectionDomainId = r.protectionDomainId,
            instanceSize = r.instanceSize,
            constFields = r.constFields.map {
                if (it.value is ValueHolder.ReferenceHolder) {
                    it.copy(value = fixRefValueHolder(it.value))
                } else {
                    it
                }
            },
            staticFields = r.staticFields.map {
                it.copy(nameString = stringsDic[it.nameStringId]?.string, value = fixRefValueHolder(it.value))
            },
            memberFields = r.memberFields.map {
                it.copy(nameString = stringsDic[it.nameStringId]?.string)
            }
        )
        classDumpsDic[classDump.id] = classDump
    }

    // HeapDumpInfoRecord
    val heapDumpInfoDic = HashMap<Long, HeapDumpInfo>()
    val heapDumpInfoRecords = (heapDumpSubRecords[HprofRecord.HeapDumpInfoRecord::class.java] ?: emptyList()) as List<HprofRecord.HeapDumpInfoRecord>
    for (r in heapDumpInfoRecords) {
        val heapDumpInfo = HeapDumpInfo(
            heapId = r.heapId,
            string = stringsDic[r.stringId]?.string
        )
        heapDumpInfoDic[heapDumpInfo.heapId] = heapDumpInfo
    }

    return HprofRecordsLinked(
        stringsDic = stringsDic,
        loadedClassesDic = loadedClassesDic,
        loadedClassesSerialNumberDic = loadedClassesSerialNumberDic,
        stackFramesDic = stackFramesDic,
        stackTracesDic = stackTracesDic,
        rootsDic = rootsDic,
        instancesDic = instanceDic,
        classDumpsDic = classDumpsDic,
        heapDumpInfoDic = heapDumpInfoDic
    )
}