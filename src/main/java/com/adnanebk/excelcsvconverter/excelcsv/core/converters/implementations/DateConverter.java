package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;

import java.util.Date;


public class DateConverter implements Converter<Date> {

    private final DateParserFormatter dateParserFormatter;

    public DateConverter(DateParserFormatter dateParserFormatter) {
        this.dateParserFormatter = dateParserFormatter;
    }

    @Override
    public Date convertToFieldValue(String cellValue) {
        return dateParserFormatter.parseToDate(cellValue);
    }

    @Override
    public String convertToCellValue(Date fieldValue) {
        return dateParserFormatter.format(fieldValue);
    }
}
