package com.adnanebk.excelcsvconverter.excelcsv.utils;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnumFormat;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.IgnoreCell;
import com.adnanebk.excelcsvconverter.excelcsv.annotations.SheetDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.excelcsv.models.SheetField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtil<T> {
    private final List<SheetField<T>> fields;
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionUtil(Class<T> type) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
        fields = getSheetInfo().filter(SheetDefinition::includeAllFields)
                .map(info->getAllFields())
                .orElseGet(this::getAnnotatedFields);
    }
    public List<SheetField<T>> getFields() {
        return fields;
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

    private List<SheetField<T>> getAnnotatedFields() {
        return Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .map(field -> {
                    var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                    String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                                           .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                    return buildField(title, cellDefinition.value(), field);
                })
                .sorted(Comparator.comparing(SheetField::colIndex))
                .toList();
    }
    private List<SheetField<T>> getAllFields() {
        var titles = classType.getAnnotation(SheetDefinition.class).titles();
        int index=0;
        List<SheetField<T>> sheetFields = new ArrayList<>();
        for (var field : classType.getDeclaredFields()) {
            if (!field.isAnnotationPresent(IgnoreCell.class)) {
                String title=index < titles.length ? titles[index]:camelCaseWordsToTitleWords(field.getName());
                sheetFields.add(buildField(title, index, field));
            }
            index++;
        }
        return sheetFields;
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
    private SheetField<T> buildField(String title, int colIndex, Field field) {
        String fieldTypeName = field.getType().isEnum() ? "enum" : field.getType().getSimpleName().toLowerCase();
        String fieldName = field.getName();
        Method getter=getFieldGetter(fieldName, field.getType());
        Method setter=getFieldSetter(fieldName, field.getType());
        return new SheetField<>(fieldTypeName, title,getter,setter,colIndex, createEnumsMapper(field));
    }

    private  Map<Object,Object> createEnumsMapper(Field field) {
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

    private Map<?, ?> getEnumsMapper( Field field) {
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
            else throw new ReflectionException("expecting a method that return a map of typeName  Map<"+field.getName()+",String>");
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

}

