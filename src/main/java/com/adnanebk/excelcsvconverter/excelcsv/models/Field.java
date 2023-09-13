package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public record Field<T>(String name, Class<?> type, String title, Method getter, Method setter, int colIndex, String[] enumValues) {
    public Object getValue(T obj) {
        try {
            Object value = getter.invoke(obj);
            if(enumValues.length>0)
                return getEnumValue(value);
            return getter.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }
   public void setValue(Object obj,Object value){
       try {
           if(!type.isEnum()) {
               setter.invoke(obj, value);
               return;
           }
           var enumConstant = enumValues.length>0
                                     ?type.getEnumConstants()[getEnumOrdinal(value)]
                                     :Enum.valueOf(type.asSubclass(Enum.class),value.toString());
               setter.invoke(obj,enumConstant);
       } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
           throw new ReflectionException(e.getMessage());
       }
   }

    private int getEnumOrdinal(Object value) {
        return List.of(enumValues).indexOf(value.toString());
    }

    private String getEnumValue(Object value) {
        int ordinal = ((Enum<?>) value).ordinal();
        return enumValues[ordinal];
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
