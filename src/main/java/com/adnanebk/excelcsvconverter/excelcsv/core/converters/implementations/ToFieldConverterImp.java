package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.ToFieldConverter;

public class ToFieldConverterImp<T>  implements Converter<T> {

    private final ToFieldConverter<T> toFieldConverter;

    public ToFieldConverterImp(ToFieldConverter<T> toFieldConverter) {
        this.toFieldConverter = toFieldConverter;
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        return toFieldConverter.convertToFieldValue(cellValue);
    }

    @Override
    public String convertToCellValue(T fieldValue) {
        return fieldValue.toString();
    }
}
