package com.family.dawa.domain.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val strength: String = "",
    val notes: String = "",
    val audioPath: String? = null,
    val colorTag: Int = 0,
    val shapeTag: String = "CIRCLE",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class MedicationPhoto(
    val id: Long = 0,
    val medicationId: Long = 0,
    val path: String,
    val kind: PhotoKind = PhotoKind.BOX,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

enum class PhotoKind(val arabicLabel: String) {
    BOX("علبة الدوا"),
    PILL("شكل الحبة"),
    BLISTER("شريط الدوا"),
    BOTTLE("الزجاجة"),
    OTHER("أخرى")
}
