package com.kai.cdvinylcatalog.core.database.converter

import androidx.room.TypeConverter
import com.kai.cdvinylcatalog.core.model.Format

class FormatConverter {

    @TypeConverter
    fun fromFormat(format: Format): String = format.name

    @TypeConverter
    fun toFormat(value: String): Format {
        return try {
            Format.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Format.UNKNOWN
        }
    }
}