package com.tans.hprofparser

data class ClassDump(
    val id: Long,
    val clazz: LoadedClass?,
    val stackTrace: StackTrace?,
    val supperClass: LoadedClass?,
    val classLoader: Instance?,
    val signersId: Long,
    val protectionDomainId: Long,
    val instanceSize: Int,
    val constFields: List<ConstField>,
    val staticFields: List<StaticField>,
    val memberFields: List<MemberField>
)