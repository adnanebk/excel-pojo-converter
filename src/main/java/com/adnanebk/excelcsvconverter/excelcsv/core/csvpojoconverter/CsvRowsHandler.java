package com.adnanebk.excelcsvconverter.excelcsv.core.csvpojoconverter;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;

import java.time.DateTimeException;

public class CsvRowsHandler<T> {

    private final ReflectionHelper<T> reflectionHelper;

    public CsvRowsHandler(ReflectionHelper<T> reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
    }

    public T convertToObject(String row, String delimiter, char quoteChar) {
        String[] cellsValues = row.split(delimiter);
        var fields = reflectionHelper.getFields();
        T obj = reflectionHelper.createInstance();
        for (int i = 0; i < Math.min(cellsValues.length, fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = cellsValues[field.getCellIndex()].replace(quoteChar + "", "");
            try {
                if(!cellValue.isEmpty())
                  reflectionHelper.getFields().get(i).setValue(cellValue, obj);
            } catch (NumberFormatException e) {
                throw new SheetValidationException(String.format("Cannot convert the cell value %s to number", cellValue));
            } catch (DateTimeException e) {
                throw new SheetValidationException(String.format("Invalid or unsupported date pattern for cell value : %s", cellValue));
            }
        }
        return obj;
    }

    public String[] convertFieldValuesToStrings(T obj) {
        return reflectionHelper.getFields().stream()
                .map(field -> field.getValue(obj).toString())
                .toArray(String[]::new);
    }


}
