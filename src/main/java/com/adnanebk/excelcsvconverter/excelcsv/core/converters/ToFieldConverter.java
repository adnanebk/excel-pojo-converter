package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

public interface ToFieldConverter<T>  {

    T convertToFieldValue(String cellValue);

}
