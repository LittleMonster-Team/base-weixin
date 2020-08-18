//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xx.cloud.weixin.common.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class LocalDateTimeUtil {
    public static final String YYYY = "yyyy";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final String BASE_TIME_FORMAT = "[yyyyMMddHHmmss][yyyyMMddHHmm][yyyyMMddHH][yyyyMMdd][yyyyMM][yyyy][[-][/][.]MM][[-][/][.]dd][ ][HH][[:][.]mm][[:][.]ss][[:][.]SSS]";

    public LocalDateTimeUtil() {
    }

    public static LocalDateTime parse(String timeString) {
        return LocalDateTime.parse(timeString, getDateTimeFormatterByPattern("[yyyyMMddHHmmss][yyyyMMddHHmm][yyyyMMddHH][yyyyMMdd][yyyyMM][yyyy][[-][/][.]MM][[-][/][.]dd][ ][HH][[:][.]mm][[:][.]ss][[:][.]SSS]"));
    }

    public static LocalDateTime parseByPattern(String timeString, String pattern) {
        return LocalDateTime.parse(timeString, getDateTimeFormatterByPattern(pattern));
    }

    private static DateTimeFormatter getDateTimeFormatterByPattern(String pattern) {
        return (new DateTimeFormatterBuilder()).appendPattern(pattern).parseDefaulting(ChronoField.YEAR_OF_ERA, (long)LocalDateTime.now().getYear()).parseDefaulting(ChronoField.MONTH_OF_YEAR, (long)LocalDateTime.now().getMonthValue()).parseDefaulting(ChronoField.DAY_OF_MONTH, 1L).parseDefaulting(ChronoField.HOUR_OF_DAY, 0L).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0L).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0L).parseDefaulting(ChronoField.NANO_OF_SECOND, 0L).toFormatter();
    }

    public static LocalDateTime timestamToDatetime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static long datatimeToTimestamp(LocalDateTime ldt) {
        ZoneId zone = ZoneId.systemDefault();
        return ldt.atZone(zone).toInstant().toEpochMilli();
    }

    public static void main(String[] args) {
        long timeStamp = 1382694957000L;
        System.out.println(timestamToDatetime(timeStamp));
    }
}
