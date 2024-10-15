package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class LocalDateConverter implements Converter<LocalDate> {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


    @Override
    public LocalDate convertToFieldValue(String cellValue) {
        return LocalDate.parse(cellValue, FORMATTER);
    }

    @Override
    public String convertToCellValue(LocalDate fieldValue) {
        return FORMATTER.format(fieldValue);
    }
}
