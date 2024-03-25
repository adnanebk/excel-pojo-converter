package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.BooleanConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.EnumsConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectedField;
import com.adnanebk.excelcsvconverter.excelcsv.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

class ReflectedFieldTest {


    @Test
    void setValue() throws NoSuchFieldException {
        var reflectedField = new ReflectedField<>(Product.class.getDeclaredField("name"), null, 0);
        Assertions.assertDoesNotThrow(() -> reflectedField.setValue("a name", new Product()));
    }

    @Test
    void getValueString() throws NoSuchFieldException {
        var reflectedField = new ReflectedField<>(Product.class.getDeclaredField("name"), new StringConverter(),0);
        Product product = new Product();
        product.setName("a name");

        Assertions.assertEquals("a name added text", reflectedField.getValue(product));
        Assertions.assertEquals("string", reflectedField.getTypeName());
        reflectedField.setValue("text",product);
        Assertions.assertEquals("text added", product.getName());

    }

    @Test
    void getValueDate() throws NoSuchFieldException {
        var reflectedField = new ReflectedField<>(Product2.class.getDeclaredField("updatedDate"),null,0);
        Product2 product = new Product2();
        product.setUpdatedDate(LocalDate.now());

        Assertions.assertEquals(LocalDate.now(),reflectedField.getValue(product));
        Assertions.assertEquals("localdate",reflectedField.getTypeName());
    }

    @Test
    void testCustomEnumConverter() throws NoSuchFieldException {
        var categoryField = Product.class.getDeclaredField("category");
        var reflectedField = new ReflectedField<>(categoryField, getCategoryEnumsConverter(),0);
        Product product = new Product();
        product.setCategory(Category.A);

        Assertions.assertEquals("aa",reflectedField.getValue(product));
        Assertions.assertEquals("enum",reflectedField.getTypeName());
    }


    @Test
    void testCustomBooleanConverter() throws NoSuchFieldException {
        Field activeField = Product.class.getDeclaredField("expired");
        var reflectedField = new ReflectedField<>(activeField,new BooleanConverter("yes","no"),0);
        Product product = new Product();
        product.setActive(true);
        product.setExpired(true);

        Assertions.assertEquals("yes",reflectedField.getValue(product));
        Assertions.assertEquals("boolean",reflectedField.getTypeName());
    }

    private  EnumsConverter<Category> getCategoryEnumsConverter() {
        return new EnumsConverter<>(Category.class, new CategoryConverter().convert());
    }

}

