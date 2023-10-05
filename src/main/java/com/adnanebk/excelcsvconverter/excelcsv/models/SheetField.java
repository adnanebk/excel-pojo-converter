package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public record SheetField<T>(Class<?> type, String title, Method getter, Method setter, int colIndex, Map<?,?> enumsMapper) {
    public Object getValue(T obj) {
        try {
            if(!type.isEnum())
                return getter.invoke(obj);
            return enumsMapper.get(getter.invoke(obj));
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
            else  setter.invoke(obj, enumsMapper.get(value));
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new ReflectionException(e.getMessage());
        }
    }


}
