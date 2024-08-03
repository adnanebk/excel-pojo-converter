package com.adnanebk.excelcsvconverter.excelcsv.core;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.*;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.EnumConverterImp;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.ToCellConverterImp;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.ToFieldConverterImp;


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
        this.converter = new ToFieldConverterImp<>(converter);
    }

    public ColumnDefinition(int columnIndex, String fieldName, String title, ToCellConverter<?> converter) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = new ToCellConverterImp<>(converter);
    }

    public ColumnDefinition(int columnIndex, String fieldName, String title, EnumConverter<? extends Enum<?>> converter, Class<? extends Enum<?>> classType) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = new EnumConverterImp<>(classType,converter);
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


}
