package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

public interface Converter<T>  {

    T convertToFieldValue(String cellValue);

    String convertToCellValue(T fieldValue);

}
