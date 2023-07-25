package com.example.excelConverter.excel.utils;

import com.example.excelConverter.excel.annotations.ExcelCol;
import com.example.excelConverter.excel.models.Field;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FieldsReflectionUtil<T> extends ReflectionUtil<T> {

    private static final Class<ExcelCol> ANNOTATION = ExcelCol.class;
    public FieldsReflectionUtil(Class<T> classType) {
        super(classType);
        setFields();
        setGettersAndSetters();
    }



private void setFields(){
    fields = Arrays.stream(classType.getDeclaredFields()).filter(field -> field.isAnnotationPresent(ANNOTATION))
            .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(ANNOTATION).index()))
            .map(field->
             new Field(field.getName(),field.getType(),
             Optional.of(field.getDeclaredAnnotation(ANNOTATION).title()).filter(s->!s.isEmpty())
                     .orElseGet(()->camelCaseWordsToWordsWithSpaces(field.getName()))
             )).toList();
}

    @Override
    public List<Field> getFields() {
        return  fields;
    }
}
