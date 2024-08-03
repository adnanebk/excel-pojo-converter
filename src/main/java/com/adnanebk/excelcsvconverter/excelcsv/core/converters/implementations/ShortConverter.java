package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;


public class ShortConverter implements Converter<Short> {

    @Override
    public Short convertToFieldValue(String cellValue) {
        return Short.parseShort(cellValue);
    }

    @Override
    public String convertToCellValue(Short fieldValue) {
        return fieldValue.toString();
    }
}
