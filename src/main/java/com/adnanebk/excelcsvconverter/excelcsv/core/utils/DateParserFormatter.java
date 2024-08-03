package com.adnanebk.excelcsvconverter.excelcsv.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateParserFormatter {


    private static final List<String> DATE_TIME_PATTERNS = new ArrayList<>();
    private static final List<String> DATE_PATTERNS = new ArrayList<>();
    private final SimpleDateFormat dateFormatter;
    private DateTimeFormatter localedDateFormatter;
    private DateTimeFormatter localedDateTimeFormatter;

    public DateParserFormatter(String datePattern, String dateTimePattern) {
        if(dateTimePattern!=null && !dateTimePattern.isEmpty()){
            this.dateFormatter = new SimpleDateFormat(dateTimePattern);
            this.localedDateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        }
        else {
            this.dateFormatter = new SimpleDateFormat();
            this.localedDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }
         localedDateFormatter =(datePattern!=null && !datePattern.isEmpty())? DateTimeFormatter.ofPattern(datePattern)
                               : DateTimeFormatter.ISO_LOCAL_DATE;
    }

    public DateParserFormatter() {
        this(null,null);
    }

    static {
        for (String month : new String[]{"MM","M"}) {
            for (String day : new String[]{"dd","d"}) {
                for (String year : new String[]{"yyyy","yy"}) {
                    for (String hour : new String[]{"HH","H"}) {
                        for (String minute : new String[]{"mm","m"}) {
                            DATE_TIME_PATTERNS.add(day + "/" + month + "/" + year + " " + hour + ":" + minute);
                            DATE_TIME_PATTERNS.add(month + "/" + day + "/" + year + " " + hour + ":" + minute);
                            DATE_TIME_PATTERNS.add(year + "-" + month + "-" + day + " " + hour + ":" + minute);
                            DATE_PATTERNS.add(day + "/" + month + "/" + year);
                            DATE_PATTERNS.add(month + "/" + day + "/" + year);
                            DATE_PATTERNS.add(year + "-" + month + "-" + day);

                        }
                    }
                }
            }
        }
    }

    public synchronized Date parseToDate(String date) {
        for (String pattern : DATE_TIME_PATTERNS) {
            try {
                return dateFormatter.parse(date);
            } catch (ParseException | NumberFormatException ex) {
                dateFormatter.applyPattern(pattern);
            }
        }
        throw new DateTimeException("failed to parse to date "+date);
    }

    public LocalDateTime parseToLocalDateTime(String date) {
        for (String pattern : DATE_TIME_PATTERNS) {
            try {
                return LocalDateTime.parse(date, localedDateTimeFormatter);
            } catch (DateTimeParseException ex) {
                localedDateTimeFormatter=DateTimeFormatter.ofPattern(pattern);
            }
        }
        throw new DateTimeException("failed to parse to localdatetime "+date);
    }

    public LocalDate parseToLocalDate(String date) {
        for (String pattern : DATE_PATTERNS) {
            try {
                return LocalDate.parse(date, localedDateFormatter);
            } catch (DateTimeParseException ex) {
                localedDateFormatter=DateTimeFormatter.ofPattern(pattern);
            }
        }
        throw new DateTimeException("failed to parse to localdate "+date);
    }

    public ZonedDateTime parseToZonedDateTime(String date) {
        return parseToLocalDateTime(date).atZone(ZoneId.systemDefault());
    }

    public String format(Date date) {
       return dateFormatter.format(date);
    }
    public String format(LocalDate date) {
        return localedDateFormatter.format(date);
    }
    public String format(LocalDateTime date) {
        return localedDateTimeFormatter.format(date);
    }
    public String format(ZonedDateTime date) {
        return localedDateTimeFormatter.withZone(ZoneId.systemDefault()).format(date);
    }

}
