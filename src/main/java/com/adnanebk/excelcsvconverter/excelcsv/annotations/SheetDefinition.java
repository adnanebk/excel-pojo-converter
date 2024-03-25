package com.adnanebk.excelcsvconverter.excelcsv.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter.DEFAULT_DATE_PATTERN;
import static com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter.DEFAULT_DATE_TIME_PATTERN;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SheetDefinition {
    boolean includeAllFields() default false;

    String[] titles() default {};

    String datePattern() default DEFAULT_DATE_PATTERN;

    String dateTimePattern() default DEFAULT_DATE_TIME_PATTERN;



}
