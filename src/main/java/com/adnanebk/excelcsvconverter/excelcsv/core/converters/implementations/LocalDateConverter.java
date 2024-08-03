package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;

import java.time.LocalDate;


public class LocalDateConverter implements Converter<LocalDate> {

    private final DateParserFormatter dateParserFormatter;

    public LocalDateConverter(DateParserFormatter dateParserFormatter) {
        this.dateParserFormatter = dateParserFormatter;
    }

    @Override
    public LocalDate convertToFieldValue(String cellValue) {
        return dateParserFormatter.parseToLocalDate(cellValue);
    }

    @Override
    public String convertToCellValue(LocalDate fieldValue) {
        return dateParserFormatter.format(fieldValue);
    }
}
