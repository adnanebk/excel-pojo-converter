package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CsvCellHandlerUtil<T> {

    private final Map<String, BiFunction<String, Class<?>, ?>> cellValueMap = new HashMap<>();
    private final DateParserFormatterUtil<T> dateParserFormatterUtil;

    public CsvCellHandlerUtil(DateParserFormatterUtil<T> dateParserFormatterUtil) {
        this.dateParserFormatterUtil = dateParserFormatterUtil;
        initCellValueMap();
    }

    public Object getCellValue(String value, Class<?> type) {
        var function = cellValueMap.get(getTypeName(type));
        if(function==null)
            throw new ExcelValidationException("Unsupported field type");
        return function.apply(value, type);
    }

    private String getTypeName(Class<?> type) {
        return type.isEnum() ? Enum.class.getSimpleName().toLowerCase() : type.getSimpleName().toLowerCase();
    }

    private void initCellValueMap() {
        cellValueMap.put(String.class.getSimpleName().toLowerCase(), (value, fieldType) -> value);
        cellValueMap.put(boolean.class.getSimpleName().toLowerCase(), (value, fieldType) -> Boolean.valueOf(value));
        cellValueMap.put(Enum.class.getSimpleName().toLowerCase(), (value, fieldType) -> Enum.valueOf(fieldType.asSubclass(Enum.class), value));
        cellValueMap.put(Integer.class.getSimpleName().toLowerCase(), (value, fieldType) -> Integer.parseInt(value));
        cellValueMap.put(int.class.getSimpleName().toLowerCase(), (value, fieldType) -> Integer.parseInt(value));
        cellValueMap.put(short.class.getSimpleName().toLowerCase(), (value, fieldType) -> Short.parseShort(value));
        cellValueMap.put(long.class.getSimpleName().toLowerCase(), (value, fieldType) -> Long.parseLong(value));
        cellValueMap.put(double.class.getSimpleName().toLowerCase(), (value, fieldType) -> Double.parseDouble(value.replace(",", ".")));
        cellValueMap.put(LocalDate.class.getSimpleName().toLowerCase(), (value, fieldType) -> dateParserFormatterUtil.parseToLocalDate(value));
        cellValueMap.put(LocalDateTime.class.getSimpleName().toLowerCase(), (value, fieldType) -> dateParserFormatterUtil.parseToLocalDateTime(value));
        cellValueMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), (value, fieldType) -> dateParserFormatterUtil.parseToZonedDateTime(value));
        cellValueMap.put(Date.class.getSimpleName().toLowerCase(), (value, fieldType) -> {
            try {
                return dateParserFormatterUtil.parseToDate(value);
            } catch (ParseException e) {
                throw new ExcelValidationException("Invalid date format");
            }
        });
    }


}
