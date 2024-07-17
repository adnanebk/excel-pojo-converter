package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectedField<T> {
    private final Method getter;
    private final Method setter;
    private final int cellIndex;
    private final Converter<T> converter;
    private final String typeName;

    public ReflectedField(Field field, Converter<T> converter, int cellIndex) {
      try {
        this.converter = converter;
        this.getter = field.getDeclaringClass().getDeclaredMethod((field.getType().equals(boolean.class) ? "is" : "get") + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1));
        this.setter = field.getDeclaringClass().getDeclaredMethod("set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1), field.getType());
        this.cellIndex = cellIndex;
        this.typeName = field.getType().isEnum() ? "enum" : field.getType().getSimpleName().toLowerCase();
      } catch (NoSuchMethodException e) {
          throw new ReflectionException("not found all getters and setters");
      }
    }

    public Object getValue(Object obj) {
        try {
            Object fieldValue = getter.invoke(obj);
            if(converter!=null)
                return converter.convertToCellValue((T) fieldValue);
            return  fieldValue;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public void setValue(Object cellValue,Object obj) {
        try{
        if(converter!=null)
         setter.invoke(obj,converter.convertToFieldValue(cellValue.toString()));
        else setter.invoke(obj,cellValue);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | ClassCastException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public String getTypeName() {
        return typeName;
    }


}
