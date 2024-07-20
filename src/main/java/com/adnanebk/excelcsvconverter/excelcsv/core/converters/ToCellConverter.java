package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

public interface ToCellConverter<T>{

    String convertToCellValue(T fieldValue);

}
