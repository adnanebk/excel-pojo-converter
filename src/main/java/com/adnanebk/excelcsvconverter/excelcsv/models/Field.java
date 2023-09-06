package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record Field<T>(String name, Class<?> type, String title, Method getter,Method setter) {
    public Object getValue(T obj) {
        try {
            return getter.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }
   public void setValue(Object obj,Object value){
       try {
           setter.invoke(obj, value);
       } catch (IllegalAccessException | InvocationTargetException e) {
           throw new ReflectionException(e.getMessage());
       }
   }

}
