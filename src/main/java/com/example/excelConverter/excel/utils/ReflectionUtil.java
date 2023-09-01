package com.example.excelConverter.excel.utils;


import com.example.excelConverter.excel.annotations.ExcelCol;
import com.example.excelConverter.excel.annotations.ExcelColsDefinition;
import com.example.excelConverter.excel.annotations.ExcelConstructorParameters;
import com.example.excelConverter.excel.exceptions.ReflectionException;
import com.example.excelConverter.excel.models.AnnotationType;
import com.example.excelConverter.excel.models.Field;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public  class ReflectionUtil<T> {

    public final SimpleDateFormat dateFormatter;
    public final DateTimeFormatter localedDateFormatter;
    public final DateTimeFormatter localedDateTimeFormatter;
    public final DateTimeFormatter zonedDateTimeFormatter;
    private final ExcelColsDefinition excelDefinitionAnnotation;

    protected  List<Field> fields;
    protected  final Class<T> classType;
    private  Constructor<T> defaultConstructor;
    private  Constructor<T> argsConstructor;
    private final List<Method> setters= new ArrayList<>();
    private final List<Method> getters = new ArrayList<>();

    public ReflectionUtil(Class<T> type, AnnotationType annotationType) {
            classType=type;
            excelDefinitionAnnotation = classType.getAnnotation(ExcelColsDefinition.class);
        this.setDefaultConstructor();
        if(annotationType.equals(AnnotationType.FIELD)) {
                this.setFields();
            }
            else{
                this.setArgsConstructor();
                this.setFieldsFromArgsConstructor();
            }
            setGettersAndSetters();
            dateFormatter = dateTimeFormat().map(SimpleDateFormat::new).orElse(new SimpleDateFormat());
            localedDateFormatter =dateFormat().map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE);
            localedDateTimeFormatter =dateTimeFormat().map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            zonedDateTimeFormatter =DateTimeFormatter.ISO_ZONED_DATE_TIME;

    }

    public  List<Field> getFields(){
        return fields;
    }

    public  T getInstance(Object... values){
        try {
            T obj =  defaultConstructor.newInstance();
            for (int i = 0; i < fields.size(); i++) {
                if(values[i]!=null)
                 setters.get(i).invoke(obj,values[i]);
            }
            return obj;
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }
    public Optional<String> dateFormat(){
        return Optional.ofNullable(excelDefinitionAnnotation).map(ExcelColsDefinition::dateFormat).filter(s->!s.isEmpty());
    }
    public Optional<String> dateTimeFormat(){
        return Optional.ofNullable(excelDefinitionAnnotation).map(ExcelColsDefinition::dateTimeFormat).filter(s->!s.isEmpty());
    }

    public Object getFieldValue(T obj,int index) {
        try {
            return getters.get(index).invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }
    private String camelCaseWordsToWordsWithSpaces(String str) {
        if(StringUtils.hasLength(str))
            return str.toLowerCase().equals(str)?str:StringUtils
                    .uncapitalize(str.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
        return str;
    }
    private void setDefaultConstructor() {
        try {
            defaultConstructor = classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");

        }
    }
    private void setArgsConstructor(){
        argsConstructor = (Constructor<T>) Arrays.stream(this.classType.getDeclaredConstructors()).filter(ct -> ct.isAnnotationPresent(ExcelConstructorParameters.class))
                .findFirst().orElseThrow(() -> new ReflectionException("Annotation ExcelColsDefinition is required in constructor"));
    }
    private void setGettersAndSetters() {
        fields.forEach(field -> {
            String fieldName = field.name();
            try {
                setters.add(classType.getDeclaredMethod("set"+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1),field.type()));
                getters.add(classType.getDeclaredMethod((field.type().equals(boolean.class)?"is":"get")+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1)));
            } catch (NoSuchMethodException e) {
                throw new ReflectionException(e.getMessage());
            }
        });

    }
    private void setFields(){
        Class<ExcelCol> annotation = ExcelCol.class;
        fields = Arrays.stream(classType.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(annotation).index()))
                .map(field->
                        new Field(field.getName(),field.getType(),
                                Optional.of(field.getDeclaredAnnotation(annotation).title()).filter(s->!s.isEmpty())
                                        .orElseGet(()->camelCaseWordsToWordsWithSpaces(field.getName()))
                        )).toList();
    }
    private void setFieldsFromArgsConstructor(){
        Class<ExcelConstructorParameters> annotation = ExcelConstructorParameters.class;
        var titles = Arrays.asList(argsConstructor.getAnnotation(annotation).titles()).iterator();
        fields = Stream.of(argsConstructor.getParameters()).map(field->new Field(field.getName(),field.getType(),this.getTitle(titles, field.getName())))
                .toList();
    }
    private String getTitle(Iterator<String> titles, String fieldName) {
        return titles.hasNext()?titles.next():camelCaseWordsToWordsWithSpaces(fieldName);
    }
}

