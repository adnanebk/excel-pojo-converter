package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class ZonedDateConverter implements Converter<ZonedDateTime> {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;


    @Override
    public ZonedDateTime convertToFieldValue(String cellValue) {
        return ZonedDateTime.parse(cellValue,FORMATTER);
    }

    @Override
    public String convertToCellValue(ZonedDateTime fieldValue) {
        return FORMATTER.format(fieldValue);
    }
}
