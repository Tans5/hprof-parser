package com.tans.hprofparser

import okio.IOException
import okio.buffer
import okio.source
import java.io.File
import kotlin.jvm.Throws

data class Hprof(
    val header: HprofHeader,
    val records: Map<Class<out HprofRecord>, List<HprofRecord>>
)


@Throws(IOException::class)
fun hprofParse(filePath: String): Hprof {
    val f = File(filePath)
    val hprof = f.source().buffer().use {
        val header = it.parseHprofHeader()
        val records = it.parseRecords(header)
        Hprof(
            header = header,
            records = records
        )
    }
    return hprof
}