package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;

import java.time.ZonedDateTime;


public class ZonedDateConverter implements Converter<ZonedDateTime> {

    private final DateParserFormatter dateParserFormatter;

    public ZonedDateConverter(DateParserFormatter dateParserFormatter) {
        this.dateParserFormatter = dateParserFormatter;
    }

    @Override
    public ZonedDateTime convertToFieldValue(String cellValue) {
        return dateParserFormatter.parseToZonedDateTime(cellValue);
    }

    @Override
    public String convertToCellValue(ZonedDateTime fieldValue) {
        return dateParserFormatter.format(fieldValue);
    }
}
