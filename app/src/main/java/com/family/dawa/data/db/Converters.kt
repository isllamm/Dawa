package com.family.dawa.data.db

import androidx.room.TypeConverter
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.domain.model.MealRelation
import com.family.dawa.domain.model.PhotoKind
import com.family.dawa.domain.model.RecordedBy
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromMealRelation(relation: MealRelation?): String? = relation?.name

    @TypeConverter
    fun toMealRelation(value: String?): MealRelation =
        value?.let { runCatching { MealRelation.valueOf(it) }.getOrNull() } ?: MealRelation.NONE

    @TypeConverter
    fun fromPhotoKind(kind: PhotoKind?): String? = kind?.name

    @TypeConverter
    fun toPhotoKind(value: String?): PhotoKind =
        value?.let { runCatching { PhotoKind.valueOf(it) }.getOrNull() } ?: PhotoKind.BOX

    @TypeConverter
    fun fromEventStatus(status: EventStatus?): String? = status?.name

    @TypeConverter
    fun toEventStatus(value: String?): EventStatus =
        value?.let { runCatching { EventStatus.valueOf(it) }.getOrNull() } ?: EventStatus.MISSED

    @TypeConverter
    fun fromRecordedBy(recordedBy: RecordedBy?): String? = recordedBy?.name

    @TypeConverter
    fun toRecordedBy(value: String?): RecordedBy =
        value?.let { runCatching { RecordedBy.valueOf(it) }.getOrNull() } ?: RecordedBy.SYSTEM
}
