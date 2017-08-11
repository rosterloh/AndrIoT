package com.rosterloh.andriot.db;

import android.arch.persistence.room.TypeConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTypeConverter {

    @TypeConverter
    public static LocalDateTime toDate(String timestamp) {
        return timestamp == null ? null : LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @TypeConverter
    public static String toTimestamp(LocalDateTime date) {
        return date == null ? null : date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
