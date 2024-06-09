package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

@FunctionalInterface
public interface FieldConverter<T> extends Converter<T> {


    @Override
    default T convertToFieldValue(String cellValue) {
        throw new UnsupportedOperationException();
    }
}
