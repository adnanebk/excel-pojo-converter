package com.adnanebk.excelcsvconverter.excelcsv.core.heplers;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.*;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.BooleanConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.EnumConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.ToCellConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.ToFieldConverterAdapter;

import java.util.HashMap;

public class ColumnDefinition {

    private int columnIndex;
    private String fieldName;
    private String title;
    private Converter<?> converter;
    public ColumnDefinition(int columnIndex, String fieldName, String title) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
    }

    public ColumnDefinition(int columnIndex, String fieldName, String title, Converter<?> converter) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = converter;
    }

    public ColumnDefinition(int columnIndex, String fieldName, String title, ToFieldConverter<?> converter) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = new ToFieldConverterAdapter<>(converter);
    }

    public ColumnDefinition(int columnIndex, String fieldName, String title, ToCellConverter<?> converter) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = new ToCellConverterAdapter<>(converter);
    }

    public ColumnDefinition(int columnIndex, String fieldName, String title, EnumConverter<? extends Enum<?>> converter, Class<? extends Enum<?>> classType) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = new EnumConverterAdapter<>(classType,converter);
    }


    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Converter<?> getConverter() {
        return converter;
    }

    public void setConverter(Converter<?> converter) {
        this.converter = converter;
    }

    public void setConverterIfNotExist(Class<?> classType) {
        if(converter==null && classType.isEnum())
            this.converter = new EnumConverterAdapter<>(classType,new HashMap<>());
        else if(converter==null && classType.equals(Boolean.class) || classType.equals(boolean.class))
            this.converter = new BooleanConverterAdapter();

    }
}
