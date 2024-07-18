package com.adnanebk.excelcsvconverter.excelcsv.annotations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.CellConverterI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CellConverter {
    Class<? extends CellConverterI<?>> value();
}
