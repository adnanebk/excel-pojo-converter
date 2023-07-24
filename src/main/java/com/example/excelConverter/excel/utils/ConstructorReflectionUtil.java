package com.example.excelConverter.excel.utils;

import com.example.excelConverter.excel.annotations.ExcelConstructorParameters;
import com.example.excelConverter.excel.models.Field;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ConstructorReflectionUtil<T> extends ReflectionUtil<T> {

    private static final Class<ExcelConstructorParameters> ANNOTATION  = ExcelConstructorParameters.class;
    private final Constructor<T> constructor;

    public ConstructorReflectionUtil(Class<T> classType) {
        super(classType);
        this.constructor = getArgsConstructor();
        setFields(Arrays.asList(constructor.getAnnotation(ANNOTATION).titles()).iterator());
    }


    private void setFields(Iterator<String> titles){
        fields =Stream.of(constructor.getParameters()).map(field->new Field(field.getName(),field.getType(),this.getTitle(titles, field.getName())))
                .toList();
    }

    private String getTitle(Iterator<String> titles, String fieldName) {
        return titles.hasNext()?titles.next():camelCaseWordsToWordsWithSpaces(fieldName);
    }



    private Constructor<T> getArgsConstructor() {
        return (Constructor<T>) Arrays.stream(this.classType.getDeclaredConstructors()).filter(ct -> ct.isAnnotationPresent(ExcelConstructorParameters.class))
                .findFirst().orElseThrow(() -> new RuntimeException("Annotation ExcelColsDefinition is required in constructor"));
    }
    @Override
    public List<Field> getFields() {
        return  fields;
    }

}
