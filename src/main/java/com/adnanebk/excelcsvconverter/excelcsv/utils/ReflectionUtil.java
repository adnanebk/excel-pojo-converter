package com.adnanebk.excelcsvconverter.excelcsv.utils;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnumFormat;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.IgnoreCell;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ReflectionUtil<T> {
    private String dateTimePattern;
    private String datePattern;
    private  List<Field<T>> fields;
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionUtil(Class<T> type) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
        this.fields = getAnnotatedFields();
        getSheetInfo().ifPresent(sheetInfo->{
            this.datePattern = sheetInfo.datePattern();
            this.dateTimePattern = sheetInfo.dateTimePattern();
            if(sheetInfo.includeAllFields())
              this.fields = getAllFields();
        });
    }
    public List<Field<T>> getFields() {
        return fields;
    }

    public T createInstanceAndSetValues(Object[] values) {

        T obj = createInstance();
        for (int i = 0; i < values.length; i++) {
                fields.get(i).setValue(obj, values[i]);
            }
            return obj;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }
    private Optional<SheetDefinition> getSheetInfo() {
        return Optional.ofNullable(classType.getAnnotation(SheetDefinition.class));
    }
    private  T createInstance(){
        try{
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

    private List<Field<T>> getAnnotatedFields() {
        Class<CellDefinition> annotation = CellDefinition.class;
        return Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotation))
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
                .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
    }
    private String getTitle(Iterator<String> titles, String fieldName) {
        return titles.hasNext() ? titles.next() : camelCaseWordsToTitleWords(fieldName);
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
        Method getter=getFieldGetter(fieldName, fieldType);
        Method setter=getFieldSetter(fieldName, fieldType);
        //just for optimization
        getter.setAccessible(true);
        setter.setAccessible(true);
        return new Field<>(fieldType, getTitle(titles, fieldName),getter,setter,colIndex, createEnumsMapper(field));
    }

    private  Map<Object,Object> createEnumsMapper(java.lang.reflect.Field field) {
        Map<Object, Object> enumsMapper = new HashMap<>();
        if (!field.getType().isEnum())
            return enumsMapper;
        for(var entry : getEnumsMapper(field).entrySet()){
            enumsMapper.put(entry.getKey(),entry.getValue());
            enumsMapper.put(entry.getValue(),entry.getKey());
        }
        var constants = field.getType().asSubclass(Enum.class).getEnumConstants();
        for (var constant : Arrays.stream(constants).filter(constant -> !enumsMapper.containsKey(constant)).toList()) {
            enumsMapper.put(constant, constant.toString());
            enumsMapper.put(constant.toString(), constant);
        }
        return enumsMapper;
    }

    private Map<?, ?> getEnumsMapper( java.lang.reflect.Field field) {
        var enumsAnnotation = field.getDeclaredAnnotation(CellEnumFormat.class);
        if(enumsAnnotation==null)
            return new HashMap<>();
        var enumMapperMethod = enumsAnnotation.enumsMapperMethod();
        try {
            Method method = field.getDeclaringClass().getDeclaredMethod(enumMapperMethod);
            method.setAccessible(true);
            if(method.invoke(createInstance()) instanceof Map<?, ?> enumsMapper
                    && enumsMapper.entrySet().stream().allMatch(entry -> field.getType().isInstance(entry.getKey())
                                                                 && entry.getValue() instanceof String))
              return enumsMapper;
            else throw new ReflectionException("expecting a method that return a map of type  Map<"+field.getName()+",String>");
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ReflectionException("no method found for the argument enumsMapMethod,you must create a method that returns a map");
        }
    }

    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0))+"";
        String remaining = word.substring(1);
            return firstChar + (remaining.toLowerCase().equals(remaining)?remaining:
                    remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

    public String getFieldTypeName(Class<?> fieldType) {
        return fieldType.isEnum() ? "enum" : fieldType.getSimpleName().toLowerCase();
    }
}

