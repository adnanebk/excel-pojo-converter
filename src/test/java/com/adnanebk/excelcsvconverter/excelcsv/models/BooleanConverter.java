package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;

public class BooleanConverter implements Converter<Boolean> {

    @Override
    public Boolean convertToFieldValue(String cellValue) {
        return cellValue.equalsIgnoreCase("yes");
    }

    @Override
    public String convertToCellValue(Boolean fieldValue) {
        return fieldValue?"Yes":"No";
    }
}
