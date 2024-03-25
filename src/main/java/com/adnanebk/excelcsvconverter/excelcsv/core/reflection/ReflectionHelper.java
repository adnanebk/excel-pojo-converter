package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.*;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.BooleanConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.EnumsConverter;
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

    public Optional<SheetDefinition> getSheetInfo() {
        return Optional.ofNullable(classType.getAnnotation(SheetDefinition.class));
    }

    private Constructor<T> getDefaultConstructor() {
        try {
            return classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private void setFieldsAndTitles(){
        boolean hasIncludeAllFields =  getSheetInfo().filter(SheetDefinition::includeAllFields).isPresent();
        if(hasIncludeAllFields)
           this.createAllFieldsAndTitles();
        else this.createAnnotatedFieldsAndTitles();
    }
    private void createAnnotatedFieldsAndTitles() {
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
    private void createAllFieldsAndTitles() {
        var titles = classType.getAnnotation(SheetDefinition.class).titles();
        int index=0;
        for (var field : classType.getDeclaredFields()) {
            if (!field.isAnnotationPresent(IgnoreCell.class)) {
                String title=index < titles.length ? titles[index]:camelCaseWordsToTitleWords(field.getName());
                headers.add(title);
                fields.add(new ReflectedField<>(field,this.createConverter(field, null),index));
            }
            index++;
        }
    }

    private Converter<?> createConverter(Field field, CellDefinition cellDefinition) {
    try {
        if(cellDefinition!=null && !cellDefinition.converter().isInterface())
           return cellDefinition.converter().getDeclaredConstructor().newInstance();
        else if (field.isAnnotationPresent(CellConverter.class))
           return field.getDeclaredAnnotation(CellConverter.class).converter().getDeclaredConstructor().newInstance();
        if(field.getType().isEnum()) {
            var enumsAnnotation = field.getDeclaredAnnotation(CellEnum.class);
            if(enumsAnnotation!=null)
              return new EnumsConverter<>(field.getType(),enumsAnnotation.converter().getDeclaredConstructor().newInstance().convert());
            return new EnumsConverter<>(field.getType(),new HashMap<>());
        }
        else  if(field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            return  Optional.ofNullable(field.getDeclaredAnnotation(CellBoolean.class))
                    .map(cell->new BooleanConverter(cell.trueValue(),cell.falseValue()))
                    .orElseGet(BooleanConverter::new);
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

