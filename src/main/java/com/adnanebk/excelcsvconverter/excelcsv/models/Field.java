package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public record Field<T>(String name, Class<?> type, String title, Method getter, Method setter, int colIndex, String[] enumValues) {
    public Object getValue(T obj) {
        try {
            if(enumValues.length==0)
                return getter.invoke(obj);
            return getEnumValue(obj);
        } catch (IllegalAccessException | InvocationTargetException | IndexOutOfBoundsException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public void setValue(Object obj,Object value){
        try {
            if(value==null || value.toString().isEmpty())
                return;
            if(!type.isEnum())
             setter.invoke(obj, value);
            else setEnumValue(obj, value);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new ReflectionException(e.getMessage());
        }
    }
    private Object getEnumValue(T obj) throws IllegalAccessException, InvocationTargetException {
        Object value = getter.invoke(obj);
        int ordinal = ((Enum<?>) value).ordinal();
        return ordinal<enumValues.length?enumValues[ordinal]:"";

    }
    private void setEnumValue(Object obj, Object value) throws InvocationTargetException, IllegalAccessException {
        try {
           setter.invoke(obj,Enum.valueOf(type.asSubclass(Enum.class), value.toString().toUpperCase().trim()));
       } catch (IllegalArgumentException ex) {
           if (enumValues.length > 0)
               setter.invoke(obj, type.getEnumConstants()[getEnumOrdinal(value)]);
       }
    }

    private int getEnumOrdinal(Object value) {
        for (int i = 0; i < enumValues.length; i++) {
            String enumVal = enumValues[i];
            if (enumVal.equalsIgnoreCase(value.toString()))
                return i;
        }
        throw new ReflectionException("Unknown or invalid enum value {"+value.toString()+"}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Field<?> field)) return false;
        return name.equals(field.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                '}';
    }
}
