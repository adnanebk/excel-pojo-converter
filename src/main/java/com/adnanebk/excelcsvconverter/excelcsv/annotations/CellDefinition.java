package com.adnanebk.excelcsvconverter.excelcsv.annotations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.CellConverterI;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.FieldConverterI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CellDefinition {
    String title() default "";

    int value();

    Class<? extends Converter> converter() default Converter.class;
    Class<? extends FieldConverterI> fieldConverter() default FieldConverterI.class;
    Class<? extends CellConverterI> cellConverter() default CellConverterI.class;
}
