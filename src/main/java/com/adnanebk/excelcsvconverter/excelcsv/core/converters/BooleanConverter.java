package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

public class BooleanConverter implements Converter<Boolean> {
    private  String trueValue = "true";
    private  String  falseValue = "false";

    public BooleanConverter(String trueValue, String falseValue) {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    public BooleanConverter() {
    }

    @Override
    public String convertToCellValue(Boolean fieldValue) {
        return  (fieldValue != null && fieldValue) ? trueValue : falseValue;
    }

    @Override
    public Boolean convertToFieldValue(String cellValue) {
        return this.trueValue.equalsIgnoreCase(cellValue);
    }
}
