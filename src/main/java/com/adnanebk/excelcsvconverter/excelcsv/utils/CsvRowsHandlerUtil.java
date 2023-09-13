package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CsvRowsHandlerUtil<T> {

    private final Map<String, Function<String,Object>> cellValueMap = new HashMap<>();
    private final DateParserFormatterUtil dateParserFormatterUtil;
    private final ReflectionUtil<T> reflectionUtil;

    public CsvRowsHandlerUtil(ReflectionUtil<T> reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
        this.dateParserFormatterUtil=new DateParserFormatterUtil(reflectionUtil.getDatePattern(),reflectionUtil.getDateTimePattern());
        initCellValueMap();
    }

    public Object getCellValue(String value, Class<?> type) {
        var function = cellValueMap.get(reflectionUtil.getTypeName(type));
        if(function==null)
            throw new ExcelValidationException("Unsupported field type");
        return function.apply(value);
    }

    private void initCellValueMap() {
        cellValueMap.put(String.class.getSimpleName().toLowerCase(), value -> value);
        cellValueMap.put(boolean.class.getSimpleName().toLowerCase(), Boolean::valueOf);
        cellValueMap.put(Enum.class.getSimpleName().toLowerCase(), value -> value);
        cellValueMap.put(Integer.class.getSimpleName().toLowerCase(), Integer::parseInt);
        cellValueMap.put(int.class.getSimpleName().toLowerCase(), Integer::parseInt);
        cellValueMap.put(short.class.getSimpleName().toLowerCase(), Short::parseShort);
        cellValueMap.put(long.class.getSimpleName().toLowerCase(), Long::parseLong);
        cellValueMap.put(double.class.getSimpleName().toLowerCase(),value -> Double.parseDouble(value.replace(",", ".")));
        cellValueMap.put(LocalDate.class.getSimpleName().toLowerCase(), dateParserFormatterUtil::parseToLocalDate);
        cellValueMap.put(LocalDateTime.class.getSimpleName().toLowerCase(), dateParserFormatterUtil::parseToLocalDateTime);
        cellValueMap.put(ZonedDateTime.class.getSimpleName().toLowerCase(), dateParserFormatterUtil::parseToZonedDateTime);
        cellValueMap.put(Date.class.getSimpleName().toLowerCase(), value -> {
            try {
                return dateParserFormatterUtil.parseToDate(value);
            } catch (ParseException e) {
                throw new ExcelValidationException("Invalid date format");
            }
        });
    }


    public T createObjectFromCells(String row,String delimiter) {
        String[] cellsValues = row.split(delimiter);
        Object[] values = new Object[cellsValues.length];
        var fields = reflectionUtil.getFields();
        for (int i = 0; i < Math.min(cellsValues.length,fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = cellsValues[field.colIndex()];
            values[i] = getCellValue(cellValue, field.type());
        }
        return reflectionUtil.getInstance(values);
    }
}
