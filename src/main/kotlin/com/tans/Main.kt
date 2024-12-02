package com.tans

import com.tans.hprofparser.Instance
import com.tans.hprofparser.ValueHolder
import com.tans.hprofparser.hprofParse
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
fun main() {
    LocalFileLogPrinter(File("./log.txt")).use { logPrinter ->
        val hprof = hprofParse("./dump.hprof")
        val linked = hprof.recordsLinked
        logPrinter.printLine("LoadedClasses(${linked.loadedClassesDic.size} classes): ")
        for ((_, c) in linked.loadedClassesDic.toList().take(500)) {
            logPrinter.printLine("   ${c.className}")
        }
        logPrinter.printLine("")
        logPrinter.printLine("ObjectInstance(${linked.clazzNameObjectInstanceDic.size} classes): ")
        for ((clazzName, instances) in linked.clazzNameObjectInstanceDic.toList().sortedByDescending { it.second.size }.take(500)) {
            logPrinter.printLine("   $clazzName: ${instances.size} instances.")
        }

        val stringInstance = linked.queryObjectInstanceByClazzName("java.util.ArrayList")?.getOrNull(2)
        if (stringInstance != null) {
            logPrinter.printLine("")
            fun newLine(
                outputLine: StringBuilder,
                depth: Int
            ) {
                outputLine.append("\n")
                repeat(depth) {
                    outputLine.append(" ")
                }
            }
            fun generateInstanceLine(
                instance: Instance,
                outputLine: StringBuilder,
                depth: Int,
                route: HashSet<Long>
            ) {
                route.add(instance.id)
                newLine(outputLine, depth)
                when (instance) {
                    is Instance.ObjectInstance -> {
                        outputLine.append("ObjectInstance(id=${instance.id}): ")
                        for (mf in instance.memberFields) {
                            newLine(outputLine, depth + 1)
                            outputLine.append("Name=${mf.field.nameString},")
                            when (val vh = mf.value) {
                                is ValueHolder.BooleanHolder -> {
                                    outputLine.append("Type=Boolean, Value=${vh.value}")
                                }
                                is ValueHolder.ByteHolder -> {
                                    outputLine.append("Type=Byte, Value=${vh.value.toHexString(HexFormat.UpperCase)}")
                                }
                                is ValueHolder.CharHolder -> {
                                    outputLine.append("Type=Char, Value=${vh.value}")
                                }
                                is ValueHolder.DoubleHolder -> {
                                    outputLine.append("Type=Double, Value=${vh.value}")
                                }
                                is ValueHolder.FloatHolder -> {
                                    outputLine.append("Type=Float, Value=${vh.value}")
                                }
                                is ValueHolder.IntHolder -> {
                                    outputLine.append("Type=Int, Value=${vh.value}")
                                }
                                is ValueHolder.LongHolder -> {
                                    outputLine.append("Type=Long, Value=${vh.value}")
                                }
                                is ValueHolder.ShortHolder -> {
                                    outputLine.append("Type=Short, Value=${vh.value}")
                                }
                                is ValueHolder.ReferenceHolder -> {
                                    outputLine.append("Type=Reference, Value=${vh.value}")
                                    val i = vh.getRefInstance(linked.instancesDic)
                                    if (i != null && (i is Instance.ClassDumpInstance || !route.contains(i.id))) {
                                        generateInstanceLine(i, outputLine, depth + 2, route)
                                    }
                                }
                            }
                        }
                    }
                    is Instance.BoolArrayInstance -> {
                        outputLine.append("BooleanArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.ByteArrayInstance -> {
                        outputLine.append("ByteArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.CharArrayInstance -> {
                        outputLine.append("CharArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.DoubleArrayInstance -> {
                        outputLine.append("DoubleArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.FloatArrayInstance -> {
                        outputLine.append("FloatArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.ShortArrayInstance -> {
                        outputLine.append("ShortArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.IntArrayInstance -> {
                        outputLine.append("IntArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.LongArrayInstance -> {
                        outputLine.append("LongArrayInstance(${instance.array.contentToString()})")
                    }
                    is Instance.ObjectArrayInstance -> {
                        outputLine.append("ObjectArrayInstance:")
                        for (i in instance.elements) {
                            if (i != null && (i is Instance.ClassDumpInstance || !route.contains(i.id))) {
                                generateInstanceLine(i, outputLine, depth + 1, route)
                            }
                        }
                    }
                    is Instance.ClassDumpInstance -> {
                        outputLine.append("ClassDumpInstance(${instance.clazz?.className})")
                    }
                }
            }
            val line = StringBuilder("")
            val route = HashSet<Long>()
            generateInstanceLine(stringInstance, line, 0, route)
            logPrinter.printLine(line.toString())
        }
    }
}