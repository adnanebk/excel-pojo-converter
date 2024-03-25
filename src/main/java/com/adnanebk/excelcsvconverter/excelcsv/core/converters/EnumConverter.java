package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

import java.util.Map;

public interface EnumConverter<T> {
   Map<T,String> convert();

    }
