package com.kai.cdvinylcatalog.data.database.converter

import androidx.room.TypeConverter
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.parseStoredFormat

class FormatConverter {

    @TypeConverter
    fun fromFormat(format: Format): String = format.name

    @TypeConverter
    fun toFormat(value: String): Format = parseStoredFormat(value)
}