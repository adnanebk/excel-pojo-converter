package com.adnanebk.excelcsvconverter.excelcsv.utils;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.ConstructorCells;
import com.adnanebk.excelcsvconverter.excelcsv.models.AnnotationType;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class ReflectionUtil<T> {
    private String dateTimeFormat;
    private String dateFormat;
    protected List<Field<T>> fields;
    protected final Class<T> classType;
    private Constructor<T> defaultConstructor;
    private Constructor<?> argsConstructor;


    public ReflectionUtil(Class<T> type, AnnotationType annotationType) {
        classType = type;
        this.setDefaultConstructor();
        if (annotationType.equals(AnnotationType.FIELD))
            this.setFields();
        else {
            this.setArgsConstructor();
            this.setFieldsFromArgsConstructor();
        }
        setDateFormats();
    }

    public List<Field<T>> getFields() {
        return fields;
    }

    public T getInstance(Object... values) {
        try {
            T obj = defaultConstructor.newInstance();
            for (int i = 0; i < fields.size(); i++) {
                if (values[i] != null)
                    fields.get(i).setValue(obj, values[i]);
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public Optional<String> getDateFormat() {
        return Optional.ofNullable(dateFormat).filter(s -> !s.isEmpty());
    }

    public Optional<String> getDateTimeFormat() {
        return Optional.ofNullable(dateTimeFormat).filter(s -> !s.isEmpty());
    }

    private void setDateFormats() {
        Optional.ofNullable(classType.getAnnotation(SheetDefinition.class))
                .ifPresent(excelDefinitionAnnotation -> {
                    this.dateFormat = excelDefinitionAnnotation.dateFormat();
                    this.dateTimeFormat = excelDefinitionAnnotation.dateTimeFormat();
                });
    }
    private void setDefaultConstructor() {
        try {
            defaultConstructor = classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private void setArgsConstructor() {
        argsConstructor = Arrays.stream(this.classType.getDeclaredConstructors()).filter(ct -> ct.isAnnotationPresent(ConstructorCells.class))
                .findFirst().orElseThrow(() -> new ReflectionException("Annotation ExcelColsDefinition is required in constructor"));
    }

    private void setFields() {
        Class<CellDefinition> annotation = CellDefinition.class;
        fields = Arrays.stream(classType.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(annotation).index()))
                .map(field -> {
                    String fieldName=field.getName();
                    Class<?> fieldType=field.getType();
                    return new Field<T>(fieldName, fieldType, getFieldTitle(annotation, field)
                                   ,getFieldGetter(fieldName,fieldType),getFieldSetter(fieldName, fieldType));
                }).toList();
    }

    private void setFieldsFromArgsConstructor() {
        Class<ConstructorCells> annotation = ConstructorCells.class;
        var titles = Arrays.asList(argsConstructor.getAnnotation(annotation).titles()).iterator();
        fields = Stream.of(argsConstructor.getParameters()).map(field -> {
                    String fieldName=field.getName();
                    Class<?> fieldType=field.getType();
            return new Field<T>(field.getName(), field.getType(), this.getTitle(titles, field.getName()),
                                getFieldGetter(fieldName,fieldType),getFieldSetter(fieldName,fieldType));
                }).toList();
    }
    private String getFieldTitle(Class<CellDefinition> annotation, java.lang.reflect.Field field) {
        return Optional.of(field.getDeclaredAnnotation(annotation).title()).filter(s -> !s.isEmpty())
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
    private String camelCaseWordsToWordsWithSpaces(String str) {
        if (StringUtils.hasLength(str))
            return str.toLowerCase().equals(str) ? str : StringUtils
                    .uncapitalize(str.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
        return str;
    }
}

