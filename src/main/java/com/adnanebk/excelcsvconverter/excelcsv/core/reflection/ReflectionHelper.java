package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.*;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.BooleanConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.EnumConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.ToCellConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.adapters.ToFieldConverterAdapter;
import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ReflectionHelper<T> {
    private  final List<ReflectedField<?>> fields = new ArrayList<>();
    private  final List<String> headers = new ArrayList<>();
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionHelper(Class<T> type) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
        this.setFieldsAndTitles();
    }

    public ReflectionHelper(Class<T> type, ColumnDefinition[] columnsDefinitions) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
            Arrays.sort(columnsDefinitions,Comparator.comparing(ColumnDefinition::getColumnIndex));
            for (ColumnDefinition op : columnsDefinitions) {
            try {
             op.setConverterIfNotExist(type.getDeclaredField(op.getFieldName()).getType());
             fields.add(new ReflectedField<>(type.getDeclaredField(op.getFieldName()),op.getConverter(),op.getColumnIndex()));
             headers.add(op.getTitle());
            } catch (NoSuchFieldException e) {
                throw new ReflectionException("the specified field name {"+op.getFieldName()+"} is not found");
            }
        }
    }

    public List<ReflectedField<?>> getFields() {
        return fields;
    }
    public List<String> getHeaders() {
        return headers;
    }

    public T createInstance(){
        try{
            return defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public DateParserFormatter getDateParserFormatter() {
        return Optional.ofNullable(classType.getAnnotation(SheetDefinition.class))
                .map(info->new DateParserFormatter(info.datePattern(),info.dateTimePattern()))
                .orElseGet(DateParserFormatter::new);
    }

    private Constructor<T> getDefaultConstructor() {
        try {
            return classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private void setFieldsAndTitles(){
        Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(CellDefinition.class).value()))
                .forEach(field->{
                    var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                    var reflectedField = new ReflectedField<>(field,this.createConverter(field,cellDefinition),cellDefinition.value());
                    String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                            .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                    fields.add(reflectedField);
                    headers.add(title);
                });
    }

    private Converter<?> createConverter(Field field, CellDefinition cellDefinition) {
    try {
        if(cellDefinition==null)
            return null;
        if(!cellDefinition.converter().isInterface())
           return cellDefinition.converter().getDeclaredConstructor().newInstance();
        if(!cellDefinition.toCellConverter().isInterface())
            return new ToCellConverterAdapter<>(cellDefinition.toCellConverter().getDeclaredConstructor().newInstance());
        if(!cellDefinition.toFieldConverter().isInterface())
            return new ToFieldConverterAdapter<>(cellDefinition.toFieldConverter().getDeclaredConstructor().newInstance());
        if(!cellDefinition.enumConverter().isInterface())
            return new EnumConverterAdapter<>(field.getType(),
                    cellDefinition.enumConverter().getDeclaredConstructor().newInstance());
        if(field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            return new BooleanConverterAdapter();
        }
        return  null;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e.getMessage());
        }
    }
    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0))+"";
        String remaining = word.substring(1);
        return firstChar + (remaining.toLowerCase().equals(remaining)?remaining:
                remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

}

