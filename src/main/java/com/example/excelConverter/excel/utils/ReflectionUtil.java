package com.example.excelConverter.excel.utils;


import com.example.excelConverter.excel.annotations.ExcelColsDefinition;
import com.example.excelConverter.excel.exceptions.ReflectionException;
import com.example.excelConverter.excel.models.Field;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class ReflectionUtil<T> {

    private final ExcelColsDefinition classAnnotation;
    public final SimpleDateFormat dateFormatter;
    public final DateTimeFormatter localedDateFormatter;
    public final DateTimeFormatter localedDateTimeFormatter;
    public final DateTimeFormatter zonedDateTimeFormatter;
    protected  List<Field> fields;
    protected  final Class<T> classType;

    private final Set<String> numberTypes = new HashSet<>(Arrays.asList(int.class.getName(), long.class.getName(), double.class.getName(),short.class.getName()));
    private final Constructor<T> defaultConstructor;

    protected ReflectionUtil(Class<T> type) {
        classType=type;
        try {
            defaultConstructor = classType.getDeclaredConstructor();
            classAnnotation = classType.getAnnotation(ExcelColsDefinition.class);
            dateFormatter = dateTimeFormat().map(SimpleDateFormat::new).orElse(new SimpleDateFormat());
            localedDateFormatter =dateFormat().map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE);
            localedDateTimeFormatter =dateTimeFormat().map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            zonedDateTimeFormatter =DateTimeFormatter.ISO_ZONED_DATE_TIME;
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    public abstract List<Field> getFields();


    public boolean isNumberType(Field field){
        Class<?> fieldType=field.type();
        if(fieldType.isPrimitive())
            return numberTypes.contains(fieldType.getName());
        return Number.class.isAssignableFrom(fieldType);
    }

    public   Object getFieldValue(T obj, Field field) {
        try {
            String fieldName=field.name();
            return obj.getClass().getDeclaredMethod((field.type().equals(boolean.class)?"is":"get")+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1)).invoke(obj);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }

    }
    public void setValue(T obj, Field field, Object value){
        try {
          if(value!=null)
            obj.getClass().getDeclaredMethod("set"+Character.toUpperCase(field.name().charAt(0))+field.name().substring(1),field.type()).invoke(obj,value);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }

    }

    public  boolean isStringValue(Field field){
        return field.type().equals(String.class);
    }
    public  boolean isLocalDateValue(Field field){
        return  LocalDate.class.equals(field.type());
    }
    public  boolean isLocalDateTimeValue(Field field){
        return  LocalDateTime.class.equals(field.type());
    }
    public  boolean isZonedDateValue(Field field){
        return  ZonedDateTime.class.equals(field.type());
    }
    public  boolean isDateValue(Field field){
        return field.type().equals(Date.class);
    }
    public  boolean isEnumValue(Field field){
        return field.type().isEnum();
    }
    public  boolean isBooleanValue(Field field){
        return field.type().equals(Boolean.class) || field.type().equals(boolean.class);
    }
    public  boolean isIntegerValue(Field field){
        return field.type().equals(Integer.class) || field.type().equals(int.class);
    }

    public  boolean isLongValue(Field field){
        return field.type().equals(Long.class) || field.type().equals(long.class);
    }

    public  boolean isDoubleValue(Field field){
        return field.type().equals(Double.class) || field.type().equals(double.class);
    }

    public  T getInstance(Object... values){
        try {
            T obj =  defaultConstructor.newInstance();
            int i =0;
            for (Field field : fields) {
                setValue(obj, field, values[i++]);
            }
            return obj;
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException("Something bad happened when reading the file");
        }
    }
    public Optional<String> dateFormat(){
        return Optional.ofNullable(classAnnotation).map(ExcelColsDefinition::dateFormat).filter(s->!s.isEmpty());
    }
    public Optional<String> dateTimeFormat(){
        return Optional.ofNullable(classAnnotation).map(ExcelColsDefinition::dateTimeFormat).filter(s->!s.isEmpty());
    }
    protected String camelCaseWordsToWordsWithSpaces(String str) {
        if(StringUtils.hasLength(str))
            return str.toLowerCase().equals(str)?str:StringUtils
                    .uncapitalize(str.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
        return str;
    }
}

