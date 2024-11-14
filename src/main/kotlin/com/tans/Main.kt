package com.tans

import com.tans.hprofparser.hprofParse

fun main() {
    val hprof = hprofParse("./dump.hprof")
    println(hprof.header)
}