package com.tans

import com.tans.hprofparser.Instance
import com.tans.hprofparser.ValueHolder
import com.tans.hprofparser.hprofParse

@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val hprof = hprofParse("./dump.hprof")
    val linked = hprof.recordsLinked
    println("LoadedClasses(${linked.loadedClassesDic.size} classes): ")
    for ((_, c) in linked.loadedClassesDic.toList().take(500)) {
        println("   ${c.className}")
    }
    println("")
    println("ObjectInstance(${linked.clazzNameObjectInstanceDic.size} classes): ")
    for ((clazzName, instances) in linked.clazzNameObjectInstanceDic.toList().sortedByDescending { it.second.size }.take(500)) {
        println("   $clazzName: ${instances.size} instances.")
    }

    val stringInstance = linked.queryObjectInstanceByClazzName("java.lang.String")?.getOrNull(0)
    if (stringInstance != null) {
        println("")
        println("StringInstance(id: ${stringInstance.id}): ")
        fun newLine(outputLine: StringBuilder, depth: Int) {
            outputLine.append("\n")
            repeat(depth) {
                outputLine.append(" ")
            }
        }
        fun generateInstanceLine(instance: Instance, outputLine: StringBuilder, depth: Int) {
            newLine(outputLine, depth)
            when (instance) {
                is Instance.ObjectInstance -> {
                    outputLine.append("ObjectInstance")
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
                                if (i != null) {
                                    generateInstanceLine(i, outputLine, depth + 2)
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
                        if (i != null) {
                            generateInstanceLine(i, outputLine, depth + 1)
                        }
                    }
                }
                is Instance.ClassDump -> {
                    outputLine.append("ClassDump(${instance.clazz?.className})")
                }
            }
        }
        val line = StringBuilder("")
        generateInstanceLine(stringInstance, line, 1)
        println(line.toString())
    }
}