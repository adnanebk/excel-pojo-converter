package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.ToCellConverter;

public class ToCellConverterImp<T>  implements Converter<T> {

    private final ToCellConverter<T> toCellConverter;

    public ToCellConverterImp(ToCellConverter<T> toCellConverter) {
        this.toCellConverter = toCellConverter;
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        throw new UnsupportedOperationException("Not supported operation. cannot convert to field value, use Converter interface to support both conversions");
    }

    @Override
    public String convertToCellValue(T fieldValue) {
        return toCellConverter.convertToCellValue(fieldValue);
    }
}
