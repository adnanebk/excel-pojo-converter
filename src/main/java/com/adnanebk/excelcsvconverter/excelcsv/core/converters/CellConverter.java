package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

@FunctionalInterface
public interface CellConverter<T> extends Converter<T> {

    @Override
     default String convertToCellValue(T fieldValue) {
       throw new UnsupportedOperationException();
    }
}
