package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.*;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class ReflectionHelper<T> {
    private final List<ReflectedField<?>> fields = new ArrayList<>();
    private final List<String> headers = new ArrayList<>();
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionHelper(Class<T> type) {
        classType = type;
        defaultConstructor = getDefaultConstructor();
        this.setFieldsAndTitles();
    }

    public ReflectionHelper(Class<T> type, ColumnDefinition<?>[] columnsDefinitions) {
        classType = type;
        defaultConstructor = getDefaultConstructor();
        Arrays.sort(columnsDefinitions,Comparator.comparing(ColumnDefinition::getColumnIndex));
        for (var cd : columnsDefinitions) {
            try {
                Field field = type.getDeclaredField(cd.getFieldName());
                if (cd.getConverter() == null)
                    cd.setConverter(this.getDefaultConverter(field),field.getType());
                if(!cd.getClassType().equals(field.getType()))
                    throw new ReflectionException(String.format("The converter of the field %s is not compatible with its type %s",cd.getFieldName(),field.getType().getSimpleName()));
                fields.add(new ReflectedField<>(field, cd.getConverter(), cd.getColumnIndex()));
                headers.add(cd.getTitle());
            } catch (NoSuchFieldException e) {
                throw new ReflectionException("the specified field name '" + cd.getFieldName() + "' is not found in the class '"+classType.getSimpleName()+"'");
            }
        }
    }

    public List<ReflectedField<?>> getFields() {
        return fields;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public T createInstance() {
        try {
            return defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    private Constructor<T> getDefaultConstructor() {
        try {
            return classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private void setFieldsAndTitles() {
        Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(CellDefinition.class).value()))
                .forEach(field -> {
                    var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                    var reflectedField = new ReflectedField<>(field, this.createConverter(field, cellDefinition), cellDefinition.value());
                    String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                            .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                    fields.add(reflectedField);
                    headers.add(title);
                });
    }

    private Converter<?> createConverter(Field field, CellDefinition cellDefinition) {
        try {
            if (cellDefinition == null)
                return null;
            if (!cellDefinition.converter().isInterface())
                return cellDefinition.converter().getDeclaredConstructor().newInstance();
            if (!cellDefinition.fieldConverter().isInterface())
                return new FieldConverterImp<>(cellDefinition.fieldConverter().getDeclaredConstructor().newInstance());
            if (!cellDefinition.cellConverter().isInterface())
                return new CellConverterImp<>(cellDefinition.cellConverter().getDeclaredConstructor().newInstance());
            if (!cellDefinition.enumConverter().isInterface())
                return new EnumConverterImp<>(field.getType(),
                        cellDefinition.enumConverter().getDeclaredConstructor().newInstance());
            return getDefaultConverter(field);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    private Converter<?> getDefaultConverter(Field field) {
        if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))
            return new BooleanConverterImp();
        if (field.getType().isEnum())
            return new EnumConverterImp<>(field.getType(), new HashMap<>());

        if (field.getType().equals(Date.class)) {
            return new DateConverter();
        }
        if (field.getType().equals(LocalDate.class)) {
            return new LocalDateConverter();
        }
        if (field.getType().equals(LocalDateTime.class)) {
            return new LocalDateTimeConverter();
        }
        if (field.getType().equals(ZonedDateTime.class)) {
            return new ZonedDateConverter();
        }
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            return new IntegerConverter();
        }
        if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
            return new LongConverter();
        }
        if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
            return new DoubleConverter();
        }
        return null;
    }

    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0)) + "";
        String remaining = word.substring(1);
        return firstChar + (remaining.toLowerCase().equals(remaining) ? remaining :
                remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

}

