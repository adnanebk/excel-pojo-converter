package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.models.Field;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class CsvRowsHandlerUtil<T> {

    private final DateParserFormatterUtil dateParserFormatterUtil;
    private final ReflectionUtil<T> reflectionUtil;

    public CsvRowsHandlerUtil(ReflectionUtil<T> reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
        this.dateParserFormatterUtil=new DateParserFormatterUtil(reflectionUtil.getDatePattern(),reflectionUtil.getDateTimePattern());
    }

    public Object getCellValue(String value, Field<T> field) {
        try {
            return switch (reflectionUtil.getFieldTypeName(field.type())) {
                case "string", "enum" -> value;
                case "boolean" -> Boolean.parseBoolean(value);
                case "integer", "int" -> Integer.parseInt(value);
                case "short" -> Short.parseShort(value);
                case "long" -> Long.parseLong(value);
                case "double" -> Double.parseDouble(value.replace(",", "."));
                case "localdate" -> dateParserFormatterUtil.parseToLocalDate(value);
                case "localdatetime" -> dateParserFormatterUtil.parseToLocalDateTime(value);
                case "zoneddatetime" -> dateParserFormatterUtil.parseToZonedDateTime(value);
                case "date" -> convertToDate(value);
                default -> throw new ExcelValidationException("Unsupported type: " + field.type());
            };
        }catch (RuntimeException ex){
            throw new ExcelValidationException("Unexpected or Invalid cell value {"+value+"}  ");
        }
    }

    private Date convertToDate(String value) {
        try {
            return dateParserFormatterUtil.parseToDate(value);
        } catch (ParseException e) {
            throw new ExcelValidationException("Invalid date format");
        }
    }

    public T createObjectFromCells(String row,String delimiter) {
        String[] cellsValues = row.split(delimiter);
        Object[] values = new Object[cellsValues.length];
        var fields = reflectionUtil.getFields();
        for (int i = 0; i < Math.min(cellsValues.length,fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = cellsValues[field.colIndex()];
            values[i] = getCellValue(cellValue, field);
        }
        return reflectionUtil.createInstance(values);
    }
    public String[] convertObjectToStringColumns(T obj) {
        return  reflectionUtil.getFields().stream()
                .map(field -> {
                    Object value = field.getValue(obj);
                    if(field.type().equals(Date.class))
                        return dateParserFormatterUtil.format((Date) value);
                    if(field.type().equals(LocalDate.class))
                        return dateParserFormatterUtil.format((LocalDate) value);
                    if(field.type().equals(LocalDateTime.class))
                        return dateParserFormatterUtil.format((LocalDateTime) value);
                    if(field.type().equals(ZonedDateTime.class))
                        return dateParserFormatterUtil.format((ZonedDateTime) value);
                    return value.toString();
                })
                .toArray(String[]::new);

    }

    public String[] getHeaders(){
        return reflectionUtil.getFields().stream().map(Field::title).toArray(String[]::new);
    }

}
