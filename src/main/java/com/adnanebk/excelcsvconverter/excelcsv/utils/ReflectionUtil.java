package com.adnanebk.excelcsvconverter.excelcsv.utils;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnumValues;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.IgnoreCell;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionUtil<T> {
    private String dateTimePattern;
    private String datePattern;
    private final List<Field<T>> fields;
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;
    private boolean includeAllFields;


    public ReflectionUtil(Class<T> type) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
        setSheetInfo();
        this.fields = includeAllFields?this.getAllFields():getAnnotationFields();
    }

    public List<Field<T>> getFields() {
        return fields;
    }

    public T getInstance(Object... values) {
        try {
            T obj = defaultConstructor.newInstance();
            for (int i = 0; i < values.length; i++) {
                fields.get(i).setValue(obj, values[i]);
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }
    private void setSheetInfo() {
        Optional.ofNullable(classType.getAnnotation(SheetDefinition.class))
                .ifPresent(excelDefinitionAnnotation -> {
                    this.datePattern = excelDefinitionAnnotation.datePattern();
                    this.dateTimePattern = excelDefinitionAnnotation.dateTimePattern();
                    this.includeAllFields = excelDefinitionAnnotation.includeAllFields();
                });
    }
    private Constructor<T> getDefaultConstructor() {
        try {
            return classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private List<Field<T>> getAnnotationFields() {
        Class<CellDefinition> annotation = CellDefinition.class;
        return Arrays.stream(classType.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(annotation).value()))
                .map(field -> buildField(List.of(getFieldTitle(field)).iterator()
                        ,field.getDeclaredAnnotation(annotation).value(),field)).toList();
    }
    private List<Field<T>> getAllFields() {
        Class<SheetDefinition> annotation = SheetDefinition.class;
        var titles = Arrays.asList(classType.getAnnotation(annotation).titles()).iterator();
        AtomicInteger index=new AtomicInteger(0);
        return Arrays.stream(classType.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(IgnoreCell.class))
                .map(field -> buildField(titles,index.getAndIncrement(),field)).toList();
    }

    private String getFieldTitle(java.lang.reflect.Field field) {
        return Optional.of(field.getDeclaredAnnotation(CellDefinition.class).title()).filter(s -> !s.isEmpty())
                .orElseGet(() -> camelCaseWordsToWordsWithSpaces(field.getName()));
    }
    private String getTitle(Iterator<String> titles, String fieldName) {
        return titles.hasNext() ? titles.next() : camelCaseWordsToWordsWithSpaces(fieldName);
    }
    private Method getFieldGetter(String fieldName, Class<?> fieldType) {
        try {
            return classType.getDeclaredMethod((fieldType.equals(boolean.class) ? "is" : "get") + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1));
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException("No getter found");
        }
    }
    private Method getFieldSetter(String fieldName, Class<?> fieldType) {
        try {
            return classType.getDeclaredMethod("set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1), fieldType);
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException("No setter found");
        }
    }
    private Field<T> buildField(Iterator<String> titles,int colIndex,java.lang.reflect.Field field) {
        var fieldType = field.getType();
        var fieldName = field.getName();
        return new Field<>(fieldName, fieldType, getTitle(titles, fieldName)
                ,getFieldGetter(fieldName, fieldType), getFieldSetter(fieldName, fieldType),colIndex, getEnumValues(field));
    }

    private static String[] getEnumValues(java.lang.reflect.Field field) {
        return Optional.ofNullable(field.getDeclaredAnnotation(CellEnumValues.class))
                .map(CellEnumValues::value).orElse(new String[]{});
    }

    private String camelCaseWordsToWordsWithSpaces(String str) {
        if (StringUtils.hasLength(str))
            return str.toLowerCase().equals(str) ? str : StringUtils
                    .uncapitalize(str.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
        return str;
    }

    public String getTypeName(Class<?> fieldType) {
        return fieldType.isEnum() ? Enum.class.getSimpleName().toLowerCase() : fieldType.getSimpleName().toLowerCase();
    }
}

