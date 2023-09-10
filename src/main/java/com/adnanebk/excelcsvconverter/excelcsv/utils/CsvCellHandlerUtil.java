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

public class CsvCellHandlerUtil<T> {

    private final Map<String, Function<String,Object>> cellValueMap = new HashMap<>();
    private final DateParserFormatterUtil<T> dateParserFormatterUtil;

    public CsvCellHandlerUtil(DateParserFormatterUtil<T> dateParserFormatterUtil) {
        this.dateParserFormatterUtil = dateParserFormatterUtil;
        initCellValueMap();
    }

    public Object getCellValue(String value, Class<?> type) {
        var function = cellValueMap.get(getTypeName(type));
        if(function==null)
            throw new ExcelValidationException("Unsupported field type");
        return function.apply(value);
    }

    private String getTypeName(Class<?> type) {
        return type.isEnum() ? Enum.class.getSimpleName().toLowerCase() : type.getSimpleName().toLowerCase();
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
        cellValueMap.put(Date.class.getSimpleName().toLowerCase(), (value) -> {
            try {
                return dateParserFormatterUtil.parseToDate(value);
            } catch (ParseException e) {
                throw new ExcelValidationException("Invalid date format");
            }
        });
    }


}
