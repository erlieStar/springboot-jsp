package com.st.util;



import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DateTimeUtil {

    public static String str2second(String str) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dataStr = str + " 00:00:00";
        LocalDateTime localDateTime = LocalDateTime.parse(dataStr, df);
        long second = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
        return String.valueOf(second);
    }

    public static String str2secondPlusOneDay(String str) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dataStr = str + " 00:00:00";
        LocalDateTime localDateTime = LocalDateTime.parse(dataStr, df).plusDays(1);
        long second = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
        return String.valueOf(second);
    }

    public static boolean judgeTime(String str) {
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dataStr = str + " 00:00:00";
            LocalDateTime localDateTime = LocalDateTime.parse(dataStr, df);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean judgeLegal(String startime, String endtime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        startime = startime + " 00:00:00";
        endtime = endtime + " 00:00:00";
        LocalDateTime start = LocalDateTime.parse(startime, df);
        LocalDateTime end = LocalDateTime.parse(endtime, df);
        if (start.isAfter(end)) {
            return false;
        }
        return true;
    }

    public static String seconds2Str(long seconds) {
        Instant instant = Instant.ofEpochMilli(seconds * 1000);
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return df.format(localDateTime);
    }

    public static String getNextDateStr(String str) {

        if (str.equals("")) {
            return "";
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate = LocalDate.parse(str, df);
        LocalDate nextDate = endDate.plusDays(1);
        return df.format(nextDate);
    }

    public static int getDays(String startDateStr, String endDateStr) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(startDateStr, df);
        LocalDate endDate = LocalDate.parse(endDateStr, df);
        int num = (int)(endDate.toEpochDay() - startDate.toEpochDay());
        return num;
    }

    public static void main(String[] args) {

        String str = DateTimeUtil.str2second("aaa");
        System.out.println(str);
    }
}
