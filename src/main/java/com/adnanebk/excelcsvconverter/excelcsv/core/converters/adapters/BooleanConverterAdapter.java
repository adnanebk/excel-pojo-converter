package com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ConverterException;

public class BooleanConverterAdapter implements Converter<Boolean> {
    private final String trueValue = "true";
    private final String  falseValue = "false";


    @Override
    public String convertToCellValue(Boolean fieldValue) {
        return  (fieldValue != null && fieldValue) ? trueValue : falseValue;
    }

    @Override
    public Boolean convertToFieldValue(String cellValue) {
        if(this.trueValue.equalsIgnoreCase(cellValue))
            return true;
        else if(this.falseValue.equalsIgnoreCase(cellValue))
            return false;
        throw new ConverterException("Cannot convert to boolean, you must create a converter");
    }
}
