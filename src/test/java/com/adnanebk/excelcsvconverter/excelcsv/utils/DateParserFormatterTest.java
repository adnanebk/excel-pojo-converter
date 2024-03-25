package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter.DEFAULT_DATE_PATTERN;
import static com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter.DEFAULT_DATE_TIME_PATTERN;

class DateParserFormatterTest {




    @Test
    void parseToDate() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToDate("2024-03-15 22:45"));
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToDate("3/15/2023 9:11"));
    }

    @Test
    void parseToLocalDateTime() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToLocalDateTime("2024-03-15 22:45"));
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToLocalDateTime("3/15/2023 9:11"));
    }

    @Test
    void parseToLocalDate() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToLocalDate("2024-03-15"));
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToLocalDate("3/15/2023"));
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToLocalDate("15/3/2023"));

    }

    @Test
    void parseToZonedDateTime() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertDoesNotThrow(()-> dateParserFormatter.parseToZonedDateTime("2024-03-15 22:45"));
    }

    @Test
    void formatDate() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertEquals(new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN).format(new Date()), dateParserFormatter.format(new Date()));
    }
    @Test
    void formatLocalDateTime() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertEquals(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN).format(LocalDateTime.now()), dateParserFormatter.format(LocalDateTime.now()));
    }
    @Test
    void formatLocalDate() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertEquals(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN).format(LocalDate.now()), dateParserFormatter.format(LocalDate.now()));
    }
    @Test
    void formatZonedDate() {
        DateParserFormatter dateParserFormatter = new DateParserFormatter();
        Assertions.assertEquals(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN).format(ZonedDateTime.now()), dateParserFormatter.format(ZonedDateTime.now()));
    }


}